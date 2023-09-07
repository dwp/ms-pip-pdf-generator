package uk.gov.dwp.health.pip.pdf.generator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import uk.gov.dwp.health.pip.pdf.generator.model.AuditableFormSpecification;

public class GetFormSpecificationUtil {

  static ObjectMapper objectMapper = new ObjectMapper();

  public static Object getTestFormSpecAsObject() throws IOException {
    return objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get("src/test/resources/v2TestData/form_specification_123456789.json")))
        , Object.class);
  }

  public static AuditableFormSpecification getTestFormSpecAsFormSpec()
      throws IOException {
    var formSpecString = objectMapper.writeValueAsString(getTestFormSpecAsObject());
    return objectMapper.readValue(formSpecString, AuditableFormSpecification.class);
  }

}
