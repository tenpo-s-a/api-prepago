package cl.multicaja.prepaid.model.v10;

import java.io.Serializable;

/**
 * @author abarazarte
 */
public enum PrepaidCardStatus implements Serializable {

  ACTIVE,
  LOCKED,
  LOCKED_HARD,
  EXPIRED;

  public static PrepaidCardStatus valueOfEnum(String name) {
    try {
      return PrepaidCardStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
