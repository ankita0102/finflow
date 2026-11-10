package dev.ankita.finflow.outbox;

import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {
  private final OutboxClaimService claimService;
  private final OutboxStateService stateService;
  private final KafkaTemplate<String, String> kafka;
  private final MeterRegistry metrics;
  private final String topic;
  private final long publishTimeoutSeconds;

  public OutboxPublisher(OutboxClaimService claimService, OutboxStateService stateService,
      KafkaTemplate<String, String> kafka, MeterRegistry metrics,
      @Value("${finflow.kafka.payment-topic:payment.events}") String topic,
      @Value("${finflow.outbox.publish-timeout-seconds:10}") long publishTimeoutSeconds) {
    this.claimService = claimService;
    this.stateService = stateService;
    this.kafka = kafka;
    this.metrics = metrics;
    this.topic = topic;
    this.publishTimeoutSeconds = publishTimeoutSeconds;
  }

  @Scheduled(fixedDelayString = "${finflow.outbox.poll-interval-ms:500}")
  public void publishBatch() {
    for (var event : claimService.claimBatch()) {
      try {
        var record = new ProducerRecord<String, String>(topic, event.getAggregateId().toString(), event.getPayload());
        record.headers().add("eventId", event.getId().toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));
        kafka.send(record).get(publishTimeoutSeconds, TimeUnit.SECONDS);
        if (stateService.published(event.getId(), event.getClaimToken())) {
          metrics.counter("finflow.outbox.published", "event.type", event.getEventType()).increment();
        }
      } catch (Exception exception) {
        if (stateService.failed(event.getId(), event.getClaimToken(), exception.getMessage())) {
          metrics.counter("finflow.outbox.publish.failures", "event.type", event.getEventType()).increment();
        }
      }
    }
  }
}
