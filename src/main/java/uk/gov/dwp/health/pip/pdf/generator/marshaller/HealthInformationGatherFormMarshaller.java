package uk.gov.dwp.health.pip.pdf.generator.marshaller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.dwp.health.pip2.model.HealthInformationGatherForm;

@Component
@RequiredArgsConstructor
public class HealthInformationGatherFormMarshaller {
  private final ObjectMapper objectMapper;

  public HealthInformationGatherForm toHealthDisabilityForm(Object object)
      throws JsonProcessingException {
    return this.toHealthDisabilityForm(objectMapper.writeValueAsString(object));
  }

  public HealthInformationGatherForm toHealthDisabilityForm(String json)
      throws JsonProcessingException {
    return objectMapper.readValue(json, HealthInformationGatherForm.class);
  }
}
