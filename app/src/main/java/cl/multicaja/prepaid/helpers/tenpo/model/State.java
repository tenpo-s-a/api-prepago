package cl.multicaja.prepaid.helpers.tenpo.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public enum State implements Serializable {

  PENDING,
  VALIDATED,
  UNCONFIRMED,
  ACTIVE,
  BLOCKED;

  public static State valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return State.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
