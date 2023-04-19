package uk.gov.dwp.health.pip.pdf.generator.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskExceptionTest {

  @Test
  void testCreateTaskException() {
    var cut = new TaskException("perform task failed");
    assertEquals("perform task failed", cut.getMessage());
  }
}
