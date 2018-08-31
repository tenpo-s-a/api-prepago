package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public enum UserFileStatus implements Serializable {

  ENABLED(1200),
  DISABLED(1201);

  private Integer value;

  UserFileStatus(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public static UserFileStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return UserFileStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
