package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.schema2beans.ValidateException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Test_AutoReconciliation_FullTest extends TestBaseUnitAsync {
  static Account account;
  static UserAccount userAccount;
  static PrepaidUser10 prepaidUser;
  static PrepaidCard10 prepaidCard;
  static ReconciliationFile10 topupReconciliationFile10;
  static ReconciliationFile10 topupReverseReconciliationFile10;
  static ReconciliationFile10 withdrawReconciliationFile10;
  static ReconciliationFile10 withdrawReverseReconciliationFile10;
  static ReconciliationFile10 tecnocomReconciliationFile10;

  static String switchNotFoundId = "[No_Encontrado_En_Switch]";
  static String tecnocomNotFoundId = "[No_Encontrado_En_Tecnocom]";

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
      Test_Reconciliation_FullTest test = new Test_Reconciliation_FullTest();
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

      // Insertar un archivo extra de cada tipo para expirar movimientos
      newReconciliationFile10.setFileName("archivo_tecnocom_2.txt");
      newReconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

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

  @Test(expected = ValidateException.class)
  public void processTecnocomTableData_cardDoesNotExist() throws Exception {
    MovimientoTecnocom10 movimientoTecnocom10 = createMovimientoTecnocom(tecnocomReconciliationFile10.getId());
    movimientoTecnocom10 = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(movimientoTecnocom10);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
  }

  MovimientoTecnocom10 createMovimientoTecnocom(Long fileId) {
    MovimientoTecnocom10 registroTecnocom = new MovimientoTecnocom10();
    registroTecnocom.setIdArchivo(fileId);
    registroTecnocom.setNumAut(getRandomNumericString(6));
    registroTecnocom.setTipoFac(TipoFactura.COMPRA_INTERNACIONAL);
    registroTecnocom.setIndNorCor(IndicadorNormalCorrector.NORMAL.getValue());
    registroTecnocom.setPan(getRandomNumericString(15));
    registroTecnocom.setCentAlta("fill");
    registroTecnocom.setClamone(CodigoMoneda.USA_USD);
    registroTecnocom.setCmbApli(new BigDecimal(1L));
    registroTecnocom.setCodAct(0);
    registroTecnocom.setCodCom(getRandomString(10));
    registroTecnocom.setCodEnt(getRandomString(4));
    registroTecnocom.setCodPais(1);
    registroTecnocom.setContrato(account.getAccountNumber());
    registroTecnocom.setCuenta(account.getAccountNumber());
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
    return registroTecnocom;
  }

  MovimientoTecnocom10 createMovimientoTecnocom(Long fileId, PrepaidMovement10 prepaidMovement10) {
    MovimientoTecnocom10 registroTecnocom = new MovimientoTecnocom10();
    registroTecnocom.setIdArchivo(fileId);
    registroTecnocom.setNumAut(prepaidMovement10.getNumaut());
    registroTecnocom.setTipoFac(prepaidMovement10.getTipofac());
    registroTecnocom.setIndNorCor(prepaidMovement10.getIndnorcor().getValue());
    registroTecnocom.setPan(prepaidCard.getEncryptedPan());
    registroTecnocom.setCentAlta(prepaidMovement10.getCentalta());
    registroTecnocom.setClamone(CodigoMoneda.fromValue(prepaidMovement10.getClamone()));
    registroTecnocom.setCmbApli(new BigDecimal(prepaidMovement10.getCmbapli()));
    registroTecnocom.setCodAct(prepaidMovement10.getCodact());
    registroTecnocom.setCodCom(prepaidMovement10.getCodcom());
    registroTecnocom.setCodEnt(prepaidMovement10.getCodent());
    registroTecnocom.setCodPais(prepaidMovement10.getCodpais().getValue());
    registroTecnocom.setContrato(account.getAccountNumber());
    registroTecnocom.setCuenta(prepaidMovement10.getCuenta());
    registroTecnocom.setFecFac(new Date(prepaidMovement10.getFecfac().getTime()).toLocalDate());
    registroTecnocom.setFecTrn(prepaidMovement10.getFechaCreacion());
    registroTecnocom.setImpautcon(new NewAmountAndCurrency10());
    registroTecnocom.setImpDiv(new NewAmountAndCurrency10(prepaidMovement10.getImpdiv(), prepaidMovement10.getClamon()));
    registroTecnocom.setImpFac(new NewAmountAndCurrency10(prepaidMovement10.getImpfac(), prepaidMovement10.getClamon()));
    registroTecnocom.setImpLiq(new NewAmountAndCurrency10(prepaidMovement10.getImpliq(), prepaidMovement10.getClamon()));
    registroTecnocom.setIndProaje(prepaidMovement10.getIndproaje().getValue());
    registroTecnocom.setLinRef(prepaidMovement10.getLinref());
    registroTecnocom.setNomPob(prepaidMovement10.getNompob());
    registroTecnocom.setNumExtCta(new Long(prepaidMovement10.getNumextcta()));
    registroTecnocom.setNumMovExt(new Long(prepaidMovement10.getNummovext()));
    registroTecnocom.setNumRefFac(prepaidMovement10.getNumreffac());
    registroTecnocom.setOriginOpe(OriginOpeType.API_ORIGIN.getValue());
    registroTecnocom.setTipoLin(prepaidMovement10.getTipolin());
    return registroTecnocom;
  }

  TestData prepareTestData(PrepaidMovementType movementType, String merchantCode, IndicadorNormalCorrector indnorcor, Long tecnocomFileId) throws Exception {
    TestData testData = new TestData();

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setFirstTopup(false);
    if(PrepaidMovementType.TOPUP.equals(movementType)) {
      testData.cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    } else {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
      prepaidWithdraw.setAmount(prepaidTopup.getAmount());
      prepaidWithdraw.setMerchantCode(prepaidTopup.getMerchantCode());
      prepaidWithdraw.setTransactionId(prepaidTopup.getTransactionId());
      testData.cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    }

    testData.prepaidMovement = buildPrepaidMovementV2(prepaidUser, prepaidTopup, prepaidCard, testData.cdtTransaction, movementType);

    testData.prepaidMovement.setIndnorcor(indnorcor);
    testData.prepaidMovement.setNumaut(getRandomNumericString(6));
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.systemDefault());
    zonedDateTime = zonedDateTime.minusHours(1); // Crear un movimiento viejo, asi si no vienen en los archivos, expirara.
    ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    testData.prepaidMovement.setFechaCreacion(Timestamp.valueOf(utcDateTime.toLocalDateTime())); //Timestamp.from(Instant.now()));
    testData.prepaidMovement.setMonto(prepaidTopup.getAmount().getValue());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    testData.prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);

    if(tecnocomFileId != null) {
      testData.tecnocomMovement = createMovimientoTecnocom(tecnocomFileId, testData.prepaidMovement);
    } else {
      testData.tecnocomMovement = null;
    }

    if(testData.prepaidMovement != null) {
      testData.accountingData = IndicadorNormalCorrector.NORMAL.equals(indnorcor) ? createAccountingData(testData.prepaidMovement) : null;
    } else {
      testData.accountingData = null;
    }

    if(testData.prepaidMovement != null) {
      AccountingStatusType clearingStatus = TipoFactura.RETIRO_TRANSFERENCIA.equals(testData.prepaidMovement.getTipofac()) ? AccountingStatusType.PENDING : AccountingStatusType.INITIAL;
      testData.clearingData = IndicadorNormalCorrector.NORMAL.equals(indnorcor) ? createClearingData(testData.accountingData, clearingStatus) : null;
    } else {
      testData.clearingData = null;
    }

    return testData;
  }

  TestData createTestData(TestData preparedData) throws Exception {
    if(preparedData.cdtTransaction != null) {
      preparedData.cdtTransaction = createCdtTransaction10(preparedData.cdtTransaction);
      if (preparedData.prepaidMovement != null) {
        preparedData.prepaidMovement.setIdMovimientoRef(preparedData.cdtTransaction.getTransactionReference());
      }
    }

    if(preparedData.prepaidMovement != null) {
      preparedData.prepaidMovement = createPrepaidMovement10(preparedData.prepaidMovement);
    }

    if(preparedData.switchMovement != null) {
      preparedData.switchMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, preparedData.switchMovement);
    }

    if(preparedData.tecnocomMovement != null) {
      preparedData.tecnocomMovement = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(preparedData.tecnocomMovement);
    }

    if(preparedData.accountingData != null && preparedData.prepaidMovement != null) {
      preparedData.accountingData.setIdTransaction(preparedData.prepaidMovement.getId());
      preparedData.accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, preparedData.accountingData);
    }

    if(preparedData.clearingData != null && preparedData.prepaidMovement != null) {
      preparedData.clearingData.setAccountingId(preparedData.accountingData.getId());
      preparedData.clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, preparedData.clearingData);
    }

    return preparedData;
  }

  ClearingData10 createClearingData(AccountingData10 accountingData10, AccountingStatusType status) throws Exception {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(status);
    clearing10.setUserBankAccount(userAccount);
    clearing10.setAccountingId(accountingData10.getId());
    return clearing10;
  }

  AccountingData10 createAccountingData(PrepaidMovement10 prepaidMovement10) throws Exception {
    PrepaidAccountingMovement prepaidAccountingMovement = new PrepaidAccountingMovement();
    prepaidAccountingMovement.setPrepaidMovement10(prepaidMovement10);
    LocalDate localDate = LocalDate.now(ZoneId.systemDefault()).plusYears(100);
    prepaidAccountingMovement.setReconciliationDate(Timestamp.valueOf(localDate.atStartOfDay()));
    return getPrepaidAccountingEJBBean10().buildAccounting10(prepaidAccountingMovement, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
  }

  class TestData {
    PrepaidMovement10 prepaidMovement;
    CdtTransaction10 cdtTransaction;
    McRedReconciliationFileDetail switchMovement;
    MovimientoTecnocom10 tecnocomMovement;
    AccountingData10 accountingData;
    ClearingData10 clearingData;
  }
}
