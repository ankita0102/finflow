package dev.ankita.finflow.payment;

import dev.ankita.finflow.account.*;
import dev.ankita.finflow.event.*;
import dev.ankita.finflow.ledger.*;
import dev.ankita.finflow.outbox.OutboxService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentWorkflowServiceTest {
  private final PaymentRepository payments = mock(PaymentRepository.class);
  private final AccountRepository accounts = mock(AccountRepository.class);
  private final LedgerEntryRepository ledger = mock(LedgerEntryRepository.class);
  private final OutboxService outbox = mock(OutboxService.class);
  private final ProcessedEventRepository processed = mock(ProcessedEventRepository.class);
  private final PaymentWorkflowService workflow =
      new PaymentWorkflowService(payments, accounts, ledger, outbox, processed);

  @Test
  void createdEventReservesFundsAndEmitsAuthorizedEvent() {
    var fixture = fixture(PaymentStatus.PENDING);
    when(payments.findByIdForUpdate(fixture.payment.getId())).thenReturn(Optional.of(fixture.payment));
    when(accounts.findByIdForUpdate(fixture.source.getId())).thenReturn(Optional.of(fixture.source));
    when(accounts.findByIdForUpdate(fixture.destination.getId())).thenReturn(Optional.of(fixture.destination));

    workflow.handle(event(fixture.payment, PaymentEvent.CREATED));

    assertEquals(PaymentStatus.AUTHORIZED, fixture.payment.getStatus());
    assertEquals(new BigDecimal("75.00"), fixture.source.getAvailableBalance());
    verify(outbox).append(argThat(e -> PaymentEvent.AUTHORIZED.equals(e.eventType())));
    verify(processed).save(any(ProcessedEvent.class));
  }

  @Test
  void authorizedEventCapturesReservationExactlyOnceAndEmitsSettledEvent() {
    var fixture = fixture(PaymentStatus.PENDING);
    fixture.source.reserve(fixture.payment.getAmount());
    fixture.payment.markAuthorized(OffsetDateTime.now(ZoneOffset.UTC));
    when(payments.findByIdForUpdate(fixture.payment.getId())).thenReturn(Optional.of(fixture.payment));
    when(accounts.findByIdForUpdate(fixture.source.getId())).thenReturn(Optional.of(fixture.source));
    when(accounts.findByIdForUpdate(fixture.destination.getId())).thenReturn(Optional.of(fixture.destination));

    workflow.handle(event(fixture.payment, PaymentEvent.AUTHORIZED));

    assertEquals(PaymentStatus.SETTLED, fixture.payment.getStatus());
    assertEquals(new BigDecimal("75.00"), fixture.source.getBalance());
    assertEquals(new BigDecimal("35.00"), fixture.destination.getBalance());
    verify(ledger, times(2)).save(any(LedgerEntry.class));
    verify(outbox).append(argThat(e -> PaymentEvent.SETTLED.equals(e.eventType())));
  }

  @Test
  void insufficientFundsBecomesBusinessFailureWithoutLedgerEntries() {
    var fixture = fixture(PaymentStatus.PENDING);
    var expensivePayment = new Payment(fixture.payment.getId(), fixture.source.getId(),
        fixture.destination.getId(), new BigDecimal("125.00"), "EUR", PaymentStatus.PENDING,
        "key-2", fixture.payment.getCreatedAt(), fixture.payment.getUpdatedAt());
    when(payments.findByIdForUpdate(expensivePayment.getId())).thenReturn(Optional.of(expensivePayment));
    when(accounts.findByIdForUpdate(fixture.source.getId())).thenReturn(Optional.of(fixture.source));
    when(accounts.findByIdForUpdate(fixture.destination.getId())).thenReturn(Optional.of(fixture.destination));

    workflow.handle(event(expensivePayment, PaymentEvent.CREATED));

    assertEquals(PaymentStatus.FAILED, expensivePayment.getStatus());
    assertEquals("Insufficient available funds", expensivePayment.getFailureReason());
    verifyNoInteractions(ledger);
    verify(outbox).append(argThat(e -> PaymentEvent.FAILED.equals(e.eventType())));
  }

  @Test
  void processedEventIsIgnoredWithoutChangingBalances() {
    var fixture = fixture(PaymentStatus.PENDING);
    var event = event(fixture.payment, PaymentEvent.CREATED);
    when(processed.existsByConsumerNameAndEventId("payment-workflow", event.eventId())).thenReturn(true);

    workflow.handle(event);

    verifyNoInteractions(payments, accounts, ledger, outbox);
  }

  private static Fixture fixture(PaymentStatus status) {
    var now = OffsetDateTime.now(ZoneOffset.UTC);
    var source = new Account(UUID.randomUUID(), "Alice", "EUR", new BigDecimal("100.00"), now);
    var destination = new Account(UUID.randomUUID(), "Bob", "EUR", new BigDecimal("10.00"), now);
    var payment = new Payment(UUID.randomUUID(), source.getId(), destination.getId(),
        new BigDecimal("25.00"), "EUR", status, "key-1", now, now);
    return new Fixture(source, destination, payment);
  }

  private static PaymentEvent event(Payment payment, String eventType) {
    return new PaymentEvent(UUID.randomUUID(), eventType, payment.getId(), payment.getSourceAccountId(),
        payment.getDestinationAccountId(), payment.getAmount(), payment.getCurrency(), payment.getStatus(),
        payment.getFailureReason(), OffsetDateTime.now(ZoneOffset.UTC));
  }

  private record Fixture(Account source, Account destination, Payment payment) {}
}
