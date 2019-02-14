package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cl.multicaja.test.integration.v10.async.Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables;
import org.springframework.jdbc.core.RowMapper;

public class Test_PrepaidMovementEJB10_fullClearingResolution extends TestBaseUnitAsync {
  static String folderDir = "src/test/resources/multicajared/clearing/clearing_test/";

  @Test
  public void runF1() throws Exception {
    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Santiago"));
    String dirName = folderDir;
    dirName = dirName.concat(String.format("TRX_PREPAGO_%s.CSV", date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));

    File file = new File(dirName);
    InputStream inputStream = new FileInputStream(file);

    String[] splits = dirName.split("/");
    String fileName = splits[splits.length - 1];
    System.out.println("File name:" + fileName);
    getPrepaidClearingEJBBean10().processClearingResponse(inputStream, fileName);
    inputStream.close();
  }

  @Test
  public void runF3() throws Exception {
    getPrepaidMovementEJBBean10().clearingResolution();
  }

  @Test
  public void fullClearingResolution() throws Exception {

    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));

    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Santiago"));
    String fileId = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String fileName = folderDir;
    fileName = fileName.concat(String.format("TRX_PREPAGO_%s.CSV", date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));

    System.out.println("File id salvado: " + fileId);
    AccountingFiles10 files10 = new AccountingFiles10();
    files10.setFileId(fileId);
    files10.setFileFormatType(AccountingFileFormatType.CSV);
    files10.setFileType(AccountingFileType.CLEARING);
    files10.setName(fileName);
    files10.setStatus(AccountingStatusType.OK);

    files10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null, files10);

    List<ClearingData10> clearingData10ToFile = new ArrayList<>();

    // 1.- Preparar Test All OK: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables allOk;
    allOk = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    allOk.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(allOk.clearingData10);

    // 2.- Preparar Test: >>No es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing Ok
    ResolutionPreparedVariables notWithdraw = new Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables();
    {
      User user = registerUser();
      UserAccount userAccount = createBankAccount(user);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
      prepaidCard = createPrepaidCard10(prepaidCard);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
      prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
      prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

      CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
      cdtTransaction = createCdtTransaction10(cdtTransaction);

      PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
      prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(new BigDecimal(numberUtils.random(5000, 200000)));

      AccountingData10 accountingData = buildRandomAccouting();
      accountingData.setAmount(amount);
      accountingData.setAmountBalance(new NewAmountAndCurrency10(amount.getValue()));
      accountingData.setAmountMastercard(new NewAmountAndCurrency10(amount.getValue()));
      accountingData.setAmountUsd(new NewAmountAndCurrency10(amount.getValue().divide(new BigDecimal(680), 2, RoundingMode.HALF_UP)));
      accountingData.setFileId(files10.getId());
      accountingData.setAccountingMovementType(AccountingMovementType.CARGA_WEB);
      accountingData.setIdTransaction(prepaidMovement.getId());
      accountingData.setType(AccountingTxType.CARGA_WEB);
      accountingData.setStatus(AccountingStatusType.PENDING);
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setFileId(accountingData.getFileId());
      clearingData.setAccountingId(accountingData.getId());
      clearingData.setStatus(AccountingStatusType.PENDING);
      clearingData.setUserBankAccount(userAccount);
      clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

      // Set values not returned by insertClearing
      Test_PrepaidMovementEJB10_clearingResolution.copyAccountingValues(accountingData, clearingData);
      clearingData.setUserBankAccount(userAccount);

      notWithdraw.accountingData10 = accountingData;
      notWithdraw.cdtTransaction10 = cdtTransaction;
      notWithdraw.prepaidMovement10 = prepaidMovement;
      notWithdraw.clearingData10 = clearingData;
    }
    notWithdraw.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(notWithdraw.clearingData10);

    // 3.- Preparar Test: Es RETIRO + >>No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables notWeb;
    notWeb = prepareTest(files10.getId(), getRandomNumericString(6), ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    notWeb.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(notWeb.clearingData10);

    // 4.- Preparar Test: Es RETIRO + Es WEB + >>Not_reconciled Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables notTecnocom;
    notTecnocom = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.NOT_RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    notTecnocom.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(notTecnocom.clearingData10);

    // 5.- Preparar Test: Es RETIRO + Es WEB + >>Pending Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables pendingTecnocom;
    pendingTecnocom = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.PENDING, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    pendingTecnocom.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(pendingTecnocom.clearingData10);

    // 6.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + >>Ya Conciliado en BD + MovStatus: process OK + Clearing OK
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables reconciledMovement;
    reconciledMovement = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    getPrepaidMovementEJBBean10().createMovementConciliate(null, reconciledMovement.prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
    reconciledMovement.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(reconciledMovement.clearingData10);

    // 7.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + >>MovStatus: distinto de ok + Clearing OK
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables movementRejected;
    movementRejected = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.REJECTED, AccountingStatusType.PENDING);
    movementRejected.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(movementRejected.clearingData10);

    // 8.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing ya estaba OK en BD
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables clearingAlreadyOK;
    clearingAlreadyOK = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);
    clearingData10ToFile.add(clearingAlreadyOK.clearingData10);

    // 9.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_amount
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_amount;
    invalidInformation_amount = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amount.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amount.clearingData10.getAmount().setValue(invalidInformation_amount.clearingData10.getAmount().getValue().add(new BigDecimal(1L))); // Altera el valor de amount
    clearingData10ToFile.add(invalidInformation_amount.clearingData10);

    // 10.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_amountBalance
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_amountBalance;
    invalidInformation_amountBalance = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amountBalance.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amountBalance.clearingData10.getAmountBalance().setValue(invalidInformation_amountBalance.clearingData10.getAmountBalance().getValue().add(new BigDecimal(1L))); // Altera el valor de amount balance
    clearingData10ToFile.add(invalidInformation_amountBalance.clearingData10);

    // 11.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_amountMastercard
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_amountMastercard;
    invalidInformation_amountMastercard = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amountMastercard.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amountMastercard.clearingData10.getAmountMastercard().setValue(invalidInformation_amountMastercard.clearingData10.getAmountMastercard().getValue().add(new BigDecimal(1L))); // Altera el valor de amount mastercard
    clearingData10ToFile.add(invalidInformation_amountMastercard.clearingData10);

    // 12.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_accountRut
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_accountRut;
    invalidInformation_accountRut = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_accountRut.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().setValue(invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().getValue() + 1); // Altera rut
    invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().setDv("x");
    clearingData10ToFile.add(invalidInformation_accountRut.clearingData10);

    // 13.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_bankAccount
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_bankAccount;
    invalidInformation_bankAccount = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_bankAccount.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_bankAccount.clearingData10.getUserBankAccount().setAccountNumber("11111111"); // Altera cuenta bancaria
    clearingData10ToFile.add(invalidInformation_bankAccount.clearingData10);

    // 14.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing Rejected
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables rejectedClearing;
    rejectedClearing = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    rejectedClearing.clearingData10.setStatus(AccountingStatusType.REJECTED); // Para preparar para el archivo
    clearingData10ToFile.add(rejectedClearing.clearingData10);

    // 15.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >> Clearing Rejected_Formmat
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables rejectedFormatClearing;
    rejectedFormatClearing = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    rejectedFormatClearing.clearingData10.setStatus(AccountingStatusType.REJECTED_FORMAT); // Para preparar para el archivo
    rejectedFormatClearing.clearingData10.getUserBankAccount().setBankName("0786");
    clearingData10ToFile.add(rejectedFormatClearing.clearingData10);

    // 16.- Agrega un movimiento que no esta en la db pero que vendra en el archivo
    ClearingData10 notInBd = new ClearingData10();
    notInBd.setId(getUniqueLong());
    notInBd.setFileId(files10.getId());
    notInBd.setIdTransaction(getUniqueLong());
    notInBd.setType(AccountingTxType.RETIRO_WEB);
    notInBd.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal(666));
    notInBd.setAmount(amount);
    notInBd.setAmountMastercard(amount);
    notInBd.setAmountUsd(amount);
    notInBd.setExchangeRateDif(new BigDecimal(100));
    notInBd.setFee(new BigDecimal(10));
    notInBd.setFeeIva(new BigDecimal(19));
    notInBd.setCollectorFee(new BigDecimal(90));
    notInBd.setCollectorFeeIva(new BigDecimal(9));
    notInBd.setAmountBalance(amount);
    notInBd.setStatus(AccountingStatusType.OK);
    UserAccount bankAccount = new UserAccount();
    bankAccount.setId(55L);
    bankAccount.setAccountNumber(getRandomNumericString(8));
    Rut rut = new Rut();
    rut.setValue(Integer.valueOf(getRandomNumericString(8)));
    rut.setDv("3");
    bankAccount.setRut(rut);
    bankAccount.setAccountType("Cuenta Corriente");
    bankAccount.setBankName("BANCO DE CHILE");
    notInBd.setUserBankAccount(bankAccount);
    clearingData10ToFile.add(notInBd);

    // 17.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing NotInFile
    ResolutionPreparedVariables notInFile;
    notInFile = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    // Este caso NO va en el archivo

    getPrepaidClearingEJBBean10().createAccountingCSV(fileName, fileId, clearingData10ToFile);

    runF1();
    runF3();

    // 1. Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, allOk.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNotNull("Debe existir la confirmacion en el cdt", foundCdtTransation);
      Assert.assertEquals("Deben ser por montos iguales", allOk.cdtTransaction10.getAmount(), foundCdtTransation.getAmount());

      // Revisar que se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(allOk.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio confirmed", BusinessStatusType.CONFIRMED, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting haya cambiado a OK
      AccountingData10 foundAccounting = getAccountingData(allOk.accountingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a OK
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, allOk.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(allOk.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado reconciled", ReconciliationStatusType.RECONCILED, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion none", ReconciliationActionType.NONE, reconciliedMovement10.getActionType());
    }

    // 2. Chequea test: >> No es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notWithdraw.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notWithdraw.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notWithdraw.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, notWithdraw.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(notWithdraw.prepaidMovement10.getId());
      Assert.assertNull("No debe existir reconciled", reconciliedMovement10);

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", notWithdraw.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING, researchMovement10.getOrigen());
    }

    // 3. Chequea test: Es RETIRO + >> No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notWeb.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notWeb.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notWeb.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, notWeb.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(notWeb.prepaidMovement10.getId());
      Assert.assertNull("No debe existir reconciled", reconciliedMovement10);

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", notWeb.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING, researchMovement10.getOrigen());
    }

    // 4. Chequea test: Es RETIRO + Es WEB + >> Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notTecnocom.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notTecnocom.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, notTecnocom.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(notTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", notTecnocom.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 5. Chequea test: Es RETIRO + Es WEB + >> Tecnocom: PENDING + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, pendingTecnocom.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(pendingTecnocom.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, pendingTecnocom.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertNull("No debe existir reconciled", reconciliedMovement10);

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", pendingTecnocom.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING, researchMovement10.getOrigen());
    }

    // 6. Chequea test: Es RETIRO + Es WEB + OK Tecnocom + >> Ya Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, reconciledMovement.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(reconciledMovement.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(reconciledMovement.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, reconciledMovement.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento debe estar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(reconciledMovement.prepaidMovement10.getId());
      Assert.assertNotNull("Ya existe conciliado", reconciliedMovement10);

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", reconciledMovement.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING, researchMovement10.getOrigen());
    }

    // 7. Chequea test: Es RETIRO + Es WEB + Tecnocom: RECONCILED + NO Conciliado en BD + >> MovStatus: distinto de OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, movementRejected.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(movementRejected.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(movementRejected.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, movementRejected.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(movementRejected.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", movementRejected.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 8. Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + >> Clearing ya estaba OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, clearingAlreadyOK.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(clearingAlreadyOK.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(clearingAlreadyOK.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, clearingAlreadyOK.clearingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundClearing.getStatus());

      // El movimiento debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(clearingAlreadyOK.prepaidMovement10.getId());
      Assert.assertNotNull("Debe existir reconciled", reconciliedMovement10);

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", clearingAlreadyOK.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING, researchMovement10.getOrigen());
    }

    // 9. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Invalid Amount
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, invalidInformation_amount.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(invalidInformation_amount.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(invalidInformation_amount.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, invalidInformation_amount.clearingData10.getId());
      Assert.assertEquals("Debe tener estado Invalid Information", AccountingStatusType.INVALID_INFORMATION, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(invalidInformation_amount.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", invalidInformation_amount.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 10. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Invalid Amount Balance
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, invalidInformation_amountBalance.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(invalidInformation_amountBalance.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(invalidInformation_amountBalance.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, invalidInformation_amountBalance.clearingData10.getId());
      Assert.assertEquals("Debe tener estado Invalid Information", AccountingStatusType.INVALID_INFORMATION, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(invalidInformation_amountBalance.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", invalidInformation_amountBalance.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 11. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Invalid Amount Mastercard
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, invalidInformation_amountMastercard.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(invalidInformation_amountMastercard.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(invalidInformation_amountMastercard.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, invalidInformation_amountMastercard.clearingData10.getId());
      Assert.assertEquals("Debe tener estado Invalid Information", AccountingStatusType.INVALID_INFORMATION, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(invalidInformation_amountMastercard.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", invalidInformation_amountMastercard.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 12. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Invalid rut
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, invalidInformation_accountRut.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(invalidInformation_accountRut.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(invalidInformation_accountRut.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, invalidInformation_accountRut.clearingData10.getId());
      Assert.assertEquals("Debe tener estado Invalid Information", AccountingStatusType.INVALID_INFORMATION, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(invalidInformation_accountRut.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", invalidInformation_accountRut.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 13. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Invalid account
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, invalidInformation_bankAccount.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(invalidInformation_bankAccount.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(invalidInformation_bankAccount.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a lo que venga en el archivo
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, invalidInformation_bankAccount.clearingData10.getId());
      Assert.assertEquals("Debe tener estado Invalid Information", AccountingStatusType.INVALID_INFORMATION, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(invalidInformation_bankAccount.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", invalidInformation_bankAccount.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }

    // 14. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Rejected
    {
      // Check banco rechaza
      PrepaidMovement10 foundMovement = null;
      for(int i = 0; i < 20; i++) {
        foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(rejectedClearing.prepaidMovement10.getId());
        if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
          break;
        }
        Thread.sleep(500); // Esperar que el async ejecute la reversa
        System.out.println("Buscando...");
      }

      Assert.assertNotNull("Debe encontrarse el movimiento", foundMovement);
      Assert.assertEquals("Debe tener estado reversado", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());

      PrepaidMovement10 foundReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(rejectedClearing.prepaidMovement10.getIdTxExterno(), rejectedClearing.prepaidMovement10.getTipoMovimiento(), IndicadorNormalCorrector.CORRECTORA);
      Assert.assertNotNull("Debe existir una reversa", foundReverse);

      // Chequear que exista el cdt confirmado
      CdtTransaction10 originalCdtTransaction = getCdtEJBBean10().buscaMovimientoReferencia(null, rejectedClearing.prepaidMovement10.getIdMovimientoRef());
      CdtTransaction10 foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, rejectedClearing.prepaidMovement10.getIdTxExterno(), rejectedClearing.cdtTransaction10.getCdtTransactionTypeConfirm());
      Assert.assertNotNull("Debe existir la confirmacion del movimiento cdt", foundCdtTransaction);
      Assert.assertEquals("Debe referenciar al movimiento original", originalCdtTransaction.getId(), foundCdtTransaction.getTransactionReference());

      // Chequear que exista la reversa del cdt
      CdtTransaction10 reverseCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, rejectedClearing.prepaidMovement10.getIdTxExterno(), originalCdtTransaction.getCdtTransactionTypeReverse());
      Assert.assertNotNull("Debe existir la reversa en el cdt", reverseCdtTransaction);

      // Debe existir la confirmacion de la reversa
      CdtTransaction10 reverseConfirm = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, rejectedClearing.prepaidMovement10.getIdTxExterno(), reverseCdtTransaction.getCdtTransactionTypeConfirm());
      Assert.assertNotNull("Debe existir la confirmacion de la reversa", reverseConfirm);

      // Revisar que no haya cambiado el estado de negocio
      Assert.assertEquals("Debe cambiar su estado de negocio", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting haya cambiado a rejected
      AccountingData10 foundAccounting = getAccountingData(rejectedClearing.accountingData10.getId());
      Assert.assertEquals("Debe tener estado REJECTED", AccountingStatusType.REJECTED, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a rejected
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, rejectedClearing.clearingData10.getId());
      Assert.assertEquals("Debe tener estado REJECTED", AccountingStatusType.REJECTED, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(rejectedClearing.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement10.getActionType());

      // No debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", rejectedClearing.prepaidMovement10.getId()));
      Assert.assertNull("No debe estar en research", researchMovement10);
    }

    // 15. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing Rejected Format
    {
      // Check banco rechaza
      PrepaidMovement10 foundMovement = null;
      for(int i = 0; i < 20; i++) {
        foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(rejectedFormatClearing.prepaidMovement10.getId());
        if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
          break;
        }
        Thread.sleep(500); // Esperar que el async ejecute la reversa
        System.out.println("Buscando...");
      }

      Assert.assertNotNull("Debe encontrarse el movimiento", foundMovement);
      Assert.assertEquals("Debe tener estado reversado", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());

      PrepaidMovement10 foundReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(rejectedFormatClearing.prepaidMovement10.getIdTxExterno(), rejectedFormatClearing.prepaidMovement10.getTipoMovimiento(), IndicadorNormalCorrector.CORRECTORA);
      Assert.assertNotNull("Debe existir una reversa", foundReverse);

      // Chequear que exista el cdt confirmado
      CdtTransaction10 originalCdtTransaction = getCdtEJBBean10().buscaMovimientoReferencia(null, rejectedFormatClearing.prepaidMovement10.getIdMovimientoRef());
      CdtTransaction10 foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, rejectedFormatClearing.prepaidMovement10.getIdTxExterno(), rejectedFormatClearing.cdtTransaction10.getCdtTransactionTypeConfirm());
      Assert.assertNotNull("Debe existir la confirmacion del movimiento cdt", foundCdtTransaction);
      Assert.assertEquals("Debe referenciar al movimiento original", originalCdtTransaction.getId(), foundCdtTransaction.getTransactionReference());

      // Chequear que exista la reversa del cdt
      CdtTransaction10 reverseCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, rejectedFormatClearing.prepaidMovement10.getIdTxExterno(), originalCdtTransaction.getCdtTransactionTypeReverse());
      Assert.assertNotNull("Debe existir la reversa en el cdt", reverseCdtTransaction);

      // Debe existir la confirmacion de la reversa
      CdtTransaction10 reverseConfirm = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, rejectedFormatClearing.prepaidMovement10.getIdTxExterno(), reverseCdtTransaction.getCdtTransactionTypeConfirm());
      Assert.assertNotNull("Debe existir la confirmacion de la reversa", reverseConfirm);

      // Revisar que no haya cambiado el estado de negocio
      Assert.assertEquals("Debe cambiar su estado de negocio", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting haya cambiado a rejected
      AccountingData10 foundAccounting = getAccountingData(rejectedFormatClearing.accountingData10.getId());
      Assert.assertEquals("Debe tener estado REJECTED FORMAT", AccountingStatusType.REJECTED_FORMAT, foundAccounting.getStatus());

      // Revisar que el estado de clearing haya cambiado a rejected
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, rejectedFormatClearing.clearingData10.getId());
      Assert.assertEquals("Debe tener estado REJECTED FORMAT", AccountingStatusType.REJECTED_FORMAT, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(rejectedFormatClearing.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement10.getActionType());

      // No debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", rejectedFormatClearing.prepaidMovement10.getId()));
      Assert.assertNull("No debe estar en research", researchMovement10);
    }

    // 16. In file, but not in DB
    {
      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", notInBd.getIdTransaction()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING, researchMovement10.getOrigen());
    }

    // 17. Chequea test: Es RETIRO + Es WEB + Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + >> Clearing NotInFile
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notInFile.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notInFile.prepaidMovement10.getId());
      Assert.assertEquals("No debe cambiar su estado de negocio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notInFile.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // Revisar que el estado de clearing no haya cambiado
      ClearingData10 foundClearing = getPrepaidClearingEJBBean10().searchClearingDataById(null, notInFile.clearingData10.getId());
      Assert.assertEquals("Debe tener estado NOT IN FILE", AccountingStatusType.NOT_IN_FILE, foundClearing.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(notInFile.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      ResearchMovement10 researchMovement10 = getPrepaidMovementEJBBean10().getResearchMovementByIdMovRef(String.format("idMov=%d", notInFile.prepaidMovement10.getId()));
      Assert.assertNotNull("Debe estar en research", researchMovement10);
      Assert.assertEquals("Debe venir del clearing", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement10.getOrigen());
    }
  }

  private Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables prepareTest(Long fileId, String merchantCode, ReconciliationStatusType tecnocomStatus, PrepaidMovementStatus movementStatus, AccountingStatusType clearingStatus) throws Exception {
    return new Test_PrepaidMovementEJB10_clearingResolution().prepareTest(fileId, merchantCode, tecnocomStatus, movementStatus, clearingStatus);
  }

  private AccountingData10 getAccountingData(Long idMov) {
    RowMapper rowMapper = (rs, rowNum) -> {
      AccountingData10 accountingData10 = new AccountingData10();
      accountingData10.setId(numberUtils.toLong(rs.getLong("id")));
      accountingData10.setStatus(AccountingStatusType.fromValue(String.valueOf(rs.getString("status"))));
      return accountingData10;
    };
    List<AccountingData10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT id, status FROM %s.accounting where id = %d", getSchemaAccounting(), idMov), rowMapper);
    AccountingData10 accountingData10 = data.get(0);
    return accountingData10;
  }
}
