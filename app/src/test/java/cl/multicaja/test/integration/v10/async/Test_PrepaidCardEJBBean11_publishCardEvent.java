package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.Queue;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.TARJETA_NO_EXISTE;

public class Test_PrepaidCardEJBBean11_publishCardEvent extends TestBaseUnitAsync {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_cuenta cascade", getSchema()));
  }

  @Test
  public void publishCardEvent() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 card = buildPrepaidCard10(prepaidUser10);
    card = createPrepaidCard10(card);

    // Crea cuenta/contrato
    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), getRandomNumericString(15));

    // Actualiza la tarjeta
    String pan = getRandomNumericString(16);
    String encryptedPan = getRandomString(20);
    String hashedPan = getRandomString(20);
    PrepaidCardStatus cardStatus = PrepaidCardStatus.ACTIVE;
    Integer cardExpiration = 1023;
    String nameOnCard = getRandomString(20);
    String producto = getRandomNumericString(2);
    String numeroUnico = getRandomNumericString(8);

    card.setStatus(cardStatus);
    card.setExpiration(cardExpiration);
    card.setNameOnCard(nameOnCard);
    card.setPan(pan);
    card.setEncryptedPan(encryptedPan);
    card.setHashedPan(hashedPan);
    card.setProducto(producto);
    card.setNumeroUnico(numeroUnico);
    card.setAccountId(account.getId());

    getPrepaidCardEJBBean11().updatePrepaidCard(null, card.getId(), Long.MAX_VALUE, card);

    card = getPrepaidCardEJBBean11().getPrepaidCardById(null, card.getId());

    // Revisar que envia a tarjeta creada
    getPrepaidCardEJBBean11().publishCardEvent(prepaidUser10.getUserIdMc().toString(), account.getUuid(), card.getId(), KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CREATED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, card.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo id", card.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", card.getPan(), cardEvent.getCard().getPan());

    // Revisar que envia a tarjeta cerrada
    getPrepaidCardEJBBean11().publishCardEvent(prepaidUser10.getUserIdMc().toString(), account.getUuid(), card.getId(), KafkaEventsRoute10.SEDA_CARD_CLOSED_EVENT);

    qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CLOSED_TOPIC);
    event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, card.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo id", card.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", card.getPan(), cardEvent.getCard().getPan());
  }

  @Test(expected = BadRequestException.class)
  public void publishCardEvent_externalUserIdNull() throws Exception {
    try {
      getPrepaidCardEJBBean11().publishCardEvent(null, null, null, KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishCardEvent_accountIdNull() throws Exception {
    try {
      getPrepaidCardEJBBean11().publishCardEvent(getRandomString(5), null, null, KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishCardEvent_cardIdNull() throws Exception {
    try {
      getPrepaidCardEJBBean11().publishCardEvent(getRandomString(5), getRandomString(5), null, KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = ValidationException.class)
  public void publishCardEvent_cardDoesNotExist() throws Exception {
    try {
      getPrepaidCardEJBBean11().publishCardEvent(getRandomString(5), getRandomString(5), Long.MAX_VALUE, KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);
      Assert.fail("Should not be here");
    } catch (ValidationException vex) {
      Assert.assertEquals("Error de parametro faltante",TARJETA_NO_EXISTE.getValue(), vex.getCode());
      throw vex;
    }
  }

}
