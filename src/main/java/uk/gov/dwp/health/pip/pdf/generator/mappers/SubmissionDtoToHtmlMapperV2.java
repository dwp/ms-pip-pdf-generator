package uk.gov.dwp.health.pip.pdf.generator.mappers;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static org.apache.commons.text.StringEscapeUtils.escapeXml11;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLConstants;
import uk.gov.dwp.health.pip.pdf.generator.exception.InvalidFormDataException;
import uk.gov.dwp.health.pip.pdf.generator.marshaller.HealthInformationGatherFormMarshaller;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.PersonalDetailsDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;
import uk.gov.dwp.health.pip2.model.about.Address;
import uk.gov.dwp.health.pip2.model.about.Condition;
import uk.gov.dwp.health.pip2.model.about.Health;
import uk.gov.dwp.health.pip2.model.about.HealthProfessional;
import uk.gov.dwp.health.pip2.model.dailyliving.DailyLiving;
import uk.gov.dwp.health.pip2.model.dailyliving.Dressing;
import uk.gov.dwp.health.pip2.model.dailyliving.EatingDrinking;
import uk.gov.dwp.health.pip2.model.dailyliving.ManageTreatment;
import uk.gov.dwp.health.pip2.model.dailyliving.ManagingMoney;
import uk.gov.dwp.health.pip2.model.dailyliving.MixingPeople;
import uk.gov.dwp.health.pip2.model.dailyliving.PreparingFood;
import uk.gov.dwp.health.pip2.model.dailyliving.Reading;
import uk.gov.dwp.health.pip2.model.dailyliving.TalkingListening;
import uk.gov.dwp.health.pip2.model.dailyliving.ToiletIncontinence;
import uk.gov.dwp.health.pip2.model.dailyliving.WashingBathing;
import uk.gov.dwp.health.pip2.model.mobility.Mobility;
import uk.gov.dwp.health.pip2.model.mobility.MovingAround;
import uk.gov.dwp.health.pip2.model.mobility.PlanningFollowing;
import uk.gov.dwp.health.pip2.model.mobility.SeverityLevel;
import uk.gov.dwp.health.pip2.model.other.OtherInformation;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubmissionDtoToHtmlMapperV2 {

  private final TemplateEngine templateEngine;
  private final HealthInformationGatherFormMarshaller healthInformationGatherFormMarshaller;
  private static final Map<String, String> SEVERITY_MAP;

  static {
    SEVERITY_MAP = new HashMap<>();
    SEVERITY_MAP.put(
        SeverityLevel.INMOBILE.grade(), "I cannot stand and move even using my aids or appliances");
    SEVERITY_MAP.put(SeverityLevel.LESS_THAN_20M.grade(), "Less than 20 metres");
    SEVERITY_MAP.put(SeverityLevel.BET_20M_50M.grade(), "Between 20 and up to 50 metres");
    SEVERITY_MAP.put(SeverityLevel.BET_50M_200M.grade(), "Between 50 and up to 200 metres");
    SEVERITY_MAP.put(SeverityLevel.MORE_THAN_200M.grade(), "More than 200 metres");
    SEVERITY_MAP.put(SeverityLevel.VARIES.grade(), "It varies");
  }

  public String writeDataToTemplate(SubmissionDtoV3 submissionDto, String templateHtml)
      throws ParseException, JsonProcessingException {
    var healthInformationGatherForm =
        healthInformationGatherFormMarshaller.toHealthDisabilityForm(submissionDto.getFormData());
    if (!healthInformationGatherForm.validate()) {
      final String errorMsg =
          String.format(
              "Form data validation failed: %s", healthInformationGatherForm.errorsToString());
      log.error(errorMsg);
      throw new InvalidFormDataException(errorMsg);
    }
    Context context = new Context();
    populatePersonalDetails(context, submissionDto.getRegistrationDetails().getPersonalDetails());
    populateAboutYourHealth(context, healthInformationGatherForm.getAboutYourHealth());
    populateDailyLiving(context, healthInformationGatherForm.getDailyLiving());
    populateMobility(context, healthInformationGatherForm.getMobility());
    populateAdditionalDetails(context, healthInformationGatherForm.getOtherInformation());
    context.setVariable(
        "submissionDate", getFormattedDate(escapeXml11(submissionDto.getSubmissionDate())));
    String process = templateEngine.process(templateHtml, context);
    return process;
  }

  private void populateAdditionalDetails(Context context, OtherInformation otherInformation) {
    context.setVariable("otherInfoYesOrNo", otherInformation.getAdditionalInformation());
    context.setVariable("otherInfoDescription", escapeXml11(otherInformation.getDescription()));
  }

  private static void sanitiseAndSetAdditionalDetails(
      Context context, OtherInformation otherInformation) {
    context.setVariable("additionalDetails", otherInformation);
  }

  private void populateMobility(Context context, Mobility mobility) {
    sanitiseAndSetPlanningFollowing(context, mobility.getPlanningFollowing());
    sanitiseAndSetMovingAround(context, mobility.getMovingAround());
  }

  private static void sanitiseAndSetMovingAround(Context context, MovingAround movingAround) {
    context.setVariable("movingAroundYesOrNo", movingAround.getAffected());
    if (nonNull(movingAround.getSeverity()) && nonNull(movingAround.getSeverity().getGrade())) {
      if (SeverityLevel.VARIES
          .grade()
          .equalsIgnoreCase(movingAround.getSeverity().getGrade().grade())) {
        context.setVariable(
            "movingAroundGrade", SEVERITY_MAP.get(movingAround.getSeverity().getGrade().grade()));
        if (StringUtils.isNotBlank(movingAround.getSeverity().getNote())) {
          context.setVariable(
              "movingAroundNotes", escapeXml11(movingAround.getSeverity().getNote()));
        }
      } else {
        context.setVariable(
            "movingAroundGrade", SEVERITY_MAP.get(movingAround.getSeverity().getGrade().grade()));
      }
    }
    context.setVariable("movingAroundDescription", escapeXml11(movingAround.getDescription()));
  }

  private static void sanitiseAndSetPlanningFollowing(
      Context context, PlanningFollowing planningFollowing) {
    context.setVariable("planningFollowingYesOrNo", planningFollowing.getAffected());
    context.setVariable(
        "planningFollowingDescription", escapeXml11(planningFollowing.getDescription()));
  }

  private void populateDailyLiving(Context context, DailyLiving dailyLiving) {
    sanitiseAndSetPreparingFood(context, dailyLiving.getPreparingFood());
    sanitiseAndSetEatingDrinking(context, dailyLiving.getEatingDrinking());
    sanitiseAndSetManagingTreatments(context, dailyLiving.getManagingTreatments());
    sanitiseAndSetWashingBathing(context, dailyLiving.getWashingBathing());
    sanitiseAndSetToiletIncontinence(context, dailyLiving.getToiletIncontinence());
    sanitiseAndSetDressingUndressing(context, dailyLiving.getDressingUndressing());
    sanitiseAndSetTalking(context, dailyLiving.getTalkingListening());
    sanitiseAndSetReading(context, dailyLiving.getReading());
    sanitiseAndSetMixing(context, dailyLiving.getMixingPeople());
    sanitiseAndSetManagingMoney(context, dailyLiving.getManagingMoney());
  }

  private static void sanitiseAndSetManagingMoney(Context context, ManagingMoney managingMoney) {
    context.setVariable("managingMoneyYesOrNo", managingMoney.getAffected());
    context.setVariable("managingMoneyDescription", escapeXml11(managingMoney.getDescription()));
  }

  private static void sanitiseAndSetMixing(Context context, MixingPeople mixingPeople) {
    context.setVariable("mixingPeopleYesOrNo", mixingPeople.getAffected());
    context.setVariable("mixingPeopleDescription", escapeXml11(mixingPeople.getDescription()));
  }

  private static void sanitiseAndSetTalking(Context context, TalkingListening talkingListening) {
    context.setVariable("talkingYesOrNo", talkingListening.getAffected());
    context.setVariable("talkingDescription", escapeXml11(talkingListening.getDescription()));
  }

  private static void sanitiseAndSetDressingUndressing(Context context, Dressing dressing) {
    context.setVariable("dressingYesOrNo", dressing.getAffected());
    context.setVariable("dressingDescription", escapeXml11(dressing.getDescription()));
  }

  private static void sanitiseAndSetToiletIncontinence(
      Context context, ToiletIncontinence toiletIncontinence) {
    context.setVariable("toiletIncontinenceYesOrNo", toiletIncontinence.getAffected());
    context.setVariable(
        "toiletIncontinenceDescription", escapeXml11(toiletIncontinence.getDescription()));
  }

  private static void sanitiseAndSetWashingBathing(Context context, WashingBathing washingBathing) {
    context.setVariable("washingBathingYesOrNo", washingBathing.getAffected());
    context.setVariable("washingBathingDescription", escapeXml11(washingBathing.getDescription()));
  }

  private static void sanitiseAndSetManagingTreatments(
      Context context, ManageTreatment manageTreatment) {
    context.setVariable("managingTreatmentsYesOrNo", manageTreatment.getAffected());
    context.setVariable(
        "managingTreatmentsDescription", escapeXml11(manageTreatment.getDescription()));
    context.setVariable("managingTreatmentsTherapies", escapeXml11(manageTreatment.getTherapy()));
  }

  private static void sanitiseAndSetPreparingFood(Context context, PreparingFood preparingFood) {
    context.setVariable("preparingFoodYesOrNo", preparingFood.getAffected());
    context.setVariable("preparingFoodDescription", escapeXml11(preparingFood.getDescription()));
  }

  private static void sanitiseAndSetEatingDrinking(Context context, EatingDrinking eatingDrinking) {
    context.setVariable("eatingYesOrNo", eatingDrinking.getAffected());
    context.setVariable("useFeedingTube", eatingDrinking.getUseFeedingTube());
    context.setVariable("eatingDescription", escapeXml11(eatingDrinking.getDescription()));
  }

  private static void sanitiseAndSetReading(Context context, Reading reading) {
    context.setVariable("readingYesOrNo", reading.getAffected());
    context.setVariable("readingDescription", escapeXml11(reading.getDescription()));
  }

  private void populateAboutYourHealth(Context context, Health aboutYourHealth) {
    context.setVariable(
        "conditions", sanitizeAndSortHealthConditions(aboutYourHealth.getConditions()));
    context.setVariable(
        "healthProfessionalDetails", sanitizeAndSortHCP(aboutYourHealth.getHealthProfessionals()));
  }

  private List<HealthProfessional> sanitizeAndSortHCP(
      List<HealthProfessional> healthProfessionals) {
    return Optional.ofNullable(healthProfessionals).orElse(emptyList()).stream()
        .map(
            healthProfessional ->
                HealthProfessional.builder()
                    .order(healthProfessional.getOrder())
                    .name(escapeXml11(healthProfessional.getName()))
                    .profession(escapeXml11(healthProfessional.getProfession()))
                    .address(
                        Address.builder()
                            .line1(escapeXml11(healthProfessional.getAddress().getLine1()))
                            .line2(escapeXml11(healthProfessional.getAddress().getLine2()))
                            .townOrCity(
                                escapeXml11(healthProfessional.getAddress().getTownOrCity()))
                            .county(escapeXml11(healthProfessional.getAddress().getCounty()))
                            .postCode(escapeXml11(healthProfessional.getAddress().getPostCode()))
                            .build())
                    .lastContact(escapeXml11(healthProfessional.getLastContact()))
                    .phoneNumber(escapeXml11(healthProfessional.getPhoneNumber()))
                    .build())
        .sorted(comparing(HealthProfessional::getOrder))
        .toList();
  }

  private List<Condition> sanitizeAndSortHealthConditions(List<Condition> conditions) {
    return Optional.ofNullable(conditions).orElse(emptyList()).stream()
        .map(
            condition ->
                new Condition(
                    condition.getOrder(),
                    escapeXml11(condition.getHealthCondition()),
                    escapeXml11(condition.getDescription()),
                    escapeXml11(condition.getApproximateStartDate()),
                    condition.getMeta()))
        .sorted(comparing(Condition::getOrder))
        .toList();
  }

  private void populatePersonalDetails(Context context, PersonalDetailsDto personalDetails)
      throws ParseException {
    context.setVariable("firstName", escapeXml11(personalDetails.getFirstName()));
    context.setVariable("lastName", escapeXml11(personalDetails.getSurname()));
    context.setVariable(
        "nino", personalDetails.getNationalInsuranceNumber().replaceAll("..", "$0 "));
    context.setVariable("dateOfBirth", getFormattedDate(personalDetails.getDateOfBirth()));
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
}
