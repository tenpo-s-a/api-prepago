package cl.multicaja.prepaid.helpers.tecnocom.model;

import cl.multicaja.prepaid.model.v10.TecnocomOperationType;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TecnocomReconciliationRegisterType {
  AU("AU"),
  OP("OP");

  String value;

  TecnocomReconciliationRegisterType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static TecnocomReconciliationRegisterType valueOfEnum(String name) {
    try {
      return TecnocomReconciliationRegisterType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, TecnocomReconciliationRegisterType> FORMAT_MAP = Stream
    .of(TecnocomReconciliationRegisterType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static TecnocomReconciliationRegisterType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
