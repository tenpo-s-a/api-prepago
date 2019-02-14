package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MovementOriginType {
  API("API"),
  SAT("SAT"),
  OPE("OPE");
  String value;

  MovementOriginType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  private static Map<String, MovementOriginType> FORMAT_MAP = Stream
    .of(MovementOriginType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static MovementOriginType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
