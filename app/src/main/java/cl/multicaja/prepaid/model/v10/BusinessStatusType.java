package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BusinessStatusType {

  OK("OK"),
  REVERSED("REVERSED");

  public String getValue() {
    return value;
  }

  String value;

  BusinessStatusType(String value) {
    this.value = value;
  }
  public static BusinessStatusType valueOfEnum(String name) {
    try {
      return BusinessStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, BusinessStatusType> FORMAT_MAP = Stream
    .of(BusinessStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static BusinessStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
