package dev.ankita.finflow.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class OutboxClaimService {
  private final OutboxEventRepository events;
  private final int batchSize;
  private final long staleLockSeconds;

  public OutboxClaimService(OutboxEventRepository events,
      @Value("${finflow.outbox.batch-size:50}") int batchSize,
      @Value("${finflow.outbox.stale-lock-seconds:60}") long staleLockSeconds) {
    this.events = events;
    this.batchSize = batchSize;
    this.staleLockSeconds = staleLockSeconds;
  }

  @Transactional
  public List<OutboxEvent> claimBatch() {
    var now = OffsetDateTime.now(ZoneOffset.UTC);
    var claimed = events.findClaimable(now, now.minusSeconds(staleLockSeconds), batchSize);
    claimed.forEach(event -> event.markProcessing(now));
    events.saveAll(claimed);
    return List.copyOf(claimed);
  }
}
