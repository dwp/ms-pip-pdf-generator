package uk.gov.dwp.health.pip.pdf.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfObject {
  private String id;
  private String bucketName;
  private String contentInBase64;
}
