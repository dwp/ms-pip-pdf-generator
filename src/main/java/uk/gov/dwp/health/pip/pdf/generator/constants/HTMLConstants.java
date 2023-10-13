package uk.gov.dwp.health.pip.pdf.generator.constants;

public class HTMLConstants {

  public static final String YES = "Yes";
  public static final String NO = "No";
  public static final String CONDITIONS_TABLE = "conditionsTable";
  public static final String PROFESSIONAL_TABLE = "professionalsTable";
  public static final String INPUT_DATE_FORMAT_YMD = "yyyy-MM-dd";
  public static final String INPUT_DATE_FORMAT_DMY = "dd-MM-yyyy";
  public static final String INPUT_DATE_REGEX_YMD = "\\d{4}-\\d{2}-\\d{2}";
  public static final String INPUT_DATE_REGEX_DMY = "\\d{2}-\\d{2}-\\d{4}";
  public static final String OUTPUT_DATE_FORMAT = "dd MMMMM yyyy";
  public static final String SUBMISSION_DATE = "submissionDate";
  public static final String TITLE = "title";
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String DOB = "dateOfBirth";
  public static final String NINO = "nino";
  public static final String OTHER_AFFECTED = "otherAffected";
  public static final String OTHER_DESC = "otherDescription";
  public static final String FOOD_AFFECTED = "foodAffected";
  public static final String FOOD_DESC = "foodDescription";
  public static final String EATING_AFFECTED = "eatingAffected";
  public static final String EATING_DESC = "eatingDescription";
  public static final String TREATMENT_AFFECTED = "treatmentAffected";
  public static final String TREATMENT_DESC = "treatmentDescription";
  public static final String WASHING_AFFECTED = "washingAffected";
  public static final String WASHING_DESC = "washingDescription";
  public static final String TOILET_AFFECTED = "toiletAffected";
  public static final String TOILET_DESC = "toiletDescription";
  public static final String DRESSING_AFFECTED = "dressingAffected";
  public static final String DRESSING_DESC = "dressingDescription";
  public static final String COMM_AFFECTED = "commAffected";
  public static final String COMM_DESC = "commDescription";
  public static final String READING_AFFECTED = "readingAffected";
  public static final String READING_DESC = "readingDescription";
  public static final String SOCIAL_AFFECTED = "socialAffected";
  public static final String SOCIAL_DESC = "socialDescription";
  public static final String MONEY_AFFECTED = "moneyAffected";
  public static final String MONEY_DESC = "moneyDescription";
  public static final String HEALTH_CONDITION = "healthCondition";
  public static final String CONDITION_DESC = "conditionDescription";
  public static final String MORE_CONDITIONS = "moreConditions";
  public static final String START_DATE = "startDate";
  public static final String FULL_NAME = "fullName";
  public static final String PROFESSION = "profession";
  public static final String PHONE = "phone";
  public static final String ADDRESS = "address";
  public static final String LAST_SEEN = "lastSeen";
  public static final String NAV_AFFECTED = "navAffected";
  public static final String NAV_DESC = "navDescription";
  public static final String MOVING_AFFECTED = "movingAffected";
  public static final String MOVING_DESC = "movingDescription";
  public static final String FIRST_CONDITION = "yourFirst";
  public static final String YESORNO = "yesOrNo";
  public static final String MORE_PROFESSIONALS = "moreProfessionals";
  public static final String OTHER = "other";
  @SuppressWarnings("java:S1192")
  public static final String FOOD_AFFECTED_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with preparing food and how you manage them"
      + "</h3>\n"
      + "<br/>\n"
      + "%s";
  @SuppressWarnings("java:S1192")
  public static final String EATING_AFFECTED_HTML =
      "<h3>Do you use a feeding tube or similar device to eat or drink?</h3>\n"
          + "<br/>\n"
          + "%s\n"
          + "<br/><br/>\n"
          + "<h3>Tell us about the difficulties you have with eating and drinking and how you"
          + " manage them</h3>\n"
          + "<br/>\n\n"
          + "%s";
  public static final String TREATMENT_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with monitoring changes in your health "
      + "condition or disability and taking medication, and how you manage them</h3>\n"
      + "<br/>\n"
      + "%s"
      + "<br/>\n"
      + "<h3>Tell us about any therapies you take at home that need the help of another person"
      + "</h3>\n"
      + "<br/>\n"
      + "%s";
  @SuppressWarnings("java:S1192")
  public static final String WASHING_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with washing and bathing and how you manage"
      + " them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String TOILET_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with going to the toilet and how you manage"
      + " them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String DRESSING_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with dressing and undressing and how you"
      + " manage them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String COMM_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with talking, listening and understanding"
      + " and how you manage them</h3>\n"
      + "%s";
  public static final String READING_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with reading words or symbols and how you"
      + " manage them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String SOCIAL_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with mixing with other people and how you"
      + " manage them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String MONEY_HTML = "<br/>\n"
      + "<h3>Tell us about the difficulties you have with managing your money and how you manage"
      + " them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String NAV_HTML = "<br/>\n"
      + "<h3>Tell us more about the difficulties you have with planning and following journeys"
      + " and how you manage them</h3>\n"
      + "<br/>\n"
      + "%s";
  public static final String MOVING_HTML = "<br/>\n"
      + "<h3>How far can you walk using any aids or appliances you need?</h3>\n"
      + "<br/>\n"
      + "%s\n"
      + "<br/>\n"
      + "%s\n"
      + "<h3>Tell us more about the difficulties you have with moving around and how you manage"
      + " them</h3>\n"
      + "<br/>\n"
      + "%s";

  public static final String SEVERITY_HTML = "<h3>Why does the distance you can walk vary?</h3>\n"
                                             + "<br/>\n"
                                             + "%s";

  private HTMLConstants() {
  }
}
