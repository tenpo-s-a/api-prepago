package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OriginOpeType {

  SAT_ORIGIN("ONLI"),
  API_ORIGIN("MAUT"),
  AUT_ORIGIN("AUTO");

  String value;

  OriginOpeType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static OriginOpeType valueOfEnum(String name) {
    try {
      return OriginOpeType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, OriginOpeType> FORMAT_MAP = Stream
    .of(OriginOpeType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static OriginOpeType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
