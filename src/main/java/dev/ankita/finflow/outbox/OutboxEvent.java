package dev.ankita.finflow.outbox;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
  @Id private UUID id;
  @Column(name = "aggregate_type", nullable = false, length = 80) private String aggregateType;
  @Column(name = "aggregate_id", nullable = false) private UUID aggregateId;
  @Column(name = "event_type", nullable = false, length = 120) private String eventType;
  @Column(nullable = false, columnDefinition = "TEXT") private String payload;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private OutboxStatus status;
  @Column(name = "attempt_count", nullable = false) private int attemptCount;
  @Column(name = "available_at", nullable = false) private OffsetDateTime availableAt;
  @Column(name = "locked_at") private OffsetDateTime lockedAt;
  @Column(name = "claim_token") private UUID claimToken;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
  @Column(name = "published_at") private OffsetDateTime publishedAt;
  @Column(name = "last_error", length = 1000) private String lastError;

  protected OutboxEvent() {}

  public OutboxEvent(UUID id, String aggregateType, UUID aggregateId, String eventType,
      String payload, OffsetDateTime createdAt) {
    this.id = id;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.status = OutboxStatus.PENDING;
    this.availableAt = createdAt;
    this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public UUID getAggregateId() { return aggregateId; }
  public String getEventType() { return eventType; }
  public String getPayload() { return payload; }
  public int getAttemptCount() { return attemptCount; }
  public UUID getClaimToken() { return claimToken; }

  public void markProcessing(OffsetDateTime now) {
    status = OutboxStatus.PROCESSING;
    lockedAt = now;
    claimToken = UUID.randomUUID();
  }

  public boolean markPublished(UUID expectedClaimToken, OffsetDateTime now) {
    if (status != OutboxStatus.PROCESSING || !Objects.equals(claimToken, expectedClaimToken)) return false;
    status = OutboxStatus.PUBLISHED;
    publishedAt = now;
    lockedAt = null;
    claimToken = null;
    lastError = null;
    return true;
  }

  public boolean reschedule(UUID expectedClaimToken, String error, OffsetDateTime now, int maxAttempts) {
    if (status != OutboxStatus.PROCESSING || !Objects.equals(claimToken, expectedClaimToken)) return false;
    attemptCount++;
    lastError = abbreviate(error, 1000);
    lockedAt = null;
    claimToken = null;
    if (attemptCount >= maxAttempts) {
      status = OutboxStatus.FAILED;
      return true;
    }
    status = OutboxStatus.PENDING;
    long delaySeconds = Math.min(60, 1L << Math.min(attemptCount, 6));
    availableAt = now.plusSeconds(delaySeconds);
    return true;
  }

  private static String abbreviate(String value, int maxLength) {
    if (value == null) return "Unknown publishing error";
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
