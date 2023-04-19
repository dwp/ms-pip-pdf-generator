package uk.gov.dwp.health.pip.pdf.generator.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.gov.dwp.health.pip.pdf.generator.model.PdfObject;
import uk.gov.dwp.health.pip.pdf.generator.openapi.api.V1Api;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfRequest;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfRequestS3;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.S3PdfReturn;
import uk.gov.dwp.health.pip.pdf.generator.service.PdfGeneratorService;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.S3FileWriterImpl;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

import java.util.Base64;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PdfGeneratorApiImpl implements V1Api {

  private final PdfGeneratorService pdfGeneratorService;
  private final S3FileWriterImpl s3Service;
  private final Base64.Decoder decoder;
  private final FileUtils fileUtils;

  @Override
  public ResponseEntity<Resource> createPDF(CreatePdfRequest createPdfRequest) {
    byte[] pdf =
        decoder.decode(
            pdfGeneratorService.handlePdfGeneration(
                createPdfRequest.getClaimId(), createPdfRequest.getFormData()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(pdf.length)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format("attachment; filename=form-%s.pdf", createPdfRequest.getClaimId()))
        .body(new ByteArrayResource(pdf));
  }

  @Override
  public ResponseEntity<S3PdfReturn> s3CreatePDF(CreatePdfRequestS3 createS3PdfRequest) {
    final String pdf =
        pdfGeneratorService.handlePdfGeneration(
            createS3PdfRequest.getClaimId(), createS3PdfRequest.getFormData());
    final PdfObject pdfObject =
        PdfObject.builder()
            .id(createS3PdfRequest.getClaimId())
            .bucketName(createS3PdfRequest.getBucket())
            .contentInBase64(pdf)
            .build();
    S3PdfReturn s3PdfReturn = new S3PdfReturn();
    s3PdfReturn.setS3Ref(s3Service.writeObjectToS3(pdfObject));
    s3PdfReturn.setBucket(pdfObject.getBucketName());
    s3PdfReturn.setFileSizeKb(
        fileUtils.fileSizeInKb(decoder.decode(pdfObject.getContentInBase64()).length));
    return ResponseEntity.status(HttpStatus.CREATED).body(s3PdfReturn);
  }
}
