package dev.ankita.finflow.account;
import dev.ankita.finflow.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class AccountService {
  private final AccountRepository repo;
  public AccountService(AccountRepository repo){this.repo=repo;}

  @Transactional
  public AccountResponse create(CreateAccountRequest r){
    var a=new Account(UUID.randomUUID(),r.ownerName(),r.currency(),r.openingBalance(),OffsetDateTime.now(ZoneOffset.UTC));
    return AccountResponse.from(repo.save(a));
  }

  @Transactional(readOnly=true)
  public AccountResponse get(UUID id){
    return AccountResponse.from(repo.findById(id).orElseThrow(() -> new NotFoundException("Account not found: "+id)));
  }
}
