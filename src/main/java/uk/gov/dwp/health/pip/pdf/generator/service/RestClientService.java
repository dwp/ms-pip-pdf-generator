package uk.gov.dwp.health.pip.pdf.generator.service;

import org.springframework.http.ResponseEntity;

public interface RestClientService {

  ResponseEntity<String> postCreateRequest(String htmlData);
}
