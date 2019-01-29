package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReconciliationStatusType {

  PENDING("PENDING"),
  RECONCILED("RECONCILED"),
  NOT_RECONCILED("NOT_RECONCILED"),
  NEED_VERIFICATION("NEED_VERIFICATION"),
  NO_CASE("NO_CASE"),
  COUNTER_MOVEMENT("COUNTER_MOVEMENT"),
  TO_REFUND("TO_REFUND");

  public String getValue() {
    return value;
  }

  String value;

  ReconciliationStatusType(String value) {
    this.value = value;
  }
  public static ReconciliationStatusType valueOfEnum(String name) {
    try {
      return ReconciliationStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, ReconciliationStatusType> FORMAT_MAP = Stream
    .of(ReconciliationStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static ReconciliationStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
