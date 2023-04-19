package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.model.PdfObject;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileWriterImplTest {

  @InjectMocks private S3FileWriterImpl cut;
  @Mock private S3Client s3Client;
  @Mock private KmsEncryptionServiceImpl encryptionService;
  @Mock private ObjectMapper objectMapper;
  @Captor private ArgumentCaptor<RequestBody> requestBodyArgumentCaptor;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;
  @Captor private ArgumentCaptor<CryptoMessage> cryptoMessageArgumentCaptor;

  @Test
  void testWriteObjectToS3() throws IOException {
    PdfObject pdfObject = mock(PdfObject.class);
    when(pdfObject.getId()).thenReturn("123456");
    when(pdfObject.getBucketName()).thenReturn("pip_bucket");
    when(pdfObject.getContentInBase64()).thenReturn("clear_base64_content");
    CryptoMessage cryptoMessage = mock(CryptoMessage.class);
    when(encryptionService.encrypt(anyString())).thenReturn(cryptoMessage);
    when(objectMapper.writeValueAsBytes(any(CryptoMessage.class)))
        .thenReturn("ENCRYPTED".getBytes());
    String actual = cut.writeObjectToS3(pdfObject);
    InOrder order = inOrder(s3Client, encryptionService, objectMapper);
    order.verify(encryptionService).encrypt(stringArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getValue()).isEqualTo("clear_base64_content");
    order.verify(objectMapper).writeValueAsBytes(cryptoMessageArgumentCaptor.capture());
    assertThat(cryptoMessageArgumentCaptor.getValue()).isInstanceOf(CryptoMessage.class);
    order
        .verify(s3Client)
        .putObject(any(PutObjectRequest.class), requestBodyArgumentCaptor.capture());
    assertThat(
            IOUtils.toString(
                requestBodyArgumentCaptor.getValue().contentStreamProvider().newStream()))
        .isEqualTo("ENCRYPTED");
    assertThat(actual, containsString("123456"));
  }

  @Test
  @DisplayName("Test fail serialise throw task exception")
  void testFailSerialiseThrowPdfGenerationException() throws JsonProcessingException {
    PdfObject pdfObject = mock(PdfObject.class);
    when(pdfObject.getId()).thenReturn("123456");
    when(pdfObject.getBucketName()).thenReturn("pip_bucket");
    when(pdfObject.getContentInBase64()).thenReturn("clear_base64_content");
    CryptoMessage cryptoMessage = mock(CryptoMessage.class);
    when(encryptionService.encrypt(anyString())).thenReturn(cryptoMessage);
    when(objectMapper.writeValueAsBytes(any(CryptoMessage.class)))
        .thenThrow(JsonProcessingException.class);
    assertThrows(TaskException.class, () -> cut.writeObjectToS3(pdfObject));
    verifyNoInteractions(s3Client);
  }

  @Test
  @DisplayName("Test fail to put file in S3 throw task exception")
  void testFailToPutFileInS3ThrowTaskException() throws JsonProcessingException {
    PdfObject pdfObject = mock(PdfObject.class);
    when(pdfObject.getId()).thenReturn("123456");
    when(pdfObject.getBucketName()).thenReturn("pip_bucket");
    when(pdfObject.getContentInBase64()).thenReturn("clear_base64_content");
    CryptoMessage cryptoMessage = mock(CryptoMessage.class);
    when(encryptionService.encrypt(anyString())).thenReturn(cryptoMessage);
    when(objectMapper.writeValueAsBytes(any(CryptoMessage.class)))
        .thenReturn("ENCRYPTED".getBytes());
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(AwsServiceException.class);
    assertThrows(TaskException.class, () -> cut.writeObjectToS3(pdfObject));
  }
}
