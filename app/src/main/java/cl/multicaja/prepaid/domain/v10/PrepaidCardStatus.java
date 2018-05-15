package cl.multicaja.prepaid.domain.v10;

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
}
