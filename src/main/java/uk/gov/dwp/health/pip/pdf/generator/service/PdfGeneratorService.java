package uk.gov.dwp.health.pip.pdf.generator.service;

public interface PdfGeneratorService {

  String handlePdfGeneration(String claimId, String formData);
}
