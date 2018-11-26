package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingOriginType {

  IPM("IpmFile"),
  MOVEMENT("Movement");


  AccountingOriginType(String value) {
    this.value = value;
  }

  String value;
  public String getValue() {
    return value;
  }
  public static AccountingOriginType valueOfEnum(String name) {
    try {
      return AccountingOriginType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, AccountingOriginType> FORMAT_MAP = Stream
    .of(AccountingOriginType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static AccountingOriginType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
