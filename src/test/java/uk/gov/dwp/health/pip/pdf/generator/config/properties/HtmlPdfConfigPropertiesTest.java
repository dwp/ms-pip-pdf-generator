package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlPdfConfigPropertiesTest {

  private static Validator validator;
  private HtmlPdfConfigProperties underTest;

  @BeforeAll
  static void setupSpec() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  private static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of("base_url", "path", "pdf_A", 0),
        Arguments.of(null, null, null, 3),
        Arguments.of("", "", "", 3),
        Arguments.of("base_url", "path", "", 1),
        Arguments.of("", "", null, 3));
  }

  @BeforeEach
  void setup() {
    underTest = new HtmlPdfConfigProperties();
  }

  @Test
  void testGetSetHtmlToPdfServiceUrl() {
    underTest.setBaseUrl("base_url");
    assertThat(underTest.getBaseUrl()).isEqualTo("base_url");
  }

  @Test
  void testGetSetHtmlToPdfConformanceLevel() {
    underTest.setHtmlToPdfConformanceLevel("pdf_A");
    assertThat(underTest.getHtmlToPdfConformanceLevel()).isEqualTo("pdf_A");
  }

  @ParameterizedTest
  @MethodSource(value = "testCases")
  void testValidationConstraintOnFields(
      final String baseUrl, final String endpoint, final String conformance, int expected) {
    underTest.setBaseUrl(baseUrl);
    underTest.setEndpointPath(endpoint);
    underTest.setHtmlToPdfConformanceLevel(conformance);
    assertThat(validator.validate(underTest).size()).isEqualTo(expected);
  }
}
