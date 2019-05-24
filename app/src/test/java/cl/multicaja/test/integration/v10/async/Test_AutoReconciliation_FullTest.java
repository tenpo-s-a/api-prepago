package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.fees.FeeService;
import cl.multicaja.prepaid.helpers.fees.model.Charge;
import cl.multicaja.prepaid.helpers.fees.model.ChargeType;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationRegisterType;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.netbeans.modules.schema2beans.ValidateException;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Test_AutoReconciliation_FullTest extends TestBaseUnitAsync {
  static Account account;
  static UserAccount userAccount;
  static PrepaidUser10 prepaidUser;
  static PrepaidCard10 prepaidCard;
  static ReconciliationFile10 tecnocomReconciliationFile10;

  @BeforeClass
  public static void prepareDB() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_archivos_conciliacion CASCADE", getSchema()));

    try {
      Test_AutoReconciliation_FullTest test = new Test_AutoReconciliation_FullTest();
      //USUARIO
      prepaidUser = test.buildPrepaidUserv2();
      prepaidUser = test.createPrepaidUserV2(prepaidUser);

      //CUENTA
      account = test.buildAccountFromTecnocom(prepaidUser);
      account = test.createAccount(account.getUserId(),account.getAccountNumber());

      //TARJETA
      prepaidCard = test.buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard = test.createPrepaidCardV2(prepaidCard);

      String contrato = account.getAccountNumber();
      String nomcomred = "Multi test";
      String pan = EncryptUtil.getInstance().decrypt(prepaidCard.getEncryptedPan());

      PrepaidTopup10 prepaidTopup = test.buildPrepaidTopup10();
      prepaidTopup.getAmount().setValue(new BigDecimal(200000));
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.setFirstTopup(false);
      CdtTransaction10 cdtTransaction = test.buildCdtTransaction10(prepaidUser, prepaidTopup);
      PrepaidMovement10 prepaidMovement = test.buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
      prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement.setNumaut(getRandomNumericString(6));
      prepaidMovement.setFechaCreacion(null);
      prepaidMovement.setMonto(prepaidTopup.getAmount().getValue());
      prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
      prepaidMovement.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      cdtTransaction = test.createCdtTransaction10(cdtTransaction);
      prepaidMovement.setIdMovimientoRef(cdtTransaction.getTransactionReference());
      prepaidMovement = test.createPrepaidMovement10(prepaidMovement);

      TecnocomServiceHelper.getInstance().getTecnocomService().cambioProducto(contrato, "", TipoDocumento.RUT, TipoAlta.NIVEL2);

      TecnocomServiceHelper.getInstance().topup(contrato, pan, nomcomred, prepaidMovement);

      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

      ReconciliationFile10 newReconciliationFile10 = new ReconciliationFile10();
      newReconciliationFile10.setStatus(FileStatus.OK);
      newReconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);
      newReconciliationFile10.setFileName("archivo_tecnocom.txt");
      newReconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      tecnocomReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      Thread.sleep(100);

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Error al crear el usuario y su tarjeta");
    }
  }

  @AfterClass
  public static void clearDB() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_archivos_conciliacion CASCADE", getSchema()));
  }

  @Test
  public void processTecnocomTableData_expireNotified() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    insertedMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Crea 1 archivo extra para que se expire el movimiento
    List<ReconciliationFile10> createdFiles = createReconciliationFiles(2);

    // Como hay dos archivos tecnocom en la tabla, debe expirar los movimientos NOTIFIED
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe haber cambiado estado con tecnocom a NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, foundMovement.getConTecnocom());
    Assert.assertEquals("Debe haber cambiado a estado EXPIRED", PrepaidMovementStatus.EXPIRED, foundMovement.getEstado());

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, foundMovement.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", foundMovement.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", "SUSCRIPTION", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "REVERSED", transactionEvent.getTransaction().getStatus());

    deleteReconciliationFiles(createdFiles);
  }

  @Test
  public void processTecnocomTableData_expireAuthorized() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    insertedMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    insertedMovement.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).minusHours(1)));
    insertedMovement = createPrepaidMovement11(insertedMovement);

    AccountingData10 accdata = buildRandomAccouting();
    accdata.setIdTransaction(insertedMovement.getId());
    accdata.setStatus(AccountingStatusType.PENDING);
    accdata.setAccountingStatus(AccountingStatusType.PENDING);

    getPrepaidAccountingEJBBean10().saveAccountingData(null, accdata);

    ClearingData10 liqInsert = createClearingData(accdata, AccountingStatusType.INITIAL);

    getPrepaidClearingEJBBean10().insertClearingData(null, liqInsert);

    // Crea 7 archivos extra
    List<ReconciliationFile10> createdFiles = createReconciliationFiles(7);

    // Como hay 7 archivos tecnocom en la tabla, debe expirar los movimientos AUTHORIZED
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe haber cambiado estado con tecnocom a NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, foundMovement.getConTecnocom());
    Assert.assertEquals("Debe haber cambiado a estado EXPIRED", PrepaidMovementStatus.EXPIRED, foundMovement.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,foundMovement.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado NOT_OK", AccountingStatusType.NOT_OK, acc.getAccountingStatus());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado NOT_SEND", AccountingStatusType.NOT_SEND, liq.getStatus());

    // Verificar que exista en la cola de eventos transaction_reversed
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, foundMovement.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", foundMovement.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", "SUSCRIPTION", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "REVERSED", transactionEvent.getTransaction().getStatus());

    deleteReconciliationFiles(createdFiles);
  }

  @Test
  public void processTecnocomTableData_whenMovNotInDBAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.AU);
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Prepara un mock del servicio de fees, para que retornes las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue());

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());

    // Verificar que exista en la cola de eventos transaction_authorized
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidMovement10.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", "PURCHASE", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "AUTHORIZED", transactionEvent.getTransaction().getStatus());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 2 fees asignadas", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.81)));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de iva", ivaFee);
    BigDecimal expectedIvaFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.19)));
    Assert.assertEquals("Debe tener un valor de ", expectedIvaFee.setScale(0, RoundingMode.HALF_UP), ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));
  }

  @Test
  public void processTecnocomTableData_whenMovNotInDBAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    // Inserta el movimiento que vino en el archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setReconciled(false);
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(1.005))); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    ipmMovement10 = createIpmMovement(ipmMovement10);

    // Prepara un mock del servicio de fees, para que retornes las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue());

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista en la BD en estado OP
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe exitir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK + fecha de conciliacion tiene que ser "ahora")
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // El movimiento ha pasado a OP, verificar que su valor de contabilidad mastercard ha sido actualizado con el del IPM
    Assert.assertEquals("Deben tener mismo valor", ipmMovement10.getCardholderBillingAmount().setScale(2, RoundingMode.HALF_UP), acc.getAmountMastercard().getValue().setScale(2, RoundingMode.HALF_UP));

    // El movimiento en IPM debe pasar a conciliado
    IpmMovement10 foundIpmMovement = getIpmMovementById(ipmMovement10.getId());
    Assert.assertTrue("Debe estar en estado reconciled", foundIpmMovement.getReconciled());

    // Verificar que exista en la cola de eventos transaction_authorized
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidMovement10.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", "PURCHASE", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "AUTHORIZED", transactionEvent.getTransaction().getStatus());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 2 fees asignadas", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.81)));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de iva", ivaFee);
    BigDecimal expectedIvaFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.19)));
    Assert.assertEquals("Debe tener un valor de ", expectedIvaFee.setScale(0, RoundingMode.HALF_UP), ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));
  }

  @Test
  public void processTecnocomTableData_whenMovInDBIsNotifiedAndFileIsAU_movIsInsertedAndLiqAccIsInsertedMustInitialPendingState() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.AU);
    movimientoTecnocom10.setTipoFac(TipoFactura.COMPRA_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(movimientoTecnocom10.getTipoFac().getCorrector());
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.PURCHASE);
    insertedMovement.setTipofac(TipoFactura.COMPRA_INTERNACIONAL);
    insertedMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    insertedMovement.setNumaut(movimientoTecnocom10.getNumAut());
    insertedMovement.setCodcom(movimientoTecnocom10.getCodCom());
    insertedMovement.setCuenta(movimientoTecnocom10.getCuenta());
    insertedMovement = createPrepaidMovement11(insertedMovement);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista en la BD en estado AUTHORIZED
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe exitir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK + fecha de conciliacion tiene que ser "ahora")
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());
  }

  @Test
  public void processTecnocomTableData_whenMovInDBIsNotifiedAndFileIsOP_movIsInsertedAndLiqAccIsInsertedMustPendingOKState() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Inserta el movimiento que vino en el archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    movimientoTecnocom10.setTipoFac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(movimientoTecnocom10.getTipoFac().getCorrector());
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(1.005))); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    ipmMovement10 = createIpmMovement(ipmMovement10);

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    insertedMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    insertedMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    insertedMovement.setNumaut(movimientoTecnocom10.getNumAut());
    insertedMovement.setCodcom(movimientoTecnocom10.getCodCom());
    insertedMovement.setCuenta(movimientoTecnocom10.getCuenta());
    insertedMovement.setNomcomred("PruebaComercioNombre");
    insertedMovement = createPrepaidMovement11(insertedMovement);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista en la BD en estado PROCESS_OK
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe exitir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK + fecha de conciliacion tiene que ser "ahora")
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertTrue("Debe tener fecha conciliacion", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(),5));

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // El movimiento ha pasado a OP, verificar que su valor de contabilidad mastercard ha sido actualizado con el del IPM
    Assert.assertEquals("Deben tener mismo valor", ipmMovement10.getCardholderBillingAmount().setScale(2, RoundingMode.HALF_UP), acc.getAmountMastercard().getValue().setScale(2, RoundingMode.HALF_UP));

    // El movimiento en IPM debe pasar a conciliado
    IpmMovement10 foundIpmMovement = getIpmMovementById(ipmMovement10.getId());
    Assert.assertTrue("Debe estar en estado reconciled", foundIpmMovement.getReconciled());
  }

  @Test
  public void processTecnocomTableData_whenMovInDBIsAuthorizedAndFileIsOP_movIsUpdatedAndLiqAccIsInsertedMustPendingOKState() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Inserta el movimiento que vino en el archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    movimientoTecnocom10.setTipoFac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(movimientoTecnocom10.getTipoFac().getCorrector());
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(0.995))); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    ipmMovement10 = createIpmMovement(ipmMovement10);

    // Se inserta un movimiento en estado AUTHORIZED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    insertedMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    insertedMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    insertedMovement.setNumaut(movimientoTecnocom10.getNumAut());
    insertedMovement.setCodcom(movimientoTecnocom10.getCodCom());
    insertedMovement.setCuenta(movimientoTecnocom10.getCuenta());
    insertedMovement = createPrepaidMovement11(insertedMovement);

    AccountingData10 accdata = buildRandomAccouting();
    accdata.setIdTransaction(insertedMovement.getId());
    accdata.setStatus(AccountingStatusType.PENDING);
    accdata.setAccountingStatus(AccountingStatusType.PENDING);

    getPrepaidAccountingEJBBean10().saveAccountingData(null, accdata);

    ClearingData10 liqInsert = createClearingData(accdata, AccountingStatusType.INITIAL);

    getPrepaidClearingEJBBean10().insertClearingData(null, liqInsert);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista en la BD en estado PROCESS_OK
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertNotNull("Debe exitir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK + fecha de conciliacion tiene que ser "ahora")
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertTrue("Debe tener fecha conciliacion", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(),5));

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // El movimiento ha pasado a OP, verificar que su valor de contabilidad mastercard ha sido actualizado con el del IPM
    Assert.assertEquals("Deben tener mismo valor", ipmMovement10.getCardholderBillingAmount().setScale(2, RoundingMode.HALF_UP), acc.getAmountMastercard().getValue().setScale(2, RoundingMode.HALF_UP));

    // El movimiento en IPM debe pasar a conciliado
    IpmMovement10 foundIpmMovement = getIpmMovementById(ipmMovement10.getId());
    Assert.assertTrue("Debe estar en estado reconciled", foundIpmMovement.getReconciled());
  }

  private void prepareCalculateFeesMock(BigDecimal amount) throws TimeoutException, BaseException {
    // Prepara un mock del servicio de fees
    BigDecimal expectedPrepaidFee = amount.multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.81)));
    BigDecimal expectedIvaFee = amount.multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.19)));

    List<Charge> chargesList = new ArrayList<>();

    Charge prepaidCharge = new Charge();
    prepaidCharge.setChargeType(ChargeType.COMMISSION);
    prepaidCharge.setAmount(expectedPrepaidFee.longValue());
    chargesList.add(prepaidCharge);

    Charge ivaCharge = new Charge();
    ivaCharge.setChargeType(ChargeType.IVA);
    ivaCharge.setAmount(expectedIvaFee.longValue());
    chargesList.add(ivaCharge);

    // Prepara una fee esperada para que devuelva el servicio
    Fee returnedFee = new Fee();
    returnedFee.setTotal(expectedPrepaidFee.add(expectedIvaFee).longValue());
    returnedFee.setCharges(chargesList);

    // Setea que calculaFees() como mock para que devuelva la fee esperada
    FeeService mockFeeService = Mockito.mock(FeeService.class);
    Mockito.doReturn(returnedFee).when(mockFeeService).calculateFees(Mockito.any(), Mockito.any(), Mockito.any());
    getTecnocomReconciliationEJBBean10().setFeeService(mockFeeService);
  }

  private PrepaidMovement10 getPrepaidMovement(PrepaidMovementType movementType, TipoFactura tipofac, String numaut, String pan, String codcom) throws Exception {
    List<PrepaidMovement10> prepaidMovement10s = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null, null, movementType,
      null, null, null, null, tipofac, null, numaut, null, null, null, pan, codcom);
    return (prepaidMovement10s != null && !prepaidMovement10s.isEmpty()) ? prepaidMovement10s.get(0) : null;
  }

  private List<ReconciliationFile10> createReconciliationFiles(int numberOfFiles) throws Exception {
    ArrayList<ReconciliationFile10> reconciliationFile10s = new ArrayList<>();
    for (int i = 0; i < numberOfFiles; i++) {
      ReconciliationFile10 newReconciliationFile10 = new ReconciliationFile10();
      newReconciliationFile10.setStatus(FileStatus.OK);
      newReconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);
      newReconciliationFile10.setFileName(getRandomString(10));
      newReconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      newReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);
      reconciliationFile10s.add(newReconciliationFile10);
    }
    return reconciliationFile10s;
  }

  private void deleteReconciliationFiles(List<ReconciliationFile10> reconciliationFile10s) {
    for (ReconciliationFile10 reconciliationFile10 : reconciliationFile10s) {
      getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_archivos_conciliacion WHERE id = %d", getSchema(), reconciliationFile10.getId()));
    }
  }

  MovimientoTecnocom10 createMovimientoTecnocom(Long fileId) {
    MovimientoTecnocom10 registroTecnocom = new MovimientoTecnocom10();
    registroTecnocom.setIdArchivo(fileId);
    registroTecnocom.setNumAut(getRandomNumericString(6));
    registroTecnocom.setTipoFac(TipoFactura.COMPRA_INTERNACIONAL);
    registroTecnocom.setIndNorCor(IndicadorNormalCorrector.NORMAL.getValue());
    registroTecnocom.setPan(prepaidCard.getHashedPan());
    registroTecnocom.setCentAlta("fill");
    registroTecnocom.setClamone(CodigoMoneda.USA_USD);
    registroTecnocom.setCmbApli(new BigDecimal(1L));
    registroTecnocom.setCodAct(0);
    registroTecnocom.setCodCom(getRandomString(15));
    registroTecnocom.setCodEnt(getRandomString(4));
    registroTecnocom.setCodPais(CodigoPais.CHILE.getValue());
    registroTecnocom.setContrato(account.getAccountNumber());
    registroTecnocom.setCuenta(getRandomNumericString(10));
    registroTecnocom.setFecFac(LocalDate.now(ZoneId.of("UTC")));
    registroTecnocom.setFecTrn(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
    registroTecnocom.setImpautcon(new NewAmountAndCurrency10(new BigDecimal(5000L)));
    registroTecnocom.setImpDiv(new NewAmountAndCurrency10(new BigDecimal(5000L)));
    registroTecnocom.setImpFac(new NewAmountAndCurrency10(new BigDecimal(5000L)));
    registroTecnocom.setImpLiq(new NewAmountAndCurrency10(new BigDecimal(5000L)));
    registroTecnocom.setIndProaje("a");
    registroTecnocom.setLinRef(1);
    registroTecnocom.setNomPob(getRandomString(10));
    registroTecnocom.setNumExtCta(123L);
    registroTecnocom.setNumMovExt(123L);
    registroTecnocom.setNumRefFac(getRandomString(10));
    registroTecnocom.setOriginOpe(OriginOpeType.AUT_ORIGIN.getValue());
    registroTecnocom.setTipoLin(getRandomString(4));
    registroTecnocom.setTipoReg(TecnocomReconciliationRegisterType.OP);
    registroTecnocom.setNomcomred("PruebaNombre");
    return registroTecnocom;
  }

  private ClearingData10 createClearingData(AccountingData10 accountingData10, AccountingStatusType status) {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(status);
    clearing10.setUserBankAccount(userAccount);
    clearing10.setAccountingId(accountingData10.getId());
    return clearing10;
  }
}
