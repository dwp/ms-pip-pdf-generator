package uk.gov.dwp.health.pip.pdf.generator.api.v2;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.dwp.health.pip.pdf.generator.api.AppControllerAdvise;
import uk.gov.dwp.health.pip.pdf.generator.api.v1.PdfGeneratorApiImpl;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfRequest;
import uk.gov.dwp.health.pip.pdf.generator.openapi.model.CreatePdfRequestS3;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.PdfGeneratorServiceImpl;
import uk.gov.dwp.health.pip.pdf.generator.service.impl.S3FileWriterImpl;
import uk.gov.dwp.health.pip.pdf.generator.util.FileUtils;

@AutoConfigureMockMvc
@ContextConfiguration(
    classes = {
        PdfGeneratorApiImpl.class,
        AppControllerAdvise.class,
        PdfGeneratorServiceImpl.class,
        S3FileWriterImpl.class,
    })
@WebMvcTest
@Disabled
class PdfGeneratorApiImplHttpTest {

  private final ObjectMapper MAPPER = new ObjectMapper();
  private final CreatePdfRequest PDF_REQUEST = new CreatePdfRequest();
  private final CreatePdfRequestS3 PDF_REQUEST_S3 = new CreatePdfRequestS3();
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private PdfGeneratorServiceImpl pdfService;
  @MockBean
  private S3FileWriterImpl s3Service;
  @MockBean
  private Base64.Decoder decoder;
  @MockBean
  private FileUtils fileUtils;

  @BeforeEach
  void setup() {
    MAPPER.registerModule(new ParameterNamesModule());
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    PDF_REQUEST.setClaimId("b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    PDF_REQUEST.setFormData("sample form data");
    PDF_REQUEST_S3.setBucket("testbucket");
    PDF_REQUEST_S3.setClaimId("b0a0d4fb-e6c8-419e-8cb9-af45914bd1a6");
    PDF_REQUEST_S3.setFormData("sample form data");
  }

  @Test
  void testCreatePdfFailRequestValidation() throws Exception {
    PDF_REQUEST.setClaimId("");
    mockMvc
        .perform(
            post("/v1/pdf/createpdf")
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(PDF_REQUEST)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreatePdfFailureThrowsInternalServerError() throws Exception {
    when(pdfService.handlePdfGeneration(anyString(), anyString()))
        .thenThrow(RuntimeException.class);
    mockMvc
        .perform(
            post("/v1/pdf/createpdf")
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(PDF_REQUEST)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testCreateS3PdfFailRequestValidation() throws Exception {
    PDF_REQUEST_S3.setClaimId("");
    mockMvc
        .perform(
            post("/v1/pdf/s3createpdf")
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(PDF_REQUEST_S3)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateS3PdfFailureThrowsInternalServerError() throws Exception {
    when(pdfService.handlePdfGeneration(anyString(), anyString()))
        .thenThrow(RuntimeException.class);
    mockMvc
        .perform(
            post("/v1/pdf/s3createpdf")
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(PDF_REQUEST_S3)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("Test create pdf returns response with applicationPdf media type")
  void testCreatePdfReturnsResponseWithApplicationPdfMediaType() throws Exception {
    when(pdfService.handlePdfGeneration(anyString(), anyString())).thenReturn("VEVTVCBURVNU");
    when(decoder.decode(anyString())).thenReturn("VEVTVCBURVNU".getBytes());
    MvcResult actual =
        mockMvc
            .perform(
                post("/v1/pdf/createpdf")
                    .characterEncoding("utf-8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(PDF_REQUEST)))
            .andDo(print())
            .andReturn();
    assertAll(
        "Assert response",
        () -> assertEquals(201, actual.getResponse().getStatus()),
        () -> assertEquals("application/pdf", actual.getResponse().getContentType()),
        () -> assertEquals("VEVTVCBURVNU", actual.getResponse().getContentAsString()));
  }
}
