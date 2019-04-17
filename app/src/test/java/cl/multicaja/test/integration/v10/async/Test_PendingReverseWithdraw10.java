package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;

/**
 * @author abarazarte
 **/

public class Test_PendingReverseWithdraw10 extends TestBaseUnitAsync {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
  }

  @Test
  public void reverseRetryCount4() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, prepaidMovement, 4);

    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_WITHDRAW_REVERSE, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    {
      //mensaje procesado por processPendingWithdrawReversal pero que falla y deja en cola de error
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger(30000, 60000)
        .getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un reverse", remoteReverse);
      Assert.assertNotNull("Deberia existir un reverse", remoteReverse.getData());

      System.out.println("Steps: " + remoteReverse.getProcessorMetadata());

      PrepaidMovement10 prepaidMovementResp = remoteReverse.getData().getPrepaidMovementReverse();

      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);

      PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

      Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
      Assert.assertEquals("El movimiento debe ser procesado con error", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_WITHDRAW_REVERSE, prepaidMovementInDb.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado con error", BusinessStatusType.IN_PROCESS, prepaidMovementInDb.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getClamone());

      //verifica que la ultima cola por la cual paso el mensaje sea ERROR_REVERSAL_WITHDRAW_REQ
      ProcessorMetadata lastProcessorMetadata = remoteReverse.getLastProcessorMetadata();
      String endpoint = TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_REQ;

      Assert.assertEquals("debe ser intento 5", 5, lastProcessorMetadata.getRetry());
      Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
      Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
    }
  }

  @Test
  public void reverseWithdraw_OriginalMovement_ProcessOk_pos() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(getRandomString(15));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setEstadoNegocio(BusinessStatusType.CONFIRMED);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setNumaut(null);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio PROCESS_OK", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.RETIRO_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  @Test
  public void reverseWithdraw_OriginalMovement_ErrorTecnocom_pos() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(50000));

    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber(), prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(getRandomString(15));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());

    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.RETIRO_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  /*
    ERROR_TIMEOUT_RESPONSE - Mov Original no se realizo
   */
  @Test
  public void reverseWithdraw_OriginalMovement_ErrorTimeoutResponse1_pos() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(50000));

    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber(), prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(getRandomString(15));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + account.getAccountNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.RETIRO_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  /*
    ERROR_TIMEOUT_RESPONSE - Mov Original si se realizo
   */
  @Test
  public void reverseWithdraw_OriginalMovement_ErrorTimeoutResponse2_pos() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(50000));
    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber() , prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(getRandomString(15));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    InclusionMovimientosDTO withdrawTecnocom = inclusionMovimientosTecnocom(account,prepaidCard10, originalWithdraw);
    Assert.assertTrue("Debe ser exitosa", withdrawTecnocom.isRetornoExitoso());

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.RETIRO_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  @Test
  public void reverseWithdraw_OriginalMovement_ProcessOk_web() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setEstadoNegocio(BusinessStatusType.CONFIRMED);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setNumaut(null);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio PROCESS_OK", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  @Test
  public void reverseWithdraw_OriginalMovement_ErrorTecnocom_web() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(50000));

    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber(), prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());

    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  /*
    ERROR_TIMEOUT_RESPONSE - Mov Original no se realizo
   */
  @Test
  public void reverseWithdraw_OriginalMovement_ErrorTimeoutResponse1_web() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber() , prepaidCard10, BigDecimal.valueOf(50000));

    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber() , prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  /*
    ERROR_TIMEOUT_RESPONSE - Mov Original si se realizo
   */
  @Test
  public void reverseWithdraw_OriginalMovement_ErrorTimeoutResponse2_web() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(50000));
    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber(), prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionType(withdraw10.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser.getDocumentNumber());
    cdtTransaction.setGloss(withdraw10.getCdtTransactionType().getName()+" "+ withdraw10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdraw10.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

    Assert.assertTrue("Debe crear la transaccion CDT", cdtTransaction.isNumErrorOk());

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    InclusionMovimientosDTO withdrawTecnocom = inclusionMovimientosTecnocom(account, prepaidCard10, originalWithdraw);
    Assert.assertTrue("Debe ser exitosa", withdrawTecnocom.isRetornoExitoso());

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = originalWithdraw.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", originalWithdraw.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  /*
    ERROR_TIMEOUT_RESPONSE - Mov Original rechazado por monto
   */
  @Test
  public void reverseWithdraw_OriginalMovement_Rejected() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO firstTopup = topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(50000));
    Assert.assertTrue("Debe ser exitosa", firstTopup.isRetornoExitoso());

    ConsultaSaldoDTO balance = getTecnocomService().consultaSaldo(account.getAccountNumber() , prepaidUser.getRut().toString(), TipoDocumento.RUT);
    Assert.assertTrue("Debe ser exitosa", balance.isRetornoExitoso());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(5000));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(BigDecimal.valueOf(100000));
    originalWithdraw.setImpfac(BigDecimal.valueOf(100000));
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    // primer intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
    }

    // segundo intento
    {
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.CONFIRMED, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());

      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", BusinessStatusType.REVERSED, originalDb.getEstadoNegocio());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
      Assert.assertEquals("Deberia estar con estado negocio CONFIRMED", BusinessStatusType.CONFIRMED, reverseDb.getEstadoNegocio());

    }
  }


  private void addAccountingAndClearing(PrepaidMovement10 prepaidMovement, AccountingStatusType clearingStatus) throws Exception {
    PrepaidAccountingMovement pam = new PrepaidAccountingMovement();
    pam.setPrepaidMovement10(prepaidMovement);
    pam.setReconciliationDate(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toInstant()));

    AccountingData10 accounting = getPrepaidAccountingEJBBean10().buildAccounting10(pam, AccountingStatusType.PENDING, AccountingStatusType.PENDING);

    accounting = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting);

    // Insertar en clearing
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(clearingStatus);
    clearing10.setUserBankAccount(null);
    clearing10.setAccountingId(accounting.getId());
    getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
  }

  private void addAccountingAndClearing(PrepaidMovement10 prepaidMovement) throws Exception {
    addAccountingAndClearing(prepaidMovement, AccountingStatusType.INITIAL);
  }

  private Boolean checkReconciliationDate(Timestamp ts) {

    Instant instant = Instant.now();
    ZoneId z = ZoneId.of( "UTC" );
    ZonedDateTime nowUtc = instant.atZone(z);

    LocalDateTime localDateTime = ts.toLocalDateTime();
    ZonedDateTime reconciliationDateUtc = localDateTime.atZone(ZoneOffset.UTC);

    return reconciliationDateUtc.isBefore(nowUtc);
  }
}
