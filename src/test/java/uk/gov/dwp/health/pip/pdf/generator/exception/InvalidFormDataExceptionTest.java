package uk.gov.dwp.health.pip.pdf.generator.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidFormDataExceptionTest {

  @Test
  void testCreateInvalidFormDataException() {
    InvalidFormDataException cut = new InvalidFormDataException("invalid form data");
    assertEquals("invalid form data", cut.getMessage());
  }
}
