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

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      card1.setAccountId(account.getId());
      createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      card2.setAccountId(account.getId());
      createPrepaidCardV2(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.ACTIVE);
      card3.setAccountId(account.getId());
      createPrepaidCardV2(card3);

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


      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      card1.setAccountId(account.getId());
      createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      card2.setAccountId(account.getId());
      createPrepaidCardV2(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.PENDING);
      card3.setAccountId(account.getId());
      createPrepaidCardV2(card3);

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


      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      card1.setAccountId(account.getId());
      createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.PENDING);
      card2.setAccountId(account.getId());
      createPrepaidCardV2(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.LOCKED);
      card3.setAccountId(account.getId());
      createPrepaidCardV2(card3);

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


    PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
    card1.setStatus(PrepaidCardStatus.PENDING);
    card1.setAccountId(account.getId());
    createPrepaidCardV2(card1);

    PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
    card2.setStatus(PrepaidCardStatus.LOCKED);
    card2.setAccountId(account.getId());
    createPrepaidCardV2(card2);

    PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
    card3.setStatus(PrepaidCardStatus.ACTIVE);
    card3.setAccountId(account.getId());
    createPrepaidCardV2(card3);

    PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountId(null, account.getId());

    Assert.assertNotNull("debe existir", prepaidCard);
    Assert.assertEquals("debe ser igual a la ultima creada", card3, prepaidCard);
  }

  @Test
  public void checkFormattedExpiration() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser10();

    PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);

    card1.setExpiration(201812);
    Assert.assertEquals("debe tener formato MM/yyyy", "12/2018", card1.getFormattedExpiration());

    //se verifica este caso, pero si ocurre significa que la expiracion es invalida
    card1.setExpiration(20181);
    Assert.assertEquals("debe tener formato M/yyyy", "1/2018", card1.getFormattedExpiration());

    //se verifica este caso, pero si ocurre significa que la expiracion es invalida
    card1.setExpiration(2018);
    Assert.assertEquals("debe tener formato yyyy", "2018", card1.getFormattedExpiration());
  }
}
