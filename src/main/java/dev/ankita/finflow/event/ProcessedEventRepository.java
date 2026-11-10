package dev.ankita.finflow.event;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
  boolean existsByConsumerNameAndEventId(String consumerName, UUID eventId);
}
