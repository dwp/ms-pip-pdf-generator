package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.service.EncryptionService;

@Slf4j
@Service
public class KmsEncryptionServiceImpl implements EncryptionService<String, CryptoMessage> {

  private final CryptoDataManager cryptoDataManager;

  public KmsEncryptionServiceImpl(CryptoDataManager cryptoDataManager) {
    this.cryptoDataManager = cryptoDataManager;
  }

  @Override
  public CryptoMessage encrypt(final String clearBase64) {
    try {
      return cryptoDataManager.encrypt(clearBase64);
    } catch (CryptoException e) {
      final String message = String.format("Encryption failed %s", e.getMessage());
      log.error(message);
      throw new TaskException(message);
    }
  }
}
