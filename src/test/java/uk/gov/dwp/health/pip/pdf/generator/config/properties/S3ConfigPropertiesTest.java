package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class S3ConfigPropertiesTest {

  private static Validator VALIDATOR;
  @InjectMocks private S3ConfigProperties cut;

  @BeforeAll
  static void setupSpec() {
    VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void testAwsS3PropertiesFailOneMissingRegion() {
    assertThat(VALIDATOR.validate(cut).size()).isEqualTo(2);
  }

  @Test
  void testAWSS3PropertiesOk() {
    cut.setAwsRegion("eu_west_2");
    cut.setBucket("mock_bucket");
    cut.setEndpointOverride("mock_endpoint_override");
    assertThat(VALIDATOR.validate(cut).size()).isZero();
    assertThat(cut.isPathStyleEnable()).isFalse();
    assertThat(cut.getAwsRegion()).isEqualTo("eu_west_2");
    assertThat(cut.getBucket()).isEqualTo("mock_bucket");
    assertThat(cut.getEndpointOverride()).isEqualTo("mock_endpoint_override");
  }
}
