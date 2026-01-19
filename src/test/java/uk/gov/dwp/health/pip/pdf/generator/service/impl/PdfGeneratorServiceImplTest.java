package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.text.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.mappers.SubmissionDtoToHtmlMapperV2;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorServiceImplTest {
  @Captor ArgumentCaptor<String> strCaptor;
  ResponseEntity<String> pdfResponse;
  @InjectMocks private PdfGeneratorServiceImpl underTest;
  @Mock private SubmissionDtoToHtmlMapperV2 submissionDtoToHtmlMapperV2;
  @Mock private PdfClientServiceImpl pdfClientService;

  @BeforeEach
  void setup() {
    pdfResponse = createTestPdf();
  }

  private ResponseEntity<String> createTestPdf() {
    HttpHeaders header = new HttpHeaders();
    return new ResponseEntity<>("testPDFData", header, HttpStatus.OK);
  }

  @Test
  void test_v3_pdf_generator_service_called_with_html_string() throws IOException, ParseException {
    when(submissionDtoToHtmlMapperV2.writeDataToTemplate(any(), any())).thenReturn("testPdfData");
    when(pdfClientService.postCreateRequest(anyString())).thenReturn(pdfResponse);
    SubmissionDtoV3 submissionDtoV3 = new SubmissionDtoV3();
    submissionDtoV3.setClaimantId("222123");

    String pdfData = underTest.handleV3PdfGeneration(submissionDtoV3);

    verify(pdfClientService, times(1)).postCreateRequest(any());
    verify(pdfClientService).postCreateRequest(strCaptor.capture());
    assertThat(strCaptor.getValue()).contains("testPdfData");
    assertEquals("testPDFData", pdfData);
  }

  @Test
  void test_v3_pdf_generator_service_throws_pdf_converter_exception()
      throws ParseException, JsonProcessingException {
    String exceptionMessage = "Unable to connect";
    when(submissionDtoToHtmlMapperV2.writeDataToTemplate(any(), any())).thenReturn("testPdfData");
    when(pdfClientService.postCreateRequest("testPdfData"))
        .thenThrow(new PdfClientException(exceptionMessage));
    SubmissionDtoV3 submissionDtoV3 = new SubmissionDtoV3();
    submissionDtoV3.setClaimantId("222123");
    submissionDtoV3.setApplicationId("application_id");

    assertThatThrownBy(() -> underTest.handleV3PdfGeneration(submissionDtoV3))
        .isInstanceOf(PdfGenerationException.class)
        .hasMessageContaining(
            String.format(
                "V3 Pdf generation failed for claimant [%s] with application id %s - %s",
                submissionDtoV3.getClaimantId(),
                submissionDtoV3.getApplicationId(),
                exceptionMessage));

    verify(pdfClientService, times(1)).postCreateRequest(any());
    verify(pdfClientService).postCreateRequest(strCaptor.capture());
    assertThat(strCaptor.getValue()).contains("testPdfData");
  }
}
