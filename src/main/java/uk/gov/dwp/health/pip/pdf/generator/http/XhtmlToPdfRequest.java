package uk.gov.dwp.health.pip.pdf.generator.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XhtmlToPdfRequest {

  @JsonIgnore private static ObjectMapper mapper = new ObjectMapper();

  @JsonProperty("page_html")
  private String htmlData;

  @JsonProperty("conformance_level")
  private String conformanceLevel;

  public String toJson() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Unable to map to json string {}", e.getMessage());
    }
    return "{}";
  }
}
