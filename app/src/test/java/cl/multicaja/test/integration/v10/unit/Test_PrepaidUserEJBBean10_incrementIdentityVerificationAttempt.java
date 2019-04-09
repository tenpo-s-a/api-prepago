package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.ERROR_INTERNO_BBDD;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @author abarazarte
 **/
public class Test_PrepaidUserEJBBean10_incrementIdentityVerificationAttempt extends TestBaseUnit {

  //TODO: Esto se eliminara
  @Ignore
  @Test
  public void incrementIdentityVerificationAttempt_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNotNull("Intento de validacion debe ser 0", prepaidUser10.getIdentityVerificationAttempts());
    Assert.assertEquals("Intento de validacion debe ser 0", Integer.valueOf(0), prepaidUser10.getIdentityVerificationAttempts());

    prepaidUser10 = getPrepaidUserEJBBean10().incrementIdentityVerificationAttempt(null, prepaidUser10);

    Assert.assertNotNull("Intento de validacion debe ser 1", prepaidUser10.getIdentityVerificationAttempts());
    Assert.assertEquals("Intento de validacion debe ser 1", Integer.valueOf(1), prepaidUser10.getIdentityVerificationAttempts());

  }
  @Ignore
  @Test
  public void incrementIdentityVerificationAttempt_not_ok() throws Exception {

    {
      try {
        getPrepaidUserEJBBean10().incrementIdentityVerificationAttempt(null, null);
        Assert.fail("Should not be here");
      } catch(BadRequestException bex) {
        Assert.assertEquals("debe se error PARAMETRO_FALTANTE_$VALUE", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
      }
    }

    {
      try {
        getPrepaidUserEJBBean10().incrementIdentityVerificationAttempt(null, new PrepaidUser10());
        Assert.fail("Should not be here");
      } catch(BadRequestException bex) {
        Assert.assertEquals("debe se error PARAMETRO_FALTANTE_$VALUE", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
      }
    }

    {
      PrepaidUser10 prepaidUser10 = new PrepaidUser10();
      prepaidUser10.setId(Long.MAX_VALUE);
      try {
        getPrepaidUserEJBBean10().incrementIdentityVerificationAttempt(null, prepaidUser10);
        Assert.fail("Should not be here");
      } catch(BaseException bex) {
        Assert.assertEquals("debe se error ERROR_INTERNO_BBDD", ERROR_INTERNO_BBDD.getValue(), bex.getCode());
      }
    }
  }
}
