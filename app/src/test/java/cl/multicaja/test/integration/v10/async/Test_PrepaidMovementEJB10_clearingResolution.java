package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidMovementEJB10_clearingResolution extends TestBaseUnitAsync {

  @BeforeClass
  @AfterClass
  public static void clearData() {
    /*
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
    */
  }

  @Test
  public void clearingResolution_Reversed() throws Exception {
    // Banco rechaza
    ResolutionPreparedVariables rejectedClearing;
    rejectedClearing = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.REJECTED);

    // Banco rechaza por formato
    ResolutionPreparedVariables rejectedFormatClearing;
    rejectedFormatClearing = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.REJECTED_FORMAT);

    getPrepaidMovementEJBBean10().clearingResolution();

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

      System.out.println("Id mov ref: " + foundReverse.getIdMovimientoRef());

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
      Assert.assertNotNull("Debe exitir la confirmacion de la reversa", reverseConfirm);
    }

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

      System.out.println("Id mov ref: " + foundReverse.getIdMovimientoRef());

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
      Assert.assertNotNull("Debe exitir la confirmacion de la reversa", reverseConfirm);
    }
  }

  private ResolutionPreparedVariables prepareTest(Long fileId, String merchantCode, ReconciliationStatusType tecnocomStatus, PrepaidMovementStatus movementStatus, AccountingStatusType clearingStatus) throws Exception {
    User user = registerUser();
    UserAccount userAccount = createBankAccount(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setBankAccountId(userAccount.getId());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement.setConTecnocom(tecnocomStatus);
    prepaidMovement.setEstado(movementStatus);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(new BigDecimal(numberUtils.random(5000, 200000)));

    AccountingData10 accountingData = buildRandomAccouting();
    accountingData.setAmount(amount);
    accountingData.setAmountBalance(new NewAmountAndCurrency10(amount.getValue()));
    accountingData.setAmountMastercard(new NewAmountAndCurrency10(amount.getValue()));
    accountingData.setAmountUsd(new NewAmountAndCurrency10(amount.getValue().divide(new BigDecimal(680), 2, RoundingMode.HALF_UP)));
    accountingData.setFileId(fileId);
    accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData.setIdTransaction(prepaidMovement.getId());
    accountingData.setType(AccountingTxType.RETIRO_WEB);
    accountingData.setStatus(AccountingStatusType.PENDING);
    accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);

    ClearingData10 clearingData = new ClearingData10();
    clearingData.setFileId(accountingData.getFileId());
    clearingData.setAccountingId(accountingData.getId());
    clearingData.setStatus(clearingStatus);
    clearingData.setUserBankAccount(userAccount);
    clearingData.setFileId(accountingData.getFileId());
    clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

    // Set values not returned by insertClearing
    copyAccountingValues(accountingData, clearingData);
    clearingData.setUserBankAccount(userAccount);
    System.out.println("Creada clearing con id: " + clearingData.getId());

    ResolutionPreparedVariables resolutionPreparedVariables = new ResolutionPreparedVariables();
    resolutionPreparedVariables.accountingData10 = accountingData;
    resolutionPreparedVariables.cdtTransaction10 = cdtTransaction;
    resolutionPreparedVariables.prepaidMovement10 = prepaidMovement;
    resolutionPreparedVariables.clearingData10 = clearingData;
    return resolutionPreparedVariables;
  }

  private class ResolutionPreparedVariables {
    PrepaidMovement10 prepaidMovement10;
    CdtTransaction10 cdtTransaction10;
    AccountingData10 accountingData10;
    ClearingData10 clearingData10;
  }

  @Test
  public void populateDB() throws Exception{
    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Santiago"));
    String fileId = date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String fileName = "src/test/resources/multicajared/clearing/clearing_test/";
    fileName = fileName.concat(String.format("TRX_PREPAGO_%s.CSV", date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));

    AccountingFiles10 files10 = new AccountingFiles10();
    files10.setFileId(fileId);
    files10.setFileFormatType(AccountingFileFormatType.CSV);
    files10.setFileType(AccountingFileType.CLEARING);
    files10.setName(fileName);
    files10.setStatus(AccountingStatusType.OK);

    files10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null, files10);

    List<ClearingData10> clearingData10ToFile = new ArrayList<>();

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables allOk;
    allOk = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    allOk.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(allOk.clearingData10);

    // Preparar Test: Es RETIRO + No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables notWeb;
    notWeb = prepareTest(files10.getId(), getUniqueLong().toString(), ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    notWeb.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(notWeb.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + No Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables notTecnocom;
    notTecnocom = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.NOT_RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    notTecnocom.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(notTecnocom.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + Pending Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables pendingTecnocom;
    pendingTecnocom = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.PENDING, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    pendingTecnocom.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(pendingTecnocom.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: no ok + Clearing OK
    ResolutionPreparedVariables movementRejected;
    movementRejected = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.REJECTED, AccountingStatusType.PENDING);
    movementRejected.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(movementRejected.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing NotInFile
    ResolutionPreparedVariables notInFile;
    notInFile = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    // Este caso NO va en el archivo

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing InvalidInformation_amount
    ResolutionPreparedVariables invalidInformation_amount;
    invalidInformation_amount = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amount.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amount.clearingData10.getAmount().setValue(invalidInformation_amount.clearingData10.getAmount().getValue().add(new BigDecimal(1L))); // Altera el valor de amount
    clearingData10ToFile.add(invalidInformation_amount.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing InvalidInformation_amountBalance
    ResolutionPreparedVariables invalidInformation_amountBalance;
    invalidInformation_amountBalance = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amountBalance.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amountBalance.clearingData10.getAmountBalance().setValue(invalidInformation_amountBalance.clearingData10.getAmountBalance().getValue().add(new BigDecimal(1L))); // Altera el valor de amount balance
    clearingData10ToFile.add(invalidInformation_amountBalance.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing InvalidInformation_amountMastercard
    ResolutionPreparedVariables invalidInformation_amountMastercard;
    invalidInformation_amountMastercard = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_amountMastercard.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_amountMastercard.clearingData10.getAmountMastercard().setValue(invalidInformation_amountMastercard.clearingData10.getAmountMastercard().getValue().add(new BigDecimal(1L))); // Altera el valor de amount mastercard
    clearingData10ToFile.add(invalidInformation_amountMastercard.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing InvalidInformation_accountRut
    ResolutionPreparedVariables invalidInformation_accountRut;
    invalidInformation_accountRut = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_accountRut.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().setValue(invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().getValue() + 1); // Altera rut
    invalidInformation_accountRut.clearingData10.getUserBankAccount().getRut().setDv("x");
    clearingData10ToFile.add(invalidInformation_accountRut.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing InvalidInformation_bankAccount
    ResolutionPreparedVariables invalidInformation_bankAccount;
    invalidInformation_bankAccount = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    invalidInformation_bankAccount.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    invalidInformation_bankAccount.clearingData10.getUserBankAccount().setAccountNumber(invalidInformation_bankAccount.clearingData10.getUserBankAccount().getAccountNumber().substring(2)); // Altera cuenta bancaria
    clearingData10ToFile.add(invalidInformation_bankAccount.clearingData10);

    // Preparar Test: No es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing Ok
    ResolutionPreparedVariables notWithdraw = new ResolutionPreparedVariables();
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

      AccountingData10 accountingData = buildRandomAccouting();
      accountingData.setFileId(files10.getId());
      accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
      accountingData.setIdTransaction(prepaidMovement.getId());
      accountingData.setType(AccountingTxType.RETIRO_WEB);
      accountingData.setStatus(AccountingStatusType.PENDING);
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setAccountingId(accountingData.getId());
      clearingData.setStatus(AccountingStatusType.PENDING);
      clearingData.setUserBankAccount(userAccount);
      clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

      // Set values not returned by insertClearing
      copyAccountingValues(accountingData, clearingData);
      clearingData.setUserBankAccount(userAccount);

      notWithdraw.accountingData10 = accountingData;
      notWithdraw.cdtTransaction10 = cdtTransaction;
      notWithdraw.prepaidMovement10 = prepaidMovement;
      notWithdraw.clearingData10 = clearingData;
    }
    notWithdraw.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(notWithdraw.clearingData10);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + Ya Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables reconciledMovement;
    reconciledMovement = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    getPrepaidMovementEJBBean10().createMovementConciliate(null, reconciledMovement.prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
    reconciledMovement.clearingData10.setStatus(AccountingStatusType.OK); // Para preparar para el archivo
    clearingData10ToFile.add(reconciledMovement.clearingData10);

    // Banco rechaza
    ResolutionPreparedVariables rejectedClearing;
    rejectedClearing = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    rejectedClearing.clearingData10.setStatus(AccountingStatusType.REJECTED); // Para preparar para el archivo
    clearingData10ToFile.add(rejectedClearing.clearingData10);

    // Banco rechaza por formato
    ResolutionPreparedVariables rejectedFormatClearing;
    rejectedFormatClearing = prepareTest(files10.getId(), NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.PENDING);
    rejectedFormatClearing.clearingData10.setStatus(AccountingStatusType.REJECTED_FORMAT); // Para preparar para el archivo
    rejectedFormatClearing.clearingData10.getUserBankAccount().setAccountNumber("");
    clearingData10ToFile.add(rejectedFormatClearing.clearingData10);

    // Agrega un movimiento que no esta en la db pero que vendra en el archivo
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
    bankAccount.setAccountType("Cuesta Corriente");
    bankAccount.setBankName("BANCO DE CHILE");
    notInBd.setUserBankAccount(bankAccount);
    clearingData10ToFile.add(notInBd);

    getPrepaidClearingEJBBean10().createAccountingCSV(fileName, fileId, clearingData10ToFile);
  }

  private void copyAccountingValues(AccountingData10 accountingData, ClearingData10 clearingData) {
    clearingData.setIdTransaction(accountingData.getIdTransaction());
    clearingData.setAmountMastercard(accountingData.getAmountMastercard());
    clearingData.setAmount(accountingData.getAmount());
    clearingData.setAmountUsd(accountingData.getAmountUsd());
    clearingData.setAmountBalance(accountingData.getAmountBalance());
    clearingData.setCollectorFee(accountingData.getCollectorFee());
    clearingData.setCollectorFeeIva(accountingData.getCollectorFeeIva());
    clearingData.setFee(accountingData.getFee());
    clearingData.setFeeIva(accountingData.getFeeIva());
    clearingData.setExchangeRateDif(accountingData.getExchangeRateDif());
    clearingData.setAccountingMovementType(accountingData.getAccountingMovementType());
    clearingData.setType(accountingData.getType());
    clearingData.setOrigin(accountingData.getOrigin());
    clearingData.setConciliationDate(accountingData.getConciliationDate());
    clearingData.setTimestamps(accountingData.getTimestamps());
    clearingData.setTransactionDate(accountingData.getTransactionDate());
  }
}
