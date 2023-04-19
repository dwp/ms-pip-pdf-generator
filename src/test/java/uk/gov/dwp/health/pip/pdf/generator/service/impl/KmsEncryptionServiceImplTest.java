package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KmsEncryptionServiceImplTest {

  @InjectMocks private KmsEncryptionServiceImpl cut;
  @Mock private CryptoDataManager dataManager;

  @Captor private ArgumentCaptor<String> stringArgumentCaptor;

  @Test
  void testEncryption() throws Exception {
    String data = "clear_base64_content";
    when(dataManager.encrypt(anyString())).thenReturn(new CryptoMessage());
    cut.encrypt(data);
    verify(dataManager).encrypt(stringArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getValue()).isEqualTo("clear_base64_content");
  }

  @Test
  void testEncryptionServiceThrowAwsKmsCryptoException() throws Exception {
    String data = "clear_base64_content";
    when(dataManager.encrypt(anyString())).thenThrow(CryptoException.class);
    assertThrows(TaskException.class, () -> cut.encrypt(data));
  }
}
