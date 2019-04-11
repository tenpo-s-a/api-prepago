package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.model.v10.PrepaidBalanceInfo10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @autor vutreras
 */
public class Test_AccountEJBBean10_updateBalance extends TestBaseUnit {

  private PrepaidBalanceInfo10 newBalance() {
    return new PrepaidBalanceInfo10(152, 152,
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)),
      BigDecimal.valueOf(numberUtils.random(100, 1000)));
  }

  @Test
  public void updateBalance_ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = createRandomAccount(prepaidUser10);

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    final PrepaidBalanceInfo10 newBalance = newBalance();

    getAccountEJBBean10().updateBalance(account.getId(), newBalance);

    account = getAccountEJBBean10().findById(account.getId());

    Assert.assertEquals("Saldo debe igual", newBalance, JsonUtils.getJsonParser().fromJson(account.getBalanceInfo(), PrepaidBalanceInfo10.class));
    Assert.assertTrue("Saldo expiracion debe ser mayor al currentTimeMillis actual", account.getExpireBalance() > Instant.now().toEpochMilli());
  }

  @Test
  public void updateBalance_accountId_null() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = createRandomAccount(prepaidUser10);

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    try {

      getAccountEJBBean10().updateBalance(null, newBalance());

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error PARAMETRO_FALTANTE_$VALUE", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    account = getAccountEJBBean10().findById(account.getId());

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());
  }

  @Test
  public void updateBalance_balance_null() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = createRandomAccount(prepaidUser10);

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    try {

      getAccountEJBBean10().updateBalance(account.getId(), null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error PARAMETRO_FALTANTE_$VALUE", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    account = getAccountEJBBean10().findById(account.getId());

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());
  }

  @Test
  public void updateBalance_account_null() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = createRandomAccount(prepaidUser10);

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());

    try {

      getAccountEJBBean10().updateBalance(Long.MAX_VALUE, newBalance());

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(Exception vex) {

    }

    account = getAccountEJBBean10().findById(account.getId());

    Assert.assertTrue("Saldo debe ser null", StringUtils.isAllBlank(account.getBalanceInfo()));
    Assert.assertEquals("Saldo expiracion debe ser 0", Long.valueOf(0L), account.getExpireBalance());
  }
}
