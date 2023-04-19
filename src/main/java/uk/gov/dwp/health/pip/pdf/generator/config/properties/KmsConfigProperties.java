package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
