package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum NameStatus implements Serializable {

  UNVERIFIED(800),
  VERIFIED(802),
  IN_REVIEW(801);

  private Integer value;

  NameStatus(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public static NameStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return NameStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
