package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum  SourceType {

  EMAIL(1),
  PORTAL(2),
  PHONE(3),
  CHAT(7),
  MOBIHELP(8),
  FEEDBACKWIDGET(9),
  OUTBOUNDEMAIL(10);

  private Integer value;

  SourceType(Integer value) {
    this.value = value;
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }

  private static Map<Integer, SourceType> FORMAT_MAP = Stream
    .of(SourceType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static SourceType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
