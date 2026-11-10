package dev.ankita.finflow.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

@Service
public class OutboxStateService {
  private final OutboxEventRepository events;
  private final int maxAttempts;

  public OutboxStateService(OutboxEventRepository events,
      @Value("${finflow.outbox.max-attempts:10}") int maxAttempts) {
    this.events = events;
    this.maxAttempts = maxAttempts;
  }

  @Transactional
  public boolean published(UUID id, UUID claimToken) {
    return events.findByIdForUpdate(id)
        .map(event -> event.markPublished(claimToken, OffsetDateTime.now(ZoneOffset.UTC)))
        .orElse(false);
  }

  @Transactional
  public boolean failed(UUID id, UUID claimToken, String error) {
    return events.findByIdForUpdate(id)
        .map(event -> event.reschedule(claimToken, error,
            OffsetDateTime.now(ZoneOffset.UTC), maxAttempts))
        .orElse(false);
  }
}
