package uk.gov.dwp.health.pip.pdf.generator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;
import uk.gov.dwp.health.pip.pdf.generator.util.JsonTransformation;
import uk.gov.dwp.health.pip2.common.marshaller.Pip2HealthDisabilityFormMarshaller;

import java.util.Base64;

@Configuration
public class AppConfig {

  @Bean
  public Pip2HealthDisabilityFormMarshaller htmlFormMarshaller() {
    return new Pip2HealthDisabilityFormMarshaller();
  }

  @Autowired
  @Bean
  public RestTemplate restTemplate(
      final HttpRespStatusHandler errorHandler, final RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.errorHandler(errorHandler).build();
  }

  @Bean
  public JsonTransformation transformationUtils() {
    return new JsonTransformation();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public FileUtils fileUtils() {
    return new FileUtils();
  }

  @Bean
  public Base64.Decoder base64Decoder() {
    return Base64.getDecoder();
  }
}
