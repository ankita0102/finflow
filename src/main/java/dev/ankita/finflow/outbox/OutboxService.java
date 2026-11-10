package dev.ankita.finflow.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ankita.finflow.event.PaymentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class OutboxService {
  private final OutboxEventRepository events;
  private final ObjectMapper objectMapper;

  public OutboxService(OutboxEventRepository events, ObjectMapper objectMapper) {
    this.events = events;
    this.objectMapper = objectMapper;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void append(PaymentEvent event) {
    try {
      events.save(new OutboxEvent(event.eventId(), "PAYMENT", event.paymentId(), event.eventType(),
          objectMapper.writeValueAsString(event), event.occurredAt()));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Could not serialize payment event", exception);
    }
  }
}
