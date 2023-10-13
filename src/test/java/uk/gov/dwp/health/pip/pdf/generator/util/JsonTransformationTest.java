package uk.gov.dwp.health.pip.pdf.generator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLConstants;
import uk.gov.dwp.health.pip2.common.Pip2HealthDisabilityForm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JsonTransformationTest {

  private static Pip2HealthDisabilityForm pipHealthDisabilityForm;
  private static ObjectMapper objectMapper;

  @InjectMocks private JsonTransformation jsonTransformation;

  @BeforeAll
  static void setup() throws IOException {
    String incomingJson =
        IOUtils.toString(
            Objects.requireNonNull(
                JsonTransformationTest.class.getResourceAsStream("/valid-test-case.json")),
            StandardCharsets.UTF_8);
    objectMapper = new ObjectMapper();
    pipHealthDisabilityForm = objectMapper.readValue(incomingJson, Pip2HealthDisabilityForm.class);
  }

  @Test
  void testTransformPipFormSuccess() throws IOException {
    Map<String, String> result = jsonTransformation.transformPipForm(pipHealthDisabilityForm);
    assertEquals("13 November 2020", result.get("submissionDate"));
    assertEquals("MR", result.get("title"));
    assertEquals("claimant_first_name", result.get("firstName"));
    assertEquals("claimant_last_name", result.get("lastName"));
    assertEquals("RN 00 00 64 C", result.get("nino"));
    assertEquals("08 August 1970", result.get("dateOfBirth"));
    assertEquals("Yes", result.get("foodAffected"));
    assertEquals(
        "<br/>\n"
            + "<h3>Tell us about the difficulties you have with preparing food and how you manage them</h3>\n"
            + "<br/>\n"
            + "reason not be able to preparing food",
        result.get("foodDescription"));
    assertEquals("Yes", result.get("navAffected"));
    assertEquals(
        "<br/>\n"
            + "<h3>Tell us more about the difficulties you have with planning and following journeys and how you manage them</h3>\n"
            + "<br/>\n"
            + "reason not be able to plan or navigate",
        result.get("navDescription"));
    assertEquals("Yes", result.get("otherAffected"));
    assertEquals(
        "<h3>Additional information:</h3>\n"
            + "<br/>\n"
            + "additional information to support claim\n"
            + "<br/>",
        result.get("other"));

    assertEquals(
        "<h2>Health professional details</h2>\n"
            + "<br/>\n"
            + "<h3>Name</h3>\n"
            + "<br/>\n"
            + "Doctor Who\n"
            + "<br/>\n"
            + "<h3>Profession:</h3>\n"
            + "<br/>\n"
            + "GP\n"
            + "<br/>\n"
            + "<h3>Phone number including dialling code:</h3>\n"
            + "<br/>\n"
            + "07777\n"
            + "<br/>\n"
            + "<h3>Their address</h3>\n"
            + "<br>Quarry House Leeds</br>\n"
            + "<br>Leeds</br>\n"
            + "<br>England</br>\n"
            + "<br>LS1 1XX</br>\n"
            + "<br/>\n"
            + "<h3>When did you last speak to them?</h3>\n"
            + "<br/>\n"
            + "October 2019\n"
            + "<br/>\n"
            + "<h2>Are there any other health professionals that you would like to tell us about?</h2>\n"
            + "<br/>\n"
            + "No\n",
        result.get("professionalsTable"));
    assertEquals(
        "<br/>\n"
            + "<h2>What health condition or disability do you have?</h2>\n"
            + "<h3>Name of your first condition or disability:</h3>\n"
            + "<br/>\n"
            + "Kidney Failure\n"
            + "<br/>\n"
            + "<h3>Approximate start date:</h3>\n"
            + "<br/>\n"
            + "1999-8-08\n"
            + "<br/>\n"
            + "<h3>About this condition or disability:</h3>\n"
            + "<br/>\n"
            + "description of the condition\n"
            + "<br/>\n"
            + "<h2>Do you have another condition or disability?</h2>\n"
            + "<br/>\n"
            + "Yes<br/>\n"
            + "<h2>What health condition or disability do you have?</h2>\n"
            + "<h3>Name of condition or disability:</h3>\n"
            + "<br/>\n"
            + "Diabetes\n"
            + "<br/>\n"
            + "<h3>Approximate start date:</h3>\n"
            + "<br/>\n"
            + "1988-8-08\n"
            + "<br/>\n"
            + "<h3>About this condition or disability:</h3>\n"
            + "<br/>\n"
            + "description of the diabetes\n"
            + "<br/>\n"
            + "<h2>Do you have another condition or disability?</h2>\n"
            + "<br/>\n"
            + "No",
        result.get("conditionsTable"));
  }

  @Test
  void testTransformPipFormHandlesEscapeChars() throws IOException {
    final String incomingJson =
        IOUtils.toString(
            Objects.requireNonNull(
                JsonTransformationTest.class.getResourceAsStream(
                    "/valid-test-case-escaped-chars.json")),
            StandardCharsets.UTF_8);
    pipHealthDisabilityForm = objectMapper.readValue(incomingJson, Pip2HealthDisabilityForm.class);
    assertTrue(pipHealthDisabilityForm.validate());
    Map<String, String> result = jsonTransformation.transformPipForm(pipHealthDisabilityForm);

    Supplier<String> todayString =
        () -> {
          SimpleDateFormat fmt = new SimpleDateFormat(HTMLConstants.OUTPUT_DATE_FORMAT);
          return fmt.format(new Date());
        };
    assertEquals(todayString.get(), result.get("submissionDate"));
    assertEquals("MR", result.get("title"));
    assertEquals("claimant_&quot;first_name", result.get("firstName"));
    assertEquals("claimant_last_name&amp;", result.get("lastName"));
    assertEquals("RN 00 00 64 C", result.get("nino"));
    assertEquals("08 August 1970", result.get("dateOfBirth"));
    assertEquals("Yes", result.get("foodAffected"));
    assertEquals(
        "<br/>\n"
            + "<h3>Tell us about the difficulties you have with preparing food and how you manage them</h3>\n"
            + "<br/>\n"
            + "reason not be &quot;able to preparing food",
        result.get("foodDescription"));
    assertEquals("Yes", result.get("navAffected"));
    assertEquals(
        "<br/>\n"
            + "<h3>Tell us more about the difficulties you have with planning and following journeys and how you manage them</h3>\n"
            + "<br/>\n"
            + "reason not be able to plan or navigate",
        result.get("navDescription"));
    assertEquals("Yes", result.get("otherAffected"));
    assertEquals(
        "<h3>Additional information:</h3>\n"
            + "<br/>\n"
            + "additional information to support claim\n"
            + "<br/>",
        result.get("other"));
    assertEquals(
        "<br/>\n"
            + "<h2>What health condition or disability do you have?</h2>\n"
            + "<h3>Name of your first condition or disability:</h3>\n"
            + "<br/>\n"
            + "Kidney Failure\n"
            + "<br/>\n"
            + "<h3>Approximate start date:</h3>\n"
            + "<br/>\n"
            + "1999-8-08\n"
            + "<br/>\n"
            + "<h3>About this condition or disability:</h3>\n"
            + "<br/>\n"
            + "description of the condition&lt;\n"
            + "<br/>\n"
            + "<h2>Do you have another condition or disability?</h2>\n"
            + "<br/>\n"
            + "Yes<br/>\n"
            + "<h2>What health condition or disability do you have?</h2>\n"
            + "<h3>Name of condition or disability:</h3>\n"
            + "<br/>\n"
            + "Diabetes\n"
            + "<br/>\n"
            + "<h3>Approximate start date:</h3>\n"
            + "<br/>\n"
            + "1988-8-08\n"
            + "<br/>\n"
            + "<h3>About this condition or disability:</h3>\n"
            + "<br/>\n"
            + "description of the diabetes&gt;\n"
            + "<br/>\n"
            + "<h2>Do you have another condition or disability?</h2>\n"
            + "<br/>\n"
            + "No",
        result.get("conditionsTable"));
  }
}
