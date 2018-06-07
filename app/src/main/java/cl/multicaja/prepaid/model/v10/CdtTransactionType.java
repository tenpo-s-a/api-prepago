package cl.multicaja.prepaid.model.v10;

/**
 * @author abarazarte
 */
public enum CdtTransactionType {

  PRIMERA_CARGA("Solicitud Primera Carga"),
  PRIMERA_CARGA_CONF("Confirmación Primera Carga"),
  CARGA_WEB("Solicitud Carga Web"),
  CARGA_WEB_CONF("Confirmación Carga Web"),
  CARGA_POS("Solicitud Carga POS"),
  CARGA_POS_CONF("Confirmación Carga POS"),
  RETIRO_WEB("Solicitud Retiro Web"),
  RETIRO_WEB_CONF("Confirmación Retiro Web"),
  RETIRO_POS("Solicitud Retiro POS"),
  RETIRO_POS_CONF("Confirmación Retiro POS"),
  REVERSA_RETIRO("Solicitud Reversa de Retiro"),
  REVERSA_RETIRO_CONF("Confirmación Reversa de Retiro"),
  REVERSA_CARGA("Solicitud Reversa de Carga"),
  REVERSA_CARGA_CONF("Confirmación Reversa de Carga");

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
}
