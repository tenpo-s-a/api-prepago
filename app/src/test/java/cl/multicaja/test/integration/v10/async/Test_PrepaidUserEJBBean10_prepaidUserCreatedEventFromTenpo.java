package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_PrepaidUserEJBBean10_prepaidUserCreatedEventFromTenpo extends TestBaseUnitAsync {

  private final Integer sleepTimerMillis = 2000;

  @After
  @Before
  public void clearDataBefore(){
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimiento CASCADE", SCHEMA));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_usuario CASCADE", SCHEMA));
  }


  @Test
  public void listenPrepaidUserCreateEventWithProcessor() throws Exception{

    PrepaidUser10 userToCreate = buildPrepaidUser11();
    userToCreate.setDocumentNumber(userToCreate.getDocumentNumber()+"k");
    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();


    userEventSend.setDocumentNumber(userToCreate.getDocumentNumber());
    userEventSend.setFirstName(userToCreate.getName());
    userEventSend.setId(userToCreate.getUuid());
    userEventSend.setLastName(userToCreate.getLastName());
    userEventSend.setLevel(userToCreate.getUserLevel().toString());
    userEventSend.setState(userToCreate.getStatus().toString());
    userEventSend.setPlan(userToCreate.getUserPlan().name());

    String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
    Assert.assertNotNull("No es nulo", messageId);
    Assert.assertNotEquals("No es cero",0,messageId);

    Thread.sleep(sleepTimerMillis);

    //Find if data was saved on DataBase
    PrepaidUser10 userFound = findPrepaidUserByExtId(userToCreate.getUuid());

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userToCreate.getUuid(),userFound.getUuid());
    Assert.assertEquals("Igual",userToCreate.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreate.getName(),userFound.getName());
    Assert.assertEquals("Igual",userToCreate.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userToCreate.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userToCreate.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userToCreate.getUserLevel(),userFound.getUserLevel());
    Assert.assertEquals("Igual",userToCreate.getUserPlan(),userFound.getUserPlan());

  }


  @Test
  public void listenPrepaidUserCreateEventWithProcessorNotValidFields() throws Exception {
    PrepaidUser10 userToCreate = buildPrepaidUser11();

    cl.multicaja.prepaid.kafka.events.model.User userEventSend = new cl.multicaja.prepaid.kafka.events.model.User();

    userEventSend.setId(userToCreate.getUuid());
    userEventSend.setDocumentNumber(userToCreate.getDocumentNumber());
    userEventSend.setFirstName(userToCreate.getName());
    userEventSend.setLastName(userToCreate.getLastName());
    userEventSend.setLevel(userToCreate.getUserLevel().toString());
    userEventSend.setState(userToCreate.getStatus().toString());
    userEventSend.setPlan(userToCreate.getUserPlan().name());

    //uiid
    {
      userEventSend.setId("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNull("Es nulo", userFound);
    }

    //documentNumber
    {
      userEventSend.setDocumentNumber("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNull("Es nulo", userFound);
    }

    //firstName
    {
      userEventSend.setFirstName("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNull("Es nulo", userFound);
    }

    //lastName
    {
      userEventSend.setLastName("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNull("Es nulo", userFound);
    }

    //level
    {
      userEventSend.setLevel("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNull("Es nulo", userFound);
    }

    //state
    {
      userEventSend.setState("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.SEDA_USER_CREATE_EVENT,userEventSend);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(sleepTimerMillis);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserByExtId(userEventSend.getId());
      Assert.assertNull("Es nulo", userFound);
    }


  }


}
