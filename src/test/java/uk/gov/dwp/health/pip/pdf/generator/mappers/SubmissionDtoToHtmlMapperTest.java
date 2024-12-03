package uk.gov.dwp.health.pip.pdf.generator.mappers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.health.pip.forms.FormSpecification;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLTemplateFiles;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDto;

class SubmissionDtoToHtmlMapperTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String htmlTemplate = IOUtils.toString(
      Objects.requireNonNull(
          getClass().getResourceAsStream(HTMLTemplateFiles.PIP2_VERSIONED_TEMPLATE)),
      UTF_8);
  private SubmissionDtoToHtmlMapper mapper;

  SubmissionDtoToHtmlMapperTest() throws IOException {
  }

  @BeforeEach
  void beforeEach() {
    mapper = new SubmissionDtoToHtmlMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void when_called_with_correct_args_map_to_template() throws IOException, ParseException {
    final SubmissionDto submissionDto = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/testSubmissionDto.json").toFile(),
        SubmissionDto.class);
    final FormSpecification formSpec = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/form_specification_123456789.json").toFile(),
        FormSpecification.class);

    for (int i = 0; i<10; i++) {
      new Thread(() -> {
        try {
          final SubmissionDto submissionDto2 = objectMapper.readValue(
              Paths.get("src/test/resources/v2TestData/testSubmissionDto.json").toFile(),
              SubmissionDto.class);
          mapper.writeVersionedDataToTemplate(submissionDto2, formSpec, htmlTemplate);
        } catch (Exception e) {
          System.err.println(e);
        }
      }).start();
    }
    final String result = mapper.writeVersionedDataToTemplate(submissionDto, formSpec, htmlTemplate);
    assertThat(result).contains(
        "Tell us how your health condition or disability affects you (PIP 2)");
    assertThat(result).contains("<h2>What health condition or disability do you have?</h2>");
    assertThat(result).contains("<h2>Health or care professional details</h2>");
    assertThat(result).contains("<h3>Postcode</h3><p>NE6 5DX</p>");
    assertThat(result).contains(
        "<h3>Does your condition affect you preparing food, or prevent you from doing so?</h3><p>Yes</p><h3>Tell us about the difficulties you have with preparing food and how you manage them</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p>");
    assertThat(result).contains("1 April 2023").as("Expecting submission date to be formatted for accessibility");
    assertThat(result).contains("1 January 2000").as("Expecting birth date to be formatted for accessibility");
    assertThat(result).isInstanceOf(String.class);
    assertThat(result).doesNotContain("${sectionHeading}");
    assertThat(result).doesNotContain("${questionHeading}");
    assertThat(result).doesNotContain("${questionAnswer}");
    final String expected = "<html>\n" +
        "<head>\n" +
        "  <style>\n" +
        "    body {\n" +
        "      font-family: 'arial', serif;\n" +
        "    }\n" +
        "  </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "<h1>Tell us how your health condition or disability affects you (PIP 2)</h1>\n" +
        "<br/><br/>\n" +
        "<h2>First Name</h2>\n" +
        "TestFirst\n" +
        "<br/>\n" +
        "<h2>Last Name</h2>\n" +
        "TestSurname\n" +
        "<br/>\n" +
        "<h2>National Insurance number</h2>\n" +
        "SC 00 02 39 A\n" +
        "<br/>\n" +
        "<h2>Date of birth</h2>\n" +
        "01 January 2000\n" +
        "<hr style='margin: 30px 0'/>\n" +
        "<h2>Tell us about your health</h2><br/><h2>What health condition or disability do you have?</h2><h3>Name of condition or disability</h3><p>C1</p><h3>Approximate start date</h3><p>12 months ago</p><h3>About this condition or disability</h3><p>This is the details about C1</p><h2>What health condition or disability do you have?</h2><h3>Name of condition or disability</h3><p>C2</p><h3>Approximate start date</h3><p>12 months ago</p><h3>About this condition or disability</h3><p>This is the details about C2</p><h3>Do you have another condition or disability?</h3><p>No</p><h3>Are there any health or care professionals that you would like to tell us about?</h3><p>Yes</p><h2>Health or care professional details</h2><h3>Their name</h3><p>Jim</p><h3>Profession</h3><p>GP</p><h3>Phone number, including dialling code</h3><p>07808555876</p><h3>Building and street</h3><p>123</p><p>Testing Street</p><h3>Town or city</h3><p>Newcastle</p><h3>Postcode</h3><p>NE6 5DX</p><h3>When did you last speak to them?</h3><p>October 2019</p><h2>Health or care professional details</h2><h3>Their name</h3><p>Jim</p><h3>Profession</h3><p>GP</p><h3>Phone number, including dialling code</h3><p>07808555876</p><h3>Building and street</h3><p>123</p><p>Testing Street</p><h3>Town or city</h3><p>Newcastle</p><h3>Postcode</h3><p>NE6 5DX</p><h3>When did you last speak to them?</h3><p>October 2019</p><h3>Are there any other health or care professionals that you would like to tell us about?</h3><p>No</p><hr style='margin: 30px 0' /><h2>Tell us about your daily living activities</h2><br/><h3>Does your condition affect you preparing food, or prevent you from doing so?</h3><p>Yes</p><h3>Tell us about the difficulties you have with preparing food and how you manage them</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p><h3>Does your condition affect you eating and drinking?</h3><p>Yes</p><h3>Do you use a feeding tube or similar device to eat or drink?</h3><p>Yes</p><h3>Tell us about the difficulties you have with eating and drinking and how you manage them</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p><h3>Does your condition affect you managing your treatments?</h3><p>No</p><h3>Does your condition affect you washing and bathing?</h3><p>No</p><h3>Does your condition affect you using the toilet or managing incontinence?</h3><p>Yes</p><h3>Tell us about the difficulties you have using the toilet and how you manage them</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p><h3>Does your condition affect you dressing or undressing?</h3><p>No</p><h3>Does your condition affect you talking, listening and understanding?</h3><p>No</p><h3>Does your condition affect your ability to read?</h3><p>No</p><h3>Does your condition affect you mixing with other people?</h3><p>No</p><h3>Does your condition affect you managing your money?</h3><p>No</p><hr style='margin: 30px 0' /><h2>Tell us about your mobility activities</h2><br/><h3>Does your condition affect you planning and following journeys?</h3><p>No</p><h3>Does your condition affect you moving around?</h3><p>Yes</p><h3>Distance you can walk using any aids or appliances you need</h3><p>It varies</p><h3>Why does the distance you can walk vary?</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p><h3>Tell us more about the difficulties you have with moving around and how you manage them</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p><hr style='margin: 30px 0' /><h2>Tell us about anything else</h2><br/><h3>Is there anything else you would like to tell us about?</h3><p>Yes</p><h3>Additional information</h3><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Felis donec et odio pellentesque diam volutpat commodo sed egestas. Eget velit aliquet sagittis id consectetur purus ut faucibus.</p><hr style='margin: 30px 0' />\n" +
        "<h2>Declaration:</h2>\n" +
        "<br/>\n" +
        "Agreed\n" +
        "<br/>\n" +
        "<h3>End of questionnaire</h3>\n" +
        "<br/>\n" +
        "<h3>Submitted: 01 April 2023</h3>\n" +
        "</body>\n" +
        "</html>\n";
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void when_called_with_invalid_dates() throws IOException, ParseException {
    var submissionDto = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/testSubmissionDto_invalid_dates.json").toFile(),
        SubmissionDto.class);
    var formSpec = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/form_specification_123456789.json").toFile(),
        FormSpecification.class);

    String result = mapper.writeVersionedDataToTemplate(submissionDto, formSpec, htmlTemplate);
    assertThat(result).contains("Not a valid date").as("Expecting invalid submission date to be left as is");
    assertThat(result).contains("Invalid date").as("Expecting invalid birth date to be left as is");
  }

}
