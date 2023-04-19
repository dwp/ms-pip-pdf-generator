package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class KmsConfigPropertiesTest {

  private static Validator validator;
  private KmsConfigProperties cut;

  @BeforeAll
  static void setupSpec() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @BeforeEach
  void setup() {
    cut = new KmsConfigProperties();
  }

  @Test
  void testCryptoConfigProperties() {
    cut.setDataKey("mock-data-key");
    cut.setKmsOverride("https://localhost:4567");
    assertThat(cut.getDataKey()).isEqualTo("mock-data-key");
    assertThat(cut.getKmsOverride()).isEqualTo("https://localhost:4567");
  }

  @Test
  void testCryptoConfigPropertiesFailValidationWithOneError() {
    assertThat(validator.validate(cut).size()).isEqualTo(2);
    cut.setDataKey("");
    assertThat(validator.validate(cut).size()).isOne();
  }

  @Test
  void testCryptoConfigPropertiesPassValidation() {
    cut.setDataKey("mock-data-key");
    assertThat(validator.validate(cut).size()).isZero();
  }
}
