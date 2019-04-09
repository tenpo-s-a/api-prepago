package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.dao.UserDao;
import cl.multicaja.prepaid.kafka.events.UserEvent;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.prepaid.model.v11.User;
import cl.multicaja.prepaid.model.v11.UserStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Queue;

public class Test_PrepaidUserEJBBean10_prepaidUserUpdatedEventFromTenpo extends TestBaseUnitAsync {

  @Before
  @After
  public void clearDataBefore(){
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", SCHEMA));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", SCHEMA));
  }

  @Test
  public void listenPrepaidUserUpdateEventWithProcessor() throws Exception{

    PrepaidUser10 userToCreate = buildPrepaidUser11();
    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createPrepaidUserV10(null,userToCreate);
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

    //Data To Change for Update
    userEventSend.setLevel(PrepaidUserLevel.LEVEL_2.toString());

    String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_UPDATED_TOPIC,userEventSend,0);

    Thread.sleep(1000);

    //Find if data was saved on DataBase
    PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);

    Assert.assertNotNull("No es nulo", userFound);

    Assert.assertEquals("Igual",userCreated.getUuid(),userFound.getUuid());
    Assert.assertEquals("Igual",userCreated.getRut(),userFound.getRut());
    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userCreated.getName(),userFound.getName());
    Assert.assertEquals("Igual",userCreated.getLastName(),userFound.getLastName());

    Assert.assertEquals("Igual",userCreated.getDocumentNumber(),userFound.getDocumentNumber());

    Assert.assertEquals("Igual",userCreated.getTimestamps().getCreatedAt().toLocalDate(),userFound.getTimestamps().getCreatedAt().toLocalDate());
    Assert.assertEquals("Igual",userCreated.getTimestamps().getUpdatedAt().toLocalDate(),userFound.getTimestamps().getUpdatedAt().toLocalDate());

    Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    Assert.assertEquals("Igual",userEventSend.getLevel(),userFound.getUserLevel().toString());


  }


  @Test
  public void listenPrepaidUserUpdateEventWithProcessorNotValidFields() throws Exception{
    PrepaidUser10 userToCreate = buildPrepaidUser11();
    PrepaidUser10 userCreated = getPrepaidUserEJBBean10().createPrepaidUserV10(null,userToCreate);
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

    //uiid
    {
      userEventSend.setId("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(1000);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getId(),userFound.getUuid());
      Assert.assertEquals("Igual",userCreated.getUuid(),userFound.getUuid());
    }

    //documentNumber
    {
      userEventSend.setDocumentNumber("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(1000);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getDocumentNumber(),userFound.getDocumentNumber());
      Assert.assertEquals("Igual",userCreated.getDocumentNumber(),userFound.getDocumentNumber());
    }

    //firstName
    {
      userEventSend.setFirstName("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(1000);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getFirstName(),userFound.getName());
      Assert.assertEquals("Igual",userCreated.getName(),userFound.getName());
    }

    //lastName
    {
      userEventSend.setLastName("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(1000);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getLastName(),userFound.getLastName());
      Assert.assertEquals("Igual",userCreated.getLastName(),userFound.getLastName());
    }

    //level
    {
      userEventSend.setLevel("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(1000);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getLevel(),userFound.getUserLevel());
      Assert.assertEquals("Igual",userCreated.getUserLevel(),userFound.getUserLevel());
    }

    //state
    {
      userEventSend.setState("");

      String messageId = sendUserCreatedOrUpdated(KafkaEventsRoute10.USER_CREATED_TOPIC,userEventSend,0);
      Assert.assertNotNull("No es nulo", messageId);
      Assert.assertNotEquals("No es cero",0,messageId);

      Thread.sleep(1000);

      //Find if data was saved on DataBase
      PrepaidUser10 userFound = findPrepaidUserV10(null,userEventSend.getId(), null);
      Assert.assertNotNull("No Es nulo", userFound);
      Assert.assertNotEquals("No Igual",userEventSend.getState(),userFound.getStatus());
      Assert.assertEquals("Igual",userCreated.getStatus(),userFound.getStatus());
    }


  }

}
