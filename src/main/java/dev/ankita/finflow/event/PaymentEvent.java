package dev.ankita.finflow.event;

import dev.ankita.finflow.payment.Payment;
import dev.ankita.finflow.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentEvent(
    UUID eventId,
    String eventType,
    UUID paymentId,
    UUID sourceAccountId,
    UUID destinationAccountId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    String failureReason,
    OffsetDateTime occurredAt) {

  public static final String CREATED = "payment.created";
  public static final String AUTHORIZED = "payment.authorized";
  public static final String FAILED = "payment.failed";
  public static final String SETTLED = "payment.settled";

  public static PaymentEvent created(Payment payment, OffsetDateTime occurredAt) {
    return from(payment, CREATED, occurredAt);
  }

  public static PaymentEvent authorized(Payment payment, OffsetDateTime occurredAt) {
    return from(payment, AUTHORIZED, occurredAt);
  }

  public static PaymentEvent failed(Payment payment, OffsetDateTime occurredAt) {
    return from(payment, FAILED, occurredAt);
  }

  public static PaymentEvent settled(Payment payment, OffsetDateTime occurredAt) {
    return from(payment, SETTLED, occurredAt);
  }

  private static PaymentEvent from(Payment payment, String eventType, OffsetDateTime occurredAt) {
    return new PaymentEvent(
        UUID.randomUUID(), eventType, payment.getId(), payment.getSourceAccountId(),
        payment.getDestinationAccountId(), payment.getAmount(), payment.getCurrency(),
        payment.getStatus(), payment.getFailureReason(), occurredAt);
  }
}
