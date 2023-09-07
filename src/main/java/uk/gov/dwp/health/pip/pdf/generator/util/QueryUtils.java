package uk.gov.dwp.health.pip.pdf.generator.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class QueryUtils {

  private QueryUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static <T> Stream<T> findByPredicate(List<T> iterable, Predicate<? super T> predicate) {
    return iterable
        .stream()
        .filter(predicate);
  }

  public static <T> T findOneByPredicate(List<T> iterable, Predicate<? super T> predicate) {
    var res = findByPredicate(iterable, predicate)
        .findFirst();
    return res.orElse(null);
  }
}
