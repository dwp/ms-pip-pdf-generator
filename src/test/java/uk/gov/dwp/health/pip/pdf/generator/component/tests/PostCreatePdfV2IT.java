package uk.gov.dwp.health.pip.pdf.generator.component.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.dwp.health.pip.pdf.generator.component.utils.UrlBuilderUtil.postCreatePdfV2Url;

import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester;
import io.restassured.response.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.dwp.health.pip.pdf.generator.component.dto.responses.ErrorResponse;


class PostCreatePdfV2IT extends ApiTest {
  private final ClassLoader classLoader = getClass().getClassLoader();

  /**
   * When the expected  pdf content changes do a manual comparison line by line before overwriting.
   */
  private boolean regenerateExpectedPdfs = false;

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_valid_form_data() throws IOException {
    String formData =
            Files.readString(Path.of(classLoader.getResource("v2TestData/testSubmissionDto.json").getPath()));
    Response response = postRequest(postCreatePdfV2Url(), formData);
    byte[] actualBytes = response.body().asByteArray();
    final String filenamePrefix = "valid-test-case-v2";
    saveActual(actualBytes, filenamePrefix + ".pdf");
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/" + filenamePrefix + ".pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertTrue(PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false).isEmpty());
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_form_data_containing_minimum_data_capture() throws IOException {
    final String filenamePrefix = "minimum-data-capture-v2";
    String formData =
            Files.readString(Path.of(classLoader.getResource("json-formdata/" + filenamePrefix + ".json").getPath()));

    Response response = postRequest(postCreatePdfV2Url(), formData);
    byte[] actualBytes = response.body().asByteArray();
    saveActual(actualBytes, filenamePrefix + ".pdf");
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/" + filenamePrefix + ".pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertTrue(PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false).isEmpty());
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_form_data_containing_maximum_data_capture() throws IOException {
    final String filenamePrefix = "maximum-data-capture-v2";
    String formData =
            Files.readString(Path.of(classLoader.getResource("json-formdata/" + filenamePrefix + ".json").getPath()));

    Response response = postRequest(postCreatePdfV2Url(), formData);
    byte[] actualBytes = response.body().asByteArray();
    saveActual(actualBytes, filenamePrefix + ".pdf");
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/" + filenamePrefix + ".pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertTrue(PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false).isEmpty());
  }

  @Test
  void should_return_an_error_message_and_400_response_code_when_payload_is_invalid() {
    Response response = postRequest(postCreatePdfV2Url(), "");
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).contains("Required request body is missing");
  }

  @Test
  void should_return_an_error_message_and_500_response_code_when_form_data_does_not_match_schema() throws IOException {
    String formData =
        Files.readString(Path.of(classLoader.getResource("json-formdata/invalid-test-case-v2.json").getPath()));

    Response response = postRequest(postCreatePdfV2Url(), formData);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(errorResponse.getMessage()).contains("is null");
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_edge_form_data() throws IOException {
    String formData =
        Files.readString(Path.of(classLoader.getResource("v2TestData/EdgeCaseSubmissionDto.json").getPath()));
    Response response = postRequest(postCreatePdfV2Url(), formData);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
  }

  private void saveActual(final byte[] actualBytes, final String filename) {
    if (regenerateExpectedPdfs) try {
      final FileOutputStream fileOutputStream = new FileOutputStream(filename);
      fileOutputStream.write(actualBytes);
      fileOutputStream.flush();
      fileOutputStream.close();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
