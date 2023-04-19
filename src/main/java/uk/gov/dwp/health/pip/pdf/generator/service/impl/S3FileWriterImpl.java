package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import uk.gov.dwp.health.pip.pdf.generator.exception.FileVerificationException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.model.PdfObject;
import uk.gov.dwp.health.pip.pdf.generator.service.FileWriter;
import uk.gov.dwp.health.pip2.common.FilePrefix;
import uk.gov.dwp.health.pip2.common.FileUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class S3FileWriterImpl implements FileWriter<PdfObject, String> {

  private final S3Client s3Client;
  private final KmsEncryptionServiceImpl encryptionService;
  private final ObjectMapper objectMapper;

  public S3FileWriterImpl(
      S3Client s3Client, KmsEncryptionServiceImpl encryptionService, ObjectMapper objectMapper) {
    this.s3Client = s3Client;
    this.encryptionService = encryptionService;
    this.objectMapper = objectMapper;
  }

  @Override
  public String writeObjectToS3(PdfObject pdf) {
    final String key = FileUtils.generatePip2S3Key(FilePrefix.JCP, pdf.getId(), "PIP-FORM.pdf");
    putObjectToS3(pdf, key);
    return key;
  }

  private void putObjectToS3(PdfObject pdf, String key) {
    PutObjectResponse response;
    try {
      final byte[] encrypted =
          objectMapper.writeValueAsBytes(encryptionService.encrypt(pdf.getContentInBase64()));
      response =
          s3Client.putObject(
              PutObjectRequest.builder()
                  .bucket(pdf.getBucketName())
                  .key(key)
                  .contentType(MediaType.APPLICATION_PDF_VALUE)
                  .build(),
              RequestBody.fromBytes(encrypted));
      if (Optional.ofNullable(response).isPresent()
          && (response.eTag() == null || Objects.requireNonNull(response).eTag().isEmpty())) {
        final String msg = "Fail to persist file to S3 - no eTag/checksum in response";
        log.error(msg);
        throw new FileVerificationException(msg);
      }
    } catch (AwsServiceException
        | SdkClientException
        | FileVerificationException
        | JsonProcessingException ex) {
      final String msg =
          String.format(
              "Fail to upload file [%s] to S3 bucket [%s] - %s",
              key, pdf.getBucketName(), ex.getMessage());
      log.error(msg);
      throw new TaskException(msg);
    }
  }
}
