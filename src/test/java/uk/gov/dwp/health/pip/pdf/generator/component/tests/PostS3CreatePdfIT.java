package uk.gov.dwp.health.pip.pdf.generator.component.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.dwp.health.pip.pdf.generator.component.utils.UrlBuilderUtil.postCreateS3PdfUrl;

import io.restassured.response.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.dwp.health.pip.pdf.generator.component.dto.responses.ErrorResponse;
import uk.gov.dwp.health.pip.pdf.generator.component.dto.responses.S3CreatePdfResponse;

class PostS3CreatePdfIT extends ApiTest {
  @Test
  void should_return_a_201_response_code_with_valid_response_body()
      throws IOException, JSONException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(classLoader.getResource("json-formdata/valid-test-case.json").getPath()));

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("claim_id", "b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    jsonObject.put("bucket", "pip-bucket");
    jsonObject.put("form_data", formData);
    String requestBody = jsonObject.toString();

    Response response = postRequest(postCreateS3PdfUrl(), requestBody);
    S3CreatePdfResponse s3CreatePdfResponse = response.as(S3CreatePdfResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(s3CreatePdfResponse.getS3Ref())
        .contains("FCP_b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6_PIP-FORM.pdf_");
    assertThat(s3CreatePdfResponse.getBucket()).isEqualTo("pip-bucket");
    assertThat(s3CreatePdfResponse.getFileSizeKb()).isEqualTo("614");
  }

  @Test
  void should_add_encrypted_PDF_to_S3_bucket_after_successful_request()
      throws IOException, JSONException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(classLoader.getResource("json-formdata/valid-test-case.json").getPath()));

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("claim_id", "b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    jsonObject.put("bucket", "pip-bucket");
    jsonObject.put("form_data", formData);
    String requestBody = jsonObject.toString();

    Response response = postRequest(postCreateS3PdfUrl(), requestBody);
    S3CreatePdfResponse s3CreatePdfResponse = response.as(S3CreatePdfResponse.class);

    String s3ObjectString = s3Util.getObjectAsString(s3CreatePdfResponse.getS3Ref());
    JSONObject s3Json = new JSONObject(s3ObjectString);

    assertNotNull(s3Json.get("key"));
    assertNotNull(s3Json.get("message"));
    assertEquals("", s3Json.get("salt"));
    assertNotNull(s3Json.get("hash"));
  }

  @Test
  void should_return_an_error_message_and_400_response_code_when_payload_is_invalid()
      throws JSONException {

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("claim_id", "b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    jsonObject.put("bucket", "pip-bucket");
    String requestBody = jsonObject.toString();

    Response response = postRequest(postCreateS3PdfUrl(), requestBody);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).contains("Validation failed");
  }

  @Test
  void should_return_an_error_message_and_400_response_code_when_claimId_empty()
      throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("claim_id", "");
    jsonObject.put("form_data", "form-data");
    jsonObject.put("bucket", "pip-bucket");
    String requestBody = jsonObject.toString();

    Response response = postRequest(postCreateS3PdfUrl(), requestBody);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).contains("Validation failed");
  }

  @Test
  void should_return_an_error_message_and_500_response_code_when_form_data_does_not_match_schema()
      throws IOException, JSONException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(classLoader.getResource("json-formdata/invalid-test-case.json").getPath()));

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("claim_id", "b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    jsonObject.put("bucket", "pip-bucket");
    jsonObject.put("form_data", formData);
    String requestBody = jsonObject.toString();

    Response response = postRequest(postCreateS3PdfUrl(), requestBody);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(errorResponse.getMessage()).contains("Pdf generation failed for claim");
  }
}
