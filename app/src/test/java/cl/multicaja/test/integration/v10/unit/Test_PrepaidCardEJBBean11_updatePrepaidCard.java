package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidCardEJBBean11_updatePrepaidCard extends TestBaseUnit {

  @Test
  public void updatePrepaidCard_ok() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10Pending();
    card = createPrepaidCard10(card);
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

    getPrepaidCardEJBBean10().updatePrepaidCard(null, cardId, userId, status, card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", card, c1);
  }

  @Test(expected = BadRequestException.class)
  public void updatePrepaidCard_cardId_null() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10Pending();
    card = createPrepaidCard10(card);
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

    getPrepaidCardEJBBean10().updatePrepaidCard(null, cardId, userId, status, card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", card, c1);
  }

  @Test(expected = BadRequestException.class)
  public void updatePrepaidCard_accountId_null() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10Pending();
    card = createPrepaidCard10(card);
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

    getPrepaidCardEJBBean10().updatePrepaidCard(null, cardId, userId, status, card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", card, c1);
  }

  @Test(expected = ValidationException.class)
  public void updatePrepaidCard_card_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, Long.MAX_VALUE, Long.MAX_VALUE, null);
    } catch(ValidationException vex) {

    }
  }

  @Test(expected = ValidationException.class)
  public void updatePrepaidCard_card_doesnotExists() throws Exception {
    PrepaidCard10 card = buildPrepaidCard10Pending();
    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, Long.MAX_VALUE, Long.MAX_VALUE, card);
    } catch(ValidationException vex) {

    }
  }
}
