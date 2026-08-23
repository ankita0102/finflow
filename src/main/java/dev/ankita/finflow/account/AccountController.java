package dev.ankita.finflow.account;
import dev.ankita.finflow.ledger.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
  private final AccountService accounts;
  private final LedgerService ledger;
  public AccountController(AccountService accounts, LedgerService ledger){this.accounts=accounts;this.ledger=ledger;}

  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public AccountResponse create(@Valid @RequestBody CreateAccountRequest request){return accounts.create(request);}

  @GetMapping("/{id}")
  public AccountResponse get(@PathVariable UUID id){return accounts.get(id);}

  @GetMapping("/{id}/transactions")
  public List<LedgerEntryResponse> transactions(@PathVariable UUID id){return ledger.forAccount(id);}
}
