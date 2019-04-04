package cl.multicaja.prepaid.helpers.tenpo.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public enum Level implements Serializable {

  LEVEL_1,
  LEVEL_2;

  public static Level valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return Level.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
