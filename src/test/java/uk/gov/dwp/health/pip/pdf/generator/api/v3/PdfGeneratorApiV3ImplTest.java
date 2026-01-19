package uk.gov.dwp.health.pip.pdf.generator.api.v3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.model.PdfObject;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfS3V3;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.S3PdfReturn;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.PdfGeneratorServiceImpl;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.S3FileWriterImpl;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

@ExtendWith(MockitoExtension.class)
public class PdfGeneratorApiV3ImplTest {

  @InjectMocks private PdfGeneratorApiV3Impl pdfGeneratorApiV3;
  @Mock private PdfGeneratorServiceImpl pdfGeneratorService;
  @Mock private S3FileWriterImpl s3Service;
  @Mock private FileUtils fileUtils;
  @Mock private Base64.Decoder decoder;
  @Captor private ArgumentCaptor<PdfObject> pdfObjectArgumentCaptor;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;

  private SubmissionDtoV3 testDto;

  @BeforeEach
  void beforeEach() {
    testDto = new SubmissionDtoV3();
    testDto.claimantId("123456789");
    testDto.applicationId("123456789");
  }

  @Test
  void testCreateV3PdfHandledByPdfGeneratorService() throws IOException {
    when(pdfGeneratorService.handleV3PdfGeneration(any())).thenReturn("pdf data");
    when(decoder.decode(anyString())).thenReturn("pdf data".getBytes());

    ResponseEntity<Resource> actual = pdfGeneratorApiV3.createPDF(testDto);

    verify(pdfGeneratorService).handleV3PdfGeneration(any());
    assertArrayEquals("pdf data".getBytes(), actual.getBody().getInputStream().readAllBytes());
    assertEquals(HttpStatus.CREATED, actual.getStatusCode());
    assertEquals("form-123456789.pdf", actual.getHeaders().getContentDisposition().getFilename());
    assertEquals("attachment", actual.getHeaders().getContentDisposition().getType());
    assertEquals(MediaType.APPLICATION_PDF, actual.getHeaders().getContentType());
  }

  @Test
  void testCreateS3V3PdfHandledByS3PdfGeneratorService() {
    CreatePdfS3V3 request = new CreatePdfS3V3();
    request.setSubmissionDto(testDto);
    request.setBucket("pip-bucket");
    when(pdfGeneratorService.handleV3PdfGeneration(any()))
        .thenReturn("pdf data in clear base64 string");
    when(s3Service.writeObjectToS3(any(PdfObject.class))).thenReturn("Test Key");
    when(decoder.decode(anyString())).thenReturn(new byte[1]);
    when(fileUtils.fileSizeInKb(anyInt())).thenReturn(1);

    ResponseEntity<S3PdfReturn> actual = pdfGeneratorApiV3.s3CreatePDF(request);
    InOrder order = inOrder(pdfGeneratorService, s3Service, decoder, fileUtils);
    order.verify(pdfGeneratorService).handleV3PdfGeneration(any());
    order.verify(s3Service).writeObjectToS3(pdfObjectArgumentCaptor.capture());
    assertEquals(request.getBucket(), pdfObjectArgumentCaptor.getValue().getBucketName());
    order.verify(decoder).decode(stringArgumentCaptor.capture());
    assertEquals("pdf data in clear base64 string", stringArgumentCaptor.getValue());
    order.verify(fileUtils).fileSizeInKb(1);
    assertEquals(
        "pdf data in clear base64 string", pdfObjectArgumentCaptor.getValue().getContentInBase64());
    assertEquals(HttpStatus.CREATED, actual.getStatusCode());
    assertEquals("Test Key", actual.getBody().getS3Ref());
    assertEquals("pip-bucket", actual.getBody().getBucket());
    assertEquals(1, actual.getBody().getFileSizeKb());
  }

  @Test
  void testCreateS3V3PdfThrowsTaskException() {
    CreatePdfS3V3 request = new CreatePdfS3V3();
    request.setSubmissionDto(testDto);
    request.setBucket("TEST_BUCKET");
    when(pdfGeneratorService.handleV3PdfGeneration(any())).thenReturn("pdf data");
    when(s3Service.writeObjectToS3(any(PdfObject.class))).thenThrow(TaskException.class);
    assertThrows(TaskException.class, () -> pdfGeneratorApiV3.s3CreatePDF(request));
    InOrder order = inOrder(pdfGeneratorService, s3Service);
    order.verify(pdfGeneratorService).handleV3PdfGeneration(any());
    order.verify(s3Service).writeObjectToS3(pdfObjectArgumentCaptor.capture());
    assertEquals(request.getBucket(), pdfObjectArgumentCaptor.getValue().getBucketName());
    assertEquals("pdf data", pdfObjectArgumentCaptor.getValue().getContentInBase64());
  }

  @Test
  void testCreateS3V3PdfThrowsPdfGenerationException() {
    CreatePdfS3V3 request = new CreatePdfS3V3();
    request.setSubmissionDto(testDto);
    request.setBucket("TEST_BUCKET");
    when(pdfGeneratorService.handleV3PdfGeneration(any())).thenThrow(PdfGenerationException.class);
    assertThrows(PdfGenerationException.class, () -> pdfGeneratorApiV3.s3CreatePDF(request));
    verify(pdfGeneratorService).handleV3PdfGeneration(any());
    verifyNoInteractions(s3Service);
  }
}
