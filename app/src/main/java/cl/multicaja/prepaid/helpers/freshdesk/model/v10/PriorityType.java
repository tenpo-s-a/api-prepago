package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PriorityType {

  LOW(1),
  MEDIUM(2),
  HIGH(3),
  URGENT(4);
  private Integer value;

  PriorityType(Integer value) {
    this.value = value;
  }
  @JsonValue
  public Integer getValue() {
    return value;
  }

  private static Map<Integer, PriorityType> FORMAT_MAP = Stream
    .of(PriorityType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static PriorityType fromValue(Integer value) {
    return FORMAT_MAP.get(value);
  }


}
