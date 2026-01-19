package uk.gov.dwp.health.pip.pdf.generator.service;

import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;

public interface PdfGeneratorService {

  String handleV3PdfGeneration(SubmissionDtoV3 submissionDto);
}
