package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_AccountEJBBean10_findByUuid extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_cuenta cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void findByUuid_uuid_null() throws Exception {
    try {
      getAccountEJBBean10().findByUuid(null);
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void findByUuid_uuid_empty() throws Exception {
    try {
      getAccountEJBBean10().findByUuid("");
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void findByUuid_account_null() throws Exception {
    Account account = getAccountEJBBean10().findByUuid(UUID.randomUUID().toString());

    Assert.assertNull(account);

  }

  @Test
  public void findByUuid() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), getRandomNumericString(15));

    Account dbAccount = getAccountEJBBean10().findByUuid(account.getUuid());

    Assert.assertEquals("Debe ser la misma tarjeta", account.getId(), dbAccount.getId());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getUuid(), dbAccount.getUuid());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getUserId(), dbAccount.getUserId());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getProcessor(), dbAccount.getProcessor());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getStatus(), dbAccount.getStatus());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getAccountNumber(), dbAccount.getAccountNumber());
  }
}
