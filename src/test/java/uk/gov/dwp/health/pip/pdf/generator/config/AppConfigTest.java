package uk.gov.dwp.health.pip.pdf.generator.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;
import uk.gov.dwp.health.pip2.common.marshaller.Pip2HealthDisabilityFormMarshaller;

class AppConfigTest {

  private AppConfig appConfig;

  @BeforeEach
  void setup() {
    appConfig = new AppConfig();
  }

  @Test
  void testHtmlFormMarshaller() {
    var actual = appConfig.htmlFormMarshaller();
    assertThat(actual).isNotNull().isInstanceOf(Pip2HealthDisabilityFormMarshaller.class);
  }

  @Test
  void testCreateRestTemplateBean() {
    var restTemplateBuilder = Mockito.spy(new RestTemplateBuilder());
    var httpRespStatusHandler = new HttpRespStatusHandler();

    RestTemplate actualRestTemplate =
        appConfig.restTemplate(restTemplateBuilder, httpRespStatusHandler);

    assertThat(actualRestTemplate).isInstanceOf(RestTemplate.class).isNotNull();
    assertThat(actualRestTemplate.getErrorHandler()).isEqualTo(httpRespStatusHandler);
  }

  @Test
  @DisplayName("Test create object mapper bean")
  void testCreateObjectMapperBean() {
    ObjectMapper actual = appConfig.objectMapper();
    assertThat(actual).isNotNull().isInstanceOf(ObjectMapper.class);
  }

  @Test
  @DisplayName("Test create file utils bean")
  void testCreateFileUtilsBean() {
    FileUtils actual = appConfig.fileUtils();
    assertThat(actual).isNotNull().isInstanceOf(FileUtils.class);
  }
}
