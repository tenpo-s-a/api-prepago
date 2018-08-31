package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum RutStatus implements Serializable {

  UNVERIFIED(600),
  VERIFIED(601),
  LOCKED(602),
  EXPIRED(603),
  NOT_MATCH(604);
  private Integer value;

  RutStatus(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public static RutStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return RutStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
