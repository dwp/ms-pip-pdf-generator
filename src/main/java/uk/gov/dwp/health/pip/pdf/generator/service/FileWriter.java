package uk.gov.dwp.health.pip.pdf.generator.service;

@FunctionalInterface
public interface FileWriter<T, S> {

  S writeObjectToS3(T data);
}
