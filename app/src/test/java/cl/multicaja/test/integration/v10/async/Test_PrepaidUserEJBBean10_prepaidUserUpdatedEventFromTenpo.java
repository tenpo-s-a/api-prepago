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
