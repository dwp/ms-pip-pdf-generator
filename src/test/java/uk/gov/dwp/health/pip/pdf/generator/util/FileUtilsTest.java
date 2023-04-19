package uk.gov.dwp.health.pip.pdf.generator.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUtilsTest {

  private static FileUtils cut;

  @BeforeAll
  static void setupSpec() {
    cut = new FileUtils();
  }

  private static Stream<Arguments> testFixture() {
    return Stream.of(
        Arguments.of(0, 0),
        Arguments.of(1, 1),
        Arguments.of(1023, 1),
        Arguments.of(1024, 1),
        Arguments.of(1025, 2));
  }

  @ParameterizedTest(name = "{index} file size {0} is expected to be {1}")
  @MethodSource(value = "testFixture")
  @DisplayName("Test calculate file size in kb given an integer")
  void testCalculateFileSizeInKbGivenAnInteger(int input, int expected) {
    assertThat(cut.fileSizeInKb(input)).isEqualTo(expected);
  }

  @Test
  @DisplayName("Test illegal input and IllegalArgExceptionThrown")
  void testIllegalInputAndIllegalArgExceptionThrown() {
    assertThrows(IllegalArgumentException.class, () -> cut.fileSizeInKb(-1));
  }
}
