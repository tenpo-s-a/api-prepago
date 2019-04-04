package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @author abarazarte
 **/
public class Test_ProductChangeRoute10 extends TestBaseUnitAsync {

  @Test
  public void productChangeRetryCount4() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    String messageId = sendPendingProductChange(prepaidUser, null, prepaidCard, tipoAlta,4);

    //se verifica que el mensaje haya sido procesado
    Queue qResp = camelFactory.createJMSQueue(ProductChangeRoute10.ERROR_PRODUCT_CHANGE_RESP);
    ExchangeData<PrepaidProductChangeData10> data = (ExchangeData<PrepaidProductChangeData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un reverse", data);
    Assert.assertNotNull("Deberia existir un reverse", data.getData());

    ProcessorMetadata lastProcessorMetadata = data.getLastProcessorMetadata();
    String endpoint = ProductChangeRoute10.ERROR_PRODUCT_CHANGE_REQ;

    Assert.assertEquals("debe ser intento 5", 5, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  @Test
  public void productChange_AlreadyChanged() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setUserLevel(PrepaidUserLevel.LEVEL_2);
    prepaidUser.setDocumentType(TipoDocumento.RUT);

    prepaidUser = getPrepaidUserEJBBean10().createUser(null, prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    String messageId = sendPendingProductChange(prepaidUser, null, prepaidCard, TipoAlta.NIVEL2,0);

    //se verifica que el mensaje haya sido procesado
    Queue qResp = camelFactory.createJMSQueue(ProductChangeRoute10.PENDING_PRODUCT_CHANGE_RESP);
    ExchangeData<PrepaidProductChangeData10> data = (ExchangeData<PrepaidProductChangeData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje", data);
    Assert.assertNotNull("Deberia existir un mensaje", data.getData());

    Assert.assertEquals("Debe fallar contener el mensaje de error", "MPA0928 - EL NUEVO PRODUCTO DEBE SER DIFERENTE AL ANTERIOR", data.getData().getMsjError());
  }

  @Test
  public void productChange_ChangeOk() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setUserLevel(PrepaidUserLevel.LEVEL_1);
    prepaidUser.setDocumentType(TipoDocumento.RUT);
    prepaidUser = getPrepaidUserEJBBean10().createUser(null, prepaidUser);

    // Crea cuenta/contrato
    Account account = getAccountEJBBean10().insertAccount(prepaidUser.getId(), getRandomNumericString(15));

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);
    prepaidCard.setHashedPan(getRandomString(20));
    prepaidCard.setAccountId(account.getId());
    getPrepaidCardEJBBean11().updatePrepaidCard(null, prepaidCard.getId(), Long.MAX_VALUE, prepaidCard);
    prepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());

    String messageId = sendPendingProductChange(prepaidUser, account, prepaidCard, TipoAlta.NIVEL2,0);

    //se verifica que el mensaje haya sido procesado
    Queue qResp = camelFactory.createJMSQueue(ProductChangeRoute10.PENDING_PRODUCT_CHANGE_RESP);
    ExchangeData<PrepaidProductChangeData10> data = (ExchangeData<PrepaidProductChangeData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un mensaje", data);
    Assert.assertNotNull("Deberia existir un mensaje", data.getData());

    Assert.assertNull("No debe tener numero de error", data.getData().getNumError());
    Assert.assertNull("No debe tener mensaje de error", data.getData().getMsjError());

    // Revisar que el usuario haya cambiado su nivel a 2
    PrepaidUser10 storedPrepaidUser = getPrepaidUserEJBBean10().findById(null, prepaidUser.getId());
    Assert.assertEquals("Debe tener nivel 2", PrepaidUserLevel.LEVEL_2, storedPrepaidUser.getUserLevel());

    // Revisar que la tarjeta este cerrada
    PrepaidCard10 storedCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());
    Assert.assertEquals("La tarjeta original debe estar cerrada", PrepaidCardStatus.LOCKED_HARD, storedCard.getStatus());

    // Revisar que existan el evento de tarjeta cerrada en kafka
    qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CLOSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidCard.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo card id", prepaidCard.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", prepaidCard.getPan(), cardEvent.getCard().getPan());

    // Revisar que exista una nueva tarjeta con estado activo para este usuario
    PrepaidCard10 newStoredCard = getPrepaidCardEJBBean11().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Debe existir una nueva tarjeta con estado ACTIVE", newStoredCard);

    newStoredCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, newStoredCard.getId());

    // Revisar que existan el evento de tarjeta creada en kafka
    qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CREATED_TOPIC);
    event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, newStoredCard.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event.getData());

    cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo card id", newStoredCard.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", newStoredCard.getPan(), cardEvent.getCard().getPan());
  }
}
