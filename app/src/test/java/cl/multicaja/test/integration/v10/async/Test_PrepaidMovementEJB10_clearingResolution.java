package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test_PrepaidMovementEJB10_clearingResolution extends TestBaseUnitAsync {

  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
  }

  @Ignore
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

  //TODO: Revisar esto.
  public ResolutionPreparedVariables prepareTest(Long fileId, String merchantCode, ReconciliationStatusType tecnocomStatus, PrepaidMovementStatus movementStatus, AccountingStatusType clearingStatus) throws Exception {

    UserAccount userAccount = randomBankAccount();
    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
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
    if(PrepaidMovementType.TOPUP.equals(prepaidMovement.getTipoMovimiento())) {
      if(NewPrepaidWithdraw10.WEB_MERCHANT_CODE.equals(prepaidMovement.getCodcom())) {
        accountingData.setAccountingMovementType(AccountingMovementType.CARGA_WEB);
        accountingData.setType(AccountingTxType.CARGA_WEB);
      } else {
        accountingData.setAccountingMovementType(AccountingMovementType.CARGA_POS);
        accountingData.setType(AccountingTxType.CARGA_POS);
      }
    } else if(PrepaidMovementType.WITHDRAW.equals(prepaidMovement.getTipoMovimiento())) {
      if(NewPrepaidWithdraw10.WEB_MERCHANT_CODE.equals(prepaidMovement.getCodcom())) {
        accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
        accountingData.setType(AccountingTxType.RETIRO_WEB);
      } else {
        accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_POS);
        accountingData.setType(AccountingTxType.RETIRO_POS);
      }
    }
    accountingData.setIdTransaction(prepaidMovement.getId());
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

  static public class ResolutionPreparedVariables {
    public PrepaidMovement10 prepaidMovement10;
    public CdtTransaction10 cdtTransaction10;
    public AccountingData10 accountingData10;
    public ClearingData10 clearingData10;
  }

  static public void copyAccountingValues(AccountingData10 accountingData, ClearingData10 clearingData) {
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
