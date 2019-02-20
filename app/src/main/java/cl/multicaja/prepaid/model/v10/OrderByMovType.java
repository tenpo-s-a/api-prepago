package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OrderByMovType {

  FECFAC("FECFAC"),
  CREATED("CREATED"),
  UPDATED("UPDATED");

  String value;

  OrderByMovType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static OrderByMovType valueOfEnum(String name) {
    try {
      return OrderByMovType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, OrderByMovType> FORMAT_MAP = Stream
    .of(OrderByMovType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static OrderByMovType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

  @Override
  public String toString() {
    return getValue();
  }
}
