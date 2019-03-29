package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.TARJETA_NO_EXISTE;

public class Test_PrepaidCardEJBBean10_publishCardCreatedEvent extends TestBaseUnitAsync {

  @Test
  public void publishCardCreatedEvent() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 card = buildPrepaidCard10(prepaidUser10);
    card = createPrepaidCard10(card);

    getPrepaidCardEJBBean10().publishCardCreatedEvent(card.getId());

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CREATED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger().getMessage(qResp, card.getProcessorUserId());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo id", card.getId().toString(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", card.getProcessorUserId(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", card.getIdUser().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", card.getPan(), cardEvent.getCard().getPan());
  }

  @Test(expected = BadRequestException.class)
  public void publishCardCreatedEvent_idNull() throws Exception {

    getPrepaidCardEJBBean10().publishCardCreatedEvent(null);

    try {
      getPrepaidCardEJBBean10().publishCardCreatedEvent(null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = ValidationException.class)
  public void publishCardCreatedEvent_cardNull() throws Exception {

    try {
      getPrepaidCardEJBBean10().publishCardCreatedEvent(Long.MAX_VALUE);
      Assert.fail("Should not be here");
    } catch (ValidationException vex) {
      Assert.assertEquals("Error de parametro faltante",TARJETA_NO_EXISTE.getValue(), vex.getCode());
      throw vex;
    }
  }
}