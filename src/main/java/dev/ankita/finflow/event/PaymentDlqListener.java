package dev.ankita.finflow.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentDlqListener {
  private static final Logger log = LoggerFactory.getLogger(PaymentDlqListener.class);

  @KafkaListener(topics = "${finflow.kafka.payment-topic}.DLQ", groupId = "finflow-payment-dlq-monitor")
  public void onDeadLetter(ConsumerRecord<String, String> record) {
    log.error("Payment event moved to DLQ: topic={}, partition={}, offset={}, key={}",
        record.topic(), record.partition(), record.offset(), record.key());
  }
}
