package uk.gov.dwp.health.pip.pdf.generator.component.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.dwp.health.pip.pdf.generator.component.utils.UrlBuilderUtil.postCreateS3PdfV3Url;

import io.restassured.response.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.dwp.health.pip.pdf.generator.component.dto.responses.ErrorResponse;
import uk.gov.dwp.health.pip.pdf.generator.component.dto.responses.S3CreatePdfResponse;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.FailureResponse;

class PostS3CreatePdfV3IT extends ApiTest {
  @Test
  void should_return_a_201_response_code_with_valid_response_body() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(
                classLoader.getResource("hig-json-submission-data/valid-hig-json.json").getPath()));

    Response response = postRequest(postCreateS3PdfV3Url(), formData);
    S3CreatePdfResponse s3CreatePdfResponse = response.as(S3CreatePdfResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(s3CreatePdfResponse.getS3Ref())
        .contains("FCP_645a11207b05c5770aaa56af_PIP-FORM.pdf_" + LocalDate.now());
    assertThat(s3CreatePdfResponse.getBucket()).isEqualTo("pip-bucket");
    assertThat(s3CreatePdfResponse.getFileSizeKb()).isEqualTo("456");
  }

  @Test
  void should_add_encrypted_PDF_to_S3_bucket_after_successful_request()
      throws IOException, JSONException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(
                classLoader.getResource("hig-json-submission-data/valid-hig-json.json").getPath()));

    Response response = postRequest(postCreateS3PdfV3Url(), formData);
    S3CreatePdfResponse s3CreatePdfResponse = response.as(S3CreatePdfResponse.class);

    String s3ObjectString = s3Util.getObjectAsString(s3CreatePdfResponse.getS3Ref());
    JSONObject s3Json = new JSONObject(s3ObjectString);

    assertNotNull(s3Json.get("key"));
    assertNotNull(s3Json.get("message"));
    assertEquals("", s3Json.get("salt"));
    assertNotNull(s3Json.get("hash"));
  }

  @Test
  void should_return_an_error_message_and_400_response_code_when_payload_is_invalid() {

    Response response = postRequest(postCreateS3PdfV3Url(), "");
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).contains("Required request body is missing");
  }

  @Test
  void should_return_an_error_message_and_500_response_code_when_form_data_does_not_match_schema()
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(
                classLoader
                    .getResource("hig-json-submission-data/request-without-reg-details.json")
                    .getPath()));

    Response response = postRequest(postCreateS3PdfV3Url(), formData);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(errorResponse.getMessage()).contains("is null");
  }

  @Test
  void should_return_400_if_form_validation_fails() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String formData =
        Files.readString(
            Path.of(
                classLoader
                    .getResource("hig-json-submission-data/valid-hig-json-invalid-form.json")
                    .getPath()));
    Response response = postRequest(postCreateS3PdfV3Url(), formData);
    FailureResponse errorResponse = response.as(FailureResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    assertThat(errorResponse.getMessage())
        .isEqualTo(
            "Form data validation failed: [DLA - Preparing and eat food description is blank, whilst condition affecting the applicant]");
  }
}
