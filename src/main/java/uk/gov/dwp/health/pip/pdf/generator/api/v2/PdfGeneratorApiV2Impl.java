package uk.gov.dwp.health.pip.pdf.generator.api.v2;

import java.util.Base64;
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
import uk.gov.dwp.health.pip.pdf.generator.openapi.api.V2Api;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfS3;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.S3PdfReturn;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDto;
import uk.gov.dwp.health.pip.pdf.generator.service.PdfGeneratorService;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.S3FileWriterImpl;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PdfGeneratorApiV2Impl implements V2Api {

  private final PdfGeneratorService pdfGeneratorService;
  private final S3FileWriterImpl s3Service;
  private final Base64.Decoder decoder;
  private final FileUtils fileUtils;

  @Override
  public ResponseEntity<Resource> createPDF(SubmissionDto submissionDto) {
    byte[] pdf =
        decoder.decode(
            pdfGeneratorService.handleVersionedPdfGeneration(submissionDto));
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(pdf.length)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format("attachment; filename=form-%s.pdf", submissionDto.getClaimantId()))
        .body(new ByteArrayResource(pdf));
  }

  @Override
  public ResponseEntity<S3PdfReturn> s3CreatePDF(CreatePdfS3 createPdfS3) {
    final SubmissionDto submissionDto = createPdfS3.getSubmissionDto();
    final String pdf =
        pdfGeneratorService.handleVersionedPdfGeneration(submissionDto);
    final PdfObject pdfObject =
        PdfObject.builder()
            .id(submissionDto.getClaimantId())
            .bucketName(createPdfS3.getBucket())
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
