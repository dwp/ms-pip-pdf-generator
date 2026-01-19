package uk.gov.dwp.health.pip.pdf.generator.component.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.dwp.health.pip.pdf.generator.component.utils.UrlBuilderUtil.postCreatePdfV3Url;

import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import uk.gov.dwp.health.pip.pdf.generator.component.dto.responses.ErrorResponse;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.FailureResponse;

class PostCreatePdfV3IT extends ApiTest {
  private final ClassLoader classLoader = getClass().getClassLoader();

  /**
   * When the expected pdf content changes do a manual comparison line by line before overwriting.
   */
  boolean regenerateExpectedPdfs = false;

  /** When the expected pdf content differs from actual pdf save the diff as image for debugging */
  boolean generateDiffAsImagesIfExist = false;

  static Stream<Arguments> argumentsSupplier() {
    return Stream.of(
        Arguments.of("hig-create-pdf"),
        Arguments.of("hig-create-multiple-hcp-multiple-condition"),
        Arguments.of("hig-create-multiple-hcp-single-condition"),
        Arguments.of("hig-create-no-hcp-pip1-multiple-condition"),
        Arguments.of("hig-create-yes-for-all"),
        Arguments.of("hig-create-no-hcp"),
        Arguments.of("hig-create-edge-case"),
        Arguments.of("hig-create-unicode"),
        Arguments.of("hig-create-pdf-county"),
        Arguments.of("hig-create-pdf-county-all-hcps"));
  }

  @ParameterizedTest
  @MethodSource("argumentsSupplier")
  void should_generate_PDF_and_return_a_201_response_code_with_valid_form_data(String inputFileName)
      throws IOException {
    String formData =
        Files.readString(
            Path.of(
                classLoader
                    .getResource(String.format("hig-create-pdf-request/%s.json", inputFileName))
                    .getPath()));
    Response response = postRequest(postCreatePdfV3Url(), formData);
    byte[] actualBytes = response.getBody().asByteArray();
    saveActual(actualBytes, inputFileName + ".pdf");
    byte[] expectedBytes =
        Files.readAllBytes(
            Path.of(
                classLoader
                    .getResource("expected-pdf-outputs/hig-form-data/" + inputFileName + ".pdf")
                    .getPath()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
    List<PdfVisualTester.PdfCompareResult> pdfCompareResults =
        PdfVisualTester.comparePdfDocuments(
            expectedBytes, actualBytes, classLoader.getName(), false);
    saveImageIfDiffExists(inputFileName, pdfCompareResults);
    assertTrue(pdfCompareResults.isEmpty());
  }

  @Test
  void should_return_400_if_form_validation_fails() throws IOException {
    String formData =
        Files.readString(
            Path.of(
                classLoader
                    .getResource("hig-create-pdf-request/hig-create-invalid-form.json")
                    .getPath()));
    Response response = postRequest(postCreatePdfV3Url(), formData);
    FailureResponse errorResponse = response.as(FailureResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

    assertThat(errorResponse.getMessage())
        .isEqualTo(
            "Form data validation failed: [DLA - Preparing and eat food description is blank, whilst condition affecting the applicant]");
  }

  @Test
  void should_return_an_error_message_and_400_response_code_when_payload_is_invalid() {
    Response response = postRequest(postCreatePdfV3Url(), "");
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).contains("Required request body is missing");
  }

  @Test
  void should_return_an_error_message_and_500_response_code_when_form_data_does_not_match_schema()
      throws IOException {
    String formData =
        Files.readString(
            Path.of(
                classLoader
                    .getResource("hig-create-pdf-request/hig-create-invalid.json")
                    .getPath()));

    Response response = postRequest(postCreatePdfV3Url(), formData);
    ErrorResponse errorResponse = response.as(ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(errorResponse.getMessage()).contains("is null");
  }

  private void saveImageIfDiffExists(
      String inputFileName, List<PdfVisualTester.PdfCompareResult> pdfCompareResults) {
    if (generateDiffAsImagesIfExist) {
      IntStream.range(0, pdfCompareResults.size())
          .forEach(
              index -> {
                PdfVisualTester.TestImage testImages = pdfCompareResults.get(index).testImages;
                try {
                  if (Objects.nonNull(testImages)) {
                    ImageIO.write(
                        testImages.getActual(),
                        "png",
                        new File(String.format("%s-%s-actual.png", inputFileName, index + 1)));
                    ImageIO.write(
                        testImages.getExpected(),
                        "png",
                        new File(String.format("%s-%s-expected.png", inputFileName, index + 1)));
                  }
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }

  private void saveActual(final byte[] actualBytes, final String filename) {
    if (regenerateExpectedPdfs)
      try (FileOutputStream outputStream = new FileOutputStream(filename)) {
        outputStream.write(actualBytes);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
  }
}
