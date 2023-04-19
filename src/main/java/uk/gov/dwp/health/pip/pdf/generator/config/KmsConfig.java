package uk.gov.dwp.health.pip.pdf.generator.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.pip.pdf.generator.config.properties.KmsConfigProperties;
import uk.gov.dwp.health.pip.pdf.generator.exception.CryptoConfigException;

@Slf4j
@Configuration
public class KmsConfig {

  @SneakyThrows
  @Bean
  public CryptoConfig cryptoConfig(final KmsConfigProperties properties) {
    CryptoConfig config = new CryptoConfig(properties.getDataKey());
    if (properties.getKmsOverride() != null && !properties.getKmsOverride().isBlank()) {
      config.setKmsEndpointOverride(properties.getKmsOverride());
    }
    return config;
  }

  @SneakyThrows
  @Autowired
  @Bean
  public CryptoDataManager cryptoDataManager(final CryptoConfig configuration) {
    try {
      return new CryptoDataManager(configuration);
    } catch (CryptoException ex) {
      final String msg = String.format("kms crypto config error %s", ex.getMessage());
      log.error(msg);
      throw new CryptoConfigException(msg);
    }
  }
}
