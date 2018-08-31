package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum CellphoneStatus implements Serializable {

  UNVERIFIED,
  VERIFIED,
  TEMPORAL;

  public static CellphoneStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return CellphoneStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
