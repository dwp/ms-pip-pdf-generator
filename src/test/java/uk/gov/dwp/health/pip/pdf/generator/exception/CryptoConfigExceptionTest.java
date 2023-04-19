package uk.gov.dwp.health.pip.pdf.generator.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptoConfigExceptionTest {

  @Test
  void testCreateCryptoConfigException() {
    var cut = new CryptoConfigException("crypto config fail");
    assertEquals("crypto config fail", cut.getMessage());
  }
}
