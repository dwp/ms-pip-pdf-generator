package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(value = "html.pdf.generator")
@Getter
@Setter
@Validated
public class HtmlPdfConfigProperties {

  @NotBlank(message = "Service baseUrl required")
  private String baseUrl;

  @NotBlank(message = "Service endpointPath required")
  private String endpointPath;

  @NotBlank(message = "The htmlToPdfConformanceLevel required")
  private String htmlToPdfConformanceLevel;
}
