package cl.multicaja.prepaid.model.v10;

/**
 * @author abarazarte
 */
public enum PrepaidUserStatus {

  ACTIVE,
  DISABLED,
  PENDING,
  VALIDATED,
  UNCONFIRMED,
  BLOCKED,
  CONFIRMED,
  CLOSED;

  public static PrepaidUserStatus valueOfEnum(String name) {
    try {
      return PrepaidUserStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
