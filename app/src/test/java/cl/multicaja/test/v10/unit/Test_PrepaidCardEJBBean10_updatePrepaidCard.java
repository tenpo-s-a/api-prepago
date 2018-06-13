package cl.multicaja.test.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_updatePrepaidCard extends TestBaseUnit {

  @Test
  public void updatePrepaidCard_ok() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10Pending();
    card = createPrepaidCard10(card);
    Long cardId = card.getId();
    Long userId = card.getIdUser();
    PrepaidCardStatus state = card.getStatus();

    card.setStatus(PrepaidCardStatus.ACTIVE);
    card.setExpiration(1023);
    card.setNameOnCard(getRandomString(20));
    card.setPan(getRandomString(16));
    card.setEncryptedPan(getRandomString(20));
    card.setProducto(getRandomNumericString(2));
    card.setNumeroUnico(getRandomNumericString(8));

    boolean updated = getPrepaidCardEJBBean10().updatePrepaidCard(null,cardId,userId,state,card);

    Assert.assertTrue("La tarjeta be haber sido actualizada, debe ser true", updated);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", card, c1);
  }
}
