package uk.gov.dwp.health.pip.pdf.generator.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.model.DataKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.pip.pdf.generator.config.properties.KmsConfigProperties;
import uk.gov.dwp.health.pip.pdf.generator.exception.CryptoConfigException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KmsConfigTest {

  private KmsConfig cut;

  @BeforeEach
  void setup() {
    cut = new KmsConfig();
  }

  @Nested
  class createCryptoConfigBean {
    @Test
    void testCreateCryptoConfigBeanWithKmsOverride() {
      KmsConfigProperties props = mock(KmsConfigProperties.class);
      when(props.getKmsOverride()).thenReturn("http://localhost", "http://localhost");
      when(props.getDataKey()).thenReturn("kms_data_key");
      CryptoConfig actual = cut.cryptoConfig(props);
      assertNotNull(actual);
      assertEquals("kms_data_key", actual.getDataKeyId());
      assertEquals("http://localhost", actual.getKmsEndpointOverride());
    }

    @Test
    void testCreateCryptoConfigBeanWithoutKmsDefault() {
      KmsConfigProperties props = mock(KmsConfigProperties.class);
      when(props.getKmsOverride()).thenReturn(null);
      when(props.getDataKey()).thenReturn("kms_data_key");
      CryptoConfig actual = cut.cryptoConfig(props);
      assertNotNull(actual);
      assertEquals("kms_data_key", actual.getDataKeyId());
      assertNull(actual.getKmsEndpointOverride());
    }

    @Test
    void testCreateCryptoConfigWithCustomPropValues() {
      KmsConfigProperties props = mock(KmsConfigProperties.class);
      when(props.getKmsOverride()).thenReturn("http://localhost");
      CryptoConfig actual = cut.cryptoConfig(props);
      assertNotNull(actual);
      assertEquals(Regions.EU_WEST_2, actual.getRegion());
      assertEquals("http://localhost", actual.getKmsEndpointOverride());
    }

    @Test
    void testCreateCryptoConfigWithoutPropValues() {
      KmsConfigProperties props = mock(KmsConfigProperties.class);
      when(props.getKmsOverride()).thenReturn("");
      CryptoConfig actual = cut.cryptoConfig(props);
      assertNotNull(actual);
      assertEquals(Regions.EU_WEST_2, actual.getRegion());
      assertNull(actual.getKmsEndpointOverride());
    }
  }

  @Nested
  class createCryptoDataManagerBean {

    @Test
    void testConfigurationInvalidCryptoConfigExceptionThrown() {
      CryptoConfig config = mock(CryptoConfig.class);
      when(config.isContentValid()).thenReturn(false);
      assertThrows(CryptoConfigException.class, () -> cut.cryptoDataManager(config));
    }

    @Test
    void testCreateCryptoManagerBean() {
      CryptoConfig config = mock(CryptoConfig.class);
      when(config.isContentValid()).thenReturn(true);
      when(config.getEncryptionType()).thenReturn(DataKeySpec.AES_256.name());
      when(config.getRegion()).thenReturn(Regions.EU_WEST_2);
      CryptoDataManager actual = cut.cryptoDataManager(config);
      assertNotNull(actual);
    }
  }
}
