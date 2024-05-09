package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.encryption")
@Validated
public class KmsConfigProperties {

  private String kmsOverride;

  @NotBlank(message = "KMS data key required")
  @NotNull(message = "KMS data ket required")
  private String dataKey;
}
