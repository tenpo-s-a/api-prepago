package cl.multicaja.prepaid.model.v10;

import java.io.Serializable;

/**
 * @author abarazarte
 */
public enum PrepaidCardStatus implements Serializable {
  PENDING,
  ACTIVE,
  LOCKED,
  LOCKED_HARD,
  EXPIRED,
  CLOSED;

  public static PrepaidCardStatus valueOfEnum(String name) {
    try {
      return PrepaidCardStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
