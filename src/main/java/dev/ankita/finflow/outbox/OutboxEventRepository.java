package dev.ankita.finflow.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.*;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
  @Query(value = """
      select * from outbox_events
      where (status = 'PENDING' and available_at <= :now)
         or (status = 'PROCESSING' and locked_at <= :staleBefore)
      order by created_at
      for update skip locked
      limit :batchSize
      """, nativeQuery = true)
  List<OutboxEvent> findClaimable(@Param("now") OffsetDateTime now,
      @Param("staleBefore") OffsetDateTime staleBefore, @Param("batchSize") int batchSize);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from OutboxEvent e where e.id = :id")
  Optional<OutboxEvent> findByIdForUpdate(@Param("id") UUID id);
}
