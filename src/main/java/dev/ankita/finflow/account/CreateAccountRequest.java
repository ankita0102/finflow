package dev.ankita.finflow.account;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record CreateAccountRequest(
  @NotBlank @Size(max=120) String ownerName,
  @NotBlank @Pattern(regexp="^[A-Z]{3}$") String currency,
  @NotNull @DecimalMin("0.00") BigDecimal openingBalance
) {}
