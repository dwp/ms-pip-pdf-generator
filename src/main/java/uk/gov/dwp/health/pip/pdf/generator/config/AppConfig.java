package uk.gov.dwp.health.pip.pdf.generator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

@Configuration
public class AppConfig {

  @Bean
  public RestTemplate restTemplate(
      final RestTemplateBuilder restTemplateBuilder, final HttpRespStatusHandler errorHandler) {

    return restTemplateBuilder.errorHandler(errorHandler).build();
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
