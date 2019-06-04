package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_createPrepaidCard extends TestBaseUnit {

  @Test
  public void createPrepaidCard_ok() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);
    Assert.assertNotNull("La tarjeta no debe ser null",prepaidCard10);
  }
}
