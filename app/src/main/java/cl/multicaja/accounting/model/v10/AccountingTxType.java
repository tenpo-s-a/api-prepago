package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingTxType {

  COMPRA_SUSCRIPCION("SUSCRIPCION"),
  COMPRA_PESOS("COMPRA_PESOS"),
  COMPRA_MONEDA("COMPRA_OTRA_MONEDA"),
  CARGA_WEB("CARGA_WEB"),
  CARGA_POS("CARGA_POS"),
  RETIRO_WEB("RETIRO_WEB"),
  RETIRO_POS("RETIRO_POS"),
  DEVOLUCION("DEVOLUCION");

  AccountingTxType(String value) {
    this.value = value;
  }

  String value;
  public String getValue() {
    return value;
  }

  public static AccountingTxType valueOfEnum(String name) {
    try {
      return AccountingTxType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, AccountingTxType> FORMAT_MAP = Stream
    .of(AccountingTxType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static AccountingTxType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }


}
