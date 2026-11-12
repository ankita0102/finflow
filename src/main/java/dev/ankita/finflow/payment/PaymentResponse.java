package dev.ankita.finflow.payment;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
public record PaymentResponse(UUID id,UUID sourceAccountId,UUID destinationAccountId,BigDecimal amount,String currency,PaymentStatus status,String failureReason,OffsetDateTime createdAt,OffsetDateTime updatedAt){
  static PaymentResponse from(Payment p){return new PaymentResponse(p.getId(),p.getSourceAccountId(),p.getDestinationAccountId(),p.getAmount(),p.getCurrency(),p.getStatus(),p.getFailureReason(),p.getCreatedAt(),p.getUpdatedAt());}
}
