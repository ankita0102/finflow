package dev.ankita.finflow.payment;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="payments")
public class Payment {
  @Id private UUID id;
  @Column(name="source_account_id",nullable=false) private UUID sourceAccountId;
  @Column(name="destination_account_id",nullable=false) private UUID destinationAccountId;
  @Column(nullable=false,precision=19,scale=4) private BigDecimal amount;
  @Column(nullable=false,length=3) private String currency;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private PaymentStatus status;
  @Column(name="idempotency_key",nullable=false,unique=true) private String idempotencyKey;
  @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
  @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
  @Column(name="failure_reason",length=500) private String failureReason;
  protected Payment(){}
  public Payment(UUID id,UUID source,UUID destination,BigDecimal amount,String currency,PaymentStatus status,String key,OffsetDateTime created,OffsetDateTime updated){
    this.id=id;this.sourceAccountId=source;this.destinationAccountId=destination;this.amount=amount;this.currency=currency;this.status=status;this.idempotencyKey=key;this.createdAt=created;this.updatedAt=updated;
  }
  public UUID getId(){return id;} public UUID getSourceAccountId(){return sourceAccountId;} public UUID getDestinationAccountId(){return destinationAccountId;}
  public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public PaymentStatus getStatus(){return status;}
  public String getIdempotencyKey(){return idempotencyKey;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
  public String getFailureReason(){return failureReason;}
  public void markAuthorized(OffsetDateTime now){status=PaymentStatus.AUTHORIZED;updatedAt=now;}
  public void markSettled(OffsetDateTime now){status=PaymentStatus.SETTLED;updatedAt=now;}
  public void markFailed(String reason,OffsetDateTime now){status=PaymentStatus.FAILED;failureReason=reason;updatedAt=now;}
}
