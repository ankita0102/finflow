package dev.ankita.finflow.payment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
  private final PaymentService payments;
  public PaymentController(PaymentService payments){this.payments=payments;}

  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public PaymentResponse create(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody CreatePaymentRequest request){
    return payments.create(key,request);
  }

  @GetMapping("/{id}")
  public PaymentResponse get(@PathVariable UUID id){return payments.get(id);}
}
