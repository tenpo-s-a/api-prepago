package cl.multicaja.prepaid.model.v10;

public enum ReconciliationOriginType {

  SWITCH,
  TECNOCOM,
  MOTOR,
  CLEARING;

  public static ReconciliationOriginType valueOfEnum(String name) {
    try {
      return ReconciliationOriginType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
