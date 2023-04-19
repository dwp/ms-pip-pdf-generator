package uk.gov.dwp.health.pip.pdf.generator.service;

@FunctionalInterface
public interface EncryptionService<T, S> {

  S encrypt(T t);
}
