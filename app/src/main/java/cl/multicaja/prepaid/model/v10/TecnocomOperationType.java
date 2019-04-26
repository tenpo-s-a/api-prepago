package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum TecnocomOperationType {
  PURCHASES("PURCHASES"), // Compras internacionales, suscripciones
  REGULAR("REGULAR"); // Cargas, retiros

  String value;

  TecnocomOperationType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static TecnocomOperationType valueOfEnum(String name) {
    try {
      return TecnocomOperationType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, TecnocomOperationType> FORMAT_MAP = Stream
    .of(TecnocomOperationType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static TecnocomOperationType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
