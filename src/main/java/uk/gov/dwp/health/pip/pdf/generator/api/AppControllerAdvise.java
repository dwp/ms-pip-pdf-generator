package uk.gov.dwp.health.pip.pdf.generator.api;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.dwp.health.pip.pdf.generator.exception.FormSpecificationNotFoundException;
import uk.gov.dwp.health.pip.pdf.generator.exception.InvalidFormDataException;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.FailureResponse;

@Slf4j
@Component
@ControllerAdvice
public class AppControllerAdvise {

  @ExceptionHandler(PdfGenerationException.class)
  public ResponseEntity<FailureResponse> handlePdfException(PdfGenerationException ex) {
    return responseWithFailureResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<FailureResponse> responseWithFailureResponse(
      String message, HttpStatus status) {
    var response = new FailureResponse();
    response.setMessage(message);
    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(TaskException.class)
  public ResponseEntity<FailureResponse> handleTaskException(TaskException ex) {
    return responseWithFailureResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(InvalidFormDataException.class)
  public ResponseEntity<FailureResponse> handleFormValidationException(
      InvalidFormDataException ex) {
    return responseWithFailureResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<FailureResponse> handle500(RuntimeException ex) {
    log.error("Unknown error {}", ex.getMessage());
    return responseWithFailureResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(
      value = {
        ConstraintViolationException.class,
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
      })
  public final ResponseEntity<FailureResponse> handle400(Exception ex) {
    return responseWithFailureResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FormSpecificationNotFoundException.class)
  public final ResponseEntity<FailureResponse> handleFormSpecificationNotFound(
      FormSpecificationNotFoundException ex) {
    log.error("Form specification not found with message: {}", ex.getMessage());
    return responseWithFailureResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
  }
}
