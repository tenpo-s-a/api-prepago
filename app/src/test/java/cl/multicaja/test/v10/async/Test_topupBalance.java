package cl.multicaja.test.v10.async;


import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.NameStatus;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class Test_topupBalance extends TestBaseUnitAsync {

  @Test
  public void topupBalance_ok_first_topup_true_by_level_1() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
      Assert.assertTrue("debe ser primera carga", resp.isFirstTopup());
    }

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

    System.out.println(prepaidBalance10);

    Assert.assertEquals("El saldo del usuario debe ser 2010 pesos (carga inicial - comision de apertura (990))", 2010L, prepaidBalance10.getBalance().getValue().longValue());
  }

  @Test
  public void topupBalance_ok_first_topup_false_by_level_2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
      Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());
    }

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    System.out.println(prepaidCard10);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

    System.out.println(prepaidBalance10);

    Assert.assertEquals("El saldo del usuario debe ser 2010 pesos (carga inicial - comision de apertura (990))", 2010L, prepaidBalance10.getBalance().getValue().longValue());
  }

  @Test
  public void topupBalance_not_ok_by_topup_min() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }

    user.setNameStatus(NameStatus.VERIFIED);

    updateUser(user);

    //enesima carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }
  }

  @Test
  public void topupBalance_not_ok_by_topup_max_level_1() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }
  }

  @Test
  public void topupBalance_not_ok_by_topup_max_level_2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(500001));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga web", Integer.valueOf(108201), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(100001));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga pos", Integer.valueOf(108202), vex.getCode());
      }
    }
  }
}
