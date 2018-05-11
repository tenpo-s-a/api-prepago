package cl.multicaja.prepaid.domain;

/**
 * @author abarazarte
 */
public enum PrepaidUserStatus {
  ACTIVE,
  DISABLED;

  public static PrepaidUserStatus valueOfEnum(String name) {
    try {
      return PrepaidUserStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
