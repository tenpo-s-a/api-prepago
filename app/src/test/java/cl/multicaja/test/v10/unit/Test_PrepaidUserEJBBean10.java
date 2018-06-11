package cl.multicaja.test.v10.unit;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.model.v10.PrepaidBalance10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class Test_PrepaidUserEJBBean10 extends TestBaseUnit {

  @Test
  public void updatePrepaidUserStatus_OK() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());

    getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser10.getId(), PrepaidUserStatus.DISABLED);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser DISABLED", PrepaidUserStatus.DISABLED, prepaidUser10.getStatus());
  }

  @Test
  public void updatePrepaidUserStatus_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());

    try {
      getPrepaidUserEJBBean10().updatePrepaidUserStatus(null, prepaidUser10.getId(), null);
    } catch(ValidationException vex) {
      Assert.assertEquals("debe se error 101004", Integer.valueOf(101004), vex.getCode());
    }

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Estado debe ser ACTIVE", PrepaidUserStatus.ACTIVE, prepaidUser10.getStatus());
  }

  @Test
  public void upadetPrepaidUserBalance_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Saldo debe ser 0", BigDecimal.valueOf(0L), prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    //saldo positivo
    {
      final BigDecimal newBalance = BigDecimal.valueOf(1000L);

      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

      prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

      Assert.assertEquals("Saldo debe igual", newBalance, prepaidUser10.getBalance());
      Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
    }

    //slado negativo
    {
      final BigDecimal newBalance = BigDecimal.valueOf(-1000L);

      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

      prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

      Assert.assertEquals("Saldo debe ser igual", newBalance, prepaidUser10.getBalance());
      Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
    }
  }

  @Test
  public void upadetPrepaidUserBalance_not_ok() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Saldo debe ser 0", BigDecimal.valueOf(0L), prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    try {
      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), null);
    } catch(ValidationException vex) {
      Assert.assertEquals("debe se error 101004", Integer.valueOf(101004), vex.getCode());
    }

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Saldo debe ser 0", BigDecimal.valueOf(0L), prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());
  }

  @Test
  public void getPrepaidUserBalance() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

    Assert.assertEquals("Saldo debe ser 0", BigDecimal.valueOf(0L), prepaidUser10.getBalance());
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), prepaidUser10.getBalanceExpiration());

    {
      PrepaidBalance10 balance = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

      Assert.assertEquals("Saldo debe ser 0", BigDecimal.valueOf(0L), balance.getPrimary().getValue());
      Assert.assertEquals("debe ser codigo moneda de chile", CodigoMoneda.CHILE_CLP, balance.getPrimary().getCurrencyCode());

      Assert.assertEquals("Saldo debe ser 0", BigDecimal.valueOf(0L), balance.getSecondary().getValue());
      Assert.assertEquals("debe ser codigo moneda de chile", CodigoMoneda.USA_USN, balance.getSecondary().getCurrencyCode());
    }

    final BigDecimal newBalance = BigDecimal.valueOf(1000L);

    //saldo positivo
    {
      getPrepaidUserEJBBean10().updatePrepaidUserBalance(null, prepaidUser10.getId(), newBalance);

      prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser10.getId());

      Assert.assertEquals("Saldo debe ser igual", newBalance, prepaidUser10.getBalance());
      Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", prepaidUser10.getBalanceExpiration() > System.currentTimeMillis());
    }

    //obtener nuevo balance
    {
      PrepaidBalance10 balance = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

      System.out.println(toJson(balance));

      Assert.assertEquals("Saldo debe ser igual", newBalance, balance.getPrimary().getValue());
      Assert.assertEquals("debe ser codigo moneda de chile", CodigoMoneda.CHILE_CLP, balance.getPrimary().getCurrencyCode());

      BigDecimal balanceInUS = BigDecimal.valueOf(newBalance.doubleValue() / CalculationsHelper.getUsdValue());

      Assert.assertEquals("Saldo debe ser igual", balanceInUS, balance.getSecondary().getValue());
      Assert.assertEquals("debe ser codigo moneda de chile", CodigoMoneda.USA_USN, balance.getSecondary().getCurrencyCode());
    }
  }
}
