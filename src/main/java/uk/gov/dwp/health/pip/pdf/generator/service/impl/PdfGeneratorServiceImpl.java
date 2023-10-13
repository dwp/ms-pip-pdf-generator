package uk.gov.dwp.health.pip.pdf.generator.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLTemplateFiles;
import uk.gov.dwp.health.pip.pdf.generator.exception.InvalidFormDataException;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfClientException;
import uk.gov.dwp.health.pip.pdf.generator.exception.PdfGenerationException;
import uk.gov.dwp.health.pip.pdf.generator.mappers.SubmissionDtoToHtmlMapper;
import uk.gov.dwp.health.pip.pdf.generator.model.AuditableFormSpecification;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDto;
import uk.gov.dwp.health.pip.pdf.generator.service.GetFormSpecificationService;
import uk.gov.dwp.health.pip.pdf.generator.service.PdfGeneratorService;
import uk.gov.dwp.health.pip.pdf.generator.service.RestClientService;
import uk.gov.dwp.health.pip.pdf.generator.util.JsonTransformation;
import uk.gov.dwp.health.pip2.common.Pip2HealthDisabilityForm;
import uk.gov.dwp.health.pip2.common.marshaller.Pip2HealthDisabilityFormMarshaller;

@Slf4j
@RequiredArgsConstructor
@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

  private final JsonTransformation jsonTransformation;
  private final RestClientService pdfService;
  private final Pip2HealthDisabilityFormMarshaller htmlFormMarshaller;
  private final GetFormSpecificationService getFormSpecificationService;

  private final SubmissionDtoToHtmlMapper submissionDtoToHtmlMapper;


  @Override
  public String handlePdfGeneration(String claimId, String formData) {
    try {
      log.info("Marshal raw JSON to Pip2HtmlForm object");
      var pipHealthDisabilityForm = htmlFormMarshaller.toHealthDisabilityForm(formData);
      if (pipHealthDisabilityForm.validate()) {
        log.info("PIP form data validated OK");
        final String templateHtml = loadTemplate(false);
        final String resolvedHtml = writeDataToTemplate(templateHtml, pipHealthDisabilityForm);
        ResponseEntity<String> pdfResponse = pdfService.postCreateRequest(resolvedHtml);
        return pdfResponse.getBody();
      } else {
        final String errorMsg =
            String.format(
                "Form data validation failed %s", pipHealthDisabilityForm.errorsToString());
        log.error(errorMsg);
        throw new InvalidFormDataException(errorMsg);
      }
    } catch (InvalidFormDataException | PdfClientException | IOException ex) {
      final String msg =
          String.format("Pdf generation failed for claim [%s] - %s", claimId, ex.getMessage());
      log.error(msg);
      throw new PdfGenerationException(msg);
    }
  }

  @Override
  public String handleVersionedPdfGeneration(SubmissionDto submissionDto) {
    try {
      AuditableFormSpecification formSpecification =
          getFormSpecificationService.getFormSpecificationById(
          submissionDto.getFormSpecificationId());
      final String templateHtml = loadTemplate(true);
      String templateWithData = submissionDtoToHtmlMapper.writeVersionedDataToTemplate(
          submissionDto,
          formSpecification,
          templateHtml);
      ResponseEntity<String> pdfResponse = pdfService.postCreateRequest(
          templateWithData);
      return pdfResponse.getBody();
    } catch (PdfClientException | IOException | ParseException ex) {
      final String msg =
          String.format("Pdf generation failed for claim [%s] - %s",
              submissionDto.getClaimantId(),
              ex.getMessage()
          );
      log.error(msg);
      throw new PdfGenerationException(msg);
    }
  }

  private String loadTemplate(boolean isVersioned) throws IOException {
    log.info("Fetch PDF template");
    var templateToFetch = isVersioned ? HTMLTemplateFiles.PIP2_VERSIONED_TEMPLATE
        : HTMLTemplateFiles.PIP2_FORM_TEMPLATE;
    return IOUtils.toString(
        Objects.requireNonNull(
            getClass().getResourceAsStream(templateToFetch)),
        UTF_8);
  }

  private String writeDataToTemplate(
      String templateHtml, Pip2HealthDisabilityForm pipHealthDisabilityForm) throws IOException {
    log.info("Substitute template with data");
    var substitutor =
        new StringSubstitutor(jsonTransformation.transformPipForm(pipHealthDisabilityForm));
    return substitutor.replace(templateHtml);
  }
}
