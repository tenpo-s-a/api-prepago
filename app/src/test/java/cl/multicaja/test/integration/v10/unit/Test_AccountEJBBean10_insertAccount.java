package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountProcessor;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.junit.*;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_AccountEJBBean10_insertAccount extends TestBaseUnit {

  @Before
  @After
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_cuenta cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void insertAccount_userId_null() throws Exception {
    try {
      getAccountEJBBean10().insertAccount(null, getRandomNumericString(20));
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void insertAccount_accountNumber_null() throws Exception {
    try {
      getAccountEJBBean10().insertAccount(Long.MAX_VALUE, null);
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void insertAccount_accountNumber_empty() throws Exception {
    try {
      getAccountEJBBean10().insertAccount(Long.MAX_VALUE, "");
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void insertAccount() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    String accountNumber = getRandomNumericString(15);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), accountNumber);

    Assert.assertNotNull(account.getId());
    Assert.assertNotNull(account.getUuid());
    Assert.assertEquals(AccountStatus.ACTIVE.toString(), account.getStatus());
    Assert.assertEquals(AccountProcessor.TECNOCOM_CL.toString(), account.getProcessor());
    Assert.assertEquals("Debe ser la misma cuenta", prepaidUser10.getId(), account.getUserId());
    Assert.assertEquals("Debe ser la misma cuenta", accountNumber, account.getAccountNumber());
  }

  @Test
  public void insertAccount_alreadyExists() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    String accountNumber = getRandomNumericString(15);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), accountNumber);

    Assert.assertNotNull(account.getId());
    Assert.assertNotNull(account.getUuid());
    Assert.assertEquals(AccountStatus.ACTIVE.toString(), account.getStatus());
    Assert.assertEquals(AccountProcessor.TECNOCOM_CL.toString(), account.getProcessor());
    Assert.assertEquals("Debe ser la misma cuenta", prepaidUser10.getId(), account.getUserId());
    Assert.assertEquals("Debe ser la misma cuenta", accountNumber, account.getAccountNumber());

    // Se inserta cuenta existente
    {
      Account a = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), accountNumber);
      Assert.assertEquals("Debe ser la misma cuenta", account.getId(), a.getId());
      Assert.assertEquals("Debe ser la misma cuenta", account.getUuid(), a.getUuid());
      Assert.assertEquals("Debe ser la misma cuenta", account.getStatus(), a.getStatus());
      Assert.assertEquals("Debe ser la misma cuenta", account.getProcessor(), a.getProcessor());
      Assert.assertEquals("Debe ser la misma cuenta", account.getUserId(), a.getUserId());
      Assert.assertEquals("Debe ser la misma cuenta", account.getAccountNumber(), a.getAccountNumber());
      Assert.assertEquals("Debe ser la misma cuenta", account.getCreatedAt(), a.getCreatedAt());
      Assert.assertEquals("Debe ser la misma cuenta", account.getUpdatedAt(), a.getUpdatedAt());
    }
  }
}
