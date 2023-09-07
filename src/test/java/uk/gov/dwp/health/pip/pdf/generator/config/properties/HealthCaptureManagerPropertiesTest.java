package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import javax.validation.Validation;
import javax.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HealthCaptureManagerPropertiesTest {

  private static Validator validator;

  private HealthCaptureManagerProperties properties;

  @BeforeAll
  static void setupSpec() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @BeforeEach
  void setup() {
    properties = new HealthCaptureManagerProperties();
  }

  @Test
  void testHealthCaptureManagerProperties() {
    properties.setEndpointPath("mock-endpoint");
    properties.setBaseUrl("mock-base-url");
    assertThat(properties.getEndpointPath()).isEqualTo("mock-endpoint");
    assertThat(properties.getBaseUrl()).isEqualTo("mock-base-url");
  }

  @Test
  void testHealthCapturePropertiesEmptyFailValidationWithTwoErrors() {
    Assertions.assertThat(validator.validate(properties).size()).isEqualTo(4);
    properties.setBaseUrl("");
    properties.setEndpointPath("");
    Assertions.assertThat(validator.validate(properties).size()).isEqualTo(2);
  }

  @Test
  void testHealthCapturePropertiesNullFailValidationWithTwoError() {
    Assertions.assertThat(validator.validate(properties).size()).isEqualTo(4);
    properties.setBaseUrl(null);
    properties.setEndpointPath(null);
    Assertions.assertThat(validator.validate(properties).size()).isEqualTo(4);
  }

  @Test
  void testHealthCapturePropertiesPassValidation() {
    properties.setBaseUrl("mock-data-key");
    properties.setEndpointPath("mock-endpoint-path");
    Assertions.assertThat(validator.validate(properties).size()).isZero();
  }
}
