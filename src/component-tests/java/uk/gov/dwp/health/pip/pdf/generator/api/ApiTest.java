package uk.gov.dwp.health.pip.pdf.generator.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import uk.gov.dwp.health.pip.pdf.generator.utils.S3Util;

import static io.restassured.RestAssured.given;
import static uk.gov.dwp.health.pip.pdf.generator.utils.EnvironmentUtil.getEnv;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ApiTest {
  static RequestSpecification requestSpec;
  protected static String bucketName;
  protected static S3Util s3Util;

  @BeforeAll
  static void beforeAll() {
    RestAssured.baseURI = getEnv("MS_PIP_PDF_GENERATOR_BASE_URI", "http://localhost:8080");
    RestAssured.port = Integer.parseInt(getEnv("PORT", "9945"));
    RestAssured.defaultParser = Parser.JSON;

    String awsS3EndpointOverride = getEnv("AWS_S3_ENDPOINT_OVERRIDE", "http://localhost:4566");
    String awsRegion = getEnv("AWS_REGION", "eu-west-2");
    bucketName = getEnv("AWS_S3_BUCKET", "pip-bucket");
    s3Util = new S3Util(awsS3EndpointOverride, awsRegion, bucketName);

    requestSpec =
            new RequestSpecBuilder()
                    .setContentType(ContentType.JSON)
                    .addFilter(new AllureRestAssured())
                    .build();
  }

  protected Response postRequest(String path, Object bodyPayload) {
    return given().spec(requestSpec).body(bodyPayload).when().post(path);
  }
}
