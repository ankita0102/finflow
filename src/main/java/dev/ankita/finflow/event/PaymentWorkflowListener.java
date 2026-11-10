package dev.ankita.finflow.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ankita.finflow.payment.PaymentWorkflowService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentWorkflowListener {
  private final ObjectMapper objectMapper;
  private final PaymentWorkflowService workflow;

  public PaymentWorkflowListener(ObjectMapper objectMapper, PaymentWorkflowService workflow) {
    this.objectMapper = objectMapper;
    this.workflow = workflow;
  }

  @KafkaListener(topics = "${finflow.kafka.payment-topic}", groupId = "${finflow.kafka.workflow-group}")
  public void onPaymentEvent(String payload) throws JsonProcessingException {
    workflow.handle(objectMapper.readValue(payload, PaymentEvent.class));
  }
}
