package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.dao.UserDao;
import cl.multicaja.prepaid.kafka.events.UserEvent;
import cl.multicaja.prepaid.model.v10.PrepaidUser11;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.prepaid.model.v11.User;
import cl.multicaja.prepaid.model.v11.UserStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Queue;

public class Test_PrepaidUserEJBBean10_prepaidUserUpdatedEventFromTenpo extends TestBaseUnitAsync {

  private UserDao userDao = new UserDao();

  @Before
  @After
  public void clearDataBefore(){
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", SCHEMA));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", SCHEMA));
  }

  @Test
  public void listenPrepaidUserUpdateEvent() throws Exception{

    final String topicName = "USER_CREATED_TEST";

    userDao.setEm(createEntityManager());

    PrepaidUser11 userToCreate = buildPrepaidUser11();
    PrepaidUser11 userCreated = getPrepaidUserEJBBean10().createPrepaidUserV11(null,userToCreate);
    Assert.assertNotNull("No debe ser null",userCreated);
    Assert.assertNotNull("No debe ser null",userCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userCreated.getId().longValue());


    cl.multicaja.prepaid.kafka.events.model.User userSent = new cl.multicaja.prepaid.kafka.events.model.User();

    userSent.setDocumentNumber(userCreated.getDocumentNumber());
    userSent.setFirstName(userCreated.getName());
    userSent.setId(userCreated.getUuid());
    userSent.setLastName(userCreated.getLastName());
    userSent.setLevel(userCreated.getLevel());
    userSent.setState(userCreated.getStatus().toString());

    //Data To Change for Update
    userSent.setLevel(String.valueOf(1));

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
    Assert.assertEquals("Igual", userSent.getId(), userResponse.getId());
    Assert.assertEquals("Igual", userSent.getLastName(), userResponse.getLastName());
    Assert.assertEquals("Igual", userSent.getLevel(), userResponse.getLevel());
    Assert.assertEquals("Igual", userSent.getState(), userResponse.getState());


    //Update on DataBase.
    userCreated.setDocumentNumber(userResponse.getDocumentNumber());
    userCreated.setUuid(userResponse.getId());
    userCreated.setName(userResponse.getFirstName());
    userCreated.setLastName(userResponse.getLastName());
    userCreated.setStatus(UserStatus.valueOfEnum(userResponse.getState()));
    userCreated.setLevel(userResponse.getLevel());
    userCreated.setUuid(userResponse.getId());

    PrepaidUser11 userUpdated = updatePrepaidUserV11(userCreated);

    //find on Database for validate changes
    PrepaidUser11 userFound = findPrepaidUserV11(null,userCreated.getUuid(),null);

    Assert.assertEquals("Igual",userUpdated.getId(),userFound.getId());
    Assert.assertEquals("Igual",userUpdated.getUuid(),userFound.getUuid());
    Assert.assertEquals("Igual",userUpdated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userUpdated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userUpdated.getLastName(),userFound.getLastName());
    Assert.assertEquals("Igual",userUpdated.getDocumentNumber(),userFound.getDocumentNumber());
    Assert.assertEquals("Igual",userUpdated.getLevel(),userFound.getLevel());
    Assert.assertEquals("Igual",userUpdated.getCreatedAt(),userFound.getCreatedAt());
    Assert.assertEquals("Igual",userUpdated.getUpdatedAt(),userFound.getUpdatedAt());
    Assert.assertEquals("Igual",userUpdated.getRut(),userFound.getRut());

  }

  @Test
  public void listenPrepaidUserUpdateEventWithProcessor() throws Exception{
    userDao.setEm(createEntityManager());

    PrepaidUser11 userToCreate = buildPrepaidUser11();
    PrepaidUser11 userCreated = getPrepaidUserEJBBean10().createPrepaidUserV11(null,userToCreate);
    Assert.assertNotNull("No debe ser null",userCreated);
    Assert.assertNotNull("No debe ser null",userCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userCreated.getId().longValue());

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setDocumentNumber(userCreated.getDocumentNumber());
    userEventSend.setFirstName(userCreated.getName());
    userEventSend.setId(userCreated.getUuid());
    userEventSend.setLastName(userCreated.getLastName());
    userEventSend.setLevel(userCreated.getLevel());
    userEventSend.setState(userCreated.getStatus().toString());

    //Data To Change for Update
    userEventSend.setLevel(String.valueOf(1));

    String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_UPDATED_TOPIC,userEventSend,0);

    Thread.sleep(1000);

    //Find if data was saved on DataBase
    PrepaidUser11 userFound = findPrepaidUserV11(null,userEventSend.getId(), null);

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userCreated.getRut(),userFound.getRut());
    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userCreated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userCreated.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userCreated.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userCreated.getCreatedAt(),userFound.getCreatedAt());
    Assert.assertNotEquals("No Igual",userCreated.getUpdatedAt(),userFound.getUpdatedAt());

    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userEventSend.getLevel(),userFound.getLevel());
    Assert.assertEquals("Igual",userCreated.getUuid(),userFound.getUuid());


  }

}
