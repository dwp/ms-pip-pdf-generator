package uk.gov.dwp.health.pip.pdf.generator.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.dwp.health.pip.pdf.generator.exception.InvalidFormDataException;
import uk.gov.dwp.health.pip.pdf.generator.marshaller.HealthInformationGatherFormMarshaller;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.PersonalDetailsDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.RegistrationDetailsDto;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.SubmissionDtoV3;
import uk.gov.dwp.health.pip2.model.HealthInformationGatherForm;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class SubmissionDtoToHtmlMapperV2Test {

  @Autowired private SubmissionDtoToHtmlMapperV2 submissionDtoToHtmlMapperV2;
  @Autowired private HealthInformationGatherFormMarshaller marshaller;

  static Stream<Arguments> argumentsSupplier() {
    return Stream.of(
        Arguments.of(
            "hig-form-data/hig-final-json-full.json", "hig-form-data-html/hig-form-data-full.html"),
        Arguments.of(
            "hig-form-data/hig-final-json-additional-information.json",
            "hig-form-data-html/hig-form-data-additional-information.html"),
        Arguments.of(
            "hig-form-data/hig-final-json-prep-food-no.json",
            "hig-form-data-html/hig-form-data-prep-food-no.html"),
        Arguments.of(
            "hig-form-data/hig-final-json-daily-living-mobility-all-no.json",
            "hig-form-data-html/hig-final-json-daily-living-mobility-all-no.html"),
        Arguments.of(
            "hig-form-data/hig-final-json-mobility-varying-distance.json",
            "hig-form-data-html/hig-form-data-mobility-varying-distance.html"),
        Arguments.of(
            "hig-form-data/hig-final-json-county.json",
            "hig-form-data-html/hig-form-data-county.html"),
            Arguments.of(
                    "hig-form-data/hig-final-json-hcp-address-line-2.json",
                    "hig-form-data-html/hig-form-data-hcp-address-line-2.html"));
  }

  @ParameterizedTest(name = "Test input form json {0} generates html {1}")
  @MethodSource("argumentsSupplier")
  void test_PDF_HTML_Template_Generated(String inputHigFormObject, String expectedOutputHtml)
      throws IOException, ParseException {
    String higJson = readFromFile(inputHigFormObject);
    HealthInformationGatherForm form = marshaller.toHealthDisabilityForm(higJson);
    SubmissionDtoV3 submissionDto = getSubmissionDtoV3(form);
    String expectedHtml = readFromFile(expectedOutputHtml);

    String result =
        submissionDtoToHtmlMapperV2.writeDataToTemplate(submissionDto, "HIG-Form-Template");

    assertHtmlEquals(expectedHtml, result);
  }

  @Test
  void test_html_generator_raises_InvalidFormDataException_when_form_is_invalid()
      throws IOException {
    String higJson = readFromFile("hig-form-data/hig-final-json-invalid-form-data.json");
    HealthInformationGatherForm form = marshaller.toHealthDisabilityForm(higJson);
    SubmissionDtoV3 submissionDto = getSubmissionDtoV3(form);

    assertThrows(
        InvalidFormDataException.class,
        () -> submissionDtoToHtmlMapperV2.writeDataToTemplate(submissionDto, "HIG-Form-Template"));
  }



  @Test
  void when_called_with_correct_args_map_to_template()
      throws IOException, InterruptedException, ExecutionException {
    int executionCount = 100;
    int nThreads = 5;
    String higJson = readFromFile("hig-form-data/hig-final-json-full.json");
    HealthInformationGatherForm form = marshaller.toHealthDisabilityForm(higJson);
    SubmissionDtoV3 submissionDto = getSubmissionDtoV3(form);
    String expectedHtml = readFromFile("hig-form-data-html/hig-form-data-full.html");
    ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    List<Callable<String>> tasks = new ArrayList<>();

    for (int i = 0; i < executionCount; i++) {
      tasks.add(
          () ->
              submissionDtoToHtmlMapperV2.writeDataToTemplate(submissionDto, "HIG-Form-Template"));
    }

    List<Future<String>> futures = executor.invokeAll(tasks);
    for (int i = 0; i < futures.size(); i++) {
      assertHtmlEquals(expectedHtml, futures.get(i).get());
    }
    executor.shutdown();
  }

  @Test
  void when_called_with_invalid_dates() throws IOException, ParseException {
    String higJson = readFromFile("hig-form-data/hig-final-json-full.json");
    HealthInformationGatherForm form = marshaller.toHealthDisabilityForm(higJson);
    SubmissionDtoV3 submissionDto = getSubmissionDtoV3(form);
    submissionDto.setSubmissionDate("Invalid submission date");
    submissionDto
        .getRegistrationDetails()
        .getPersonalDetails()
        .setDateOfBirth("Invalid date of birth");
    String expectedHtml = readFromFile("hig-form-data-html/hig-form-data-invalid-dates.html");

    String result =
        submissionDtoToHtmlMapperV2.writeDataToTemplate(submissionDto, "HIG-Form-Template");
    assertHtmlEquals(expectedHtml, result);
  }

  private static SubmissionDtoV3 getSubmissionDtoV3(HealthInformationGatherForm form) {
    SubmissionDtoV3 submissionDto = new SubmissionDtoV3();
    PersonalDetailsDto personalDetailsDto = new PersonalDetailsDto();
    personalDetailsDto.setFirstName("TestFirst");
    personalDetailsDto.setSurname("TestSurname");
    personalDetailsDto.setNationalInsuranceNumber("SC000239A");
    personalDetailsDto.setPostcode("E5 9AA");
    personalDetailsDto.setDateOfBirth("01-01-2000");
    RegistrationDetailsDto registrationDetailsDto = new RegistrationDetailsDto();
    registrationDetailsDto.setPersonalDetails(personalDetailsDto);
    submissionDto.setSubmissionDate("2023-04-01");
    submissionDto.setRegistrationDetails(registrationDetailsDto);
    submissionDto.setFormData(form);
    return submissionDto;
  }

  private static void assertHtmlEquals(String expected, String actual) {
    var expectedDoc = Jsoup.parse(expected);
    var actualDoc = Jsoup.parse(actual);
    assertEquals(expectedDoc.html(), actualDoc.html());
  }

  private String readFromFile(String filePath) throws IOException {
    String formData =
        Files.readString(Path.of(getClass().getClassLoader().getResource(filePath).getPath()));
    return formData;
  }
}
