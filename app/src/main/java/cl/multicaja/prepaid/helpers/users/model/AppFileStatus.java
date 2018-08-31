package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public enum AppFileStatus implements Serializable {

  ENABLED(1100),
  DISABLED(1101);

  private Integer value;

  AppFileStatus(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public static AppFileStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return AppFileStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
