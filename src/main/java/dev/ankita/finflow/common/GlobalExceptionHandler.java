package dev.ankita.finflow.common;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiError notFound(NotFoundException ex) { return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of()); }

  @ExceptionHandler(ConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ApiError conflict(ConflictException ex) { return error(HttpStatus.CONFLICT, ex.getMessage(), List.of()); }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiError validation(MethodArgumentNotValidException ex) {
    var details = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
    return error(HttpStatus.BAD_REQUEST, "Validation failed", details);
  }

  private ApiError error(HttpStatus status, String message, List<String> details) {
    return new ApiError(OffsetDateTime.now(ZoneOffset.UTC), status.value(), status.getReasonPhrase(), message, details);
  }
}
