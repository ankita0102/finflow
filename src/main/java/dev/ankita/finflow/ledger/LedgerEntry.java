package dev.ankita.finflow.ledger;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity
@Table(name="ledger_entries")
public class LedgerEntry {
  @Id private UUID id;
  @Column(name="account_id",nullable=false) private UUID accountId;
  @Column(name="payment_id") private UUID paymentId;
  @Enumerated(EnumType.STRING) @Column(name="entry_type",nullable=false) private LedgerEntryType entryType;
  @Column(nullable=false,precision=19,scale=4) private BigDecimal amount;
  @Column(name="balance_after",nullable=false,precision=19,scale=4) private BigDecimal balanceAfter;
  @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
  protected LedgerEntry(){}
  public LedgerEntry(UUID id,UUID accountId,UUID paymentId,LedgerEntryType entryType,BigDecimal amount,BigDecimal balanceAfter,OffsetDateTime createdAt){
    this.id=id;this.accountId=accountId;this.paymentId=paymentId;this.entryType=entryType;this.amount=amount;this.balanceAfter=balanceAfter;this.createdAt=createdAt;
  }
  public UUID getId(){return id;} public UUID getAccountId(){return accountId;} public UUID getPaymentId(){return paymentId;}
  public LedgerEntryType getEntryType(){return entryType;} public BigDecimal getAmount(){return amount;}
  public BigDecimal getBalanceAfter(){return balanceAfter;} public OffsetDateTime getCreatedAt(){return createdAt;}
}
