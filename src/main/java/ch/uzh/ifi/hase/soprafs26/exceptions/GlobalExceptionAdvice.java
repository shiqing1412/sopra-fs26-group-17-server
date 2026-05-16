package ch.uzh.ifi.hase.soprafs26.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionAdvice {

	private final Logger log = LoggerFactory.getLogger(GlobalExceptionAdvice.class);
	private static final String MESSAGE_KEY = "message";

	@ExceptionHandler(MethodArgumentNotValidException.class) // Handles validation errors for @Valid annotated request bodies
	public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex){
		
		String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("Validation failed");

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(Map.of(MESSAGE_KEY, message));
	}

	@ExceptionHandler(ConstraintViolationException.class) // Handles validation errors for @Validated annotated method parameters
	public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
		
		String message = ex.getConstraintViolations().stream().findFirst()
				.map(violation -> violation.getMessage())
				.orElse("Validation failed");

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(Map.of(MESSAGE_KEY, message));
	}

	@ExceptionHandler (ResponseStatusException.class) // Handles exceptions with specific HTTP status codes
	public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
		
		String message = ex.getReason() != null 
				? ex.getReason() 
				: "Request failed";
		
		return ResponseEntity
				.status(ex.getStatusCode())
				.body(Map.of(MESSAGE_KEY, message));
	}

	@ExceptionHandler(TransactionSystemException.class) // Handles transaction system errors
	public ResponseEntity<Map<String, String>> handleTransactionSystemException(TransactionSystemException ex) {
		log.error("Transaction system error:", ex);
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(Map.of(MESSAGE_KEY, "Transaction failed due to a conflict."));
	}


	@ExceptionHandler(Exception.class) // Catches any unhandled exceptions to prevent server crashes and provide a generic error response
	public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
		log.error("Unexpected server error:", ex);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(MESSAGE_KEY, "An unexpected server error occurred."));
	}
}