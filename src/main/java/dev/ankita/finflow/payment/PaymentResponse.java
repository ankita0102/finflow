package dev.ankita.finflow.payment;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
public record PaymentResponse(UUID id,UUID sourceAccountId,UUID destinationAccountId,BigDecimal amount,String currency,PaymentStatus status,OffsetDateTime createdAt,OffsetDateTime updatedAt){
  static PaymentResponse from(Payment p){return new PaymentResponse(p.getId(),p.getSourceAccountId(),p.getDestinationAccountId(),p.getAmount(),p.getCurrency(),p.getStatus(),p.getCreatedAt(),p.getUpdatedAt());}
}
