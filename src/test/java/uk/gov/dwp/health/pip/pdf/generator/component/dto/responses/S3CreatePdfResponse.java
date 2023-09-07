package uk.gov.dwp.health.pip.pdf.generator.component.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
public class S3CreatePdfResponse {
  private String s3Ref;
  private String bucket;
  @JsonProperty("file_size_kb")
  private String fileSizeKb;
  private String message;
}
