package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.config.properties.HtmlPdfConfigProperties;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.http.XhtmlToPdfRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfClientServiceImplTest {

  private static final String REQUEST = "Test PDF Request";
  private static HttpHeaders headers;
  @InjectMocks PdfClientServiceImpl underTest;
  @Mock RestTemplate restTemplate;
  @Mock HtmlPdfConfigProperties config;

  @BeforeAll
  static void setupSpec() {
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
  }

  @Test
  void testPostRequestSubmitSuccessfully() {
    when(config.getBaseUrl()).thenReturn("https://pdfGeneration");
    when(config.getEndpointPath()).thenReturn("generate");
    when(config.getHtmlToPdfConformanceLevel()).thenReturn("PDFA_1_A");
    ResponseEntity<String> pdfResponse =
        new ResponseEntity<>("Test PDF Response", headers, HttpStatus.OK);
    HttpEntity<String> requestEntity =
        new HttpEntity<>(
            XhtmlToPdfRequest.builder()
                .htmlData("VGVzdCBQREYgUmVxdWVzdA==")
                .conformanceLevel(config.getHtmlToPdfConformanceLevel())
                .build()
                .toJson(),
            headers);
    when(restTemplate.exchange(
            "https://pdfGeneration/generate", HttpMethod.POST, requestEntity, String.class))
        .thenReturn(pdfResponse);

    ResponseEntity<String> actual = underTest.postCreateRequest(REQUEST);

    assertEquals(HttpStatus.OK, actual.getStatusCode());
    assertEquals(pdfResponse.getStatusCode(), actual.getStatusCode());
    assertEquals("Test PDF Response", actual.getBody());
  }

  @Test
  void testPostRequestServerErrorResponse() {
    when(config.getBaseUrl()).thenReturn("https://pdfGeneration");
    when(config.getEndpointPath()).thenReturn("generate");
    when(config.getHtmlToPdfConformanceLevel()).thenReturn("PDFA_1_A");
    ResponseEntity<String> pdfErrorResponse =
        new ResponseEntity<>("", headers, HttpStatus.INTERNAL_SERVER_ERROR);
    HttpEntity<String> requestEntity =
        new HttpEntity<>(
            XhtmlToPdfRequest.builder()
                .htmlData("VGVzdCBQREYgUmVxdWVzdA==")
                .conformanceLevel(config.getHtmlToPdfConformanceLevel())
                .build()
                .toJson(),
            headers);
    when(restTemplate.exchange(
            "https://pdfGeneration/generate", HttpMethod.POST, requestEntity, String.class))
        .thenReturn(pdfErrorResponse);

    assertThrows(PdfClientException.class, () -> underTest.postCreateRequest(REQUEST));
  }
}
