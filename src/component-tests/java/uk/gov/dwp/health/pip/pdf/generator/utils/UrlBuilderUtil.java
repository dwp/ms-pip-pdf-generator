package uk.gov.dwp.health.pip.pdf.generator.utils;

import static io.restassured.RestAssured.baseURI;

public class UrlBuilderUtil {

    public static String postCreatePdfUrl() {
        return baseURI + "/v1/pdf/createpdf";
    }

    public static String postCreateS3PdfUrl() {
        return baseURI + "/v1/pdf/s3createpdf";
    }
}