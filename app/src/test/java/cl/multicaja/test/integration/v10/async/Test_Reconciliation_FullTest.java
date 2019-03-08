package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoPais;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Test_Reconciliation_FullTest extends TestBaseUnitAsync {

  static User user;
  static UserAccount userAccount;
  static PrepaidUser10 prepaidUser;
  static PrepaidCard10 prepaidCard;
  static ReconciliationFile10 reconciliationFile10;

  @BeforeClass
  public static void prepareUser() {
    try {
      Test_Reconciliation_FullTest test = new Test_Reconciliation_FullTest();

      user = test.registerUser();
      userAccount = test.createBankAccount(user);
      prepaidUser = test.buildPrepaidUser10(user);
      prepaidUser = test.createPrepaidUser10(prepaidUser);
      prepaidCard = test.buildPrepaidCard10FromTecnocom(user, prepaidUser);
      prepaidCard = test.createPrepaidCard10(prepaidCard);

      ReconciliationFile10 newReconciliationFile10 = new ReconciliationFile10();
      newReconciliationFile10.setStatus(FileStatus.OK);
      newReconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);
      newReconciliationFile10.setFileName("archivo_1.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

    } catch (Exception e) {
      Assert.fail("Error al crear el usuario y su tarjeta");
    }
  }

  @Test
  public void case1_topup_BD_OK_SW_OK_TC_OK() throws Exception {
    TestData testData = prepareTestData();
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(reconciliationFile10.getId());

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.CONFIRMED, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.INITIAL, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.OK);
  }

  void assertPrepaidMovement(Long movementId, boolean exists, PrepaidMovementStatus status, BusinessStatusType businessStatus, ReconciliationStatusType switchStatus, ReconciliationStatusType tecnocomStatus) throws Exception {
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(movementId);
    if(!exists) {
      Assert.assertNull("No debe existir", prepaidMovement10);
      return;
    }
    Assert.assertNotNull("Debe existir", prepaidMovement10);
    Assert.assertEquals("Debe tener estado " + status.toString(), status, prepaidMovement10.getEstado());
    Assert.assertEquals("Debe tener estado de negocio " + businessStatus.toString(), businessStatus, prepaidMovement10.getEstadoNegocio());
    Assert.assertEquals("Debe tener estado con switch " + switchStatus.toString(), switchStatus, prepaidMovement10.getConSwitch());
    Assert.assertEquals("Debe tener estado con tecnocom " + tecnocomStatus.toString(), tecnocomStatus, prepaidMovement10.getConTecnocom());
  }

  void assertAccountingMovement(Long movementId, boolean exists, AccountingStatusType status, AccountingStatusType accountingStatus) throws Exception {
    AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, movementId);
    if(!exists) {
      Assert.assertNull("No debe existir", accountingData10);
      return;
    }
    Assert.assertNotNull("Debe existir", movementId);
    Assert.assertEquals("Debe tener status " + status.toString(), status, accountingData10.getStatus());
    Assert.assertEquals("Debe tener accounting status " + accountingStatus.toString(), accountingStatus, accountingData10.getAccountingStatus());
  }

  void assertClearingMovement(Long clearingId, boolean exists, AccountingStatusType status) throws Exception {
    ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().searchClearingDataById(null, clearingId);
    if(!exists) {
      Assert.assertNull("No debe existir", clearingData10);
      return;
    }
    Assert.assertNotNull("Debe existir", clearingData10);
    Assert.assertEquals("Debe tener status " + status.toString(), status, clearingData10.getStatus());
  }

  ClearingData10 createClearingData(AccountingData10 accountingData10) throws Exception {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(AccountingStatusType.PENDING);
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

  McRedReconciliationFileDetail createSwitchMovement(Long fileId, PrepaidMovement10 prepaidMovement10) {
    McRedReconciliationFileDetail registroSwitch = new McRedReconciliationFileDetail();
    registroSwitch.setMcCode(prepaidMovement10.getIdTxExterno());
    registroSwitch.setClientId(prepaidMovement10.getIdPrepaidUser());
    registroSwitch.setExternalId(0L);
    registroSwitch.setDateTrx(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
    registroSwitch.setFileId(fileId);
    return registroSwitch;
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
    registroTecnocom.setContrato(prepaidCard.getProcessorUserId());
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
    registroTecnocom.setOriginOpe("ONLI");
    registroTecnocom.setTipoLin(prepaidMovement10.getTipolin());
    return registroTecnocom;
  }

  TestData prepareTestData() throws Exception {
    TestData testData = new TestData();

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    testData.prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, null, prepaidTopup.getMovementType());
    testData.prepaidMovement.setNumaut(getRandomNumericString(6));
    testData.prepaidMovement.setFechaCreacion(Timestamp.from(Instant.now()));
    testData.switchMovement = createSwitchMovement(reconciliationFile10.getId(), testData.prepaidMovement);
    testData.tecnocomMovement = createMovimientoTecnocom(reconciliationFile10.getId(), testData.prepaidMovement);
    testData.accountingData = createAccountingData(testData.prepaidMovement);
    testData.clearingData = createClearingData(testData.accountingData);
    return testData;
  }

  TestData createTestData(TestData preparedData) throws Exception {
    preparedData.prepaidMovement = createPrepaidMovement10(preparedData.prepaidMovement);

    preparedData.switchMovement.setAmount(preparedData.prepaidMovement.getImpfac());
    preparedData.switchMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, preparedData.switchMovement);
    preparedData.tecnocomMovement = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(preparedData.tecnocomMovement);

    preparedData.accountingData.setIdTransaction(preparedData.prepaidMovement.getId());
    preparedData.accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, preparedData.accountingData);

    preparedData.clearingData.setAccountingId(preparedData.accountingData.getId());
    preparedData.clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, preparedData.clearingData);
    return preparedData;
  }

  class TestData {
    PrepaidMovement10 prepaidMovement;
    McRedReconciliationFileDetail switchMovement;
    MovimientoTecnocom10 tecnocomMovement;
    AccountingData10 accountingData;
    ClearingData10 clearingData;
  }

}

/*

PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
    PrepaidMovement10 withdrawMovement = createPrepaidMovement10(prepaidMovement10);

    prepaidTopup = buildPrepaidTopup10(user);
    prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
    prepaidMovement10.setIdTxExterno(topupMovement.getIdTxExterno());
    PrepaidMovement10 topupReverseMovement = createPrepaidMovement10(prepaidMovement10);

    prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
    prepaidMovement10.setIdTxExterno(withdrawMovement.getIdTxExterno());
    PrepaidMovement10 withdrawReverseMovement = createPrepaidMovement10(prepaidMovement10);
 */
