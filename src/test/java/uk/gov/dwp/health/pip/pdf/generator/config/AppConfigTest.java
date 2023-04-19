package uk.gov.dwp.health.pip.pdf.generator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;
import uk.gov.dwp.health.pip2.common.marshaller.Pip2HealthDisabilityFormMarshaller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class AppConfigTest {

  private AppConfig underTest;

  @BeforeEach
  void setup() {
    underTest = new AppConfig();
  }

  @Test
  void testHtmlFormMarshaller() {
    var actual = underTest.htmlFormMarshaller();
    assertThat(actual).isNotNull().isInstanceOf(Pip2HealthDisabilityFormMarshaller.class);
  }

  @Test
  void testCreateRestTemplateBean2() {
    ArgumentCaptor<HttpRespStatusHandler> captor =
        ArgumentCaptor.forClass(HttpRespStatusHandler.class);
    RestTemplateBuilder restTemplateBuilder = Mockito.spy(new RestTemplateBuilder());
    HttpRespStatusHandler errorHandler = new HttpRespStatusHandler();
    RestTemplate actual = underTest.restTemplate(errorHandler, restTemplateBuilder);
    verify(restTemplateBuilder).errorHandler(captor.capture());
    assertThat(captor.getValue()).isEqualTo(errorHandler);
    assertThat(actual).isInstanceOf(RestTemplate.class).isNotNull();
    assertThat(actual.getErrorHandler()).isEqualTo(errorHandler);
  }

  @Test
  @DisplayName("Test create object mapper bean")
  void testCreateObjectMapperBean() {
    ObjectMapper actual = underTest.objectMapper();
    assertThat(actual).isNotNull().isInstanceOf(ObjectMapper.class);
  }

  @Test
  @DisplayName("Test create file utils bean")
  void testCreateFileUtilsBean() {
    FileUtils actual = underTest.fileUtils();
    assertThat(actual).isNotNull().isInstanceOf(FileUtils.class);
  }
}
