package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PendingTopup10_v2 extends TestBaseUnitAsync {



  @Test
  public void pendingTopup_prepaidUser_is_null() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup = buildPrepaidTopup10(user);

    String messageId = sendPendingTopup(topup, user, null, null, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_with_card_lockedhard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("debe tener una tarjeta", remoteTopup.getData().getPrepaidCard10());
    Assert.assertEquals("debe tener una tarjeta bloqueda duro", prepaidCard.getStatus(), remoteTopup.getData().getPrepaidCard10().getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_TOPUP_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_TOPUP_REQ;

    Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  @Test
  public void pendingTopup_with_card_expired() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("debe tener una tarjeta", remoteTopup.getData().getPrepaidCard10());
    Assert.assertEquals("debe tener una tarjeta expirada", prepaidCard.getStatus(), remoteTopup.getData().getPrepaidCard10().getStatus());

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_TOPUP_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_TOPUP_REQ;

    Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
    Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }

  //TODO: este test debe probarse con la logica de devoluciones
  @Ignore
  @Test
  public void pendingTopup_with_prepaidMovement_REJECTED() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 4);

    //mensaje procesado por pedingTopup pero que falla y deja en cola de devoluciones
    {
      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);

      PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

      Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
      Assert.assertEquals("El movimiento debe ser procesado con error", PrepaidMovementStatus.REJECTED, prepaidMovementInDb.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getClamone());

      CdtTransaction10 cdtTransaction10 = remoteTopup.getData().getCdtTransaction10();

      Assert.assertNotNull("Debe tener un regisro cdt", cdtTransaction10);

      CdtTransaction10 cdtTransactionConfirm10 = remoteTopup.getData().getCdtTransactionConfirm10();

      Assert.assertNull("No debe tener un regisro cdt de confirmacion", cdtTransactionConfirm10);

      //verifica que la ultima cola por la cual paso el mensaje sea PENDING_TOPUP_RETURNS_REQ
      ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
      String endpoint = PrepaidTopupRoute10.ERROR_TOPUP_REQ;

      Assert.assertEquals("debe ser intento procesado 5", 5, lastProcessorMetadata.getRetry());
      Assert.assertTrue("debe ser redirect", lastProcessorMetadata.isRedirect());
      Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
    }

    //debe existir el mensaje en la cola de devoluciones pendientes
    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_TOPUP_RESP);
      ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);

      PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

      Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
      Assert.assertEquals("El movimiento debe ser procesado con error", PrepaidMovementStatus.REJECTED, prepaidMovementInDb.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado con error", BusinessStatusType.REJECTED, prepaidMovementInDb.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getClamone());

      CdtTransaction10 cdtTransaction10 = remoteTopup.getData().getCdtTransaction10();

      Assert.assertNotNull("Debe tener un regisro cdt", cdtTransaction10);

      CdtTransaction10 cdtTransactionConfirm10 = remoteTopup.getData().getCdtTransactionConfirm10();

      Assert.assertNull("No debe tener un regisro cdt de confirmacion", cdtTransactionConfirm10);

      //verifica que la ultima cola por la cual paso el mensaje sea PENDING_TOPUP_RETURNS_REQ
      ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
      String endpoint = PrepaidTopupRoute10.ERROR_TOPUP_REQ;

      Assert.assertEquals("debe ser primer intento", 0, lastProcessorMetadata.getRetry());
      Assert.assertFalse("debe ser redirect", lastProcessorMetadata.isRedirect());
      Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
    }
  }

  @Test
  public void pendingTopup_with_prepaidCard_ACTIVE_prepaidMovement_PROCESS_OK_and_cdt_confirm() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas cargas pendientes

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementResp.getEstado());

    if (TransactionOriginType.WEB.equals(remoteTopup.getData().getPrepaidTopup10().getTransactionOriginType())) {
      Assert.assertEquals("debe ser tipo factura CARGA_TRANSFERENCIA", TipoFactura.CARGA_TRANSFERENCIA, prepaidMovementResp.getTipofac());
    } else {
      Assert.assertEquals("debe ser tipo factura CARGA_EFECTIVO_COMERCIO_MULTICAJA", TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, prepaidMovementResp.getTipofac());
    }

    PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

    Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementInDb.getEstado());
    Assert.assertNotEquals("El movimiento debe ser procesado exitosamente", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado exitosamente", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado exitosamente", Integer.valueOf(0), prepaidMovementInDb.getClamone());

    CdtTransaction10 cdtTransaction10 = remoteTopup.getData().getCdtTransaction10();

    Assert.assertNotNull("Debe tener un regisro cdt", cdtTransaction10);

    CdtTransaction10 cdtTransactionConfirm10 = remoteTopup.getData().getCdtTransactionConfirm10();

    Assert.assertNotNull("debe tener un regisro cdt de confirmacion", cdtTransactionConfirm10);
    Assert.assertEquals("deben ser el mismo cdt accountId", cdtTransaction.getAccountId(), cdtTransactionConfirm10.getAccountId());
    Assert.assertEquals("deben ser el mismo cdt externalTransactionIdConfirm", cdtTransaction.getExternalTransactionId(), cdtTransactionConfirm10.getExternalTransactionId());
    Assert.assertEquals("deben ser el mismo cdt gloss", prepaidTopup.getCdtTransactionTypeConfirm().getName() + " " + cdtTransaction.getAmount(), cdtTransactionConfirm10.getGloss());
    Assert.assertNotEquals("deben ser distinto de 0 transactionReference", Long.valueOf(0), cdtTransactionConfirm10.getTransactionReference());

    if (cdtTransaction.getTransactionType().equals(CdtTransactionType.PRIMERA_CARGA)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.PRIMERA_CARGA_CONF, cdtTransactionConfirm10.getTransactionType());
    } else if (cdtTransaction.getTransactionType().equals(CdtTransactionType.CARGA_POS)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.CARGA_POS_CONF, cdtTransactionConfirm10.getTransactionType());
    } else if (cdtTransaction.getTransactionType().equals(CdtTransactionType.CARGA_WEB)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.CARGA_WEB_CONF, cdtTransactionConfirm10.getTransactionType());
    }
  }

  @Test
  public void pendingTopup_with_prepaidCard_PENDING_prepaidMovement_PROCESS_OK_and_cdt_confirm() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas cargas pendientes

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementResp.getEstado());

    if (TransactionOriginType.WEB.equals(remoteTopup.getData().getPrepaidTopup10().getTransactionOriginType())) {
      Assert.assertEquals("debe ser tipo factura CARGA_TRANSFERENCIA", TipoFactura.CARGA_TRANSFERENCIA, prepaidMovementResp.getTipofac());
    } else {
      Assert.assertEquals("debe ser tipo factura CARGA_EFECTIVO_COMERCIO_MULTICAJA", TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, prepaidMovementResp.getTipofac());
    }

    PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

    Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementInDb.getEstado());
    Assert.assertNotEquals("El movimiento debe ser procesado exitosamente", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
    Assert.assertNotEquals("El movimiento debe ser procesado exitosamente", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
    Assert.assertNotEquals("El movimiento debe ser procesado exitosamente", Integer.valueOf(0), prepaidMovementInDb.getClamone());

    CdtTransaction10 cdtTransaction10 = remoteTopup.getData().getCdtTransaction10();

    Assert.assertNotNull("Debe tener un regisro cdt", cdtTransaction10);

    CdtTransaction10 cdtTransactionConfirm10 = remoteTopup.getData().getCdtTransactionConfirm10();

    Assert.assertNotNull("debe tener un regisro cdt de confirmacion", cdtTransactionConfirm10);
    Assert.assertEquals("deben ser el mismo cdt accountId", cdtTransaction.getAccountId(), cdtTransactionConfirm10.getAccountId());
    Assert.assertEquals("deben ser el mismo cdt externalTransactionIdConfirm", cdtTransaction.getExternalTransactionId(), cdtTransactionConfirm10.getExternalTransactionId());
    Assert.assertEquals("deben ser el mismo cdt gloss", prepaidTopup.getCdtTransactionTypeConfirm().getName() + " " + cdtTransaction.getAmount(), cdtTransactionConfirm10.getGloss());
    Assert.assertNotEquals("deben ser distinto de 0 transactionReference", Long.valueOf(0), cdtTransactionConfirm10.getTransactionReference());

    if (cdtTransaction.getTransactionType().equals(CdtTransactionType.PRIMERA_CARGA)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.PRIMERA_CARGA_CONF, cdtTransactionConfirm10.getTransactionType());
    } else if (cdtTransaction.getTransactionType().equals(CdtTransactionType.CARGA_POS)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.CARGA_POS_CONF, cdtTransactionConfirm10.getTransactionType());
    } else if (cdtTransaction.getTransactionType().equals(CdtTransactionType.CARGA_WEB)) {
      Assert.assertEquals("deben ser transactionType de confirmacion", CdtTransactionType.CARGA_WEB_CONF, cdtTransactionConfirm10.getTransactionType());
    }

    //verifica que la ultima cola por la cual paso el mensaje sea PENDING_CARD_ISSUANCE_FEE_REQ
    ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
    String endpoint = PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ;

    Assert.assertEquals("debe ser primer intento procesado", 0, lastProcessorMetadata.getRetry());
    Assert.assertTrue("debe ser redirect", lastProcessorMetadata.isRedirect());
    Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
  }
}
