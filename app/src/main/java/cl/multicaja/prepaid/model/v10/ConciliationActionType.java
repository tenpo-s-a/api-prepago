package cl.multicaja.prepaid.model.v10;

public enum ConciliationActionType {
  CARGA,
  REVERSA_CARGA,
  RETIRO,
  REVERSA_RETIRO,
  INVESTIGACION,
  NONE;

  public static ConciliationActionType valueOfEnum(String name) {
    try {
      return ConciliationActionType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
