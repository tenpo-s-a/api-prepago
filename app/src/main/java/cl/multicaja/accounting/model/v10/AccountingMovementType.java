package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingMovementType {

  SUSCRIPCION("CARGO_COMPRA_SUSCRIP"),
  COMPRA_PESOS("CARGO_COMPRA_PESOS"),
  COMPRA_MONEDA("CARGO_COMPRA_MONEDA"),
  CARGA_WEB("ABONO_CARGA_WEB"),
  CARGA_POS("ABONO_CARGA_POS"),
  RETIRO_WEB("CARGO_RETIRO_WEB"),
  RETIRO_POS("CARGO_RETIRO_POS"),
  ABONO_ANULACION("ABONO_ANULACION"),
  ABONO_DEVOLUCION("ABONO_DEVOLUCION");


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
