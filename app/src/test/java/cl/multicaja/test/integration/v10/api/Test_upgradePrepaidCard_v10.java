package cl.multicaja.test.integration.v10.api;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

public class Test_upgradePrepaidCard_v10 extends TestBaseUnitApi {

  private HttpResponse upgradePrepaidCard(Long userIdMc, String accountId) {
    HttpResponse respHttp = apiPUT(String.format("/1.0/prepaid/%s/account/%s/upgrade_card", userIdMc, accountId), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn201_PrepaidCardUpgraded_Ok() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);
    prepaidUser10.setUserLevel(PrepaidUserLevel.LEVEL_1);
    prepaidUser10.setDocumentType(TipoDocumento.RUT);
    prepaidUser10 = getPrepaidUserEJBBean10().createUser(null, prepaidUser10);

    // Crea cuenta/contrato
    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), getRandomNumericString(15));

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser10);
    prepaidCard = createPrepaidCard10(prepaidCard);
    prepaidCard.setHashedPan(getRandomString(20));
    prepaidCard.setAccountId(account.getId());
    getPrepaidCardEJBBean11().updatePrepaidCard(null, prepaidCard.getId(), Long.MAX_VALUE, prepaidCard);
    prepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());

    HttpResponse lockResp = upgradePrepaidCard(prepaidUser10.getUserIdMc(), account.getUuid());
    Assert.assertEquals("status 200", 200, lockResp.getStatus());

    // Revisar que existan el evento de tarjeta cerrada en kafka
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CLOSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidCard.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo card id", prepaidCard.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", prepaidCard.getPan(), cardEvent.getCard().getPan());

    // Revisar que existan el evento de tarjeta creada en kafka
    qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CREATED_TOPIC);
    event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidCard.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta cerrada event", event.getData());

    cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo card id", prepaidCard.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", prepaidCard.getPan(), cardEvent.getCard().getPan());

    // Revisar que el usuario haya cambiado su nivel a 2
    PrepaidUser10 storedPrepaidUser = getPrepaidUserEJBBean10().findById(null, prepaidUser10.getId());
    Assert.assertEquals("Debe tener nivel 2", PrepaidUserLevel.LEVEL_2, storedPrepaidUser.getUserLevel());
  }

  @Test
  public void shouldReturn404_UserNoExiste() {
    HttpResponse resp = upgradePrepaidCard(Long.MAX_VALUE, getRandomNumericString(10));

    Assert.assertEquals("status 404", 404, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_AccountNoExiste() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);
    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = upgradePrepaidCard(user.getId(), getRandomNumericString(10));

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", CUENTA_NO_EXISTE.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);
    prepaidUser10.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = upgradePrepaidCard(user.getId(), getRandomNumericString(10));

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidUserAlreadyLevel2() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);
    prepaidUser10.setUserLevel(PrepaidUserLevel.LEVEL_2);
    prepaidUser10.setDocumentType(TipoDocumento.RUT);
    prepaidUser10 = getPrepaidUserEJBBean10().createUser(null, prepaidUser10);

    // Crea cuenta/contrato
    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), getRandomNumericString(15));

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser10);
    prepaidCard = createPrepaidCard10(prepaidCard);
    prepaidCard.setHashedPan(getRandomString(20));
    prepaidCard.setAccountId(account.getId());
    getPrepaidCardEJBBean11().updatePrepaidCard(null, prepaidCard.getId(), Long.MAX_VALUE, prepaidCard);
    prepaidCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard.getId());

    HttpResponse resp = upgradePrepaidCard(user.getId(), account.getUuid());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102017", CLIENTE_YA_TIENE_NIVEL_2.getValue(), errorObj.get("code"));
  }
}
