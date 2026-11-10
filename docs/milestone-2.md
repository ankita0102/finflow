# Milestone 2 Design Notes

## Payment state machine

| Current state | Input | Transactional action | Resulting event |
| --- | --- | --- | --- |
| none | `POST /payments` | Insert `PENDING` payment and outbox row | `payment.created` |
| `PENDING` | `payment.created` | Lock both accounts, validate, reserve funds | `payment.authorized` |
| `PENDING` | domain rejection | Store failure reason | `payment.failed` |
| `AUTHORIZED` | `payment.authorized` | Capture reservation, credit destination, write two ledger entries | `payment.settled` |
| terminal | duplicate/late event | No business-state change; record consumer receipt | none |

Every state transition and its event are committed in one PostgreSQL transaction. Kafka is never
called from the payment transaction.

## Outbox relay

1. Select due `PENDING` rows, plus stale `PROCESSING` rows, using `FOR UPDATE SKIP LOCKED`.
2. Mark the batch `PROCESSING`, assign a unique claim token, and commit quickly so multiple
   application replicas can poll safely.
3. Publish each JSON event to `payment.events`, keyed by `paymentId`.
4. Mark the row `PUBLISHED` after the broker acknowledges it.
5. On failure, increment `attempt_count` and reschedule with capped exponential backoff.

A crash between steps 3 and 4 causes a duplicate message, which is safe by design. A stale
`PROCESSING` claim becomes eligible again after 60 seconds. Completion updates require the active
claim token, so a slow worker cannot overwrite the result of a worker that reclaimed the row.

## Consumer consistency

Kafka offsets and PostgreSQL are not committed as one distributed transaction. Instead, each
consumer transaction checks `processed_events`, applies the business change, creates the next
outbox event, and stores its processed marker atomically. If offset commit fails, Kafka redelivers
the record and the processed marker turns the replay into a no-op.

The Kafka message key is the payment ID, preserving event order for one payment. Account rows are
locked in sorted UUID order, reducing deadlock risk when payments move in opposite directions.

## Retries and dead letters

- Outbox-to-Kafka failures: relay-controlled retry, capped at 60 seconds, ten attempts.
- Consumer failures: retries after 1, 2, and 4 seconds.
- Exhausted consumer records: `payment.events.DLQ`.
- Domain failures such as insufficient funds are not exceptions. They are valid business outcomes
  and produce `payment.failed`.

The DLQ listener currently logs record coordinates for operations. A later admin dashboard can add
inspection, replay, and resolution controls with a complete audit trail.

## Intentional trade-offs

- The polling relay is simpler to operate and explain than change-data-capture for this milestone.
  Debezium can replace it later without changing domain transactions or event contracts.
- Kafka is single-node locally; production configuration must use replicated brokers and topics.
- The audit consumer currently proves independent consumer-group and inbox behavior. A later
  milestone can turn it into a queryable event timeline/read model.
