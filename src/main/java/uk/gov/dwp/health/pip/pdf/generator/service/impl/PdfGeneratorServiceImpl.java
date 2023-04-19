package uk.gov.dwp.health.pip.pdf.generator.service.impl;

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
import uk.gov.dwp.health.pip.pdf.generator.service.PdfGeneratorService;
import uk.gov.dwp.health.pip.pdf.generator.service.RestClientService;
import uk.gov.dwp.health.pip.pdf.generator.util.JsonTransformation;
import uk.gov.dwp.health.pip2.common.Pip2HealthDisabilityForm;
import uk.gov.dwp.health.pip2.common.marshaller.Pip2HealthDisabilityFormMarshaller;

import java.io.IOException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

  private final JsonTransformation jsonTransformation;
  private final RestClientService pdfService;
  private final Pip2HealthDisabilityFormMarshaller htmlFormMarshaller;

  @Override
  public String handlePdfGeneration(String claimId, String formData) {
    try {
      log.info("Marshal raw JSON to Pip2HtmlForm object");
      var pipHealthDisabilityForm = htmlFormMarshaller.toHealthDisabilityForm(formData);
      if (pipHealthDisabilityForm.validate()) {
        log.info("PIP form data validated OK");
        final String templateHtml = loadTemplate();
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

  private String loadTemplate() throws IOException {
    log.info("Fetch PDF template");
    return IOUtils.toString(
        Objects.requireNonNull(
            getClass().getResourceAsStream(HTMLTemplateFiles.PIP2_FORM_TEMPLATE)),
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
