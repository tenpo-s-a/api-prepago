package cl.multicaja.prepaid.model.v10;

import java.util.stream.Stream;
/**
 * @author abarazarte
 */
public enum CdtTransactionType {

  PRIMERA_CARGA("Solicitud Primera Carga"),
  PRIMERA_CARGA_CONF("Confirmación Primera Carga"),
  REVERSA_PRIMERA_CARGA("Solicitud Reversa Primera Carga"),
  REVERSA_PRIMERA_CARGA_CONF("Confirmación Reversa Primera Carga"),

  CARGA_WEB("Solicitud Carga Web"),
  CARGA_WEB_CONF("Confirmación Carga Web"),
  CARGA_POS("Solicitud Carga POS"),
  CARGA_POS_CONF("Confirmación Carga POS"),
  REVERSA_CARGA("Solicitud Reversa Carga"),
  REVERSA_CARGA_CONF("Confirmación Reversa Carga"),

  RETIRO_WEB("Solicitud Retiro Web"),
  RETIRO_WEB_CONF("Confirmación Retiro Web"),
  RETIRO_POS("Solicitud Retiro POS"),
  RETIRO_POS_CONF("Confirmación Retiro POS"),
  REVERSA_RETIRO("Solicitud Reversa de Retiro"),
  REVERSA_RETIRO_CONF("Confirmacion Reversa de Retiro");

  private String name;

  CdtTransactionType(String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }

  public static CdtTransactionType valueOfEnum(String name) {
    try {
      return CdtTransactionType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  public static CdtTransactionType fromValue(String value) {
    return Stream.of(CdtTransactionType.values())
      .filter(t -> value.equals(t.getName()))
      .findFirst()
      .orElse(null);
  }

}
