package cl.multicaja.prepaid.model.v10;

import java.util.ArrayList;
import java.util.List;

/**
 * @author abarazarte
 */
public enum TecnocomInvoiceType {

  COMISION_APERTURA(3000, 0, "+"),
  ANULA_COMISION_APERTURA(3000, 1, "-"),
  CARGA_TRANSFERENCIA(3001, 0, "-"),
  ANULA_CARGA_TRANSFERENCIA(3001, 1, "+"),
  CARGA_EFECTIVO_COMERCIO_MULTICAJA(3002, 0, "-"),
  ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA(3002, 1, "+"),
  RETIRO_TRANSFERENCIA(3003, 0, "+"),
  ANULA_RETIRO_TRANSFERENCIA(3003, 1, "-"),
  RETIRO_EFECTIVO_COMERCIO_MULTICJA(3004, 0, "+"),
  ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA(3004, 1, "-"),
  REEMISION_DE_TARJETA(3005, 0, "+"),
  ANULA_REEMISION_DE_TARJETA(3005, 1, "-"),
  SUSCRIPCION_INTERNACIONAL(3006, 0, "+"),
  ANULA_SUSCRIPCION_INTERNACIONAL(3006, 1, "-"),
  COMPRA_INTERNACIONAL(3007, 0, "+"),
  ANULA_COMPRA_INTERNACIONAL(3007, 1, "-"),
  COMPRA_COMERCIO_RELACIONADO(3008, 0, "+"),
  ANULA_COMPRA_COMERCIO_RELACIONADO(3008, 1, "-"),
  COMPRA_NACIONAL(3009, 0, "+"),
  ANULA_COMPRA_NACIONAL(3009, 1, "-"),
  DEVOLUCION_COMPRA_INTERNACIONAL(3010, 0, "-"),
  ANULA_DEVOLUCION_COMPRA_INTERNACIONAL(3010, 1, "+"),
  DEVOLUCION_COMPRA_COM_RELAC(3011, 0, "-"),
  ANULA_DEVOLUCION_COMPRA_COM_RELAC(3011, 1, "+"),
  DEVOLUCION_COMPRA_NACIONAL(3012, 0, "-"),
  ANULA_DEVOLUCION_COMPRA_NACIONAL(3012, 1, "+"),
  RETIRO_MODIFICAR_DESCRIPCION(3013, 0, "+"),
  ANULA_RETIRO_MODIFICAR_DESCRIPCION(3013, 1, "-"),
  RETIRO_MODIFICAR_DESCRIPCION_1(3014, 0, "+"),
  ANULA_RETIRO_MODIFICAR_DESCRIPCION_1(3014, 1, "-");
  /*
  RETIRO_MODIFICAR_DESCRIPCION(3015, 0, "+"),
  RETIRO_MODIFICAR_DESCRIPCION(3015, 1, "-");
  */
  private int code;
  private int corrector;
  private String sign;

  TecnocomInvoiceType(int code, int corrector, String sign) {
    this.code = code;
    this.corrector = corrector;
    this.sign = sign;
  }

  public int getCode() {
    return code;
  }

  public int getCorrector() {
    return corrector;
  }

  public String getSign() {
    return sign;
  }

  public static TecnocomInvoiceType valueOfEnum(String name) {
    try {
      return TecnocomInvoiceType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  /**
   * Retorna el enum por su codigo
   * @param code
   * @return
   */
  public static List<TecnocomInvoiceType> valueOfEnumByCode(int code) {
    List<TecnocomInvoiceType> lst = new ArrayList<>();
    for (TecnocomInvoiceType t : values()) {
      if (t.getCode() == code) {
        lst.add(t);
      }
    }
    return lst.isEmpty() ? null : lst;
  }
}
