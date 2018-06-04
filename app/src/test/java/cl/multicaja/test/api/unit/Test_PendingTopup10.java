package cl.multicaja.test.api.unit;

import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PendingTopup10 extends TestBaseRouteUnit {

  @Test
  public void pendingTopup_rut_is_null() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    topup.setRut(null);
    user.setRut(null);

    String messageId = sendPendingTopup(topup, user, null, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_prepaidUser_is_null() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = sendPendingTopup(topup, user, null, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_with_card_lockedhard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard(prepaidCard);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = sendPendingTopup(topup, user, null, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("dee tener una tarjeta", remoteTopup.getData().getPrepaidCard10());
    Assert.assertEquals("dee tener una tarjeta bloqueda duro", prepaidCard.getStatus(), remoteTopup.getData().getPrepaidCard10().getStatus());
  }

  @Test
  public void pendingTopup_with_card_expired() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard(prepaidCard);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = sendPendingTopup(topup, user, null, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("dee tener una tarjeta", remoteTopup.getData().getPrepaidCard10());
    Assert.assertEquals("dee tener una tarjeta expirada", prepaidCard.getStatus(), remoteTopup.getData().getPrepaidCard10().getStatus());
  }

  @Test
  public void pendingTopup_with_prepaidMovement_PROCESS_OK() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildCardFromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //Alta de cliente

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getData().getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementResp.getCodent());

    if (TopupType.WEB.equals(remoteTopup.getData().getPrepaidTopup10().getType())) {
      Assert.assertEquals("debe ser tipo factura CARGA_TRANSFERENCIA", TipoFactura.CARGA_TRANSFERENCIA, prepaidMovementResp.getTipofac());
    } else {
      Assert.assertEquals("debe ser tipo factura CARGA_EFECTIVO_COMERCIO_MULTICAJA", TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, prepaidMovementResp.getTipofac());
    }

    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, prepaidMovementResp.getEstado());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getClamone());
  }

  @Test
  public void pendingTopup_with_prepaidMovement_ERROR_IN_PROCESS_PENDING_TOPUP() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildCardFromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, null, prepaidMovement, 4);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getData().getProcessorMetadata());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP, prepaidMovementResp.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getNumextcta());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getNummovext());
    Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getClamone());
  }

  @Test
  public void pendingTopup_with_prepaidMovement_PROCESS_OK_and_cdt_confirm() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    PrepaidCard10 prepaidCard = buildCardFromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction(user, prepaidTopup);

    cdtTransaction = createCdtTransaction(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //Alta de cliente

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getData().getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementResp.getCodent());

    if (TopupType.WEB.equals(remoteTopup.getData().getPrepaidTopup10().getType())) {
      Assert.assertEquals("debe ser tipo factura CARGA_TRANSFERENCIA", TipoFactura.CARGA_TRANSFERENCIA, prepaidMovementResp.getTipofac());
    } else {
      Assert.assertEquals("debe ser tipo factura CARGA_EFECTIVO_COMERCIO_MULTICAJA", TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, prepaidMovementResp.getTipofac());
    }

    Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, prepaidMovementResp.getEstado());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado", Integer.valueOf(0), prepaidMovementResp.getClamone());

    CdtTransaction10 cdtTransactionConfirm10 = remoteTopup.getData().getCdtTransactionConfirm10();

    Assert.assertNotNull("debe tener un regisro cdt de confirmacion", cdtTransactionConfirm10);
    Assert.assertEquals("deben ser el mismo cdt externalTransactionId", cdtTransaction.getAccountId(), cdtTransactionConfirm10.getAccountId());
    Assert.assertEquals("deben ser el mismo cdt externalTransactionId", cdtTransaction.getExternalTransactionId(), cdtTransactionConfirm10.getExternalTransactionId());
    Assert.assertEquals("deben ser el mismo cdt externalTransactionId", cdtTransaction.getGloss(), cdtTransactionConfirm10.getGloss());
    Assert.assertNotEquals("deben ser distinto de 0 transactionReference", Long.valueOf(0), cdtTransactionConfirm10.getTransactionReference());

    if (cdtTransaction.getTransactionType().equals(CdtTransactionType.PRIMERA_CARGA)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.PRIMERA_CARGA_CONF, cdtTransactionConfirm10.getTransactionType());
    } else if (cdtTransaction.getTransactionType().equals(CdtTransactionType.CARGA_POS)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.CARGA_POS_CONF, cdtTransactionConfirm10.getTransactionType());
    } else if (cdtTransaction.getTransactionType().equals(CdtTransactionType.CARGA_WEB)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.CARGA_WEB_CONF, cdtTransactionConfirm10.getTransactionType());
    }
  }
}
