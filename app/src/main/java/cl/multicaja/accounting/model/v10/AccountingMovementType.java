package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingMovementType {

  SUSCRIPCION("Cargo por compra su"),
  COMPRA_PESOS("Cargo por compra cp"),
  COMPRA_MONEDA("Cargo por compra cm"),
  CARGA_WEB("Abono por carga TEF"),
  CARGA_POS("Abono por carga POS"),
  RETIRO_WEB("Cargo por retiro TEF"),
  RETIRO_POS("Cargo por retiro POS"),
  DEVOLUCION_COMPRA("Devolucion de compra");


  AccountingMovementType(String value) {
    this.value = value;
  }

  String value;
  public String getValue() {
    return value;
  }

  public static AccountingMovementType valueOfEnum(String name) {
    try {
      return AccountingMovementType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, AccountingMovementType> FORMAT_MAP = Stream
    .of(AccountingMovementType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static AccountingMovementType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }


}
