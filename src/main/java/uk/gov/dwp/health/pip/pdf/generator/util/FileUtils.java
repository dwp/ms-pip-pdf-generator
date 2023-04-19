package uk.gov.dwp.health.pip.pdf.generator.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class FileUtils {

  private static final int ONE_KB = 1024;

  public int fileSizeInKb(final int size) {
    if (size < 0) {
      log.error("File size [{} kb] illegal", size);
      throw new IllegalArgumentException("File size input is illegal, must equal or grater than 0");
    }
    float sizeInFloat = Float.parseFloat(String.valueOf(size)) / ONE_KB;
    return BigDecimal.valueOf(sizeInFloat).setScale(0, RoundingMode.UP).intValue();
  }
}
