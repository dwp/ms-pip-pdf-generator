package uk.gov.dwp.health.pip.pdf.generator.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLConstants;
import uk.gov.dwp.health.pip.pdf.generator.constants.HTMLTemplateFiles;
import uk.gov.dwp.health.pip2.common.Pip2HealthDisabilityForm;
import uk.gov.dwp.health.pip2.common.model.about.Address;
import uk.gov.dwp.health.pip2.common.model.about.Condition;
import uk.gov.dwp.health.pip2.common.model.about.Details;
import uk.gov.dwp.health.pip2.common.model.about.HealthProfessional;
import uk.gov.dwp.health.pip2.common.model.dla.DailyLiving;
import uk.gov.dwp.health.pip2.common.model.mobility.Mobility;
import uk.gov.dwp.health.pip2.common.model.mobility.SeverityLevel;
import uk.gov.dwp.health.pip2.common.model.other.OtherInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonTransformation {

  private static final Logger LOG = LoggerFactory.getLogger(JsonTransformation.class.getName());

  private static final Map<String, String> SEVERITY_MAP;
  private static final String BLANK = "";

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

  public Map<String, String> transformPipForm(Pip2HealthDisabilityForm pip2HtmlForm)
      throws IOException {
    Map<String, String> valuesMap = new HashMap<>();
    transformPersonalDetail(valuesMap, pip2HtmlForm.getDetails());
    valuesMap.put(
        HTMLConstants.SUBMISSION_DATE, getFormattedDate(pip2HtmlForm.getSubmissionDate()));
    valuesMap.put(
        HTMLConstants.CONDITIONS_TABLE,
        transformConditionsData(pip2HtmlForm.getHealth().getConditions()));
    if (pip2HtmlForm.getHealth().getHealthProfessionals() == null
        || pip2HtmlForm.getHealth().getHealthProfessionals().isEmpty()) {
      valuesMap.put(HTMLConstants.YESORNO, HTMLConstants.NO);
      valuesMap.put(HTMLConstants.PROFESSIONAL_TABLE, BLANK);
    } else {
      valuesMap.put(HTMLConstants.YESORNO, HTMLConstants.YES);
      valuesMap.put(
          HTMLConstants.PROFESSIONAL_TABLE,
          transformProfessionalData(pip2HtmlForm.getHealth().getHealthProfessionals()));
    }
    transformDailyActivityDetails(valuesMap, pip2HtmlForm.getDailyLiving());
    transformMobilityDetails(valuesMap, pip2HtmlForm.getMobility());
    if (pip2HtmlForm.getOtherInformation() != null
        && pip2HtmlForm.getOtherInformation().isAffected()) {
      valuesMap.put(HTMLConstants.OTHER_AFFECTED, HTMLConstants.YES);
      valuesMap.put(HTMLConstants.OTHER, transformOtherDetails(pip2HtmlForm.getOtherInformation()));
    } else {
      valuesMap.put(HTMLConstants.OTHER_AFFECTED, HTMLConstants.NO);
      valuesMap.put(HTMLConstants.OTHER, BLANK);
    }
    return valuesMap;
  }

  private void transformPersonalDetail(Map<String, String> valuesMap, Details details) {
    valuesMap.put(HTMLConstants.TITLE, StringEscapeUtils.escapeXml11(details.getTitle()));
    valuesMap.put(HTMLConstants.FIRST_NAME, StringEscapeUtils.escapeXml11(details.getForename()));
    valuesMap.put(HTMLConstants.LAST_NAME, StringEscapeUtils.escapeXml11(details.getSurname()));
    valuesMap.put(HTMLConstants.NINO, details.getNino().replaceAll("..", "$0 "));
    valuesMap.put(HTMLConstants.DOB, getFormattedDate(details.getDob()));
    LOG.info("Personal details transformed successfully");
  }

  private String getFormattedDate(Date date) {
    SimpleDateFormat dtFormat = new SimpleDateFormat(HTMLConstants.INPUT_DATE_FORMAT);
    return dtFormat.format(date);
  }

  private String transformConditionsData(List<Condition> conditions) throws IOException {
    String conditionsTemplateHtml =
        IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResourceAsStream(HTMLTemplateFiles.PIP2_CONDITIONS_TEMPLATE)),
            StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder();
    StringSubstitutor substitutor;
    for (int i = 0, size = conditions.size(); i < size; i++) {
      Map<String, String> valuesMap = new HashMap<>();
      Condition condition = conditions.get(i);
      valuesMap.put(HTMLConstants.FIRST_CONDITION, (i == 0) ? " your first " : " ");
      valuesMap.put(
          HTMLConstants.HEALTH_CONDITION,
          StringEscapeUtils.escapeXml11(condition.getHealthCondition()));
      valuesMap.put(
          HTMLConstants.START_DATE, StringEscapeUtils.escapeXml11(condition.getApproxStartDate()));
      valuesMap.put(
          HTMLConstants.CONDITION_DESC, StringEscapeUtils.escapeXml11(condition.getDescription()));
      valuesMap.put(
          HTMLConstants.MORE_CONDITIONS,
          i < (conditions.size() - 1) ? HTMLConstants.YES : HTMLConstants.NO);
      substitutor = new StringSubstitutor(valuesMap);
      sb.append(substitutor.replace(conditionsTemplateHtml));
      LOG.info("Conditions details transformed successfully");
    }
    return sb.toString();
  }

  private String transformProfessionalData(List<HealthProfessional> professionals)
      throws IOException {
    String professionalsTemplateHtml =
        IOUtils.toString(
            Objects.requireNonNull(
                JsonTransformation.class.getResourceAsStream(
                    HTMLTemplateFiles.PIP2_PROFESSIONALS_TEMPLATE)),
            StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder();
    StringSubstitutor substitutor;
    for (int i = 0, size = professionals.size(); i < size; i++) {
      var valuesMap = new HashMap<String, String>();
      HealthProfessional professional = professionals.get(i);
      valuesMap.put(
          HTMLConstants.FULL_NAME, StringEscapeUtils.escapeXml11(professional.getFullName()));
      valuesMap.put(
          HTMLConstants.PROFESSION, StringEscapeUtils.escapeXml11(professional.getProfession()));
      valuesMap.put(HTMLConstants.PHONE, StringEscapeUtils.escapeXml11(professional.getPhone()));
      StringBuilder stringBuilder = new StringBuilder();
      createAddressLabel(stringBuilder, professional.getAddress());
      valuesMap.put(
          HTMLConstants.ADDRESS,
          String.join(System.lineSeparator(), stringBuilder.toString())
              + "<br>" + professional.getAddress().getPostcode() + "</br>");
      valuesMap.put(
          HTMLConstants.LAST_SEEN, StringEscapeUtils.escapeXml11(professional.getLastSeen()));
      valuesMap.put(
          HTMLConstants.MORE_PROFESSIONALS,
          i < (professionals.size() - 1) ? HTMLConstants.YES : HTMLConstants.NO);

      substitutor = new StringSubstitutor(valuesMap);
      sb.append(substitutor.replace(professionalsTemplateHtml));
      LOG.info("Professionals details transformed successfully");
    }
    return sb.toString();
  }

  private void createAddressLabel(StringBuilder stringBuilder, Address address) {
    if (address.getLine1() != null && !address.getLine1().isBlank()) {
      stringBuilder
          .append("<br>")
          .append(StringEscapeUtils.escapeXml11(address.getLine1()))
          .append("</br>\n");
    }
    if (address.getLine2() != null && !address.getLine2().isBlank()) {
      stringBuilder
          .append("<br>")
          .append(StringEscapeUtils.escapeXml11(address.getLine2()))
          .append("</br>\n");
    }
    if (address.getLine3() != null && !address.getLine3().isBlank()) {
      stringBuilder
          .append("<br>")
          .append(StringEscapeUtils.escapeXml11(address.getLine3()))
          .append("</br>\n");
    }
    if (address.getTownOrCity() != null && !address.getTownOrCity().isBlank()) {
      stringBuilder
          .append("<br>")
          .append(StringEscapeUtils.escapeXml11(address.getTownOrCity()))
          .append("</br>\n");
    }
    if (address.getCounty() != null && !address.getCounty().isBlank()) {
      stringBuilder
          .append("<br>")
          .append(StringEscapeUtils.escapeXml11(address.getCounty()))
          .append("</br>\n");
    }
    if (address.getCountry() != null && !address.getCountry().isBlank()) {
      stringBuilder
          .append("<br>")
          .append(StringEscapeUtils.escapeXml11(address.getCountry()))
          .append("</br>\n");
    }
  }

  @SuppressWarnings("java:S3776")
  private void transformDailyActivityDetails(
      Map<String, String> valuesMap, DailyLiving dailyLiving) {
    valuesMap.put(
        HTMLConstants.FOOD_AFFECTED,
        dailyLiving.getPreparingFood().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getPreparingFood().isAffected()) {
      valuesMap.put(
          HTMLConstants.FOOD_DESC,
          String.format(
              HTMLConstants.FOOD_AFFECTED_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getPreparingFood().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.FOOD_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.EATING_AFFECTED,
        dailyLiving.getEatingDrinking().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getEatingDrinking().isAffected()) {
      valuesMap.put(
          HTMLConstants.EATING_DESC,
          String.format(
              HTMLConstants.EATING_AFFECTED_HTML,
              dailyLiving.getEatingDrinking().getFeedTube(),
              StringEscapeUtils.escapeXml11(dailyLiving.getEatingDrinking().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.EATING_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.TREATMENT_AFFECTED,
        dailyLiving.getManageTreatment().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getManageTreatment().isAffected()) {
      valuesMap.put(
          HTMLConstants.TREATMENT_DESC,
          String.format(
              HTMLConstants.TREATMENT_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getManageTreatment().getDescription()),
              StringEscapeUtils.escapeXml11(dailyLiving.getManageTreatment().getTherapy())));
    } else {
      valuesMap.put(HTMLConstants.TREATMENT_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.WASHING_AFFECTED,
        dailyLiving.getWashingBathing().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getWashingBathing().isAffected()) {
      valuesMap.put(
          HTMLConstants.WASHING_DESC,
          String.format(
              HTMLConstants.WASHING_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getWashingBathing().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.WASHING_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.TOILET_AFFECTED,
        dailyLiving.getToiletIncontinence().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getToiletIncontinence().isAffected()) {
      valuesMap.put(
          HTMLConstants.TOILET_DESC,
          String.format(
              HTMLConstants.TOILET_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getToiletIncontinence().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.TOILET_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.DRESSING_AFFECTED,
        dailyLiving.getDressing().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getDressing().isAffected()) {
      valuesMap.put(
          HTMLConstants.DRESSING_DESC,
          String.format(
              HTMLConstants.DRESSING_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getDressing().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.DRESSING_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.COMM_AFFECTED,
        dailyLiving.getCognitive().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getCognitive().isAffected()) {
      valuesMap.put(
          HTMLConstants.COMM_DESC,
          String.format(
              HTMLConstants.COMM_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getCognitive().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.COMM_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.READING_AFFECTED,
        dailyLiving.getReading().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getReading().isAffected()) {
      valuesMap.put(
          HTMLConstants.READING_DESC,
          String.format(
              HTMLConstants.READING_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getReading().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.READING_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.SOCIAL_AFFECTED,
        dailyLiving.getSocial().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getSocial().isAffected()) {
      valuesMap.put(
          HTMLConstants.SOCIAL_DESC,
          String.format(
              HTMLConstants.SOCIAL_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getSocial().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.SOCIAL_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.MONEY_AFFECTED,
        dailyLiving.getManageFinance().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (dailyLiving.getManageFinance().isAffected()) {
      valuesMap.put(
          HTMLConstants.MONEY_DESC,
          String.format(
              HTMLConstants.MONEY_HTML,
              StringEscapeUtils.escapeXml11(dailyLiving.getManageFinance().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.MONEY_DESC, BLANK);
    }
    LOG.info("Daily Activities details transformed successfully");
  }

  private void transformMobilityDetails(Map<String, String> valuesMap, Mobility mobility) {

    valuesMap.put(
        HTMLConstants.NAV_AFFECTED,
        mobility.getPlanNavigate().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (mobility.getPlanNavigate().isAffected()) {
      valuesMap.put(
          HTMLConstants.NAV_DESC,
          String.format(
              HTMLConstants.NAV_HTML,
              StringEscapeUtils.escapeXml11(mobility.getPlanNavigate().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.NAV_DESC, BLANK);
    }
    valuesMap.put(
        HTMLConstants.MOVING_AFFECTED,
        mobility.getMovingRound().isAffected() ? HTMLConstants.YES : HTMLConstants.NO);
    if (mobility.getMovingRound().isAffected()) {
      valuesMap.put(
          HTMLConstants.MOVING_DESC,
          String.format(
              HTMLConstants.MOVING_HTML,
              SEVERITY_MAP.get(mobility.getMovingRound().getSeverity().getGrade().grade()),
              mobility.getMovingRound().getSeverity().getGrade() == SeverityLevel.VARIES
                  ? String.format(
                      HTMLConstants.SEVERITY_HTML,
                      StringEscapeUtils.escapeXml11(
                          mobility.getMovingRound().getSeverity().getNote()))
                  : "",
              StringEscapeUtils.escapeXml11(mobility.getMovingRound().getDescription())));
    } else {
      valuesMap.put(HTMLConstants.MOVING_DESC, BLANK);
    }
    LOG.info("Mobility details transformed successfully");
  }

  private String transformOtherDetails(OtherInformation other) throws IOException {
    String otherTemplateHtml =
        IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResourceAsStream(HTMLTemplateFiles.PIP2_OTHER_TEMPLATE)),
            StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder();
    StringSubstitutor substitutor;
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put(HTMLConstants.OTHER_DESC, StringEscapeUtils.escapeXml11(other.getDescription()));
    substitutor = new StringSubstitutor(valuesMap);
    sb.append(substitutor.replace(otherTemplateHtml));
    LOG.info("Other details transformed successfully");
    return sb.toString();
  }
}
