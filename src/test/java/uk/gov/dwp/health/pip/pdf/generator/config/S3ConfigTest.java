package uk.gov.dwp.health.pip.pdf.generator.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.dwp.health.pip.pdf.generator.config.properties.S3ConfigProperties;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ConfigTest {

  @InjectMocks private S3Config cut;
  private S3ConfigProperties prop;

  @BeforeEach
  void setup() {
    prop = mock(S3ConfigProperties.class);
  }

  @Test
  void testCreateS3ClientWithEndpointOverride() throws URISyntaxException {
    when(prop.getAwsRegion()).thenReturn("eu-west2");
    when(prop.getEndpointOverride()).thenReturn("localstack:4527");
    when(prop.isPathStyleEnable()).thenReturn(true);
    S3Client actual = cut.s3Client(prop);
    assertThat(actual).isNotNull().isInstanceOf(S3Client.class);
  }

  @Test
  void testCreateS3Client() throws URISyntaxException {
    when(prop.getEndpointOverride()).thenReturn(null);
    when(prop.getAwsRegion()).thenReturn("eu-west2");
    when(prop.isPathStyleEnable()).thenReturn(true);
    S3Client actual = cut.s3Client(prop);
    assertThat(actual).isNotNull().isInstanceOf(S3Client.class);
  }
}
