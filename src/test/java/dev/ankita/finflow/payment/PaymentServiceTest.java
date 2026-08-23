package dev.ankita.finflow.payment;
import dev.ankita.finflow.account.*;
import dev.ankita.finflow.ledger.*;
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
    var accountRepo=mock(AccountRepository.class);
    var ledgerRepo=mock(LedgerEntryRepository.class);
    var service=new PaymentService(paymentRepo,accountRepo,ledgerRepo);

    var source=UUID.randomUUID(); var destination=UUID.randomUUID(); var key="demo-123";
    var now=OffsetDateTime.now(ZoneOffset.UTC);
    var existing=new Payment(UUID.randomUUID(),source,destination,new BigDecimal("50.00"),"EUR",PaymentStatus.SETTLED,key,now,now);

    when(paymentRepo.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));
    var result=service.create(key,new CreatePaymentRequest(source,destination,new BigDecimal("50.00"),"EUR"));

    assertEquals(existing.getId(),result.id());
    verify(accountRepo,never()).save(any());
    verify(ledgerRepo,never()).save(any());
  }
}
