package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum EmailStatus implements Serializable {

  UNVERIFIED,
  VERIFIED,
  TEMPORAL;

  public static EmailStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return EmailStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
