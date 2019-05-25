package cl.multicaja.prepaid.helpers.tenpo.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public enum Plan implements Serializable {

  FREE,
  PREMIUM;

  public static Plan valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return Plan.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
