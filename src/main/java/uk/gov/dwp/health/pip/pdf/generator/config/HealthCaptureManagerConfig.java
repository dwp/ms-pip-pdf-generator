package uk.gov.dwp.health.pip.pdf.generator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import uk.gov.dwp.health.pip.pdf.generator.config.properties.HealthCaptureManagerProperties;

@Slf4j
@Configuration
public class HealthCaptureManagerConfig {

  private final HealthCaptureManagerProperties healthCaptureManagerProperties;

  public HealthCaptureManagerConfig(
      HealthCaptureManagerProperties healthCaptureManagerProperties) {
    this.healthCaptureManagerProperties = healthCaptureManagerProperties;
  }

  public String getHealthCaptureManagerUri() {
    return this.healthCaptureManagerProperties.getBaseUrl() + "/"
        + this.healthCaptureManagerProperties.getEndpointPath();
  }
}
