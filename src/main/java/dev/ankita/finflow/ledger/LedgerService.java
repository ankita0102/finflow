package dev.ankita.finflow.ledger;
import dev.ankita.finflow.account.AccountRepository;
import dev.ankita.finflow.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class LedgerService {
  private final LedgerEntryRepository ledger;
  private final AccountRepository accounts;
  public LedgerService(LedgerEntryRepository ledger,AccountRepository accounts){this.ledger=ledger;this.accounts=accounts;}
  @Transactional(readOnly=true)
  public List<LedgerEntryResponse> forAccount(UUID id){
    if(!accounts.existsById(id)) throw new NotFoundException("Account not found: "+id);
    return ledger.findByAccountIdOrderByCreatedAtDesc(id).stream().map(LedgerEntryResponse::from).toList();
  }
}
