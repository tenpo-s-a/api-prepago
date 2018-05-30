package cl.multicaja.test.api.unit;

import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PendingCard10 extends TestBaseRouteUnit {

  @Test
  public void pendingEmissionCard() throws Exception {

    User user = preRegisterUser();
    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = sendTopup(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", topup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    System.out.println("Contrato: "+remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
  }

  @Test
  public void pendingEmissionCardPrepaidUserIsNull() throws Exception {
    User user = preRegisterUser();
    PrepaidTopup10 topup = buildPrepaidTopup(user);
    String messageId = sendTopup(topup, user);
    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }


  @Test
  public void pendingCreateCard() throws Exception {

    User user = preRegisterUser();
    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = sendTopup(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATECARD_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", topup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    /******************************
     * Validacion de datos Tarjeta
     ******************************/
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNotNull("Deberia tener PAN", remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Deberia tener PAN Encriptado", remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia tener Expire Date", remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertEquals("Status Igual a",PrepaidCardStatus.ACTIVE, remoteTopup.getData().getPrepaidCard10().getStatus());
    Assert.assertNotNull("Deberia Tener Nombre",remoteTopup.getData().getPrepaidCard10().getNameOnCard());

  }

  @Test
  public void pendingCreateCardPrepaidUserIsNull() throws Exception {
    User user = preRegisterUser();
    PrepaidTopup10 topup = buildPrepaidTopup(user);
    String messageId = sendTopup(topup, user);
    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

}
