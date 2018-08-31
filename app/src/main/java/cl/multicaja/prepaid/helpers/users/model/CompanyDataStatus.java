package cl.multicaja.prepaid.helpers.users.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @autor vutreras
 */
public enum CompanyDataStatus implements Serializable {

  UNVERIFIED,
  VERIFIED,
  IN_REVIEW;

  public static CompanyDataStatus valueOfEnum(String name) {
    try {
      if (StringUtils.isBlank(name)) {
        return null;
      }
      return CompanyDataStatus.valueOf(name.trim());
    } catch(Exception ex) {
      return null;
    }
  }
}
