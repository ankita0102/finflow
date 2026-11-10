package dev.ankita.finflow.outbox;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {
  @Test
  void staleWorkerCannotCompleteANewerClaim() {
    var now = OffsetDateTime.now(ZoneOffset.UTC);
    var event = new OutboxEvent(UUID.randomUUID(), "PAYMENT", UUID.randomUUID(),
        "payment.created", "{}", now);
    event.markProcessing(now);
    var activeClaim = event.getClaimToken();

    assertFalse(event.markPublished(UUID.randomUUID(), now.plusSeconds(1)));
    assertEquals(0, event.getAttemptCount());
    assertTrue(event.markPublished(activeClaim, now.plusSeconds(1)));
  }

  @Test
  void failedPublishIsRescheduledOnlyByClaimOwner() {
    var now = OffsetDateTime.now(ZoneOffset.UTC);
    var event = new OutboxEvent(UUID.randomUUID(), "PAYMENT", UUID.randomUUID(),
        "payment.created", "{}", now);
    event.markProcessing(now);
    var activeClaim = event.getClaimToken();

    assertFalse(event.reschedule(UUID.randomUUID(), "stale", now, 10));
    assertTrue(event.reschedule(activeClaim, "broker unavailable", now, 10));
    assertEquals(1, event.getAttemptCount());
  }
}
