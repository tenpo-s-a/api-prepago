package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_PrepaidUserEJBBean10_prepaidUserUpdatedEventFromTenpo extends TestBaseUnitAsync {

  private final Integer sleepTimerMillis = 2000;

  @Before
  @After
  public void clearDataBefore(){
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimiento CASCADE", SCHEMA));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_usuario CASCADE", SCHEMA));
  }

  @Test
  public void listenPrepaidUserUpdateEventWithProcessor() throws Exception{

    PrepaidUser10 userToCreate = buildPrepaidUser11();
    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createUser(null,userToCreate);
    Assert.assertNotNull("No debe ser null",userCreated);
    Assert.assertNotNull("No debe ser null",userCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userCreated.getId().longValue());

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setDocumentNumber(getRandomString(10));
    userEventSend.setFirstName(userCreated.getName());
    userEventSend.setId(userCreated.getUuid());
    userEventSend.setLastName(userCreated.getLastName());
    userEventSend.setLevel(userCreated.getUserLevel().toString());
    userEventSend.setState(userCreated.getStatus().toString());
    userEventSend.setPlan(userToCreate.getUserPlan().name());
    userEventSend.setTributaryIdentifier(userToCreate.getDocumentNumber());
    //Data To Change for Update
    userEventSend.setLevel(PrepaidUserLevel.LEVEL_2.name());

    String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_UPDATE_EVENT,userEventSend);

    Thread.sleep(sleepTimerMillis);

    //Find if data was saved on DataBase
    PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userCreated.getUuid(),userFound.getUuid());
    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userCreated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userCreated.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userCreated.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userEventSend.getLevel(),userFound.getUserLevel().toString());
    Assert.assertEquals("Igual",userToCreate.getUserPlan(),userFound.getUserPlan());

  }


  @Test
  public void listenPrepaidUserUpdateEventWithProcessorNotValidFields() throws Exception{
    PrepaidUser10 userToCreate = buildPrepaidUser11();
    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createUser(null,userToCreate);
    Assert.assertNotNull("No debe ser null",userCreated);
    Assert.assertNotNull("No debe ser null",userCreated.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,userCreated.getId().longValue());

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setDocumentNumber(userCreated.getDocumentNumber());
    userEventSend.setFirstName(userCreated.getName());
    userEventSend.setId(userCreated.getUuid());
    userEventSend.setLastName(userCreated.getLastName());
    userEventSend.setLevel(userCreated.getUserLevel().toString());
    userEventSend.setState(userCreated.getStatus().toString());

    //firstName
    {
      userEventSend.setFirstName("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_UPDATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getFirstName(),userFound.getName());
      Assert.assertEquals("Igual",userCreated.getName(),userFound.getName());
    }

    //lastName
    {
      userEventSend.setLastName("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_UPDATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getLastName(),userFound.getLastName());
      Assert.assertEquals("Igual",userCreated.getLastName(),userFound.getLastName());
    }

    //level
    {
      userEventSend.setLevel("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_UPDATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getLevel(),userFound.getUserLevel());
      Assert.assertEquals("Igual",userCreated.getUserLevel(),userFound.getUserLevel());
    }

    //state
    {
      userEventSend.setState("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_UPDATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getState(),userFound.getStatus());
      Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    }

  }

}
