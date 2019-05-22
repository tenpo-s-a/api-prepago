package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.fees.FeeService;
import cl.multicaja.prepaid.helpers.fees.model.Charge;
import cl.multicaja.prepaid.helpers.fees.model.ChargeType;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationRegisterType;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Test_TecnocomReconciliationEJBBean10_insertAutorization extends TestBaseUnitAsync {
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
      Test_TecnocomReconciliationEJBBean10_insertAutorization test = new Test_TecnocomReconciliationEJBBean10_insertAutorization();
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

  // Devolucion (nunca esta en DB, siempre vienen OP)
  @Test
  public void processTecnocomTableData_whenMovNotInDb_IsRefundOp_movIsInsertedAndLiqAccMustExistInFinalState() throws Exception {
    // Inserta movimiento que vino en archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoFac(TipoFactura.DEVOLUCION_COMPRA_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(IndicadorNormalCorrector.NORMAL.getValue());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(0.995))); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    ipmMovement10 = createIpmMovement(ipmMovement10);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado OP
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    // Devoluciones: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", movimientoTecnocom10.getImpFac(), acc.getAmount());

    // Devoluciones: Regla de contabilidad 2: El monto afecto a saldo se llenara con el monto_trx_pesos
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", acc.getAmount(), acc.getAmountBalance());

    // Devoluciones: Regla de contabilidad 3: cuando pase a OP (osea ahora, porque todas las devoluciones vienen OP) debe guardarse fecha de conciliacion
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Devoluciones: Regla de contabilidad 4: cuando pase a OP (osea ahora, porque todas las devoluciones vienen OP) debe guardarse el monto del IPM en mastercard
    Assert.assertEquals("Debe tener monto mastercard = monto ipm", ipmMovement10.getCardholderBillingAmount().setScale(2, RoundingMode.HALF_UP), acc.getAmountMastercard().getValue());

    // Devoluciones: Regla de contabilidad 5: Los valores de Valor Dolar y Dif tipo de cambio deben ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener dif cambio = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Devoluciones: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Devoluciones: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos", acc.getAmount().getValue(), acc.getAmountBalance().getValue());

    // Verificar que los datos enclering existan
    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Verificar que NO se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 0 fees asignadas", 0, prepaidMovementFee10List.size());

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
    Assert.assertEquals("Debe tener el mismo transactiontype", "REFUND", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "AUTHORIZED", transactionEvent.getTransaction().getStatus());
  }

  // Anulacion Compra Internacional en moneda extranjera (DB = no, tipo = OP)
  @Test
  public void processTecnocomTableData_whenMovNotInDB_IsReversedInternationalPurcharseInForeignCoinOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    // Inserta movimiento que vino en archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoFac(TipoFactura.ANULA_COMPRA_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(IndicadorNormalCorrector.CORRECTORA.getValue());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    NewAmountAndCurrency10 impDiv = new NewAmountAndCurrency10();
    impDiv.setValue(movimientoTecnocom10.getImpFac().getValue());
    impDiv.setCurrencyCode(CodigoMoneda.USA_USD);
    movimientoTecnocom10.setImpDiv(impDiv);
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(0.995))); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    ipmMovement10 = createIpmMovement(ipmMovement10);

    // Preparar la respuesta del servicio de fees
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado OP
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener solo 1 fee asignada", 1, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", movimientoTecnocom10.getImpFac(), acc.getAmount());

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 3: cuando pase a OP debe guardarse fecha de conciliacion
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 4: cuando pase a OP (osea ahora, porque todas las devoluciones vienen OP) debe guardarse el monto del IPM en mastercard
    Assert.assertEquals("Debe tener monto mastercard = monto ipm", ipmMovement10.getCardholderBillingAmount().setScale(2, RoundingMode.HALF_UP), acc.getAmountMastercard().getValue());

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 5: El Valor Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Anulaciones internacionales moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Verificar que exista en la cola de eventos transaction_reversed
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidMovement10.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", "PURCHASE", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "REVERSED", transactionEvent.getTransaction().getStatus());
  }

  // Anulacion Compra Internacional en pesos (DB = no, tipo = OP)
  @Test
  public void processTecnocomTableData_whenMovNotInDB_IsReversedInternationalPurcharseInPesosOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    // Inserta movimiento que vino en archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoFac(TipoFactura.ANULA_COMPRA_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(IndicadorNormalCorrector.CORRECTORA.getValue());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    NewAmountAndCurrency10 impDiv = new NewAmountAndCurrency10();
    impDiv.setValue(movimientoTecnocom10.getImpFac().getValue());
    impDiv.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    movimientoTecnocom10.setImpDiv(impDiv);
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(0.995))); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    ipmMovement10 = createIpmMovement(ipmMovement10);

    // Preparar la respuesta del servicio de fees
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado OP
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

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

    // Anulaciones internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", movimientoTecnocom10.getImpFac(), acc.getAmount());

    // Anulaciones internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
    BigDecimal totalFees = BigDecimal.ZERO;
    BigDecimal totalFeesIva = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      if (PrepaidMovementFeeType.IVA.equals(fee.getFeeType())) {
        totalFeesIva = totalFeesIva.add(fee.getAmount());
      } else {
        totalFees = totalFees.add(fee.getAmount());
      }
    }
    Assert.assertEquals("todas las fees se suman y se guardan en fee", totalFees, acc.getFee());
    Assert.assertEquals("todas los ivas se suman y se guardan en feeIva", totalFeesIva, acc.getFeeIva());

    // Anulaciones internacionales en pesos: Regla de contabilidad 3: cuando pase a OP debe guardarse fecha de conciliacion
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Anulaciones internacionales en pesos: Regla de contabilidad 4: cuando pase a OP (osea ahora, porque todas las devoluciones vienen OP) debe guardarse el monto del IPM en mastercard
    Assert.assertEquals("Debe tener monto mastercard = monto ipm", ipmMovement10.getCardholderBillingAmount().setScale(2, RoundingMode.HALF_UP), acc.getAmountMastercard().getValue());

    // Anulaciones internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Anulaciones internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Anulaciones internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Verificar que exista en la cola de eventos transaction_reversed
    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidMovement10.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", "PURCHASE", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", "REVERSED", transactionEvent.getTransaction().getStatus());
  }

  private void prepareCalculateFeesMock(BigDecimal amount, boolean chargesIva) throws TimeoutException, BaseException {
    // Prepara un mock del servicio de fees
    BigDecimal expectedPrepaidFee = BigDecimal.ZERO;
    BigDecimal expectedIvaFee = BigDecimal.ZERO;

    List<Charge> chargesList = new ArrayList<>();

    if (chargesIva) {
      //Divide el 2% en iva y comision
      expectedPrepaidFee = amount.multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.81)));
      expectedIvaFee = amount.multiply(new BigDecimal(0.02).multiply(new BigDecimal(0.19)));

      // Agrega cargo de iva
      Charge prepaidCharge = new Charge();
      prepaidCharge.setChargeType(ChargeType.IVA);
      prepaidCharge.setAmount(expectedIvaFee.longValue());
      chargesList.add(prepaidCharge);
    } else {
      // El 2% es comision
      expectedPrepaidFee = amount.multiply(new BigDecimal(0.02));
    }

    Charge prepaidCharge = new Charge();
    prepaidCharge.setChargeType(ChargeType.COMMISSION);
    prepaidCharge.setAmount(expectedPrepaidFee.longValue());
    chargesList.add(prepaidCharge);

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
