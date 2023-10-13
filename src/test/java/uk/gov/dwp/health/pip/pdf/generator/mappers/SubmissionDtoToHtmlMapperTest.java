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
    var submissionDto = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/testSubmissionDto.json").toFile(),
        SubmissionDto.class);
    var formSpec = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/form_specification_123456789.json").toFile(),
        FormSpecification.class);

    var result = mapper.writeVersionedDataToTemplate(submissionDto, formSpec, htmlTemplate);
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
