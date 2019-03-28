package cl.multicaja.prepaid.model.v11;

import java.io.Serializable;

public enum  CardStatus implements Serializable {
    PENDING,
    ACTIVE,
    LOCKED,
    LOCKED_HARD,
    EXPIRED;

    public static CardStatus valueOfEnum(String name) {
      try {
        return CardStatus.valueOf(name);
      } catch(Exception ex) {
        return null;
      }
  }

}
