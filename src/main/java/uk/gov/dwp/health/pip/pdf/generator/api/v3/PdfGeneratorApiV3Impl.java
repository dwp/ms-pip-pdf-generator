package uk.gov.dwp.health.pip.pdf.generator.api.v3;

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
import uk.gov.dwp.health.pip.pdf.generator.openapi.api.V3Api;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfS3V3;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.S3PdfReturn;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;
import uk.gov.dwp.health.pip.pdf.generator.service.PdfGeneratorService;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.S3FileWriterImpl;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PdfGeneratorApiV3Impl implements V3Api {

  private final Base64.Decoder decoder;
  private final PdfGeneratorService pdfGeneratorService;
  private final S3FileWriterImpl s3Service;
  private final FileUtils fileUtils;

  @Override
  public ResponseEntity<S3PdfReturn> s3CreatePDF(CreatePdfS3V3 createPdfS3V3) {
    final SubmissionDtoV3 submissionDto = createPdfS3V3.getSubmissionDto();

    log.info(
        "s3CreatePDF(): About to process v3 request for application id {}",
        submissionDto.getApplicationId());

    final String pdf = pdfGeneratorService.handleV3PdfGeneration(submissionDto);

    final PdfObject pdfObject =
        PdfObject.builder()
            .id(submissionDto.getClaimantId())
            .bucketName(createPdfS3V3.getBucket())
            .contentInBase64(pdf)
            .build();

    S3PdfReturn s3PdfReturn = new S3PdfReturn();
    s3PdfReturn.setS3Ref(s3Service.writeObjectToS3(pdfObject));
    s3PdfReturn.setBucket(pdfObject.getBucketName());
    s3PdfReturn.setFileSizeKb(
        fileUtils.fileSizeInKb(decoder.decode(pdfObject.getContentInBase64()).length));

    ResponseEntity<S3PdfReturn> responseEntity =
        ResponseEntity.status(HttpStatus.CREATED).body(s3PdfReturn);

    log.info(
        "s3CreatePDFV3(): Processed v3 request for application id {}",
        submissionDto.getApplicationId());

    return responseEntity;
  }

  @Override
  public ResponseEntity<Resource> createPDF(SubmissionDtoV3 submissionDtoV3) {
    log.info(
        "createPDFV3: About to process v3 request for application id {}",
        submissionDtoV3.getApplicationId());

    byte[] pdf = decoder.decode(pdfGeneratorService.handleV3PdfGeneration(submissionDtoV3));

    ResponseEntity<Resource> responseEntity =
        ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdf.length)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=form-%s.pdf", submissionDtoV3.getClaimantId()))
            .body(new ByteArrayResource(pdf));

    log.info(
        "createPDFV3(): Processed v3 request for application id {}",
        submissionDtoV3.getApplicationId());

    return responseEntity;
  }
}
