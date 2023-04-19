package uk.gov.dwp.health.pip.pdf.generator.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class HttpRespStatusHandler implements ResponseErrorHandler {

  private static final Set<HttpStatus> HANDLEABLE_RESPONSE_CODE =
      Stream.of(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR)
          .collect(Collectors.toSet());
  private static Logger log = LoggerFactory.getLogger(HttpRespStatusHandler.class);

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return !HANDLEABLE_RESPONSE_CODE.contains(response.getStatusCode());
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    if (HANDLEABLE_RESPONSE_CODE.contains(response.getStatusCode())) {
      log.info(
          "Service responded with an error {}, {}",
          response.getStatusCode().value(),
          IOUtils.toString(response.getBody()));
    }
  }
}
