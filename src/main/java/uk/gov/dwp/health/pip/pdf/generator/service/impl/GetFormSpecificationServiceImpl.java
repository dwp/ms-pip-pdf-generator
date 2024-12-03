package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.config.HealthCaptureManagerConfig;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.model.AuditableFormSpecification;
import uk.gov.dwp.health.pip.pdf.generator.service.GetFormSpecificationService;

@RequiredArgsConstructor
@Slf4j
@Service
public class GetFormSpecificationServiceImpl implements GetFormSpecificationService {

  private final HealthCaptureManagerConfig config;
  private final RestTemplate restTemplate;

  private final ObjectMapper objectMapper;

  @Override
  public AuditableFormSpecification getFormSpecificationById(String formSpecificationId) {
    try {
      final String healthCaptureManagerEndpoint = config.getHealthCaptureManagerUri();
      ResponseEntity<AuditableFormSpecification> result = restTemplate.getForEntity(
          healthCaptureManagerEndpoint,
          AuditableFormSpecification.class, formSpecificationId);

      if (result.getStatusCode() == HttpStatus.OK) {
        var response = objectMapper.writeValueAsString(result.getBody());
        return objectMapper.readValue(response, AuditableFormSpecification.class);
      } else {
        final String msg =
            String.format("Form specification with ID [%s] not found", formSpecificationId);
        log.error(msg);
        throw new PdfClientException(msg);
      }
    } catch (JsonProcessingException ex) {
      final String msg =
          String.format("Error mapping form spec with id [%s] to FormSpecification.class",
              formSpecificationId);
      log.error(msg);
      throw new PdfClientException(msg);
    }
  }
}
