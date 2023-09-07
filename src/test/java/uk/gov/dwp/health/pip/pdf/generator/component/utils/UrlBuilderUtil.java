package uk.gov.dwp.health.pip.pdf.generator.component.utils;

import static io.restassured.RestAssured.baseURI;

public class UrlBuilderUtil {

    public static String postCreatePdfUrl() {
        return baseURI + "/v1/pdf/createpdf";
    }

    public static String postCreateS3PdfUrl() {
        return baseURI + "/v1/pdf/s3createpdf";
    }

    public static String postCreatePdfV2Url() {
        return baseURI + "/v2/pdf/createPdf";
    }

    public static String postCreateS3PdfV2Url() {
        return baseURI + "/v2/pdf/s3CreatePdf";
    }
}