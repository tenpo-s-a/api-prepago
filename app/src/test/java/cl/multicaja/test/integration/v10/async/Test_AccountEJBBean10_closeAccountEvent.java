package cl.multicaja.test.integration.v10.async;

import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.junit.*;

public class Test_AccountEJBBean10_closeAccountEvent extends TestBaseUnitAsync{

  @Before
  @After
  public  void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_cuenta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario cascade", getSchema()));
  }

  @Test
  public void listenUpdateEventCloseAccount() throws Exception{

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    Assert.assertNotNull("No debe ser null",prepaidUser);
    Assert.assertNotNull("No debe ser null",prepaidUser.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,prepaidUser.getId().longValue());
    Assert.assertNotNull("Uuid no debe ser null",prepaidUser.getUuid());

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setDocumentNumber(prepaidUser.getDocumentNumber());
    userEventSend.setFirstName(prepaidUser.getName());
    userEventSend.setId(prepaidUser.getUuid());
    userEventSend.setLastName(prepaidUser.getLastName());
    userEventSend.setLevel(prepaidUser.getUserLevel().toString());
    userEventSend.setState(PrepaidUserStatus.CLOSED.name());

    String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_UPDATE_EVENT,userEventSend);

    Thread.sleep(2000);

    //Find if data was saved on DataBase
    PrepaidUser10 userFound = getPrepaidUserEJBBean10().findByExtId(null,userEventSend.getId());
    Assert.assertNotNull("PrepaidUser no es null", userFound);

    Account accountFound = getAccountEJBBean10().findByUserId(userFound.getId());
    Assert.assertNotNull("Account no es null", accountFound);

    PrepaidCard10 cardFound = getPrepaidCardEJBBean11().getPrepaidCardByAccountId(account.getId());
    Assert.assertNotNull("PrepaidCard no es null", cardFound);

    Assert.assertEquals("PrepaidUser Status debe ser Closed", PrepaidUserStatus.CLOSED, userFound.getStatus());
    Assert.assertEquals("PrepaidUser UUID iguales",prepaidUser.getUuid(), userFound.getUuid());

    Assert.assertEquals("Account Status debe ser Closed", AccountStatus.CLOSED, accountFound.getStatus());
    Assert.assertEquals("Account UUID iguales",account.getUuid(), accountFound.getUuid());

    Assert.assertEquals("Card Status debe ser Closed", PrepaidCardStatus.CLOSED, cardFound.getStatus());
    Assert.assertEquals("Card UUID iguales",prepaidCard.getUuid(), cardFound.getUuid());
  }

}
