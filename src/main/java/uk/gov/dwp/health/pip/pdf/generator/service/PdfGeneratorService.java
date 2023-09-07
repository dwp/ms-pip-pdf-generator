package uk.gov.dwp.health.pip.pdf.generator.service;

import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDto;

public interface PdfGeneratorService {

  String handlePdfGeneration(String claimId, String formData);

  String handleVersionedPdfGeneration(SubmissionDto submissionDto);
}
