package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.TARJETA_NO_EXISTE;

public class Test_PrepaidCardEJBBean11_updatePrepaidCard extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_tarjeta cascade", getSchema()));
  }

  @Test
  public void updatePrepaidCard_ok() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10Pending();
    card = createPrepaidCard10(card);

    Long cardId = card.getId();

    String pan = getRandomNumericString(16);
    String encryptedPan = getRandomString(20);
    String hashedPan = getRandomString(20);
    PrepaidCardStatus cardStatus = PrepaidCardStatus.LOCKED_HARD;
    Integer cardExpiration = 1023;
    String nameOnCard = getRandomString(20);
    String producto = getRandomNumericString(2);
    String numeroUnico = getRandomNumericString(8);

    card.setIdUser(Long.MAX_VALUE);
    card.setStatus(cardStatus);
    card.setExpiration(cardExpiration);
    card.setNameOnCard(nameOnCard);
    card.setPan(pan);
    card.setEncryptedPan(encryptedPan);
    card.setHashedPan(hashedPan);
    card.setProducto(producto);
    card.setNumeroUnico(numeroUnico);

    getPrepaidCardEJBBean11().updatePrepaidCard(null, cardId, Long.MAX_VALUE, card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean11().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", pan, c1.getPan());
    Assert.assertEquals("la tarjeta debe estar actualizada", encryptedPan, c1.getEncryptedPan());
    Assert.assertEquals("la tarjeta debe estar actualizada", hashedPan, c1.getHashedPan());
    Assert.assertEquals("la tarjeta debe estar actualizada", cardStatus, c1.getStatus());
    Assert.assertEquals("la tarjeta debe estar actualizada", cardExpiration, c1.getExpiration());
    Assert.assertEquals("la tarjeta debe estar actualizada", nameOnCard, c1.getNameOnCard());
    Assert.assertEquals("la tarjeta debe estar actualizada", producto, c1.getProducto());
    Assert.assertEquals("la tarjeta debe estar actualizada", numeroUnico, c1.getNumeroUnico());
    Assert.assertNotNull("la tarjeta debe estar actualizada", c1.getUuid());
  }

  @Test(expected = BadRequestException.class)
  public void updatePrepaidCard_cardId_null() throws Exception {
    PrepaidCard10 card = buildPrepaidCard10Pending();

    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, null, Long.MAX_VALUE, card);
    } catch(ValidationException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void updatePrepaidCard_accountId_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, Long.MAX_VALUE, null, null);
    } catch(ValidationException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void updatePrepaidCard_card_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, Long.MAX_VALUE, Long.MAX_VALUE, null);
    } catch(ValidationException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void updatePrepaidCard_card_idNull() throws Exception {
    PrepaidCard10 card = buildPrepaidCard10Pending();
    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, Long.MAX_VALUE, Long.MAX_VALUE, card);
    } catch(ValidationException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = ValidationException.class)
  public void updatePrepaidCard_card_doesNotExists() throws Exception {
    PrepaidCard10 card = buildPrepaidCard10Pending();
    card.setId(Long.MAX_VALUE);
    try {
      getPrepaidCardEJBBean11().updatePrepaidCard(null, Long.MAX_VALUE, Long.MAX_VALUE, card);
    } catch(ValidationException vex) {
      Assert.assertEquals(TARJETA_NO_EXISTE.getValue(), vex.getCode());
      throw vex;
    }
  }
}
