package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_getLastPrepaidCard extends TestBaseUnit {

  @Test
  public void getLastPrepaidCardByUserIdAndOneOfStatus() throws Exception {

    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 card1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card1.setStatus(PrepaidCardStatus.PENDING);
      card1.setAccountId(account.getId());
      card1 = createPrepaidCardV2(card1);


      PrepaidCard10 card2 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      card2.setAccountId(account.getId());
      card2 =createPrepaidCardV2(card2);

      PrepaidCard10 card3 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card3.setStatus(PrepaidCardStatus.ACTIVE);
      card3.setAccountId(account.getId());
      card3 = createPrepaidCardV2(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndOneOfStatus(null, account.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());


      PrepaidCard10 card1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      card1.setAccountId(account.getId());
      card1=createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      card2.setAccountId(account.getId());
      card2=createPrepaidCardV2(card2);

      PrepaidCard10 card3 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card3.setStatus(PrepaidCardStatus.PENDING);
      card3.setAccountId(account.getId());
      card3=createPrepaidCardV2(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndOneOfStatus(null, account.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());


      PrepaidCard10 card1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      card1.setAccountId(account.getId());
      card1 = createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card2.setStatus(PrepaidCardStatus.PENDING);
      card2.setAccountId(account.getId());
      card2 = createPrepaidCardV2(card2);

      PrepaidCard10 card3 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card3.setStatus(PrepaidCardStatus.LOCKED);
      card3.setAccountId(account.getId());
      card3 = createPrepaidCardV2(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndOneOfStatus(null, account.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }
  }

  @Test
  public void getLastPrepaidCardByUserId() throws Exception {


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());


    PrepaidCard10 card1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    card1.setStatus(PrepaidCardStatus.PENDING);
    card1.setAccountId(account.getId());
    card1 = createPrepaidCardV2(card1);

    PrepaidCard10 card2 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    card2.setStatus(PrepaidCardStatus.LOCKED);
    card2.setAccountId(account.getId());
    card2 = createPrepaidCardV2(card2);

    PrepaidCard10 card3 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    card3.setStatus(PrepaidCardStatus.ACTIVE);
    card3.setAccountId(account.getId());
    card3 =createPrepaidCardV2(card3);

    PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountId(null, account.getId());

    Assert.assertNotNull("debe existir", prepaidCard);
    Assert.assertEquals("debe ser igual a la ultima creada", card3, prepaidCard);
  }

  @Test
  public void checkFormattedExpiration() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    prepaidCard10.setExpiration(201812);
    Assert.assertEquals("debe tener formato MM/yyyy", "12/2018", prepaidCard10.getFormattedExpiration());

    //se verifica este caso, pero si ocurre significa que la expiracion es invalida
    prepaidCard10.setExpiration(20181);
    Assert.assertEquals("debe tener formato M/yyyy", "1/2018", prepaidCard10.getFormattedExpiration());

    //se verifica este caso, pero si ocurre significa que la expiracion es invalida
    prepaidCard10.setExpiration(2018);
    Assert.assertEquals("debe tener formato yyyy", "2018", prepaidCard10.getFormattedExpiration());
  }
}
