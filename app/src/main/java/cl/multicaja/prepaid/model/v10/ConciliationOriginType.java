package cl.multicaja.prepaid.model.v10;

public enum ConciliationOriginType {

  SWITCH,
  TECNOCOM,
  MOTOR;

  public static ConciliationOriginType valueOfEnum(String name) {
    try {
      return ConciliationOriginType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
