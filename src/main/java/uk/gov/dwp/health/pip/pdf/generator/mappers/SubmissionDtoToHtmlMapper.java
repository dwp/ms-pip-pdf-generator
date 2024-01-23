package uk.gov.dwp.health.pip.pdf.generator.mappers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import uk.gov.dwp.health.pip.forms.FormSpecification;
import uk.gov.dwp.health.pip.forms.ViewSpecification;
import uk.gov.dwp.health.pip.forms.viewspecifications.TaskList;
import uk.gov.dwp.health.pip.forms.viewspecifications.abstractions.SectionEndViewSpecification;
import uk.gov.dwp.health.pip.forms.viewspecifications.elements.TaskListTask;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLConstants;
import uk.gov.dwp.health.pip.pdf.generator.exception.ViewSpecificationNotFoundException;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.MultiPartResponseDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.PersonalDetailsDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.QuestionAnswerDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.QuestionAnswerSectionDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.QuestionType;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDto;
import uk.gov.dwp.health.pip.pdf.generator.util.QueryUtils;

@Component
@Slf4j
public class SubmissionDtoToHtmlMapper {

  private static final String MULTI_PART_HEADING_TEMPLATE = "<h2>${multiPartHeading}</h2>";
  private static final String MULTI_PART_QUESTION_TEMPLATE = "<p>${questionAnswer}</p>";

  private static final String SECTION_TEMPLATE = "<h2>${sectionHeading}</h2><br/>";
  private static final String QUESTION_TEMPLATE = "<h3>${questionHeading}</h3>"
      + "<p>${questionAnswer}</p>";
  private static final String LINE_BREAK = "<hr style='margin: 30px 0' />";
  private final QuestionTypeFactory questionTypeFactory = new QuestionTypeFactory();
  private StringBuilder questionSectionsString;

  public String writeVersionedDataToTemplate(
      SubmissionDto submissionDto,
      FormSpecification formSpec, String templateHtml) throws ParseException {

    questionSectionsString = new StringBuilder();
    Map<String, String> submissionTemplateMap = new HashMap<>(mapPersonalDetails(submissionDto));
    submissionTemplateMap.put(
        "questionSections",
        mapSections(formSpec, submissionDto.getResponses()));

    StringBuilder submissionTemplateString = new StringBuilder();
    addToString(submissionTemplateMap, templateHtml, submissionTemplateString);

    log.debug(submissionTemplateString.toString());
    return submissionTemplateString.toString();
  }

  private Map<String, String> mapPersonalDetails(SubmissionDto submissionDto)
      throws ParseException {

    Map<String, String> personalDetailsMap = new HashMap<>();
    PersonalDetailsDto personalDetails = submissionDto.getRegistrationDetails()
        .getPersonalDetails();
    personalDetailsMap.put("firstName", StringEscapeUtils
        .escapeXml11(personalDetails.getFirstName()));
    personalDetailsMap.put("lastName", StringEscapeUtils
        .escapeXml11(personalDetails.getSurname()));
    personalDetailsMap.put("nino", personalDetails.getNationalInsuranceNumber()
        .replaceAll("..", "$0 "));
    personalDetailsMap.put("dateOfBirth", getFormattedDate(personalDetails.getDateOfBirth()));
    personalDetailsMap.put("submissionDate", getFormattedDate(submissionDto.getSubmissionDate()));
    return personalDetailsMap;
  }

  private String getFormattedDate(final String dateIn) throws ParseException {
    String dateOut = dateIn;
    if (dateIn != null) {
      Date date = null;
      if (dateIn.matches(HTMLConstants.INPUT_DATE_REGEX_YMD)) {
        date = new SimpleDateFormat(HTMLConstants.INPUT_DATE_FORMAT_YMD).parse(dateIn);
      } else if (dateIn.matches(HTMLConstants.INPUT_DATE_REGEX_DMY)) {
        date = new SimpleDateFormat(HTMLConstants.INPUT_DATE_FORMAT_DMY).parse(dateIn);
      }
      if (date != null) {
        SimpleDateFormat dtFormat = new SimpleDateFormat(HTMLConstants.OUTPUT_DATE_FORMAT);
        dateOut = dtFormat.format(date);
      }
    }
    return dateOut;
  }

  private String mapSections(FormSpecification formSpec, List<QuestionAnswerSectionDto> responses) {
    TaskList landingViewSpec = (TaskList) formSpec.getViewSpecificationByReference("landing");

    if (landingViewSpec == null) {
      throw new ViewSpecificationNotFoundException(
          String.format(
              "Landing view specification not found in formSpec with formSpecificationId: %s",
              formSpec.getId()));
    }

    // Filter submission section as not needed on PDF
    QueryUtils.findByPredicate(landingViewSpec.getSections(),
            section -> !section.getReference().contains("submission"))
        .forEach(taskListSection -> {

          Map<String, String> sectionMap = new HashMap<>();
          sectionMap.put("sectionHeading", taskListSection.getTitle());
          addToString(sectionMap, SECTION_TEMPLATE, questionSectionsString);

          List<TaskListTask> sectionTasks = QueryUtils.findByPredicate(landingViewSpec.getTasks(),
              x -> x.getParentReference()
                  .equals(taskListSection.getReference())).toList();

          var sectionReferences = sectionTasks.stream().map(TaskListTask::getReference).toList();

          List<QuestionAnswerSectionDto> questionAnswersForSection = QueryUtils.findByPredicate(
              responses, x -> sectionReferences
                  .contains(x.getReference())).toList();

          questionAnswersForSection.forEach(
              questionAnswerSection -> mapToQuestionAnswerSection(questionAnswerSection, formSpec)
          );
          // Section separating line
          questionSectionsString.append(LINE_BREAK);
        });
    return questionSectionsString.toString();
  }

  private void mapToQuestionAnswerSection(QuestionAnswerSectionDto questionAnswerSection,
      FormSpecification formSpecification) {

    if (questionAnswerSection.getReference().equals("about-your-health-professionals")) {
      sortHealthProfessionalsSection(questionAnswerSection, formSpecification);
    }

    QueryUtils.findByPredicate(questionAnswerSection.getQuestionAnswers(),
            questionAnswer -> !questionAnswer.getQuestionType().equals(QuestionType.ADVICE))
        .forEach(questionAnswer -> {

          if (questionAnswer.getQuestionType().equals(QuestionType.MULTI_PART_QUESTION)) {
            handleMultiPartQuestion(
                questionAnswer,
                formSpecification);
          } else {
            Map<String, String> questionMap = new HashMap<>();
            Optional<String> answerWithValue = questionTypeFactory.getQuestionAnswerWithResponse(
                questionAnswer,
                formSpecification);
            if (answerWithValue.isPresent()) {
              questionMap.put("questionHeading", questionAnswer.getQuestion());
              questionMap.put("questionAnswer", StringEscapeUtils
                  .escapeXml11(answerWithValue.get()));
              addToString(questionMap, QUESTION_TEMPLATE, questionSectionsString);
            }
          }
        });
  }

  private void handleMultiPartQuestion(
      QuestionAnswerDto questionAnswer,
      FormSpecification formSpecification) {
    Map<String, String> valuesMap = new HashMap<>();

    valuesMap.put("multiPartHeading", questionAnswer.getQuestion());
    addToString(valuesMap, MULTI_PART_HEADING_TEMPLATE, questionSectionsString);

    List<QuestionAnswerDto> multiQResponses =
        ((MultiPartResponseDto) questionAnswer).getResponses();
    multiQResponses.forEach(multiQResponse -> {

      Map<String, String> newMap = new HashMap<>();
      Optional<String> answer = questionTypeFactory
          .getQuestionAnswerWithResponse(multiQResponse, formSpecification);
      if (answer.isPresent()) {
        newMap.put("questionHeading", multiQResponse.getQuestion());
        newMap.put("questionAnswer", StringEscapeUtils
            .escapeXml11(answer.get()));

        if (multiQResponse.getQuestion() == null) {
          addToString(newMap, MULTI_PART_QUESTION_TEMPLATE, questionSectionsString);
        } else {
          addToString(newMap, QUESTION_TEMPLATE, questionSectionsString);
        }
      }
    });
  }

  private void addToString(
      Map<String, String> valuesMap, String templateToMapTo,
      StringBuilder stringToAddTo) {
    StringSubstitutor stringSub = new StringSubstitutor(valuesMap);
    stringToAddTo.append(stringSub.replace(templateToMapTo));
  }

  private void sortHealthProfessionalsSection(
      QuestionAnswerSectionDto questionAnswerSection, FormSpecification formSpecification) {

    var viewSpecsForSection = getViewSpecificationsRelatedToThisSection(formSpecification,
        questionAnswerSection.getReference());

    ViewSpecification viewSpecification =
        QueryUtils.findOneByPredicate(viewSpecsForSection,
            x -> x instanceof SectionEndViewSpecification);

    if (viewSpecification instanceof SectionEndViewSpecification y) {
      var professionalsListOrder = y.getQuestionReferences();
      Map<String, Integer> order = IntStream.range(0, professionalsListOrder.size())
          .boxed()
          .collect(Collectors.toMap(
              professionalsListOrder::get,
              Function.identity()
          ));

      Comparator<QuestionAnswerDto> comparator = Comparator.comparingInt(
          obj -> order.getOrDefault(obj.getReference(), order.size()));

      questionAnswerSection.getQuestionAnswers().sort(comparator);
    }
  }

  private List<ViewSpecification> getViewSpecificationsRelatedToThisSection(
      FormSpecification formSpecification, String reference) {

    return QueryUtils.findByPredicate(formSpecification.getViews(),
            x -> x.getParentReference() != null
                && x.getParentReference().equals(reference))
        .toList();
  }
}
