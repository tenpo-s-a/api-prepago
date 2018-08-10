package cl.multicaja.test.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.users.model.v10.NameStatus;
import cl.multicaja.users.model.v10.Rut;
import cl.multicaja.users.model.v10.RutStatus;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @author abarazarte
 **/
public class Test_PrepaidUserEJBBean10_getUserLevel {

  private static PrepaidUserEJBBean10 prepaidUserEJB10;

  @BeforeClass
  public static void setup() {
    prepaidUserEJB10 = new PrepaidUserEJBBean10();
  }

  @Test(expected = BadRequestException.class)
  public void userMcNull() throws Exception {
    try{
      prepaidUserEJB10.getUserLevel(null, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error user null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void userMcRutNull() throws Exception {
    try{
      prepaidUserEJB10.getUserLevel(new User(), null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error user.rut null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void userMcRutStatusNull() throws Exception {
    try{
      User user = new User();
      user.setRut(new Rut());
      prepaidUserEJB10.getUserLevel(user, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error user.rut.status null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void userMcNameStatusNull() throws Exception {
    try{
      User user = new User();
      Rut rut = new Rut();
      rut.setStatus(RutStatus.VERIFIED);
      user.setRut(rut);
      prepaidUserEJB10.getUserLevel(user, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error user.rut.namestatus null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void prepaidUserNull() throws Exception {
    try{
      User user = new User();
      Rut rut = new Rut();
      rut.setStatus(RutStatus.VERIFIED);
      user.setRut(rut);
      user.setNameStatus(NameStatus.VERIFIED);
      prepaidUserEJB10.getUserLevel(user, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error user.rut.namestatus null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
      throw  ex;
    }
  }

  @Test
  public void prepaidUserLevel2() throws Exception {
    PrepaidUser10 prepaidUser = new PrepaidUser10();

    User user = new User();
    Rut rut = new Rut();
    rut.setStatus(RutStatus.VERIFIED);
    user.setRut(rut);
    user.setNameStatus(NameStatus.VERIFIED);

    prepaidUser = prepaidUserEJB10.getUserLevel(user, prepaidUser);
    Assert.assertEquals("Debe ser LEVEL_2", PrepaidUserLevel.LEVEL_2, prepaidUser.getUserLevel());

  }

  @Test
  public void prepaidUserLevel1() throws Exception {
    {
      PrepaidUser10 prepaidUser = new PrepaidUser10();

      User user = new User();
      Rut rut = new Rut();
      rut.setStatus(RutStatus.UNVERIFIED);
      user.setRut(rut);
      user.setNameStatus(NameStatus.VERIFIED);

      prepaidUser = prepaidUserEJB10.getUserLevel(user, prepaidUser);
      Assert.assertEquals("Debe ser LEVEL_1", PrepaidUserLevel.LEVEL_1, prepaidUser.getUserLevel());
    }
    {
      PrepaidUser10 prepaidUser = new PrepaidUser10();

      User user = new User();
      Rut rut = new Rut();
      rut.setStatus(RutStatus.EXPIRED);
      user.setRut(rut);
      user.setNameStatus(NameStatus.VERIFIED);

      prepaidUser = prepaidUserEJB10.getUserLevel(user, prepaidUser);
      Assert.assertEquals("Debe ser LEVEL_1", PrepaidUserLevel.LEVEL_1, prepaidUser.getUserLevel());
    }
    {
      PrepaidUser10 prepaidUser = new PrepaidUser10();

      User user = new User();
      Rut rut = new Rut();
      rut.setStatus(RutStatus.LOCKED);
      user.setRut(rut);
      user.setNameStatus(NameStatus.VERIFIED);

      prepaidUser = prepaidUserEJB10.getUserLevel(user, prepaidUser);
      Assert.assertEquals("Debe ser LEVEL_1", PrepaidUserLevel.LEVEL_1, prepaidUser.getUserLevel());
    }
    {
      PrepaidUser10 prepaidUser = new PrepaidUser10();

      User user = new User();
      Rut rut = new Rut();
      rut.setStatus(RutStatus.NOT_MATCH);
      user.setRut(rut);
      user.setNameStatus(NameStatus.VERIFIED);

      prepaidUser = prepaidUserEJB10.getUserLevel(user, prepaidUser);
      Assert.assertEquals("Debe ser LEVEL_1", PrepaidUserLevel.LEVEL_1, prepaidUser.getUserLevel());
    }
  }

}
