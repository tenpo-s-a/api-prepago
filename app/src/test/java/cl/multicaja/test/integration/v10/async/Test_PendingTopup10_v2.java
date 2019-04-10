package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PendingTopup10_v2 extends TestBaseUnitAsync {

  private static TecnocomServiceHelper tc;

  @BeforeClass
  public static void getTecnocomInstance(){
    tc = TecnocomServiceHelper.getInstance();
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @AfterClass
  public static void disableAutomaticErrorInTecnocom(){
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @Test
  public void pendingTopup_prepaidUser_is_null() throws Exception {

    PrepaidTopup10 topup = buildPrepaidTopup10();

    String messageId = sendPendingTopup(topup,null , null, null, null,0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_with_card_lockedhard() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(prepaidUser10.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser10, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement, account,0);

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

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(prepaidUser10.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser10, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement, account,0);

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

  @Test
  public void pendingTopup_with_prepaidCard_ACTIVE_prepaidMovement_PROCESS_OK_and_cdt_confirm() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(prepaidUser10.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser10, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement, account, 0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas cargas pendientes

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());
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

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(prepaidUser10.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser10, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement, account,0);
    Thread.sleep(2000);
    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas cargas pendientes

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(30000, 35000).getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());
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
  }

  @Test
  public void pendingTopup_transactionAuthorizaedEvent() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(prepaidUser10.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser10, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement, account, 0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas cargas pendientes

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());
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

    Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp3, prepaidTopup.getTransactionId());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo monto", prepaidMovement.getMonto(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", "CASH_IN_MULTICAJA", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status AUTHORIZED", "AUTHORIZED", transactionEvent.getTransaction().getStatus());
  }

  @Test
  public void pendingTopup_transactionRejectedEvent() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(prepaidUser10.getId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser10, account.getAccountNumber());

    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser10, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard, cdtTransaction);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno("200");

    String messageId = sendPendingTopup(prepaidTopup, prepaidUser10, cdtTransaction, prepaidMovement, account, 0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas cargas pendientes

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovement10();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementResp.getCodent());
    Assert.assertEquals("El movimiento debe ser rechazado", PrepaidMovementStatus.REJECTED, prepaidMovementResp.getEstado());

    PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

    Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.REJECTED, prepaidMovementInDb.getEstado());

    Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REJECTED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp3, prepaidTopup.getTransactionId());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo monto", prepaidMovement.getMonto(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", "CASH_IN_MULTICAJA", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status AUTHORIZED", "REJECTED", transactionEvent.getTransaction().getStatus());

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

}
