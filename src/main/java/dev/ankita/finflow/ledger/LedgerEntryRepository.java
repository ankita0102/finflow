package dev.ankita.finflow.ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,UUID> {
  List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
