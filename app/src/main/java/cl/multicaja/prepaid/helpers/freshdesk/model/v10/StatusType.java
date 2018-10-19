package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StatusType {

  OPEN(2),
  PENDING(3),
  RESOLVED(4),
  CLOSED(5);

  private Integer value;

  StatusType(Integer value) {
    this.value = value;
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }

  private static Map<Integer, StatusType> FORMAT_MAP = Stream
    .of(StatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static StatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
