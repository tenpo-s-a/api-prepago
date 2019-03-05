package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResearchMovementFileStatusType {


  NOT_FILE_NAME(" "),
  NOT_FILE_NAME_TECNOCOM("NOT_FILE_NAME_TECNOCOM"),
  NOT_FILE_NAME_SWITCH("NOT_FILE_NAME_SWITCH");

  public String getValue() {
    return value;
  }

  String value;

  ResearchMovementFileStatusType(String value) {
    this.value = value;
  }

  public static ResearchMovementFileStatusType valueOfEnum(String name) {
    try {
      return ResearchMovementFileStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, ResearchMovementFileStatusType> FORMAT_MAP = Stream
    .of(ResearchMovementFileStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static ResearchMovementFileStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
