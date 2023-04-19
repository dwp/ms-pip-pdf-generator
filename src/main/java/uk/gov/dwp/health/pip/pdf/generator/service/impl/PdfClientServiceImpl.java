package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.config.properties.HtmlPdfConfigProperties;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.http.XhtmlToPdfRequest;
import uk.gov.dwp.health.pip.pdf.generator.service.RestClientService;

import java.util.Collections;

@Slf4j
@Service
public class PdfClientServiceImpl implements RestClientService {

  private final HtmlPdfConfigProperties config;
  private final RestTemplate restTemplate;

  public PdfClientServiceImpl(
      final RestTemplate restTemplate, final HtmlPdfConfigProperties config) {
    this.restTemplate = restTemplate;
    this.config = config;
  }

  @Override
  public ResponseEntity<String> postCreateRequest(String htmlData) {
    log.debug("Encode form in base64");
    final String base64HtmlSubmission = Base64.encodeBase64String(htmlData.getBytes());
    log.info("Send request to HTML to PDF service");
    final String uri = String.format("%s/%s", config.getBaseUrl(), config.getEndpointPath());
    log.debug("Send request to HTML to PDF service URI {}", uri);
    ResponseEntity<String> resp =
        postRequest(
            uri,
            XhtmlToPdfRequest.builder()
                .htmlData(base64HtmlSubmission)
                .conformanceLevel(config.getHtmlToPdfConformanceLevel())
                .build()
                .toJson());
    return handleCreateResponse(resp);
  }

  private ResponseEntity<String> postRequest(final String url, final String jsonPayload) {
    log.debug("Add request header content");
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
    return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
  }

  private ResponseEntity<String> handleCreateResponse(final ResponseEntity<String> resp) {
    if (resp.getStatusCode() == HttpStatus.OK) {
      log.info("Pdf request OK");
      return resp;
    } else {
      final String msg = String.format("Error Creating Pdf %d", resp.getStatusCode().value());
      log.error(msg);
      throw new PdfClientException(msg);
    }
  }
}
