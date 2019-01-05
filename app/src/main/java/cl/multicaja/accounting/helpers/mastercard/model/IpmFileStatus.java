package cl.multicaja.accounting.helpers.mastercard.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum IpmFileStatus {

  PROCESSING("PROCESSING"),
  PROCESSED("PROCESSED"),
  ERROR("ERROR"),
  SUSPICIOUS("SUSPICIOUS");

  public String getValue() {
    return value;
  }

  String value;

  IpmFileStatus(String value) {
    this.value = value;
  }
  public static IpmFileStatus valueOfEnum(String name) {
    try {
      return IpmFileStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, IpmFileStatus> FORMAT_MAP = Stream
    .of(IpmFileStatus.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static IpmFileStatus fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
