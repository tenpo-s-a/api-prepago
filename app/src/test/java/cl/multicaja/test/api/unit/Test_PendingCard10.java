package cl.multicaja.test.api.unit;

import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PendingCard10 extends TestBaseRouteUnit {

  /********************
   * Test flujo alta rapida
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getData().getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
  }

  /********************
   * Test flujo obtener tarjeta
   * @throws Exception
   *****/
  @Test
  public void pendingCreateCard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getData().getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
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

  /********************
   * Test directo de alta rapida
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCardUnit() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNull("Pan Debe ser Nulo",remoteTopup.getData().getPrepaidCard10().getPan());
  }

  /********************
   * Test directo de obtener y crear tarjeta
   * @throws Exception
   *****/
  @Test
  public void pendingCreateCardUnit() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCard(prepaidCard10);

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNotNull("Debe contener getPan",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Debe contener getNameOnCard",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNotNull("Debe contener getExpiration",remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertNotNull("Debe contener getEncryptedPan",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
  }

  /********************
   * Test directo de alta rapida (TIME OUT)
   * @throws Exception
   *****/
  @Test
  public void pendingEmissionCardUnitTimeOut() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,4);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNull("La tarjeta debe ser Nula", remoteTopup.getData().getPrepaidCard10());
  }

  /********************
   * Test directo de obtener y crear tarjeta
   * @throws Exception
   *****/
  @Test
  public void pendingCreateCardUnitTimeOut() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCard(prepaidCard10);

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 4);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CREATE_CARD_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertNotNull("Debe contener una tarjeta",remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Debe contener un contrato",remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNull("Debe contener getPan",remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNull("Debe contener getNameOnCard",remoteTopup.getData().getPrepaidCard10().getNameOnCard());
    Assert.assertNull("Debe contener getExpiration",remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertNull("Debe contener getEncryptedPan",remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
  }


}
