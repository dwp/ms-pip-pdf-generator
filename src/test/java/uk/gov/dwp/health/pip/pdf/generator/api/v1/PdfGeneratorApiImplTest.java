package uk.gov.dwp.health.pip.pdf.generator.api.v1;

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
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.exception.TaskException;
import uk.gov.dwp.health.pip.pdf.generator.model.PdfObject;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfRequest;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfRequestS3;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.S3PdfReturn;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.PdfGeneratorServiceImpl;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.S3FileWriterImpl;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

import java.io.IOException;
import java.util.Base64;

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

@ExtendWith(MockitoExtension.class)
class PdfGeneratorApiImplTest {

  @InjectMocks private PdfGeneratorApiImpl underTest;
  @Mock private PdfGeneratorServiceImpl pdfGeneratorService;
  @Mock private S3FileWriterImpl s3Service;
  @Mock private FileUtils fileUtils;
  @Mock private Base64.Decoder decoder;
  @Captor private ArgumentCaptor<PdfObject> pdfObjectArgumentCaptor;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;

  @Test
  void testCreatePdfHandledByPdfGeneratorService() throws IOException {
    CreatePdfRequest request = new CreatePdfRequest();
    request.setClaimId("test id");
    request.setFormData("form_data");
    when(pdfGeneratorService.handlePdfGeneration(anyString(), anyString())).thenReturn("pdf data");
    when(decoder.decode(anyString())).thenReturn("pdf data".getBytes());
    ResponseEntity<Resource> actual = underTest.createPDF(request);
    verify(pdfGeneratorService).handlePdfGeneration(request.getClaimId(), request.getFormData());
    assertArrayEquals("pdf data".getBytes(), actual.getBody().getInputStream().readAllBytes());
    assertEquals(HttpStatus.CREATED, actual.getStatusCode());
    assertEquals("form-test id.pdf", actual.getHeaders().getContentDisposition().getFilename());
    assertEquals("attachment", actual.getHeaders().getContentDisposition().getType());
  }

  @Test
  void testCreateS3PdfHandledByS3PdfGeneratorService() {
    CreatePdfRequestS3 request = new CreatePdfRequestS3();
    request.setClaimId("test id");
    request.setFormData("form_data");
    request.setBucket("pip-bucket");
    when(pdfGeneratorService.handlePdfGeneration(anyString(), anyString()))
        .thenReturn("pdf data in clear base64 string");
    when(s3Service.writeObjectToS3(any(PdfObject.class))).thenReturn("Test Key");
    when(decoder.decode(anyString())).thenReturn(new byte[1]);
    when(fileUtils.fileSizeInKb(anyInt())).thenReturn(1);

    ResponseEntity<S3PdfReturn> actual = underTest.s3CreatePDF(request);
    InOrder order = inOrder(pdfGeneratorService, s3Service, decoder, fileUtils);
    order
        .verify(pdfGeneratorService)
        .handlePdfGeneration(request.getClaimId(), request.getFormData());
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
  void testCreateS3PdfThrowsTaskException() {
    CreatePdfRequestS3 request = new CreatePdfRequestS3();
    request.setClaimId("test id");
    request.setFormData("form_data");
    when(pdfGeneratorService.handlePdfGeneration(anyString(), anyString())).thenReturn("pdf data");
    when(s3Service.writeObjectToS3(any(PdfObject.class))).thenThrow(TaskException.class);
    assertThrows(TaskException.class, () -> underTest.s3CreatePDF(request));
    InOrder order = inOrder(pdfGeneratorService, s3Service);
    order
        .verify(pdfGeneratorService)
        .handlePdfGeneration(request.getClaimId(), request.getFormData());
    order.verify(s3Service).writeObjectToS3(pdfObjectArgumentCaptor.capture());
    assertEquals(request.getBucket(), pdfObjectArgumentCaptor.getValue().getBucketName());
    assertEquals("pdf data", pdfObjectArgumentCaptor.getValue().getContentInBase64());
  }

  @Test
  void testCreateS3PdfThrowsPdfGenerationException() {
    CreatePdfRequestS3 request = new CreatePdfRequestS3();
    request.setClaimId("test id");
    request.setFormData("form_data");
    when(pdfGeneratorService.handlePdfGeneration(anyString(), anyString()))
        .thenThrow(PdfGenerationException.class);
    assertThrows(PdfGenerationException.class, () -> underTest.s3CreatePDF(request));
    verify(pdfGeneratorService).handlePdfGeneration(request.getClaimId(), request.getFormData());
    verifyNoInteractions(s3Service);
  }
}
