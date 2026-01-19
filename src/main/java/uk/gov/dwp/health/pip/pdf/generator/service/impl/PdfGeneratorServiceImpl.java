package uk.gov.dwp.health.pip.pdf.generator.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.mappers.SubmissionDtoToHtmlMapperV2;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;
import uk.gov.dwp.health.pip.pdf.generator.service.PdfGeneratorService;
import uk.gov.dwp.health.pip.pdf.generator.service.RestClientService;

@Slf4j
@RequiredArgsConstructor
@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

  public static final String HIG_FORM_TEMPLATE = "HIG-Form-Template";
  private final RestClientService pdfService;
  private final SubmissionDtoToHtmlMapperV2 submissionDtoToHtmlMapperV2;

  @Override
  public String handleV3PdfGeneration(SubmissionDtoV3 submissionDto) {
    try {
      final String templateWithData =
          submissionDtoToHtmlMapperV2.writeDataToTemplate(submissionDto, HIG_FORM_TEMPLATE);
      ResponseEntity<String> pdfResponse = pdfService.postCreateRequest(templateWithData);
      return pdfResponse.getBody();
    } catch (PdfClientException | JsonProcessingException | ParseException ex) {
      final String msg =
          String.format(
              "V3 Pdf generation failed for claimant [%s] with application id %s - %s",
              submissionDto.getClaimantId(), submissionDto.getApplicationId(), ex.getMessage());
      log.error(msg);
      throw new PdfGenerationException(msg);
    }
  }
}
