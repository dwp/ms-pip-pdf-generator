package uk.gov.dwp.health.pip.pdf.generator.mappers;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.dwp.health.pip.forms.FormSpecification;
import uk.gov.dwp.health.pip.forms.viewspecifications.BooleanQuestion;
import uk.gov.dwp.health.pip.forms.viewspecifications.RadioQuestion;
import uk.gov.dwp.health.pip.forms.viewspecifications.elements.RadioOption;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.BooleanResponse;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.QuestionAnswer;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.RadioResponse;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.ShortTextResponse;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.TextAreaResponse;
import uk.gov.dwp.health.pip.pdf.generator.util.QueryUtils;

@Component
@RequiredArgsConstructor
public class QuestionTypeFactory {

  public Optional<String> getQuestionAnswerWithResponse(QuestionAnswer questionAnswer,
      FormSpecification formSpecification) {
    return switch (questionAnswer.getQuestionType()) {
      case BOOL_QUESTION -> mapToBoolean(questionAnswer, formSpecification);
      case TEXT_AREA_QUESTION -> mapToTextArea(questionAnswer);
      case SHORT_TEXT_QUESTION -> mapToShortTextQuestion(questionAnswer);
      case RADIO_QUESTION -> mapToRadioQuestion(questionAnswer, formSpecification);
      default -> Optional.empty();
    };
  }

  private Optional<String> mapToRadioQuestion(QuestionAnswer questionAnswer,
      FormSpecification formSpecification) {
    RadioResponse radioResponse = (RadioResponse) questionAnswer;
    RadioQuestion radioViewSpec = (RadioQuestion) formSpecification.getViewSpecificationByReference(
        questionAnswer.getReference());
    if (radioViewSpec != null) {
      RadioOption radioOption = QueryUtils.findOneByPredicate(radioViewSpec.getOptions(),
          option -> option.getReference().equals(radioResponse.getResponseReference()));

      return radioOption != null ? Optional.ofNullable(radioOption.getLabel()) : Optional.empty();
    }
    return Optional.empty();
  }

  private Optional<String> mapToShortTextQuestion(QuestionAnswer questionAnswer) {
    return Optional.ofNullable(((ShortTextResponse) questionAnswer).getResponse());
  }

  private Optional<String> mapToTextArea(QuestionAnswer questionAnswer) {
    return Optional.ofNullable(((TextAreaResponse) questionAnswer).getResponse());
  }

  private Optional<String> mapToBoolean(QuestionAnswer questionAnswer,
      FormSpecification formSpecification) {
    BooleanResponse booleanResponse = (BooleanResponse) questionAnswer;
    BooleanQuestion booleanViewSpec = (BooleanQuestion) formSpecification
        .getViewSpecificationByReference(questionAnswer.getReference());
    if (booleanViewSpec != null) {
      return Optional.ofNullable(booleanResponse.isResponse()
          ? booleanViewSpec.getTrueText()
          : booleanViewSpec.getFalseText());
    }
    return Optional.empty();
  }
}
