package uk.gov.dwp.health.pip.pdf.generator.service;

import uk.gov.dwp.health.pip.pdf.generator.model.AuditableFormSpecification;

public interface GetFormSpecificationService {

  AuditableFormSpecification getFormSpecificationById(String formSpecificationId);

}
