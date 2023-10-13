package uk.gov.dwp.health.pip.pdf.generator.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.health.pip.forms.FormSpecification;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.BooleanResponseDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.QuestionType;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.RadioResponseDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.ShortTextResponseDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.TextAreaResponseDto;

class QuestionTypeFactoryTest {

  private FormSpecification formSpecification;
  private QuestionTypeFactory factory;

  @BeforeEach
  void beforeEach() throws IOException {
    factory = new QuestionTypeFactory();
    ObjectMapper objectMapper = new ObjectMapper();
    formSpecification = objectMapper.readValue(
        Paths.get("src/test/resources/v2TestData/form_specification_123456789.json").toFile(),
        FormSpecification.class);
  }

  @Nested
  class when_mapping_to_radio_question {

    @Test
    void and_view_spec_is_found_return_correct_response_text() {
      RadioResponseDto radioResponse = new RadioResponseDto("cannot-stand",
          QuestionType.RADIO_QUESTION);
      radioResponse.setReference("moving-around-info");

      var result = factory.getQuestionAnswerWithResponse(radioResponse, formSpecification);

      assertThat(result.get()).isEqualTo(
          "I cannot stand and move even using my aids or appliances");
    }

    @Test
    void and_view_spec_is_not_found_return_optional_empty() {
      RadioResponseDto radioResponse = new RadioResponseDto("cannot-stand",
          QuestionType.RADIO_QUESTION);
      radioResponse.setReference("NOT_FOUND");

      var result = factory.getQuestionAnswerWithResponse(radioResponse, formSpecification);

      assertThat(result).isEmpty();
    }

    @Test
    void and_radio_option_not_found_return_optional_empty() {
      RadioResponseDto radioResponse = new RadioResponseDto("TEST_OPTION",
          QuestionType.RADIO_QUESTION);
      radioResponse.setReference("moving-around-info");

      var result = factory.getQuestionAnswerWithResponse(radioResponse, formSpecification);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class when_mapping_to_boolean {

    @Test
    void and_view_spec_is_found_return_correct_true_text() {
      BooleanResponseDto booleanResponse = new BooleanResponseDto(true, QuestionType.BOOL_QUESTION);
      booleanResponse.setReference("health-professionals-question");
      var result = factory.getQuestionAnswerWithResponse(booleanResponse, formSpecification);
      assertThat(result.get()).isEqualTo("Yes");
    }

    @Test
    void and_view_spec_is_found_return_correct_false_text() {
      BooleanResponseDto booleanResponse = new BooleanResponseDto(false,
          QuestionType.BOOL_QUESTION);
      booleanResponse.setReference("health-professionals-question");
      var result = factory.getQuestionAnswerWithResponse(booleanResponse, formSpecification);
      assertThat(result.get()).isEqualTo("No");
    }

    @Test
    void and_view_spec_not_found_return_optional_empty() {
      BooleanResponseDto booleanResponse = new BooleanResponseDto(false,
          QuestionType.BOOL_QUESTION);
      booleanResponse.setReference("TEST_BOOL");
      var result = factory.getQuestionAnswerWithResponse(booleanResponse, formSpecification);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class when_mapping_to_short_text_question {

    @Test
    void return_response_from_short_text() {
      String response = "TEST_RESPONSE";
      ShortTextResponseDto shortTextResponse = new ShortTextResponseDto(
          response,
          QuestionType.SHORT_TEXT_QUESTION);

      var result = factory.getQuestionAnswerWithResponse(shortTextResponse, formSpecification);
      assertThat(result.get()).isEqualTo(response);
    }
  }

  @Nested
  class when_mapping_to_text_area_question {

    @Test
    void return_response_from_text_area() {
      String response = "TEST_RESPONSE";
      TextAreaResponseDto textAreaResponse = new TextAreaResponseDto(
          response,
          QuestionType.TEXT_AREA_QUESTION);

      var result = factory.getQuestionAnswerWithResponse(textAreaResponse, formSpecification);
      assertThat(result.get()).isEqualTo(response);
    }
  }
}
