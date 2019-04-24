package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.junit.*;

public class Test_AccountEJBBean10_closeAccount extends TestBaseUnit {

  @Before
  @After
  public  void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_cuenta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario cascade", getSchema()));
  }

  @Test
  public void testCloseAccountOK() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard = createPrepaidCardV2(prepaidCard);


    Assert.assertNotNull("PrepaidUser no es null", prepaidUser);
    Assert.assertNotNull("Account no es null", account);
    Assert.assertNotNull("PrepaidCard no es null", prepaidCard);
    Assert.assertEquals("PrepaidUser Status debe ser Active", PrepaidUserStatus.ACTIVE, prepaidUser.getStatus());
    Assert.assertEquals("Account Status debe ser Active", AccountStatus.ACTIVE, account.getStatus());
    Assert.assertEquals("Card Status debe ser Active", PrepaidCardStatus.ACTIVE, prepaidCard.getStatus());

    getAccountEJBBean10().closeAccount(prepaidUser.getUuid());

    PrepaidUser10 userFound = getPrepaidUserEJBBean10().findByExtId(null,prepaidUser.getUuid());
    Assert.assertNotNull("PrepaidUser no es null", userFound);

    Account accountFound = getAccountEJBBean10().findByUserId(userFound.getId());
    Assert.assertNotNull("Account no es null", accountFound);

    PrepaidCard10 cardFound = getPrepaidCardEJBBean11().getPrepaidCardByAccountId(account.getId());
    Assert.assertNotNull("PrepaidCard no es null", cardFound);

    Assert.assertEquals("PrepaidUser Status debe ser Closed", PrepaidUserStatus.CLOSED, userFound.getStatus());
    Assert.assertEquals("PrepaidUser UUID iguales",prepaidUser.getUuid(), userFound.getUuid());

    Assert.assertEquals("Account Status debe ser Closed", AccountStatus.CLOSED, accountFound.getStatus());
    Assert.assertEquals("Account UUID iguales",account.getUuid(), accountFound.getUuid());

    Assert.assertEquals("Card Status debe ser Closed", PrepaidCardStatus.CLOSED, cardFound.getStatus());
    Assert.assertEquals("Card UUID iguales",prepaidCard.getUuid(), cardFound.getUuid());

  }

  @Test(expected = ValidationException.class)
  public void testCloseAccountUserNoExist() throws Exception {
    try{
      getAccountEJBBean10().closeAccount(getRandomString(10));
      Assert.fail("No debe caer aca");
    }catch (ValidationException ex){
      Assert.assertEquals("Error debe ser",Errors.CLIENTE_NO_TIENE_PREPAGO.getValue(),ex.getCode());
      throw new ValidationException(Errors.ERROR_INDETERMINADO);
    }
  }

  @Test(expected = ValidationException.class)
  public void testCloseAccountAccountNoExist() throws Exception {
    try{
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      getAccountEJBBean10().closeAccount(prepaidUser.getUuid());
      Assert.fail("No debe caer aca");
    }catch (ValidationException ex){
      Assert.assertEquals("Error debe ser",Errors.CUENTA_NO_EXISTE.getValue(),ex.getCode());
      throw new ValidationException(Errors.ERROR_INDETERMINADO);
    }
  }

  @Test(expected = ValidationException.class)
  public void testCloseAccountCardNotExist() throws Exception {
    try{
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      getAccountEJBBean10().closeAccount(prepaidUser.getUuid());
      Assert.fail("No debe caer aca");
    }catch (ValidationException ex){
      Assert.assertEquals("Error debe ser",Errors.TARJETA_NO_EXISTE.getValue(),ex.getCode());
      throw new ValidationException(Errors.ERROR_INDETERMINADO);
    }
  }

}
