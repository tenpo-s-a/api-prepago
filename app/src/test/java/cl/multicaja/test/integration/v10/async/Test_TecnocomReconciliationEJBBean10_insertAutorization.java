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

  @Test
  public void processTecnocomTableData_whenMovIsRefund_movIsInsertedAndLiqAccMustExistInFinalState() throws Exception {
    // Inserta movimiento que vino en archivo OP
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoFac(TipoFactura.DEVOLUCION_COMPRA_INTERNACIONAL);
    movimientoTecnocom10.setIndNorCor(IndicadorNormalCorrector.NORMAL.getValue());
    movimientoTecnocom10.setTipoReg(TecnocomReconciliationRegisterType.OP);
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setTransactionAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(0.995))); // Alterar levemente el valor para que se reescriba
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

    // Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", movimientoTecnocom10.getImpFac(), acc.getAmount());

    // Regla de contabilidad 2: El monto afecto a saldo se llenara con el monto_trx_pesos
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", acc.getAmount(), acc.getAmountBalance());

    // Regla de contabilidad 3: cuando pase a OP (osea ahora, porque todas las devoluciones vienen OP) debe guardarse fecha de conciliacion
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Regla de contabilidad 4: cuando pase a OP (osea ahora, porque todas las devoluciones vienen OP) debe guardarse el monto del IPM en mastercard
    Assert.assertEquals("Debe tener monto mastercard = monto ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
/*
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
*/
    // Verificar que NO se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 0 fees asignadas", 0, prepaidMovementFee10List.size());
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
