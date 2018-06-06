package cl.multicaja.prepaid.model.v10;

/**
 * @author abarazarte
 */
public enum TransactionOriginType {
  POS,
  WEB;

  public static TransactionOriginType valueOfEnum(String name) {
    try {
      return TransactionOriginType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
