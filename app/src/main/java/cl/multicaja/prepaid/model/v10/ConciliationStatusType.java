package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ConciliationStatusType {

  PENDING("PENDING"),
  CONCILATE("CONCILATE"),
  NO_CONCILIATE("NO_CONCILIATE");

  public String getValue() {
    return value;
  }

  String value;

  ConciliationStatusType(String value) {
    this.value = value;
  }
  public static ConciliationStatusType valueOfEnum(String name) {
    try {
      return ConciliationStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, ConciliationStatusType> FORMAT_MAP = Stream
    .of(ConciliationStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static ConciliationStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
