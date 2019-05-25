package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;

/**
 * @autor abarazarte
 */

public class Test_PendingReverseTopup10 extends TestBaseUnitAsync {

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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,4);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de carga de retiro", remoteTopup);

      PrepaidMovement10 reverseMovement = remoteTopup.getData().getPrepaidMovementReverse();
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP_REVERSE, reverseMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());

    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger(30000, 60000)
        .getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un reverse", remoteTopup);
      Assert.assertNotNull("Deberia existir un reverse", remoteTopup.getData());

      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovementReverse();

      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);

      PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

      Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
      Assert.assertEquals("El movimiento debe ser procesado con error", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP_REVERSE, prepaidMovementInDb.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado con error", BusinessStatusType.IN_PROCESS, prepaidMovementInDb.getEstadoNegocio());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getClamone());

      //verifica que la ultima cola por la cual paso el mensaje sea ERROR_REVERSAL_WITHDRAW_REQ
      ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
      String endpoint = TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_REQ;

      Assert.assertEquals("debe ser intento 5", 5, lastProcessorMetadata.getRetry());
      Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
      Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
    }
  }

  @Test
  public void testPendingTopupReverseOk_pos() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(getRandomString(15));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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
  public void testPendingTopupReverseOk_web() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.CARGA_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.CARGA_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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
  public void testPendingTopupReverseOk_pos_expireBalanceCache() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(getRandomString(15));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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

    account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    Assert.assertNotNull(account);
    Assert.assertTrue(account.getExpireBalance() > 0L && account.getExpireBalance() <= Instant.now().toEpochMilli());
  }

  @Test
  public void testPendingTopupReverseOk_web_expireBalanceCache() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.CARGA_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.CARGA_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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

    account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    Assert.assertNotNull(account);
    Assert.assertTrue(account.getExpireBalance() > 0L && account.getExpireBalance() <= Instant.now().toEpochMilli());
  }

  @Test
  public void testPendingTopupReverseOkMovementTimeOutResponseError1_pos() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(getRandomString(15));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PENDING, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.IN_PROCESS, prepaidMovementReverseResp.getEstadoNegocio());
    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Cantidad de intentos: ",2,remoteTopup.getRetryCount());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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
  public void testPendingTopupReverseOkMovementTimeOutResponseError1_web() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PENDING, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.IN_PROCESS, prepaidMovementReverseResp.getEstadoNegocio());
    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Cantidad de intentos: ",2,remoteTopup.getRetryCount());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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
  public void testPendingTopupReverseOkMovementTimeOutResponseError2_pos() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(getRandomString(15));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    inclusionMovimientosTecnocom(account,prepaidCard,prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PENDING, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.IN_PROCESS, prepaidMovementReverseResp.getEstadoNegocio());
    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Cantidad de intentos: ",2,remoteTopup.getRetryCount());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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
  public void testPendingTopupReverseOkMovementTimeOutResponseError2_web() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard = createPrepaidCardV2(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    inclusionMovimientosTecnocom(account,prepaidCard,prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, prepaidUser, prepaidReverseMovement,0);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PENDING, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.IN_PROCESS, prepaidMovementReverseResp.getEstadoNegocio());
    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Cantidad de intentos: ",2,remoteTopup.getRetryCount());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());
    }

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
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
