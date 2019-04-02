package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_AccountEJBBean10_findByUserIdAndAccountNumber extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_cuenta cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void findByUserIdAndAccountNumber_userId_null() throws Exception {
    try {
      getAccountEJBBean10().findByUserIdAndAccountNumber(null, getRandomNumericString(20));
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void findByUserIdAndAccountNumber_accountNumber_null() throws Exception {
    try {
      getAccountEJBBean10().findByUserIdAndAccountNumber(Long.MAX_VALUE, null);
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void findByUserIdAndAccountNumber_accountNumber_empty() throws Exception {
    try {
      getAccountEJBBean10().findByUserIdAndAccountNumber(Long.MAX_VALUE, "");
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void findByUserIdAndAccountNumber_account_null() throws Exception {
    try {
      Account account = getAccountEJBBean10().findByUserIdAndAccountNumber(Long.MAX_VALUE, getRandomNumericString(20));
      Assert.assertNull("No debe existir la cuenta", account);
    } catch(ValidationException vex) {
      Assert.fail("Should not be here");
    }
  }

  @Test
  public void findByUserIdAndAccountNumber() throws Exception {
    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    String accountNumber = getRandomNumericString(20);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), accountNumber);

    Account dbAccount = getAccountEJBBean10().findByUserIdAndAccountNumber(prepaidUser10.getId(), accountNumber);

    Assert.assertEquals("Debe ser la misma tarjeta", account.getId(), dbAccount.getId());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getUuid(), dbAccount.getUuid());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getUserId(), dbAccount.getUserId());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getProcessor(), dbAccount.getProcessor());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getStatus(), dbAccount.getStatus());
    Assert.assertEquals("Debe ser la misma tarjeta", account.getAccountNumber(), dbAccount.getAccountNumber());
  }

  @Test
  public void findByUserIdAndAccountNumber_notMatch() throws Exception {
    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    String accountNumber = getRandomNumericString(20);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), accountNumber);

    {
      Account acc = getAccountEJBBean10().findByUserIdAndAccountNumber(Long.MAX_VALUE, accountNumber);
      Assert.assertNull("No debe existir la cuenta", acc);
    }
    {
      Account acc = getAccountEJBBean10().findByUserIdAndAccountNumber(prepaidUser10.getId(), getRandomNumericString(20));
      Assert.assertNull("No debe existir la cuenta", acc);
    }
  }
}
