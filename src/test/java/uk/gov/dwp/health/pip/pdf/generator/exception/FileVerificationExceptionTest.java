package uk.gov.dwp.health.pip.pdf.generator.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileVerificationExceptionTest {

  @Test
  void testCreateFileVerificationException() {
    var cut = new FileVerificationException("verification failed");
    assertThat(cut.getMessage()).isEqualTo("verification failed");
  }
}
