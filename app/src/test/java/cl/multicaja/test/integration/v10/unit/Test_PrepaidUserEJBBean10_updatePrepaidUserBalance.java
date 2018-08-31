package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidBalanceInfo10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10_updatePrepaidUserBalance extends TestBaseUnit {

  private PrepaidBalanceInfo10 newBalance() {
    return new PrepaidBalanceInfo10(152, 152,
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)));
  }

  @Test
  public void upadetPrepaidUserBalance_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    final PrepaidBalanceInfo10 newBalance = newBalance();

    getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Saldo debe igual", newBalance, prepaidUser10.getBalance());
    Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
  }

  @Test
  public void upadetPrepaidUserBalance_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    try {

      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertNull("Saldo debe ser null", prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());
  }
}
