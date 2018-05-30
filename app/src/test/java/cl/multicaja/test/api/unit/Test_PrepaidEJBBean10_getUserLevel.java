package cl.multicaja.test.api.unit;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.users.model.v10.Rut;
import cl.multicaja.users.model.v10.RutStatus;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.NameStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 */
public class Test_PrepaidEJBBean10_getUserLevel  extends TestBaseUnit  {

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_UserNull() throws Exception {
    getPrepaidEJBBean10().getUserLevel(null, null);
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_UserRutNull() throws Exception {
    User user = new User();
    getPrepaidEJBBean10().getUserLevel(user, null);
  }

  @Test(expected = ValidationException.class)
  public void shouldReturnExceptionWhen_UserRutStatusNull() throws Exception {
    User user = new User();
    user.setRut(new Rut());
    getPrepaidEJBBean10().getUserLevel(user, null);
  }

  @Test(expected = NotFoundException.class)
  public void shouldReturnExceptionWhen_PrepaidUserNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setStatus(RutStatus.VERIFIED);
    user.setRut(rut);
    getPrepaidEJBBean10().getUserLevel(user, null);
  }

  @Test
  public void shouldBeLevel1() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setStatus(RutStatus.VERIFIED);
    user.setRut(rut);
    user.setNameStatus(NameStatus.UNVERIFIED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    Assert.assertEquals("Deberia ser N1", PrepaidUserLevel.LEVEL_1, getPrepaidEJBBean10().getUserLevel(user, prepaidUser));

    rut.setStatus(RutStatus.UNVERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);

    Assert.assertEquals("Deberia ser N1", PrepaidUserLevel.LEVEL_1, getPrepaidEJBBean10().getUserLevel(user, prepaidUser));
  }

  @Test
  public void shouldBeLevel2() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setStatus(RutStatus.VERIFIED);
    user.setRut(rut);
    user.setNameStatus(NameStatus.VERIFIED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    Assert.assertEquals("Deberia ser N1", PrepaidUserLevel.LEVEL_2, getPrepaidEJBBean10().getUserLevel(user, prepaidUser));
  }
}
