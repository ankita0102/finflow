package dev.ankita.finflow.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuditListener {
  private final ObjectMapper objectMapper;
  private final PaymentAuditService audit;

  public PaymentAuditListener(ObjectMapper objectMapper, PaymentAuditService audit) {
    this.objectMapper = objectMapper;
    this.audit = audit;
  }

  @KafkaListener(topics = "${finflow.kafka.payment-topic}", groupId = "${finflow.kafka.audit-group}")
  public void onPaymentEvent(String payload) throws JsonProcessingException {
    audit.record(objectMapper.readValue(payload, PaymentEvent.class));
  }
}
