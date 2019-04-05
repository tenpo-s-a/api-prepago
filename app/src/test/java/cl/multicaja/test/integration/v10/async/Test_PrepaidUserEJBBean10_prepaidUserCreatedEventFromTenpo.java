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
  public void listenPrepaidUserCreateEvent() throws Exception{

    final String topicName = "USER_CREATED_TEST";

    PrepaidUser11 userToCreate = buildPrepaidUser11();

    cl.multicaja.prepaid.kafka.events.model.User userSent = new cl.multicaja.prepaid.kafka.events.model.User();

    userSent.setDocumentNumber(userToCreate.getRut().toString());
    userSent.setFirstName(userToCreate.getName());
    userSent.setId(userToCreate.getUiid());
    userSent.setLastName(userToCreate.getLastName());
    userSent.setLevel(userToCreate.getLevel());
    userSent.setState(userToCreate.getStatus().toString());

    String messageId = sendUserCreatedOrUpdated(topicName,userSent,0);

    Queue qResp = camelFactory.createJMSQueue(topicName);
    ExchangeData<String> userEventExchangeData = (ExchangeData<String>)camelFactory.createJMSMessenger().
      getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un evento de usuario creado", userEventExchangeData);
    Assert.assertNotNull("Deberia existir un evento de usuario creado data", userEventExchangeData.getData());

    cl.multicaja.prepaid.kafka.events.model.User userResponse = getJsonParser().
      fromJson(userEventExchangeData.getData(),cl.multicaja.prepaid.kafka.events.model.User.class);

    Assert.assertNotNull("No debe ser nulo", userResponse);
    Assert.assertEquals("Debe tener el mismo id", userSent.getId(), userResponse.getId());

    Assert.assertEquals("Igual", userSent.getDocumentNumber(), userResponse.getDocumentNumber());
    Assert.assertEquals("Igual", userSent.getFirstName(), userResponse.getFirstName());
    Assert.assertEquals("Igual", userSent.getLastName(), userResponse.getLastName());
    Assert.assertEquals("Igual", userSent.getLevel(), userResponse.getLevel());
    Assert.assertEquals("Igual", userSent.getState(), userResponse.getState());

    //Save data on DataBase
    PrepaidUser11 userToCreated = getPrepaidUserEJBBean10().createPrepaidUserV11(null,userToCreate);
    Assert.assertNotNull("No debe ser null",userToCreated);
    Assert.assertNotNull("No debe ser null",userToCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userToCreated.getId().longValue());

    //Find if data was saved on DataBase
    PrepaidUser11 userFound = findPrepaidUserV11(null,userToCreated.getUiid(),null);
    Assert.assertEquals("Igual",userToCreated.getId(),userFound.getId());
    Assert.assertEquals("Igual",userToCreated.getUiid(),userFound.getUiid());
    Assert.assertEquals("Igual",userToCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userToCreated.getLastName(),userFound.getLastName());
    Assert.assertEquals("Igual",userToCreated.getDocumentNumber(),userFound.getDocumentNumber());
    Assert.assertEquals("Igual",userToCreated.getLevel(),userFound.getLevel());
    Assert.assertEquals("Igual",userToCreated.getCreatedAt(),userFound.getCreatedAt());
    Assert.assertEquals("Igual",userToCreated.getUpdatedAt(),userFound.getUpdatedAt());
    Assert.assertEquals("Igual",userToCreated.getRut(),userFound.getRut());

  }


  @Test
  public void listenPrepaidUserCreateEventWithProcessor() throws Exception{
    userDao.setEm(createEntityManager());

    PrepaidUser11 userToCreate = buildPrepaidUser11();

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setDocumentNumber(userToCreate.getRut().toString());
    userEventSend.setFirstName(userToCreate.getName());
    userEventSend.setId(userToCreate.getUiid());
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

    Assert.assertEquals("Igual",userToCreate.getUiid(),userFound.getUiid());
    Assert.assertEquals("Igual",userToCreate.getRut(),userFound.getRut());
    Assert.assertEquals("Igual",userToCreate.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreate.getName(),userFound.getName());
    Assert.assertEquals("Igual",userToCreate.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userToCreate.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userToCreate.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreate.getLevel(),userFound.getLevel());

  }



}
