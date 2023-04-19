package uk.gov.dwp.health.pip.pdf.generator.utils;

public class EnvironmentUtil {
  public static String getEnv(String name, String defaultValue) {
    String env = System.getenv(name);
    return env == null ? defaultValue : env;
  }
}
