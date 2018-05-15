package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * @author abarazarte
 */
public enum PrepaidCardStatus implements Serializable {
  ACTIVE("activa"),
  LOCKED("bloqueada"),
  EXPIRED("expirada");

  private String value;

  PrepaidCardStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public static PrepaidCardStatus valueOfEnum(String name) {
    try {
      return PrepaidCardStatus.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
}
