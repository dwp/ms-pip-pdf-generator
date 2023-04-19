package uk.gov.dwp.health.pip.pdf.generator.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfObjectTest {

  @Test
  void testPdfObjectBuilderGetter() {
    PdfObject actual =
        PdfObject.builder()
            .bucketName("pip_bucket")
            .id("123")
            .contentInBase64("base64_string_content")
            .build();
    assertThat(actual.getBucketName()).isEqualTo("pip_bucket");
    assertThat(actual.getId()).isEqualTo("123");
    assertThat(actual.getContentInBase64()).isEqualTo("base64_string_content");
  }
}
