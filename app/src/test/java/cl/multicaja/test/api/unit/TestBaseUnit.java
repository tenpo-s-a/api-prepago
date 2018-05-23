package cl.multicaja.test.api.unit;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;

/**
 * @autor vutreras
 */
public class TestBaseUnit extends TestApiBase {

  static {
    System.setProperty("project.artifactId", "api-prepaid");
  }

  private RutUtils rutUtils = RutUtils.getInstance();

  private PrepaidTopupDelegate10 prepaidTopupDelegate10;

  protected PrepaidTopupDelegate10 getPrepaidTopupDelegate10() {
    if (prepaidTopupDelegate10 == null) {
      prepaidTopupDelegate10 = new PrepaidTopupDelegate10();
    }
    return prepaidTopupDelegate10;
  }

  private CdtEJBBean10 cdtEJBBean10;

  protected CdtEJBBean10 getCdtEJBBean10() {
    if (cdtEJBBean10 == null) {
      cdtEJBBean10 = new CdtEJBBean10();
    }
    return cdtEJBBean10;
  }

  private UsersEJBBean10 usersEJBBean10;

  protected UsersEJBBean10 getUsersEJBBean10() {
    if (usersEJBBean10 == null) {
      usersEJBBean10 = new UsersEJBBean10();
    }
    return usersEJBBean10;
  }

  private PrepaidEJBBean10 prepaidEJBBean10;

  protected PrepaidEJBBean10 getPrepaidEJBBean10() {
    if (prepaidEJBBean10 == null) {
      prepaidEJBBean10 = new PrepaidEJBBean10();
      prepaidEJBBean10.setDelegate(getPrepaidTopupDelegate10());
      prepaidEJBBean10.setUsersEJB10(getUsersEJBBean10());
      prepaidEJBBean10.setCdtEJB10(this.getCdtEJBBean10());
    }
    return prepaidEJBBean10;
  }
}
