package uk.gov.dwp.health.pip.pdf.generator.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRespStatusHandlerTest {

  private static ClientHttpResponse response;
  private final TestLogger testLogger = TestLoggerFactory.getTestLogger(HttpRespStatusHandler.class);
  private HttpRespStatusHandler cut;

  private static Stream<Arguments> testCase() {
    return Stream.of(
        Arguments.of(HttpStatus.BAD_REQUEST, false),
        Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, false));
  }

  @BeforeEach
  void setup() {
    cut = new HttpRespStatusHandler();
    response = mock(ClientHttpResponse.class);
  }

  @ParameterizedTest
  @MethodSource(value = "testCase")
  void testHasErrorWithAllowedStatusCode(HttpStatus respStatus, boolean expect) throws IOException {
    when(response.getStatusCode()).thenReturn(respStatus);
    assertThat(cut.hasError(response)).isEqualTo(expect);
  }

  @Test
  void testHandleErrorLogError() throws Exception {
    testLogger.clearAll();
    ReflectionTestUtils.setField(cut, "log", testLogger);
    when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("MOCK_ERROR_MSG".getBytes(StandardCharsets.UTF_8)));
    cut.handleError(response);
    assertThat(testLogger.getLoggingEvents()).hasSize(1);
    assertThat(testLogger.getLoggingEvents())
        .isEqualTo(
            Collections.singletonList(
                LoggingEvent.info(
                    "Service responded with an error {}, {}", 500, "MOCK_ERROR_MSG")));
  }
}
