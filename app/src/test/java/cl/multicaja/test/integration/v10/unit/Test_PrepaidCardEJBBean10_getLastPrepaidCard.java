package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_getLastPrepaidCard extends TestBaseUnit {

  @Test
  public void getLastPrepaidCardByUserIdAndOneOfStatus() throws Exception {

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }
  }

  @Test
  public void getLastPrepaidCardByUserId() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser10();

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
    card1.setStatus(PrepaidCardStatus.PENDING);
    createPrepaidCard10(card1);

    PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
    card2.setStatus(PrepaidCardStatus.LOCKED);
    createPrepaidCard10(card2);

    PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
    card3.setStatus(PrepaidCardStatus.ACTIVE);
    createPrepaidCard10(card3);

    PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserId(null, prepaidUser.getId());

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
