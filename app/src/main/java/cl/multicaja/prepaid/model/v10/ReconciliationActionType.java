package cl.multicaja.prepaid.model.v10;

public enum ReconciliationActionType {
  CARGA,
  REVERSA_CARGA,
  RETIRO,
  REVERSA_RETIRO,
  INVESTIGACION,
  REFUND,
  NONE;

  public static ReconciliationActionType valueOfEnum(String name) {
    try {
      return ReconciliationActionType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
