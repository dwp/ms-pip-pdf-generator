package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.dwp.health.pip.pdf.generator.util.JsonTransformation;
import uk.gov.dwp.health.pip2.common.Pip2HealthDisabilityForm;
import uk.gov.dwp.health.pip2.common.marshaller.Pip2HealthDisabilityFormMarshaller;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorServiceImplTest {

  private static final String claimId = "test id";
  private static final String formData = "FORM_DATA";
  @Captor ArgumentCaptor<String> strCaptor;
  @InjectMocks private PdfGeneratorServiceImpl underTest;
  @Mock private JsonTransformation jsonTransformation;
  @Mock private PdfClientServiceImpl pdfClientService;
  @Mock private Pip2HealthDisabilityFormMarshaller pipHealthDisabilityDataMarshaller;
  private Pip2HealthDisabilityForm mockForm;

  @BeforeEach
  void setup() {
    mockForm = mock(Pip2HealthDisabilityForm.class);
  }

  @Test
  void testHandlePdfGenerationThrowsJsonProcessingException() throws JsonProcessingException {
    when(pipHealthDisabilityDataMarshaller.toHealthDisabilityForm(anyString()))
        .thenThrow(JsonProcessingException.class);
    assertThrows(
        PdfGenerationException.class, () -> underTest.handlePdfGeneration(claimId, formData));
    verifyNoInteractions(jsonTransformation);
    verifyNoInteractions(pdfClientService);
  }

  @Test
  void testHandlePdfGenerationThrowsInvalidFormDataException() throws IOException {
    when(mockForm.validate()).thenReturn(false);
    when(pipHealthDisabilityDataMarshaller.toHealthDisabilityForm(anyString()))
        .thenReturn(mockForm);
    assertThrows(
        PdfGenerationException.class, () -> underTest.handlePdfGeneration(claimId, formData));
    verifyNoInteractions(jsonTransformation);
    verifyNoInteractions(pdfClientService);
  }

  @Test
  void testHandlePdfGenerationThrowsIOException() throws IOException {
    when(mockForm.validate()).thenReturn(true);
    when(pipHealthDisabilityDataMarshaller.toHealthDisabilityForm(anyString()))
        .thenReturn(mockForm);
    when(jsonTransformation.transformPipForm(any(Pip2HealthDisabilityForm.class)))
        .thenThrow(IOException.class);
    assertThrows(
        PdfGenerationException.class, () -> underTest.handlePdfGeneration(claimId, formData));
    verifyNoInteractions(pdfClientService);
  }

  @Test
  void testHandlePdfGenerationThrowsPdfConverterException() throws IOException {
    when(mockForm.validate()).thenReturn(true);
    when(pipHealthDisabilityDataMarshaller.toHealthDisabilityForm(anyString()))
        .thenReturn(mockForm);
    when(jsonTransformation.transformPipForm(mockForm)).thenReturn(Map.of("firstName", "SMITH"));
    when(pdfClientService.postCreateRequest(anyString())).thenThrow(PdfClientException.class);
    assertThrows(
        PdfGenerationException.class, () -> underTest.handlePdfGeneration(claimId, formData));
    verify(pdfClientService).postCreateRequest(strCaptor.capture());
    assertThat(strCaptor.getValue()).contains("SMITH");
  }

  @Test
  void testHandlePdfGenerationSuccess() throws IOException {
    Pattern htmlPattern = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
    String pdf = "testPDFData";
    HttpHeaders header = new HttpHeaders();
    ResponseEntity<String> pdfResponse = new ResponseEntity<>(pdf, header, HttpStatus.OK);

    when(mockForm.validate()).thenReturn(true);
    when(pipHealthDisabilityDataMarshaller.toHealthDisabilityForm(anyString()))
        .thenReturn(mockForm);
    when(jsonTransformation.transformPipForm(mockForm)).thenReturn(Map.of("firstName", "SMITH"));
    when(pdfClientService.postCreateRequest(anyString())).thenReturn(pdfResponse);

    String pdfData = underTest.handlePdfGeneration(claimId, formData);
    verify(pdfClientService).postCreateRequest(strCaptor.capture());
    assertEquals("testPDFData", pdfData);
    assertThat(strCaptor.getValue()).contains("SMITH");
    assertTrue(htmlPattern.matcher(strCaptor.getValue()).matches());
  }
}
