package dev.ankita.finflow.account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="accounts")
public class Account {
  @Id private UUID id;
  @Column(name="owner_name", nullable=false) private String ownerName;
  @Column(nullable=false, length=3) private String currency;
  @Column(nullable=false, precision=19, scale=4) private BigDecimal balance;
  @Column(name="created_at", nullable=false) private OffsetDateTime createdAt;

  protected Account() {}
  public Account(UUID id, String ownerName, String currency, BigDecimal balance, OffsetDateTime createdAt) {
    this.id=id; this.ownerName=ownerName; this.currency=currency; this.balance=balance; this.createdAt=createdAt;
  }
  public UUID getId(){return id;}
  public String getOwnerName(){return ownerName;}
  public String getCurrency(){return currency;}
  public BigDecimal getBalance(){return balance;}
  public OffsetDateTime getCreatedAt(){return createdAt;}
  public void debit(BigDecimal amount){balance=balance.subtract(amount);}
  public void credit(BigDecimal amount){balance=balance.add(amount);}
}
