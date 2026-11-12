package dev.ankita.finflow.payment;
import dev.ankita.finflow.event.PaymentEvent;
import dev.ankita.finflow.outbox.OutboxService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
  @Test
  void repeatedIdempotencyKeyReturnsExistingPaymentWithoutSecondTransfer(){
    var paymentRepo=mock(PaymentRepository.class);
    var outbox=mock(OutboxService.class);
    var service=new PaymentService(paymentRepo,outbox);

    var source=UUID.randomUUID(); var destination=UUID.randomUUID(); var key="demo-123";
    var now=OffsetDateTime.now(ZoneOffset.UTC);
    var existing=new Payment(UUID.randomUUID(),source,destination,new BigDecimal("50.00"),"EUR",PaymentStatus.SETTLED,key,now,now);

    when(paymentRepo.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));
    var result=service.create(key,new CreatePaymentRequest(source,destination,new BigDecimal("50.00"),"EUR"));

    assertEquals(existing.getId(),result.id());
    verify(paymentRepo,never()).save(any());
    verify(outbox,never()).append(any());
  }

  @Test
  void newPaymentAndCreatedEventAreWrittenTogether(){
    var paymentRepo=mock(PaymentRepository.class);
    var outbox=mock(OutboxService.class);
    var service=new PaymentService(paymentRepo,outbox);
    var source=UUID.randomUUID(); var destination=UUID.randomUUID();

    when(paymentRepo.findByIdempotencyKey("new-payment")).thenReturn(Optional.empty());
    when(paymentRepo.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result=service.create("new-payment",
        new CreatePaymentRequest(source,destination,new BigDecimal("25.00"),"EUR"));

    assertEquals(PaymentStatus.PENDING,result.status());
    verify(paymentRepo).save(any(Payment.class));
    verify(outbox).append(argThat(event -> PaymentEvent.CREATED.equals(event.eventType())
        && result.id().equals(event.paymentId())));
  }
}
