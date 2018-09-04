package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public enum AppFileStatus implements Serializable {

  ENABLED,
  DISABLED;

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
