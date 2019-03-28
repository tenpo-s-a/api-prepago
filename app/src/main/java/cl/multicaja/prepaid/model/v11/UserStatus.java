package cl.multicaja.prepaid.model.v11;

/**
 * @author JOG
 */
public enum UserStatus {

  PENDING,
  VALIDATED,
  UNCONFIRMED,
  ACTIVE,
  BLOCKED;

  public static UserStatus valueOfEnum(String name) {
    try {
      return UserStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
