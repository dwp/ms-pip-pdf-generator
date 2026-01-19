package uk.gov.dwp.health.pip.pdf.generator.component.utils;

import static io.restassured.RestAssured.baseURI;

public class UrlBuilderUtil {

  public static String postCreatePdfV3Url() {
    return baseURI + "/v3/pdf/createPdf";
  }

  public static String postCreateS3PdfV3Url() {
    return baseURI + "/v3/pdf/s3CreatePdf";
  }
}
