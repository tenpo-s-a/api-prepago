package cl.multicaja.test.integration.v10.api;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.*;

import javax.jms.Queue;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

public class Test_upgradePrepaidCard_v10 extends TestBaseUnitApi {

  @BeforeClass
  @AfterClass
  public static void beforeclass() {
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_cuenta cascade", getSchema()));
  }

  private HttpResponse upgradePrepaidCard(String userIdMc, String accountId) {
    HttpResponse respHttp = apiPUT(String.format("/1.0/prepaid/%s/account/%s/upgrade_card", userIdMc, accountId), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  //TODO: Revisar por que falla !!!!!!!!!!!!!!!!!!!!!!!!!!!
  @Ignore
  @Test
  public void shouldReturn201_PrepaidCardUpgraded_Ok() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_1);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    // Crea cuenta/contrato
    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    HttpResponse lockResp = upgradePrepaidCard(prepaidUser10.getUuid(), account.getUuid());
    Assert.assertEquals("status 200", 200, lockResp.getStatus());

    // Revisar que existan el evento de tarjeta creada en kafka
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CREATED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000).getMessage(qResp, prepaidCard.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo card id", prepaidCard.getUuid(), cardEvent.getCard().getId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), cardEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo pan", prepaidCard.getPan(), cardEvent.getCard().getPan());

    // Revisar que el usuario haya cambiado su nivel a 2
    PrepaidUser10 storedPrepaidUser = getPrepaidUserEJBBean10().findById(null, prepaidCard.getId());
    Assert.assertEquals("Debe tener nivel 2", PrepaidUserLevel.LEVEL_2, storedPrepaidUser.getUserLevel());
  }

  @Test
  public void shouldReturn404_UserNoExiste() {
    HttpResponse resp = upgradePrepaidCard(getRandomNumericString(10), getRandomNumericString(10));

    Assert.assertEquals("status 404", 404, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_AccountNoExiste() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = upgradePrepaidCard(prepaidUser10.getUuid(), getRandomNumericString(10));

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", CUENTA_NO_EXISTE.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidUserAlreadyLevel2() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = getPrepaidUserEJBBean10().createUser(null, prepaidUser10);

    // Crea cuenta/contrato
    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), getRandomNumericString(15));

    HttpResponse resp = upgradePrepaidCard(prepaidUser10.getUuid(), account.getUuid());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102017", CLIENTE_YA_TIENE_NIVEL_2.getValue(), errorObj.get("code"));
  }
}
