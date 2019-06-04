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
public class Test_PrepaidCardEJBBean10_updatePrepaidCard extends TestBaseUnit {

  @Test
  public void updatePrepaidCard_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    card.setStatus(PrepaidCardStatus.PENDING);
    card = createPrepaidCardV2(card);
    Long cardId = card.getId();
    Long userId = card.getIdUser();
    PrepaidCardStatus status = card.getStatus();

    card.setStatus(PrepaidCardStatus.ACTIVE);
    card.setExpiration(1023);
    card.setNameOnCard(getRandomString(20));
    card.setPan(getRandomString(16));
    card.setEncryptedPan(getRandomString(20));
    card.setProducto(getRandomNumericString(2));
    card.setNumeroUnico(getRandomNumericString(8));

    getPrepaidCardEJBBean11().updatePrepaidCard(null, cardId, userId, status, card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean11().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", card, c1);
  }
}
