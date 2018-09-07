package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum UserIdentityStatus implements Serializable {

  NORMAL,
  TERRORIST;

  public static UserIdentityStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return UserIdentityStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}