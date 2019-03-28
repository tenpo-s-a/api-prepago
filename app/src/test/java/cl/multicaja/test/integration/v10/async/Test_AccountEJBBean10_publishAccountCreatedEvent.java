package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_AccountEJBBean10_publishAccountCreatedEvent extends TestBaseUnitAsync {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.%s cascade", getSchema(), "prp_cuenta"));
  }

  @Test
  public void publishAccountCreatedEvent() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = getAccountEJBBean10().insertAccount(prepaidUser10.getId(), getRandomNumericString(15));

    getAccountEJBBean10().publishAccountCreatedEvent(prepaidUser10.getUserIdMc(), account);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.ACCOUNT_CREATED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger().getMessage(qResp, account.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    AccountEvent accountEvent = getJsonParser().fromJson(event.getData(), AccountEvent.class);

    Assert.assertNotNull("Debe tener uuid", accountEvent.getAccount().getId());
    Assert.assertEquals("Debe tener status", AccountStatus.ACTIVE.toString(), accountEvent.getAccount().getStatus());
    Assert.assertEquals("Debe tener mimsia fecha de creacion", account.getCreatedAt(), accountEvent.getAccount().getTimestamps().getCreatedAt());
    Assert.assertEquals("Debe tener mimsia fecha de actualizacion", account.getUpdatedAt(), accountEvent.getAccount().getTimestamps().getUpdatedAt());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc(), numberUtils.toLong(accountEvent.getUserId()));
  }

  @Test(expected = BadRequestException.class)
  public void publishAccountCreatedEvent_null() throws Exception {

    try {
      getAccountEJBBean10().publishAccountCreatedEvent(Long.MAX_VALUE, null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishAccountCreatedEvent_userId_null() throws Exception {
    Account account = new Account();
    try {
      getAccountEJBBean10().publishAccountCreatedEvent(null, account);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }
}
