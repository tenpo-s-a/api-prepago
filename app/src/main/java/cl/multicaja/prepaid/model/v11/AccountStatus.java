package cl.multicaja.prepaid.model.v11;

public enum AccountStatus {

  ACTIVE,
  LOCKED,
  CLOSED;

  public static AccountStatus valueOfEnum(String name) {
    try {
      return AccountStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
