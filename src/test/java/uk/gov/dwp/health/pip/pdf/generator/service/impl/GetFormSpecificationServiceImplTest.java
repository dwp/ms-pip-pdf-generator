package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.health.pip.pdf.generator.util.GetFormSpecificationUtil.getTestFormSpecAsObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.forms.FormSpecification;
import uk.gov.dwp.health.pip.pdf.generator.config.HealthCaptureManagerConfig;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.model.AuditableFormSpecification;

@ExtendWith(MockitoExtension.class)
public class GetFormSpecificationServiceImplTest {

  private static final String GET_FORM_SPEC_URI = "https://health-capture-manager/form-specification/{formSpecificationId}";
  @InjectMocks
  GetFormSpecificationServiceImpl getFormSpecificationService;
  @Mock
  RestTemplate restTemplate;
  @Mock
  private HealthCaptureManagerConfig config;

  @BeforeEach
  void beforeEach() {
    getFormSpecificationService = new GetFormSpecificationServiceImpl(config, restTemplate,
        new ObjectMapper());
    when(config.getHealthCaptureManagerUri()).thenReturn(GET_FORM_SPEC_URI);
  }

  @Test
  void when_form_spec_found_return_formSpecification_object() throws IOException {
    String formSpecId = "123456789";
    Object testFormSpec = getTestFormSpecAsObject();
    ResponseEntity<Object> testResponse = ResponseEntity.status(HttpStatus.OK)
        .body(testFormSpec);
    when(restTemplate.getForEntity(anyString(), any(), anyString())).thenReturn(testResponse);
    var result = getFormSpecificationService.getFormSpecificationById(formSpecId);

    verify(restTemplate, times(1)).getForEntity(GET_FORM_SPEC_URI, AuditableFormSpecification.class,
        formSpecId);
    assertThat(result).isInstanceOf(FormSpecification.class);
  }

  @Test
  void when_form_spec_not_found_throw_form_spec_not_found_exception() {
    String formSpecId = "11111111";
    ResponseEntity<Object> testResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    when(restTemplate.getForEntity(anyString(), any(), anyString())).thenReturn(testResponse);

    assertThatThrownBy(
        () -> getFormSpecificationService.getFormSpecificationById(formSpecId)).isInstanceOf(
            PdfClientException.class)
        .hasMessage("Form specification with ID [11111111] not found");
  }
}
