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

    var source=accounts.findById(r.sourceAccountId()).orElseThrow(() -> new NotFoundException("Source account not found"));
    var destination=accounts.findById(r.destinationAccountId()).orElseThrow(() -> new NotFoundException("Destination account not found"));

    if(!source.getCurrency().equals(r.currency()) || !destination.getCurrency().equals(r.currency()))
      throw new ConflictException("Payment currency must match both accounts");
    if(source.getBalance().compareTo(r.amount())<0) throw new ConflictException("Insufficient funds");

    var now=OffsetDateTime.now(ZoneOffset.UTC);
    var payment=new Payment(UUID.randomUUID(),source.getId(),destination.getId(),r.amount(),r.currency(),PaymentStatus.PENDING,key,now,now);
    payments.save(payment);

    source.debit(r.amount()); destination.credit(r.amount());
    accounts.save(source); accounts.save(destination);

    ledger.save(new LedgerEntry(UUID.randomUUID(),source.getId(),payment.getId(),LedgerEntryType.DEBIT,r.amount(),source.getBalance(),now));
    ledger.save(new LedgerEntry(UUID.randomUUID(),destination.getId(),payment.getId(),LedgerEntryType.CREDIT,r.amount(),destination.getBalance(),now));

    payment.markSettled(now);
    payments.save(payment);
    return PaymentResponse.from(payment);
  }

  @Transactional(readOnly=true)
  public PaymentResponse get(UUID id){
    return PaymentResponse.from(payments.findById(id).orElseThrow(() -> new NotFoundException("Payment not found: "+id)));
  }
}
