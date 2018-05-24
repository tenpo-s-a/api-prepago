package cl.multicaja.prepaid.model.v10;

/**
 * @author abarazarte
 */
public enum TopupType {
  POS,
  WEB;

  public static TopupType valueOfEnum(String name) {
    try {
      return TopupType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
