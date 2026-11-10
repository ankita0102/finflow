package dev.ankita.finflow.payment;

import dev.ankita.finflow.account.*;
import dev.ankita.finflow.event.*;
import dev.ankita.finflow.ledger.*;
import dev.ankita.finflow.outbox.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class PaymentWorkflowService {
  static final String CONSUMER_NAME = "payment-workflow";

  private final PaymentRepository payments;
  private final AccountRepository accounts;
  private final LedgerEntryRepository ledger;
  private final OutboxService outbox;
  private final ProcessedEventRepository processedEvents;

  public PaymentWorkflowService(PaymentRepository payments, AccountRepository accounts,
      LedgerEntryRepository ledger, OutboxService outbox, ProcessedEventRepository processedEvents) {
    this.payments = payments;
    this.accounts = accounts;
    this.ledger = ledger;
    this.outbox = outbox;
    this.processedEvents = processedEvents;
  }

  @Transactional
  public void handle(PaymentEvent event) {
    if (processedEvents.existsByConsumerNameAndEventId(CONSUMER_NAME, event.eventId())) return;

    switch (event.eventType()) {
      case PaymentEvent.CREATED -> authorize(event.paymentId());
      case PaymentEvent.AUTHORIZED -> settle(event.paymentId());
      case PaymentEvent.FAILED, PaymentEvent.SETTLED -> { }
      default -> throw new IllegalArgumentException("Unsupported payment event: " + event.eventType());
    }

    processedEvents.save(new ProcessedEvent(UUID.randomUUID(), CONSUMER_NAME, event.eventId(), now()));
  }

  private void authorize(UUID paymentId) {
    var payment = lockedPayment(paymentId);
    if (payment.getStatus() != PaymentStatus.PENDING) return;

    var lockedAccounts = lockAccounts(payment.getSourceAccountId(), payment.getDestinationAccountId());
    var source = lockedAccounts.get(payment.getSourceAccountId());
    var destination = lockedAccounts.get(payment.getDestinationAccountId());

    if (source == null) {
      fail(payment, "Source account not found");
      return;
    }
    if (destination == null) {
      fail(payment, "Destination account not found");
      return;
    }
    if (!source.getCurrency().equals(payment.getCurrency())
        || !destination.getCurrency().equals(payment.getCurrency())) {
      fail(payment, "Payment currency must match both accounts");
      return;
    }
    if (source.getAvailableBalance().compareTo(payment.getAmount()) < 0) {
      fail(payment, "Insufficient available funds");
      return;
    }

    source.reserve(payment.getAmount());
    var transitionTime = now();
    payment.markAuthorized(transitionTime);
    outbox.append(PaymentEvent.authorized(payment, transitionTime));
  }

  private void settle(UUID paymentId) {
    var payment = lockedPayment(paymentId);
    if (payment.getStatus() != PaymentStatus.AUTHORIZED) return;

    var lockedAccounts = lockAccounts(payment.getSourceAccountId(), payment.getDestinationAccountId());
    var source = Optional.ofNullable(lockedAccounts.get(payment.getSourceAccountId()))
        .orElseThrow(() -> new IllegalStateException("Reserved source account disappeared"));
    var destination = Optional.ofNullable(lockedAccounts.get(payment.getDestinationAccountId()))
        .orElseThrow(() -> new IllegalStateException("Destination account disappeared"));

    source.captureReservation(payment.getAmount());
    destination.credit(payment.getAmount());

    var transitionTime = now();
    ledger.save(new LedgerEntry(UUID.randomUUID(), source.getId(), payment.getId(),
        LedgerEntryType.DEBIT, payment.getAmount(), source.getBalance(), transitionTime));
    ledger.save(new LedgerEntry(UUID.randomUUID(), destination.getId(), payment.getId(),
        LedgerEntryType.CREDIT, payment.getAmount(), destination.getBalance(), transitionTime));

    payment.markSettled(transitionTime);
    outbox.append(PaymentEvent.settled(payment, transitionTime));
  }

  private void fail(Payment payment, String reason) {
    var transitionTime = now();
    payment.markFailed(reason, transitionTime);
    outbox.append(PaymentEvent.failed(payment, transitionTime));
  }

  private Payment lockedPayment(UUID paymentId) {
    return payments.findByIdForUpdate(paymentId)
        .orElseThrow(() -> new IllegalStateException("Payment event references missing payment: " + paymentId));
  }

  private Map<UUID, Account> lockAccounts(UUID firstId, UUID secondId) {
    var result = new HashMap<UUID, Account>();
    List.of(firstId, secondId).stream().distinct().sorted().forEach(id ->
        accounts.findByIdForUpdate(id).ifPresent(account -> result.put(id, account)));
    return result;
  }

  private static OffsetDateTime now() {
    return OffsetDateTime.now(ZoneOffset.UTC);
  }
}
