package dev.ankita.finflow.payment;
import dev.ankita.finflow.account.*;
import dev.ankita.finflow.common.*;
import dev.ankita.finflow.ledger.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class PaymentService {
  private final PaymentRepository payments;
  private final AccountRepository accounts;
  private final LedgerEntryRepository ledger;

  public PaymentService(PaymentRepository payments,AccountRepository accounts,LedgerEntryRepository ledger){
    this.payments=payments;this.accounts=accounts;this.ledger=ledger;
  }

  @Transactional
  public PaymentResponse create(String key,CreatePaymentRequest r){
    if(key==null || key.isBlank()) throw new ConflictException("Idempotency-Key header is required");

    var existing=payments.findByIdempotencyKey(key);
    if(existing.isPresent()){
      var p=existing.get();
      boolean same=p.getSourceAccountId().equals(r.sourceAccountId())
          && p.getDestinationAccountId().equals(r.destinationAccountId())
          && p.getAmount().compareTo(r.amount())==0
          && p.getCurrency().equals(r.currency());
      if(!same) throw new ConflictException("Idempotency key was already used with a different request");
      return PaymentResponse.from(p);
    }

    if(r.sourceAccountId().equals(r.destinationAccountId()))
      throw new ConflictException("Source and destination accounts must be different");

    var now=OffsetDateTime.now(ZoneOffset.UTC);
    var payment=new Payment(UUID.randomUUID(),r.sourceAccountId(),r.destinationAccountId(),r.amount(),r.currency(),PaymentStatus.PENDING,key,now,now);
    payments.save(payment);
    outbox.append(PaymentEvent.created(payment,now));
    return PaymentResponse.from(payment);
  }

  @Transactional(readOnly=true)
  public PaymentResponse get(UUID id){
    return PaymentResponse.from(payments.findById(id).orElseThrow(() -> new NotFoundException("Payment not found: "+id)));
  }
}
