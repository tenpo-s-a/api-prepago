package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
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
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
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
      PrepaidMovement10 prepaidMovement = test.buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP, false);
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
  public void processTecnocomTableData_expireNotifiedSuscription() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP,false);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    insertedMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, true);

    // Crea 1 archivo extra para que se expire el movimiento
    List<ReconciliationFile10> createdFiles = createReconciliationFiles(1);

    // Se inserta un movimiento extra en estado NOTIFIED, este no debe expirar, ya que solo tendra 1 archivo
    PrepaidMovement10 doNotExpireMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    doNotExpireMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    doNotExpireMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    doNotExpireMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    doNotExpireMovement = createPrepaidMovement11(doNotExpireMovement);

    // Crea 1 archivo extra para que se expire el movimiento
    List<ReconciliationFile10> extraFiles = createReconciliationFiles(1);

    // Como hay dos archivos tecnocom en la tabla, debe expirar el primer movimiento NOTIFIED
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe haber cambiado estado con tecnocom a NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, foundMovement.getConTecnocom());
    Assert.assertEquals("Debe haber cambiado a estado EXPIRED", PrepaidMovementStatus.EXPIRED, foundMovement.getEstado());

    // Este movimiento no debe cambiar sus estados, dado que no han pasado suficientes archivos (solo 1)
    PrepaidMovement10 foundNotExpiredMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(doNotExpireMovement.getId());
    Assert.assertEquals("Debe seguir en estado_con_ecnocom PENDING", ReconciliationStatusType.PENDING, foundNotExpiredMovement.getConTecnocom());
    Assert.assertEquals("Debe seguir en estado NOTIFIED", PrepaidMovementStatus.NOTIFIED, foundNotExpiredMovement.getEstado());

    deleteReconciliationFiles(createdFiles);
    deleteReconciliationFiles(extraFiles);

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC, foundMovement.getIdTxExterno(), "SUSCRIPTION", "REVERSED", prepaidMovementFee10List);
  }

  @Test
  public void processTecnocomTableData_expireNotifiedPurchase() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.PURCHASE);
    insertedMovement.setTipofac(TipoFactura.COMPRA_INTERNACIONAL);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, true);

    // Crea 1 archivo extra para que se expire el movimiento
    List<ReconciliationFile10> createdFiles = createReconciliationFiles(1);

    // Se inserta un movimiento extra en estado NOTIFIED, este no debe expirar, ya que solo tendra 1 archivo
    PrepaidMovement10 doNotExpireMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    doNotExpireMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    doNotExpireMovement.setTipoMovimiento(PrepaidMovementType.PURCHASE);
    doNotExpireMovement.setTipofac(TipoFactura.COMPRA_INTERNACIONAL);
    doNotExpireMovement = createPrepaidMovement11(doNotExpireMovement);

    // Crea 1 archivo extra para que se expire el movimiento
    List<ReconciliationFile10> extraFiles = createReconciliationFiles(1);

    // Como hay dos archivos tecnocom en la tabla, debe expirar el primer movimiento NOTIFIED
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe haber cambiado estado con tecnocom a NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, foundMovement.getConTecnocom());
    Assert.assertEquals("Debe haber cambiado a estado EXPIRED", PrepaidMovementStatus.EXPIRED, foundMovement.getEstado());

    // Este movimiento no debe cambiar sus estados, dado que no han pasado suficientes archivos (solo 1)
    PrepaidMovement10 foundNotExpiredMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(doNotExpireMovement.getId());
    Assert.assertEquals("Debe seguir en estado_con_ecnocom PENDING", ReconciliationStatusType.PENDING, foundNotExpiredMovement.getConTecnocom());
    Assert.assertEquals("Debe seguir en estado NOTIFIED", PrepaidMovementStatus.NOTIFIED, foundNotExpiredMovement.getEstado());

    deleteReconciliationFiles(createdFiles);
    deleteReconciliationFiles(extraFiles);

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC, foundMovement.getIdTxExterno(), "PURCHASE", "REVERSED", prepaidMovementFee10List);
  }

  @Test
  public void processTecnocomTableData_expireAuthorizedSuscription() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    insertedMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, true);

    AccountingData10 accdata = buildRandomAccouting();
    accdata.setIdTransaction(insertedMovement.getId());
    accdata.setStatus(AccountingStatusType.PENDING);
    accdata.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accdata);

    ClearingData10 liqInsert = createClearingData(accdata, AccountingStatusType.INITIAL);
    getPrepaidClearingEJBBean10().insertClearingData(null, liqInsert);

    // Crea 1 archivos extra para que se expire el movimiento
    List<ReconciliationFile10> createdFiles = createReconciliationFiles(1);

    // Se inserta un movimiento en estado AUTHORIZED que no expirarara, ya que solo tiene 6 archivos entre medio
    PrepaidMovement10 doNotExpireMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    doNotExpireMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    doNotExpireMovement.setTipoMovimiento(PrepaidMovementType.SUSCRIPTION);
    doNotExpireMovement.setTipofac(TipoFactura.SUSCRIPCION_INTERNACIONAL);
    doNotExpireMovement = createPrepaidMovement11(doNotExpireMovement);

    // Crea 6 archivos extra para que se expire el movimiento original
    List<ReconciliationFile10> extraFiles = createReconciliationFiles(6);

    // Como hay 7 archivos tecnocom en la tabla, debe expirar el movimiento original AUTHORIZED
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe haber cambiado estado con tecnocom a NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, foundMovement.getConTecnocom());
    Assert.assertEquals("Debe haber cambiado a estado EXPIRED", PrepaidMovementStatus.EXPIRED, foundMovement.getEstado());

    // Este movimiento no debe cambiar sus estados, dado que no han pasado suficientes archivos (solo 1)
    PrepaidMovement10 foundNotExpiredMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(doNotExpireMovement.getId());
    Assert.assertEquals("Debe seguir en estado_con_ecnocom PENDING", ReconciliationStatusType.PENDING, foundNotExpiredMovement.getConTecnocom());
    Assert.assertEquals("Debe seguir en estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, foundNotExpiredMovement.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,foundMovement.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado NOT_OK", AccountingStatusType.NOT_OK, acc.getAccountingStatus());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado NOT_SEND", AccountingStatusType.NOT_SEND, liq.getStatus());

    deleteReconciliationFiles(createdFiles);
    deleteReconciliationFiles(extraFiles);

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC, foundMovement.getIdTxExterno(), "SUSCRIPTION", "REVERSED", prepaidMovementFee10List);
  }

  @Test
  public void processTecnocomTableData_expireAuthorizedPurchase() throws Exception {
    PrepaidTopup10 topup = buildPrepaidTopup10();

    // Se inserta un movimiento en estado NOTIFIED
    PrepaidMovement10 insertedMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement.setTipoMovimiento(PrepaidMovementType.PURCHASE);
    insertedMovement.setTipofac(TipoFactura.COMPRA_INTERNACIONAL);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, true);

    AccountingData10 accdata = buildRandomAccouting();
    accdata.setIdTransaction(insertedMovement.getId());
    accdata.setStatus(AccountingStatusType.PENDING);
    accdata.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accdata);

    ClearingData10 liqInsert = createClearingData(accdata, AccountingStatusType.INITIAL);
    getPrepaidClearingEJBBean10().insertClearingData(null, liqInsert);

    // Crea 1 archivos extra para que se expire el movimiento
    List<ReconciliationFile10> createdFiles = createReconciliationFiles(1);

    // Se inserta un movimiento en estado AUTHORIZED que no expirarara, ya que solo tiene 6 archivos entre medio
    PrepaidMovement10 doNotExpireMovement = buildPrepaidMovement11(prepaidUser, topup, prepaidCard, null, PrepaidMovementType.TOPUP, false);
    doNotExpireMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    doNotExpireMovement.setTipoMovimiento(PrepaidMovementType.PURCHASE);
    doNotExpireMovement.setTipofac(TipoFactura.COMPRA_INTERNACIONAL);
    doNotExpireMovement = createPrepaidMovement11(doNotExpireMovement);

    // Crea 6 archivos extra para que se expire el movimiento original
    List<ReconciliationFile10> extraFiles = createReconciliationFiles(6);

    // Como hay 7 archivos tecnocom en la tabla, debe expirar el movimiento original AUTHORIZED
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe haber cambiado estado con tecnocom a NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, foundMovement.getConTecnocom());
    Assert.assertEquals("Debe haber cambiado a estado EXPIRED", PrepaidMovementStatus.EXPIRED, foundMovement.getEstado());

    // Este movimiento no debe cambiar sus estados, dado que no han pasado suficientes archivos (solo 1)
    PrepaidMovement10 foundNotExpiredMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(doNotExpireMovement.getId());
    Assert.assertEquals("Debe seguir en estado_con_ecnocom PENDING", ReconciliationStatusType.PENDING, foundNotExpiredMovement.getConTecnocom());
    Assert.assertEquals("Debe seguir en estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, foundNotExpiredMovement.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,foundMovement.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado NOT_OK", AccountingStatusType.NOT_OK, acc.getAccountingStatus());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado NOT_SEND", AccountingStatusType.NOT_SEND, liq.getStatus());

    deleteReconciliationFiles(createdFiles);
    deleteReconciliationFiles(extraFiles);

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC, foundMovement.getIdTxExterno(), "PURCHASE", "REVERSED", prepaidMovementFee10List);
  }

  // Compra en dolares, BD = NO, file = AU
  @Test
  public void processTecnocomTableData_whenMovPurchaseInForeignCoin_NotInDBAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.USA_USD);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 1 fee asignada", 1, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_MONEDA", AccountingTxType.COMPRA_MONEDA, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_MONEDA", AccountingMovementType.COMPRA_MONEDA, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "PURCHASE", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Compra en pesos, BD = NO, file = AU
  @Test
  public void processTecnocomTableData_whenMovPurchaseInPesos_NotInDBAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.CHILE_CLP);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 2 fee asignada", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.84));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de iva", ivaFee);
    BigDecimal expectedIvaFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.16));
    Assert.assertEquals("Debe tener un valor de ", expectedIvaFee.setScale(0, RoundingMode.HALF_UP), ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_PESOS", AccountingTxType.COMPRA_PESOS, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_PESOS", AccountingMovementType.COMPRA_PESOS, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
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

    // Compras Internacionales en pesos: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en pesos: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "PURCHASE", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Compra en dolares, BD = NO, file = OP
  @Test
  public void processTecnocomTableData_whenMovPurchaseInForeignCoin_NotInDBAndFileStateIsOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.USA_USD);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_OK
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 1 fee asignada", 1, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_MONEDA", AccountingTxType.COMPRA_MONEDA, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_MONEDA", AccountingMovementType.COMPRA_MONEDA, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "PURCHASE", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Compra en pesos, BD = NO, file = OP
  @Test
  public void processTecnocomTableData_whenMovPurchaseInPesos_NotInDBAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.CHILE_CLP);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 2 fee asignada", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.PURCHASE_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.84));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de iva", ivaFee);
    BigDecimal expectedIvaFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.16));
    Assert.assertEquals("Debe tener un valor de ", expectedIvaFee.setScale(0, RoundingMode.HALF_UP), ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (OK y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_PESOS", AccountingTxType.COMPRA_PESOS, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_PESOS", AccountingMovementType.COMPRA_PESOS, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
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

    // Compras Internacionales en pesos: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en pesos: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "PURCHASE", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Compra en dolares, BD = NOTIFIED, file = AU
  @Test
  public void processTecnocomTableData_whenMovPurchaseInForeignCoin_InDBNotifiedAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.USA_USD);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.PURCHASE_INT_FEE, false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_MONEDA", AccountingTxType.COMPRA_MONEDA, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_MONEDA", AccountingMovementType.COMPRA_MONEDA, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());
  }

  // Compra en pesos, BD = NOTIFIED, file = AU
  @Test
  public void processTecnocomTableData_whenMovPurchaseInPesos_InDBNotifiedAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.CHILE_CLP);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.PURCHASE_INT_FEE, true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_PESOS", AccountingTxType.COMPRA_PESOS, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_PESOS", AccountingMovementType.COMPRA_PESOS, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
    BigDecimal totalFees = BigDecimal.ZERO;
    BigDecimal totalFeesIva = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      if (PrepaidMovementFeeType.IVA.equals(fee.getFeeType())) {
        totalFeesIva = totalFeesIva.add(fee.getAmount());
      } else {
        totalFees = totalFees.add(fee.getAmount());
      }
    }
    Assert.assertEquals("todas las fees se suman y se guardan en fee", totalFees.stripTrailingZeros(), acc.getFee().stripTrailingZeros());
    Assert.assertEquals("todas los ivas se suman y se guardan en feeIva", totalFeesIva.stripTrailingZeros(), acc.getFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en pesos: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());
  }

  // Compra en dolares, BD = NOTIFIED, file = OP
  @Test
  public void processTecnocomTableData_whenMovPurchaseInForeignCoin_InDBNotifiedAndFileStateIsOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.USA_USD);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.PURCHASE_INT_FEE, false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_MONEDA", AccountingTxType.COMPRA_MONEDA, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_MONEDA", AccountingMovementType.COMPRA_MONEDA, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Compra en pesos, BD = NOTIFIED, file = OP
  @Test
  public void processTecnocomTableData_whenMovPurchaseInPesos_InDBNotifiedAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.CHILE_CLP);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.PURCHASE_INT_FEE, true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_Ok", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (OK y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo COMPRA_PESOS", AccountingTxType.COMPRA_PESOS, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov COMPRA_PESOS", AccountingMovementType.COMPRA_PESOS, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
    BigDecimal totalFees = BigDecimal.ZERO;
    BigDecimal totalFeesIva = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      if (PrepaidMovementFeeType.IVA.equals(fee.getFeeType())) {
        totalFeesIva = totalFeesIva.add(fee.getAmount());
      } else {
        totalFees = totalFees.add(fee.getAmount());
      }
    }
    Assert.assertEquals("todas las fees se suman y se guardan en fee", totalFees.stripTrailingZeros(), acc.getFee().stripTrailingZeros());
    Assert.assertEquals("todas los ivas se suman y se guardan en feeIva", totalFeesIva.stripTrailingZeros(), acc.getFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en pesos: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Compra en dolares, BD = AUTHORIZED, file = OP
  @Test
  public void processTecnocomTableData_whenMovPurchaseInForeignCoin_InDBAuthorizedAndFileStateIsOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.USA_USD);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Inserta los registros en la tabla de contabilidad en estado PENDING Y PENDING
    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setIdTransaction(insertedMovement.getId());
    accountingData10.setStatus(AccountingStatusType.PENDING);
    accountingData10.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    // Insertar los registros en la tabla de liquidacion en estado INITIAL
    ClearingData10 clearingData10 = createClearingData(accountingData10, AccountingStatusType.INITIAL);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Compra en pesos, BD = AUTHORIZED, file = OP
  @Test
  public void processTecnocomTableData_whenMovPurchaseInPesos_InDBAuthorizedAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.COMPRA_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.CHILE_CLP);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Inserta los registros en la tabla de contabilidad en estado PENDING Y PENDING
    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setIdTransaction(insertedMovement.getId());
    accountingData10.setStatus(AccountingStatusType.PENDING);
    accountingData10.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    // Insertar los registros en la tabla de liquidacion en estado INITIAL
    ClearingData10 clearingData10 = createClearingData(accountingData10, AccountingStatusType.INITIAL);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_Ok", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (OK y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    // Compras Internacionales en pesos: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en pesos: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Suscripcion en dolares, BD = NO, file = AU
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInForeignCoin_NotInDBAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.USA_USD);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 1 fee asignada", 1, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.SUSCRIPTION_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "SUSCRIPTION", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Suscripcion en pesos, BD = NO, file = AU
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInPesos_NotInDBAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.CHILE_CLP);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 2 fee asignada", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.SUSCRIPTION_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.84));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de iva", ivaFee);
    BigDecimal expectedIvaFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.16));
    Assert.assertEquals("Debe tener un valor de ", expectedIvaFee.setScale(0, RoundingMode.HALF_UP), ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
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

    // Compras Internacionales en pesos: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en pesos: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "SUSCRIPTION", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Suscription en dolares, BD = NO, file = OP
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInForeignCoin_NotInDBAndFileStateIsOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.USA_USD);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_OK
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 1 fee asignada", 1, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.SUSCRIPTION_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "SUSCRIPTION", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Suscription en pesos, BD = NO, file = OP
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInPesos_NotInDBAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.CHILE_CLP);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Prepara un mock del servicio de fees, para que retorne las fees esperadas
    prepareCalculateFeesMock(movimientoTecnocom10.getImpFac().getValue(), true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovement(movimientoTecnocom10.getMovementType(), movimientoTecnocom10.getTipoFac(), movimientoTecnocom10.getNumAut(), prepaidCard.getPan(), movimientoTecnocom10.getCodCom());
    Assert.assertNotNull("Debe existir el nuevo movimiento en la BD", prepaidMovement10);
    Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que se hayan creado las comisiones del nuevo movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener 2 fee asignada", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 prepaidFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.SUSCRIPTION_INT_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de compra internacional", prepaidFee);
    BigDecimal expectedPrepaidFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.84));
    Assert.assertEquals("Debe tener un valor de ", expectedPrepaidFee.setScale(0, RoundingMode.HALF_UP), prepaidFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee de iva", ivaFee);
    BigDecimal expectedIvaFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).multiply(new BigDecimal(0.16));
    Assert.assertEquals("Debe tener un valor de ", expectedIvaFee.setScale(0, RoundingMode.HALF_UP), ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (OK y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
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

    // Compras Internacionales en pesos: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en pesos: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());

    // Revisar que exista el evento reversado en la cola kafka
    checkIfTransactionIsInQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC, prepaidMovement10.getIdTxExterno(), "SUSCRIPTION", "AUTHORIZED", prepaidMovementFee10List);
  }

  // Suscription en dolares, BD = NOTIFIED, file = AU
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInForeignCoin_InDBNotifiedAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.USA_USD);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());
  }

  // Suscription en pesos, BD = NOTIFIED, file = AU
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInPesos_InDBNotifiedAndFileStateIsAU_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.AU, CodigoMoneda.CHILE_CLP);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado AU
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado AUTHORIZED", PrepaidMovementStatus.AUTHORIZED, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (INITIAL y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
    BigDecimal totalFees = BigDecimal.ZERO;
    BigDecimal totalFeesIva = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      if (PrepaidMovementFeeType.IVA.equals(fee.getFeeType())) {
        totalFeesIva = totalFeesIva.add(fee.getAmount());
      } else {
        totalFees = totalFees.add(fee.getAmount());
      }
    }
    Assert.assertEquals("todas las fees se suman y se guardan en fee", totalFees.stripTrailingZeros(), acc.getFee().stripTrailingZeros());
    Assert.assertEquals("todas los ivas se suman y se guardan en feeIva", totalFeesIva.stripTrailingZeros(), acc.getFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 3: no debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion futura (+1000 años)", acc.getConciliationDate().after(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")).plusYears(999))));

    // Compras Internacionales en pesos: Regla de contabilidad 4: no esta en OP, no debe tener monto mastercard
    Assert.assertEquals("Debe tener monto mastercard = zero", BigDecimal.ZERO, acc.getAmountMastercard().getValue().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado INITIAL", AccountingStatusType.INITIAL, liq.getStatus());
  }

  // Suscription en dolares, BD = NOTIFIED, file = OP
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInForeignCoin_InDBNotifiedAndFileStateIsOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.USA_USD);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, false);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 2: Dif tipo cambio debe rellenarse con la suma de las comisiones
    BigDecimal totalFees = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      totalFees = totalFees.add(fee.getAmount());
    }
    Assert.assertEquals("todas las fees se suman y se guardan en dif tipo cambio", totalFees, acc.getExchangeRateDif());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 5: El Monto Dolar debe ser zero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 6: Todos los valores de fees deben ser zero
    Assert.assertEquals("Debe tener fee = zero", BigDecimal.ZERO, acc.getFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener feeIva = zero", BigDecimal.ZERO, acc.getFeeIva().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + dif tipo cambio
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + dif cambio", acc.getAmount().getValue().add(acc.getExchangeRateDif()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Suscription en pesos, BD = NOTIFIED, file = OP
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInPesos_InDBNotifiedAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.CHILE_CLP);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.NOTIFIED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Insertar los fees del movimiento
    List<PrepaidMovementFee10> prepaidMovementFee10List = prepareFees(insertedMovement, PrepaidMovementFeeType.SUSCRIPTION_INT_FEE, true);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_Ok", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (OK y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en accounting", acc);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());
    Assert.assertEquals("Debe ser del tipo SUSCRIPCION", AccountingTxType.COMPRA_SUSCRIPCION, acc.getType());
    Assert.assertEquals("Debe ser del tipo mov SUSCRIPCION", AccountingMovementType.SUSCRIPCION, acc.getAccountingMovementType());

    // Compras Internacionales en pesos: Regla de contabilidad 1: El monto_trx_pesos se llenara con el monto del archivo de operacion impfac
    Assert.assertEquals("impfac de tecnocom debe ser igual al de contabilidad", prepaidMovement10.getImpfac().setScale(2, RoundingMode.HALF_UP), acc.getAmount().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 2: Las fees sumadas se deben guardar en Fee, los ivas en FeeIva
    BigDecimal totalFees = BigDecimal.ZERO;
    BigDecimal totalFeesIva = BigDecimal.ZERO;
    for (PrepaidMovementFee10 fee : prepaidMovementFee10List) {
      if (PrepaidMovementFeeType.IVA.equals(fee.getFeeType())) {
        totalFeesIva = totalFeesIva.add(fee.getAmount());
      } else {
        totalFees = totalFees.add(fee.getAmount());
      }
    }
    Assert.assertEquals("todas las fees se suman y se guardan en fee", totalFees.stripTrailingZeros(), acc.getFee().stripTrailingZeros());
    Assert.assertEquals("todas los ivas se suman y se guardan en feeIva", totalFeesIva.stripTrailingZeros(), acc.getFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en pesos: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    // Compras Internacionales en pesos: Regla de contabilidad 5: El Valor Dolar debe ser zero, el valor de dif tipo de cambio debe ser cero
    Assert.assertEquals("Debe tener monto dolar = zero", BigDecimal.ZERO, acc.getAmountUsd().getValue().stripTrailingZeros());
    Assert.assertEquals("dif tipo cambio debe ser = zero", BigDecimal.ZERO, acc.getExchangeRateDif().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 6: Todos los valores de fee collector deben ser zero
    Assert.assertEquals("Debe tener collectorFee = zero", BigDecimal.ZERO, acc.getCollectorFee().stripTrailingZeros());
    Assert.assertEquals("Debe tener collectorFeeIva = zero", BigDecimal.ZERO, acc.getCollectorFeeIva().stripTrailingZeros());

    // Compras Internacionales en pesos: Regla de contabilidad 7: El monto afecto a saldo debe ser igual al monto_trx_peso + fee + ivas
    Assert.assertEquals("Debe tener monto afecto a saldo = monto trx pesos + fee + feeIva", acc.getAmount().getValue().add(acc.getFee()).add(acc.getFeeIva()), acc.getAmountBalance().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertNotNull("Debe existir en clearing", liq);
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Suscription en dolares, BD = AUTHORIZED, file = OP
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInForeignCoin_InDBAuthorizedAndFileStateIsOp_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.USA_USD);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Inserta los registros en la tabla de contabilidad en estado PENDING Y PENDING
    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setIdTransaction(insertedMovement.getId());
    accountingData10.setStatus(AccountingStatusType.PENDING);
    accountingData10.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    // Insertar los registros en la tabla de liquidacion en estado INITIAL
    ClearingData10 clearingData10 = createClearingData(accountingData10, AccountingStatusType.INITIAL);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (PENDING y OK)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en moneda extranjera: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  // Suscription en pesos, BD = AUTHORIZED, file = OP
  @Test
  public void processTecnocomTableData_whenMovSuscriptionInPesos_InDBAuthorizedAndFileStateIsOP_movIsInsertedAndLiqAccMustExistInInitialState() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = prepareMovimientoTecnocom(TipoFactura.SUSCRIPCION_INTERNACIONAL, TecnocomReconciliationRegisterType.OP, CodigoMoneda.CHILE_CLP);

    // Insertar el movimiento en la BD
    PrepaidMovement10 insertedMovement = buildPrepaidMovementFromTecnocomMovement(movimientoTecnocom10);
    insertedMovement.setEstado(PrepaidMovementStatus.AUTHORIZED);
    insertedMovement = createPrepaidMovement11(insertedMovement);

    // Inserta el movimiento que vino en el archivo IPM (para hacer un match, y reescribir su valor)
    IpmMovement10 ipmMovement10 = prepareIpmMovement(movimientoTecnocom10);

    // Inserta los registros en la tabla de contabilidad en estado PENDING Y PENDING
    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setIdTransaction(insertedMovement.getId());
    accountingData10.setStatus(AccountingStatusType.PENDING);
    accountingData10.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    // Insertar los registros en la tabla de liquidacion en estado INITIAL
    ClearingData10 clearingData10 = createClearingData(accountingData10, AccountingStatusType.INITIAL);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    // Verificar que exista el movimiento nuevo en la BD en estado Process_Ok
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean11().getPrepaidMovementById(insertedMovement.getId());
    Assert.assertEquals("Debe tener estado Process_Ok", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());

    // Verificar que exista en la tablas de contabilidad (acc y liq) en sus estados (OK y PENDING)
    AccountingData10 acc = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null,prepaidMovement10.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, acc.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, acc.getAccountingStatus());

    // Compras Internacionales en pesos: Regla de contabilidad 3: debe tener fecha de conciliacion reciente
    Assert.assertTrue("Debe tener fecha de conciliacion reciente", isRecentLocalDateTime(acc.getConciliationDate().toLocalDateTime(), 5));

    // Compras Internacionales en pesos: Regla de contabilidad 4: esta en OP, debe tener monto mastercard corregido por ipm
    Assert.assertEquals("Debe tener monto mastercard = ipm", ipmMovement10.getCardholderBillingAmount(), acc.getAmountMastercard().getValue());

    ClearingData10 liq = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, acc.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, liq.getStatus());
  }

  private void prepareCalculateFeesMock(BigDecimal amount, boolean chargesIva) throws TimeoutException, BaseException {
    // Prepara un mock del servicio de fees
    BigDecimal expectedPrepaidFee = BigDecimal.ZERO;
    BigDecimal expectedIvaFee = BigDecimal.ZERO;

    List<Charge> chargesList = new ArrayList<>();

    BigDecimal mainFee = amount.multiply(new BigDecimal(0.02));

    if (chargesIva) {
      //Divide el 2% en iva y comision
      expectedPrepaidFee = mainFee.multiply(new BigDecimal(100)).divide(new BigDecimal(119), 0, RoundingMode.HALF_UP);
      expectedIvaFee = mainFee.subtract(expectedPrepaidFee);

      // Agrega cargo de iva
      Charge prepaidCharge = new Charge();
      prepaidCharge.setChargeType(ChargeType.IVA);
      prepaidCharge.setAmount(expectedIvaFee.longValue());
      chargesList.add(prepaidCharge);
    } else {
      // El 2% es toda la comision
      expectedPrepaidFee = mainFee;
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
    registroTecnocom.setIndProaje("A");
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

  private PrepaidMovement10 buildPrepaidMovementFromTecnocomMovement(MovimientoTecnocom10 movimientoTecnocom10) throws BadRequestException {
    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();

    prepaidMovement10.setIdMovimientoRef(0L);
    prepaidMovement10.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement10.setIdTxExterno("");
    prepaidMovement10.setTipoMovimiento(movimientoTecnocom10.getMovementType());
    prepaidMovement10.setMonto(movimientoTecnocom10.getImpFac().getValue());
    prepaidMovement10.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement10.setOriginType(MovementOriginType.OPE);
    prepaidMovement10.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
    prepaidMovement10.setFechaActualizacion(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))));
    prepaidMovement10.setCodent(movimientoTecnocom10.getCodEnt());
    prepaidMovement10.setCentalta(movimientoTecnocom10.getCentAlta());
    prepaidMovement10.setCuenta(movimientoTecnocom10.getCuenta());
    prepaidMovement10.setClamon(movimientoTecnocom10.getImpFac().getCurrencyCode()); // ??
    prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.fromValue(movimientoTecnocom10.getIndNorCor()));
    prepaidMovement10.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(movimientoTecnocom10.getTipoFac().getCode(), movimientoTecnocom10.getIndNorCor()));
    prepaidMovement10.setFecfac(Date.from(ZonedDateTime.now(ZoneId.of("UTC")).toInstant()));
    prepaidMovement10.setNumreffac(movimientoTecnocom10.getNumRefFac());
    prepaidMovement10.setPan(movimientoTecnocom10.getPan());
    prepaidMovement10.setClamondiv(movimientoTecnocom10.getImpDiv().getCurrencyCode().getValue());
    prepaidMovement10.setImpdiv(movimientoTecnocom10.getImpDiv().getValue());
    prepaidMovement10.setImpfac(movimientoTecnocom10.getImpFac().getValue());
    prepaidMovement10.setCmbapli(movimientoTecnocom10.getCmbApli().intValue());
    prepaidMovement10.setNumaut(movimientoTecnocom10.getNumAut());
    prepaidMovement10.setIndproaje(IndicadorPropiaAjena.fromValue(movimientoTecnocom10.getIndProaje()));
    prepaidMovement10.setCodcom(movimientoTecnocom10.getCodCom());
    prepaidMovement10.setCodact(movimientoTecnocom10.getCodAct());
    prepaidMovement10.setImpliq(movimientoTecnocom10.getImpLiq().getValue());
    prepaidMovement10.setClamonliq(movimientoTecnocom10.getImpLiq().getCurrencyCode().getValue());
    prepaidMovement10.setCodpais(CodigoPais.fromValue(movimientoTecnocom10.getCodPais()));
    prepaidMovement10.setNompob(movimientoTecnocom10.getNomPob());
    prepaidMovement10.setNumextcta(movimientoTecnocom10.getNumExtCta().intValue());
    prepaidMovement10.setNummovext(movimientoTecnocom10.getNumMovExt().intValue());
    prepaidMovement10.setClamone(movimientoTecnocom10.getClamone().getValue());
    prepaidMovement10.setTipolin(movimientoTecnocom10.getTipoLin());
    prepaidMovement10.setLinref(movimientoTecnocom10.getLinRef());
    prepaidMovement10.setNumbencta(0); 
    prepaidMovement10.setNumplastico(0L);
    prepaidMovement10.setNomcomred("");
    prepaidMovement10.setCardId(prepaidCard.getId());

    return prepaidMovement10;
  }

  private void checkIfTransactionIsInQueue(String queueName, String idTxExterno, String transactionType, String transactionStatus, List<PrepaidMovementFee10> feeList) {
    Queue qResp = camelFactory.createJMSQueue(queueName);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, idTxExterno);

    Assert.assertNotNull("Deberia existir un evento de transaccion", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion con data", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", idTxExterno, transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", account.getUuid(), transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser.getUuid(), transactionEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo transactiontype", transactionType, transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el mismo status", transactionStatus, transactionEvent.getTransaction().getStatus());

    if (feeList != null && !feeList.isEmpty()) {
      List<cl.multicaja.prepaid.kafka.events.model.Fee> eventFeeList = transactionEvent.getTransaction().getFees();
      Assert.assertEquals("Debe tener todas las fees", feeList.size(), eventFeeList.size());

      for (PrepaidMovementFee10 storedFee : feeList) {
        cl.multicaja.prepaid.kafka.events.model.Fee foundFee = eventFeeList.stream().filter(f -> f.getType().equals(storedFee.getFeeType().toString())).findAny().orElse(null);
        Assert.assertNotNull("Debe existir la misma fee en la lista", foundFee);
        Assert.assertEquals("Debe tener el mismo monto", storedFee.getAmount().stripTrailingZeros(), foundFee.getAmount().getValue().stripTrailingZeros());
      }
    }
  }

  private MovimientoTecnocom10 prepareMovimientoTecnocom(TipoFactura tipofac, TecnocomReconciliationRegisterType registerType, CodigoMoneda currencyCode) throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10.setTipoFac(tipofac);
    movimientoTecnocom10.setIndNorCor(tipofac.getCorrector());
    movimientoTecnocom10.setTipoReg(registerType);
    movimientoTecnocom10.getImpDiv().setCurrencyCode(currencyCode);
    return getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);
  }

  private IpmMovement10 prepareIpmMovement(MovimientoTecnocom10 movimientoTecnocom10) throws Exception {
    IpmMovement10 ipmMovement10 = buildIpmMovement10();
    ipmMovement10.setCardholderBillingAmount(movimientoTecnocom10.getImpFac().getValue().multiply(new BigDecimal(0.995)).setScale(2, RoundingMode.HALF_UP)); // Alterar levemente el valor para que se reescriba
    ipmMovement10.setPan(prepaidCard.getPan());
    ipmMovement10.setMerchantCode(movimientoTecnocom10.getCodCom());
    ipmMovement10.setApprovalCode(movimientoTecnocom10.getNumAut());
    return createIpmMovement(ipmMovement10);
  }

  private List<PrepaidMovementFee10> prepareFees(PrepaidMovement10 prepaidMovement10, PrepaidMovementFeeType purchaseIntFee, boolean chargesIva) throws Exception {
    List<PrepaidMovementFee10> prepaidMovementFee10List = new ArrayList<>();

    if (chargesIva) {
      BigDecimal totalFee = prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02));

      PrepaidMovementFee10 prepaidFee = new PrepaidMovementFee10();
      prepaidFee.setMovementId(prepaidMovement10.getId());
      prepaidFee.setFeeType(purchaseIntFee);
      prepaidFee.setAmount(totalFee.multiply(new BigDecimal(0.84)).setScale(2, RoundingMode.HALF_UP));
      prepaidMovementFee10List.add(prepaidFee);

      PrepaidMovementFee10 ivaFee = new PrepaidMovementFee10();
      ivaFee.setMovementId(prepaidMovement10.getId());
      ivaFee.setFeeType(PrepaidMovementFeeType.IVA);
      ivaFee.setAmount(totalFee.subtract(prepaidFee.getAmount()).setScale(2, RoundingMode.HALF_UP));
      prepaidMovementFee10List.add(ivaFee);
    } else {
      PrepaidMovementFee10 prepaidFee = new PrepaidMovementFee10();
      prepaidFee.setMovementId(prepaidMovement10.getId());
      prepaidFee.setFeeType(purchaseIntFee);
      prepaidFee.setAmount(prepaidMovement10.getImpfac().multiply(new BigDecimal(0.02)).setScale(2, RoundingMode.HALF_UP));
      prepaidMovementFee10List.add(prepaidFee);
    }

    getPrepaidMovementEJBBean11().addPrepaidMovementFeeList(prepaidMovementFee10List);
    return prepaidMovementFee10List;
  }
}
