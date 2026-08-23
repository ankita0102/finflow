package dev.ankita.finflow.ledger;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
public record LedgerEntryResponse(UUID id,UUID accountId,UUID paymentId,LedgerEntryType type,BigDecimal amount,BigDecimal balanceAfter,OffsetDateTime createdAt){
  static LedgerEntryResponse from(LedgerEntry e){return new LedgerEntryResponse(e.getId(),e.getAccountId(),e.getPaymentId(),e.getEntryType(),e.getAmount(),e.getBalanceAfter(),e.getCreatedAt());}
}
