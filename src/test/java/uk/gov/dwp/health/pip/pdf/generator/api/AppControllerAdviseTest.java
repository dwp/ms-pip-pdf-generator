package uk.gov.dwp.health.pip.pdf.generator.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.health.pip.pdf.generator.exception.InvalidFormDataException;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.FailureResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppControllerAdviseTest {

  private static AppControllerAdvise underTest;

  @BeforeAll
  static void setupSpec() {
    underTest = new AppControllerAdvise();
  }

  @Test
  void testHandledRuntimeException() {
    RuntimeException exp = mock(RuntimeException.class);
    when(exp.getMessage()).thenReturn("test exception");
    ResponseEntity<FailureResponse> actual = underTest.handle500(exp);
    assertAll(
        "assert response",
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode()),
        () -> assertEquals("test exception", actual.getBody().getMessage()));
  }

  @Test
  void testHandlePdfGenerationException() {
    PdfGenerationException exp = mock(PdfGenerationException.class);
    when(exp.getMessage()).thenReturn("failure");
    ResponseEntity<FailureResponse> actual = underTest.handlePdfException(exp);
    assertAll(
        "assert response",
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode()),
        () -> assertEquals("failure", actual.getBody().getMessage()));
  }

  @Test
  void testHandleTaskException() {
    TaskException exp = mock(TaskException.class);
    when(exp.getMessage()).thenReturn("task failure");
    ResponseEntity<FailureResponse> actual = underTest.handleTaskException(exp);
    assertAll(
        "assert response",
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode()),
        () -> assertEquals("task failure", actual.getBody().getMessage()));
  }

  @Test
  @DisplayName("Test handle exception of constraint violation, bad request method etc")
  void testHandleExceptionOfConstraintViolationBadRequestMethodEtc() {
    Exception exception = new Exception("bad request");
    ResponseEntity<FailureResponse> actual = underTest.handle400(exception);
    assertAll(
        "assert response",
        () -> assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode()),
        () -> assertEquals("bad request", actual.getBody().getMessage()));
  }

  @Test
  @DisplayName("Test handle fail to validation form data bad request returned")
  void testHandleFailToValidationFormDataBadRequestReturned() {
    InvalidFormDataException exp = new InvalidFormDataException("fail to valid form data");
    ResponseEntity<FailureResponse> actual = underTest.handleFormValidationException(exp);
    assertAll(
        "assert response",
        () -> assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode()),
        () -> assertEquals("fail to valid form data", actual.getBody().getMessage()));
  }
}
