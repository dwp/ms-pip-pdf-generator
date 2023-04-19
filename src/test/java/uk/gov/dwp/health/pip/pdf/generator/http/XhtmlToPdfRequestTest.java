package uk.gov.dwp.health.pip.pdf.generator.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XhtmlToPdfRequestTest {

  @Test
  void testWhenExceptionThrowErrorLoggedEmptyJsonReturned() throws Exception {
    var cut = new XhtmlToPdfRequest();
    ObjectMapper mapper = mock(ObjectMapper.class);
    ReflectionTestUtils.setField(cut, "mapper", mapper);
    when(mapper.writeValueAsString(any(XhtmlToPdfRequest.class)))
        .thenThrow(new JsonProcessingException("") {});
    String actual = cut.toJson();
    assertThat(actual).isEqualTo("{}");
  }
}
