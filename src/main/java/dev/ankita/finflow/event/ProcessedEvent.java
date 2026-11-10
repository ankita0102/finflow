package dev.ankita.finflow.event;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events", uniqueConstraints =
    @UniqueConstraint(name = "uk_processed_event_consumer", columnNames = {"consumer_name", "event_id"}))
public class ProcessedEvent {
  @Id private UUID id;
  @Column(name = "consumer_name", nullable = false, length = 120) private String consumerName;
  @Column(name = "event_id", nullable = false) private UUID eventId;
  @Column(name = "processed_at", nullable = false) private OffsetDateTime processedAt;

  protected ProcessedEvent() {}

  public ProcessedEvent(UUID id, String consumerName, UUID eventId, OffsetDateTime processedAt) {
    this.id = id;
    this.consumerName = consumerName;
    this.eventId = eventId;
    this.processedAt = processedAt;
  }
}
