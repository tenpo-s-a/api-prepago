package cl.multicaja.test.api.unit;

import cl.multicaja.prepaid.domain.PrepaidUser;
import cl.multicaja.prepaid.domain.PrepaidUserStatus;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10 extends TestBaseUnit {

  @Inject
  private PrepaidEJBBean10 prepaidEJBBean10 = new PrepaidEJBBean10();

  private PrepaidUser createUser() throws Exception {
    PrepaidUser user = new PrepaidUser();
    user.setIdUser(new Long(getUniqueInteger()));
    user.setRut(getUniqueRutNumber());
    user.setStatus(PrepaidUserStatus.ACTIVE);
    return prepaidEJBBean10.createPrepaidUser(null, user);
  }

  @Test
  public void insertUserOk() throws Exception {

    PrepaidUser user = createUser();

    Assert.assertNotNull("debe retornar un usuario", user);
    Assert.assertEquals("debe tener id", true, user.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, user.getIdUser() > 0);
    Assert.assertEquals("debe tener rut", true, user.getRut() > 0);
    Assert.assertNotNull("debe tener status", user.getStatus());
  }
}
