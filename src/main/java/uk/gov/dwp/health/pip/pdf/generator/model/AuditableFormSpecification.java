package uk.gov.dwp.health.pip.pdf.generator.model;

import java.time.LocalDateTime;
import lombok.Getter;
import uk.gov.dwp.health.pip.forms.FormSpecification;

@Getter
public class AuditableFormSpecification extends FormSpecification {

  private LocalDateTime createdDate;
}
