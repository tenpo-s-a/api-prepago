package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import cl.multicaja.test.integration.v10.async.Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables;

public class Test_PrepaidMovementEJB10_fullClearingResolution extends TestBaseUnitAsync {
  static String folderDir = "src/test/resources/multicajared/clearing/clearing_test/";

  @Test
  public void manual_populateDB() throws Exception{
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
      accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
      accountingData.setIdTransaction(prepaidMovement.getId());
      accountingData.setType(AccountingTxType.RETIRO_WEB);
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

    // 7.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + >>MovStatus: no ok + Clearing OK
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables movementRejected;
    movementRejected = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.REJECTED, AccountingStatusType.PENDING);
    movementRejected.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(movementRejected.clearingData10);

    // 8.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_amount
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_amount;
    invalidInformation_amount = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amount.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amount.clearingData10.getAmount().setValue(invalidInformation_amount.clearingData10.getAmount().getValue().add(new BigDecimal(1L))); // Altera el valor de amount
    clearingData10ToFile.add(invalidInformation_amount.clearingData10);

    // 9.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_amountBalance
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_amountBalance;
    invalidInformation_amountBalance = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amountBalance.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amountBalance.clearingData10.getAmountBalance().setValue(invalidInformation_amountBalance.clearingData10.getAmountBalance().getValue().add(new BigDecimal(1L))); // Altera el valor de amount balance
    clearingData10ToFile.add(invalidInformation_amountBalance.clearingData10);

    // 10.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_amountMastercard
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_amountMastercard;
    invalidInformation_amountMastercard = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amountMastercard.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amountMastercard.clearingData10.getAmountMastercard().setValue(invalidInformation_amountMastercard.clearingData10.getAmountMastercard().getValue().add(new BigDecimal(1L))); // Altera el valor de amount mastercard
    clearingData10ToFile.add(invalidInformation_amountMastercard.clearingData10);

    // 11.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_accountRut
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_accountRut;
    invalidInformation_accountRut = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_accountRut.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().setValue(invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().getValue() + 1); // Altera rut
    invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().setDv("x");
    clearingData10ToFile.add(invalidInformation_accountRut.clearingData10);

    // 12.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing InvalidInformation_bankAccount
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables invalidInformation_bankAccount;
    invalidInformation_bankAccount = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_bankAccount.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_bankAccount.clearingData10.getUserBankAccount().setAccountNumber("11111111"); // Altera cuenta bancaria
    clearingData10ToFile.add(invalidInformation_bankAccount.clearingData10);

    // 13.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing Rejected
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables rejectedClearing;
    rejectedClearing = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    rejectedClearing.clearingData10.setStatus(AccountingStatusType.REJECTED); // Para preparar para el archivo
    clearingData10ToFile.add(rejectedClearing.clearingData10);

    // 14.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing Rejected_Formmat
    Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables rejectedFormatClearing;
    rejectedFormatClearing = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    rejectedFormatClearing.clearingData10.setStatus(AccountingStatusType.REJECTED_FORMAT); // Para preparar para el archivo
    rejectedFormatClearing.clearingData10.getUserBankAccount().setBankName("0786");
    clearingData10ToFile.add(rejectedFormatClearing.clearingData10);

    // 15.- Agrega un movimiento que no esta en la db pero que vendra en el archivo
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

    // 16.- Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >>Clearing NotInFile
    ResolutionPreparedVariables notInFile;
    notInFile = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    // Este caso NO va en el archivo

    getPrepaidClearingEJBBean10().createAccountingCSV(fileName, fileId, clearingData10ToFile);
  }

  @Test
  public void manual_runF1() throws Exception {
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
  public void manual_runF3() throws Exception {
    getPrepaidMovementEJBBean10().clearingResolution();
  }

  @Test
  public void fullClearingResolution() throws Exception {
    manual_populateDB();
    manual_runF1();
    manual_runF3();

    // Check all asserts

  }

  private Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables prepareTest(Long fileId, String merchantCode, ReconciliationStatusType tecnocomStatus, PrepaidMovementStatus movementStatus, AccountingStatusType clearingStatus) throws Exception {
    return new Test_PrepaidMovementEJB10_clearingResolution().prepareTest(fileId, merchantCode, tecnocomStatus, movementStatus, clearingStatus);
  }
}
