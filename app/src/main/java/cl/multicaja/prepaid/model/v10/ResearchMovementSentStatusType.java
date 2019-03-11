package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResearchMovementSentStatusType {

  SENT_RESEARCH_PENDING("PENDING"),
  SENT_RESEARCH_OK("SENT_OK");

  public String getValue() {
    return value;
  }

  String value;


  ResearchMovementSentStatusType(String value) {
    this.value = value;
  }

  public static ResearchMovementSentStatusType valueOfEnum(String name) {
    try {
      return ResearchMovementSentStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, ResearchMovementSentStatusType> FORMAT_MAP = Stream
    .of(ResearchMovementSentStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static ResearchMovementSentStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
