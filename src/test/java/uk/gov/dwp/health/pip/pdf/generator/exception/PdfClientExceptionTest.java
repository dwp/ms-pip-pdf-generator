package uk.gov.dwp.health.pip.pdf.generator.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfClientExceptionTest {

  @Test
  void testPdfGenerationException() {
    PdfClientException cut = new PdfClientException("pdf generation exception");
    assertEquals("pdf generation exception", cut.getMessage());
  }
}
