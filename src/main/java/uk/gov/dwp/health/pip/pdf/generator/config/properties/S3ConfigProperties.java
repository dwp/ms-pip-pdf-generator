package uk.gov.dwp.health.pip.pdf.generator.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "aws.s3")
@Component
@Validated
@Getter
@Setter
public class S3ConfigProperties {

  @NotBlank(message = "AWS region required")
  @NotNull(message = "AWS region required")
  private String awsRegion;

  private String bucket;

  private String endpointOverride;

  private boolean pathStyleEnable;
}
