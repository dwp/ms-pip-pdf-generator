package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(value = "health.capture.manager")
@Getter
@Setter
@Validated
public class HealthCaptureManagerProperties {

  @NotBlank(message = "Service baseUrl required")
  @NotNull(message = "baseUrl required")
  private String baseUrl;

  @NotBlank(message = "Service endpointPath required")
  @NotNull(message = "endpointPath required")
  private String endpointPath;
}
