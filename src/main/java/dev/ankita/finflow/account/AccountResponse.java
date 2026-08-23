package dev.ankita.finflow.account;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
public record AccountResponse(UUID id,String ownerName,String currency,BigDecimal balance,OffsetDateTime createdAt) {
  static AccountResponse from(Account a){return new AccountResponse(a.getId(),a.getOwnerName(),a.getCurrency(),a.getBalance(),a.getCreatedAt());}
}
