package uk.gov.dwp.health.pip.pdf.generator.marshaller;

import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class HealthInformationGatherFormMarshallerTest {

  private HealthInformationGatherFormMarshaller healthInformationGatherFormMarshaller;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    healthInformationGatherFormMarshaller =
        new HealthInformationGatherFormMarshaller(new ObjectMapper());
  }

  @Test
  void testMarshallerReadJsonStringToFormObject() throws IOException, JSONException {
    var path = Paths.get("src", "test", "resources", "hig-form-data", "hig-final-json-full.json");
    var fileContents = Files.readString(path, StandardCharsets.UTF_8);

    var pip2HealthDisabilityForm =
        healthInformationGatherFormMarshaller.toHealthDisabilityForm(fileContents);

    JSONAssert.assertEquals(
        objectMapper.writeValueAsString(pip2HealthDisabilityForm), fileContents, STRICT);
  }

  @Test
  void testMarshallerReadJsonStringToHtmlFormObject() throws IOException, JSONException {
    var path = Paths.get("src", "test", "resources", "hig-form-data", "hig-final-json-full.json");
    var fileContents = Files.readString(path, StandardCharsets.UTF_8);
    JsonNode jsonNode = objectMapper.readTree(fileContents);

    var pip2HealthDisabilityForm =
        healthInformationGatherFormMarshaller.toHealthDisabilityForm(jsonNode);

    JSONAssert.assertEquals(
        objectMapper.writeValueAsString(pip2HealthDisabilityForm), fileContents, STRICT);
  }
}
