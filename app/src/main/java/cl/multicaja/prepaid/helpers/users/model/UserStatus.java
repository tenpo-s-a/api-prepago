package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum UserStatus implements Serializable {

  ENABLED,
  DISABLED,
  LOCKED,
  DELETED,
  PREREGISTERED;
  public static UserStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return UserStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
