package uk.gov.dwp.health.pip.pdf.generator.api;

import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.dwp.health.pip.pdf.generator.dto.responses.ErrorResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.dwp.health.pip.pdf.generator.utils.UrlBuilderUtil.postCreatePdfUrl;

class PostCreatePdfIT extends ApiTest {
  private final ClassLoader classLoader = getClass().getClassLoader();

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_valid_form_data() throws IOException, JSONException {
    String formData =
        Files.readString(Path.of(classLoader.getResource("json-formdata/valid-test-case.json").getPath()));
    String requestBody = createJsonRequestWithFormData(formData);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    byte[] actualBytes = response.body().asByteArray();
    byte[] expectedBytes =
        Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/valid-test-case.pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertTrue(PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false).isEmpty());
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_form_data_containing_multiple_health_conditions() throws IOException, JSONException {
    final String filenamePrefix = "multiple-health-conditions";
    String formData =
            Files.readString(Path.of(classLoader.getResource("json-formdata/" + filenamePrefix + ".json").getPath()));
    String requestBody = createJsonRequestWithFormData(formData);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    byte[] actualBytes = response.body().asByteArray();
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/" + filenamePrefix + ".pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    final List<PdfVisualTester.PdfCompareResult> pdfCompareResults = PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false);
    assertTrue(pdfCompareResults.isEmpty());
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_form_data_containing_multiple_HCPs() throws IOException, JSONException {
    final String filenamePrefix = "multiple-hcps";
    String formData =
            Files.readString(Path.of(classLoader.getResource("json-formdata/" + filenamePrefix + ".json").getPath()));
    String requestBody = createJsonRequestWithFormData(formData);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    byte[] actualBytes = response.body().asByteArray();
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/" + filenamePrefix + ".pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    final List<PdfVisualTester.PdfCompareResult> pdfCompareResults = PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false);
    assertTrue(pdfCompareResults.isEmpty());
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_form_data_containing_minimum_data_capture() throws IOException, JSONException {
    String formData =
            Files.readString(Path.of(classLoader.getResource("json-formdata/minimum-data-capture.json").getPath()));
    String requestBody = createJsonRequestWithFormData(formData);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    byte[] actualBytes = response.body().asByteArray();
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/minimum-data-capture.pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertTrue(PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false).isEmpty());
  }

  @Test
  void should_generate_PDF_and_return_a_201_response_code_with_form_data_containing_maximum_data_capture() throws IOException, JSONException {
    final String filenamePrefix = "maximum-data-capture";
    String formData =
            Files.readString(Path.of(classLoader.getResource("json-formdata/" + filenamePrefix + ".json").getPath()));
    String requestBody = createJsonRequestWithFormData(formData);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    byte[] actualBytes = response.body().asByteArray();
    byte[] expectedBytes =
            Files.readAllBytes(Path.of(classLoader.getResource("expected-pdf-outputs/" + filenamePrefix + ".pdf").getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    final List<PdfVisualTester.PdfCompareResult> pdfCompareResults = PdfVisualTester.comparePdfDocuments(expectedBytes, actualBytes, classLoader.getName(), false);
    assertTrue(pdfCompareResults.isEmpty());
  }

  @Test
  void should_return_an_error_message_and_400_response_code_when_payload_is_invalid() throws JSONException {
    String requestBody = createJsonRequestWithFormData(null);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).contains("Validation failed");
  }

  @Test
  void should_return_an_error_message_and_500_response_code_when_form_data_does_not_match_schema() throws IOException, JSONException {
    String formData =
        Files.readString(Path.of(classLoader.getResource("json-formdata/invalid-test-case.json").getPath()));
    String requestBody = createJsonRequestWithFormData(formData);

    Response response = postRequest(postCreatePdfUrl(), requestBody);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(errorResponse.getMessage()).contains("Pdf generation failed for claim");
  }

  private String createJsonRequestWithFormData(String formData) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("claim_id", "b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    jsonObject.put("form_data", formData);
    return jsonObject.toString();
  }
}
