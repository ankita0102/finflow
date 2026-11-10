package dev.ankita.finflow.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

@Service
public class PaymentAuditService {
  static final String CONSUMER_NAME = "payment-audit";
  private final ProcessedEventRepository processedEvents;

  public PaymentAuditService(ProcessedEventRepository processedEvents) {
    this.processedEvents = processedEvents;
  }

  @Transactional
  public void record(PaymentEvent event) {
    if (processedEvents.existsByConsumerNameAndEventId(CONSUMER_NAME, event.eventId())) return;
    processedEvents.save(new ProcessedEvent(UUID.randomUUID(), CONSUMER_NAME, event.eventId(),
        OffsetDateTime.now(ZoneOffset.UTC)));
  }
}
