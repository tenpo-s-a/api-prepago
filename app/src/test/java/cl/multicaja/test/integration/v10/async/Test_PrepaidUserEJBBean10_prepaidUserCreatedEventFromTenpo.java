package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.dao.UserDao;
import cl.multicaja.prepaid.model.v10.PrepaidUser11;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import javax.jms.Queue;

public class Test_PrepaidUserEJBBean10_prepaidUserCreatedEventFromTenpo extends TestBaseUnitAsync {

  private UserDao userDao = new UserDao();

  @After
  @Before
  public void clearDataBefore(){
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", SCHEMA));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", SCHEMA));
  }


  @Test
  public void listenPrepaidUserCreateEventWithProcessor() throws Exception{
    userDao.setEm(createEntityManager());

    PrepaidUser11 userToCreate = buildPrepaidUser11();

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setDocumentNumber(userToCreate.getRut().toString());
    userEventSend.setFirstName(userToCreate.getName());
    userEventSend.setId(userToCreate.getUuid());
    userEventSend.setLastName(userToCreate.getLastName());
    userEventSend.setLevel(userToCreate.getLevel());
    userEventSend.setState(userToCreate.getStatus().toString());

    String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
    Assert.assertNotNull("No es nulo", messageId);
    Assert.assertNotEquals("No es cero",0,messageId);

    Thread.sleep(1000);

    //Find if data was saved on DataBase
    PrepaidUser11 userFound = findPrepaidUserV11(null,userEventSend.getId(), null);

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userToCreate.getUuid(),userFound.getUuid());
    Assert.assertEquals("Igual",userToCreate.getRut(),userFound.getRut());
    Assert.assertEquals("Igual",userToCreate.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreate.getName(),userFound.getName());
    Assert.assertEquals("Igual",userToCreate.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userToCreate.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userToCreate.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreate.getLevel(),userFound.getLevel());

  }



}
