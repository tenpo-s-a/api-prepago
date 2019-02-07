package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

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

  @Test
  public void clearingResolution_Reversed() throws Exception {
    // Banco rechaza
    ResolutionPreparedVariables rejectedClearing;
    rejectedClearing = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.REJECTED);

    // Banco rechaza por formato
    ResolutionPreparedVariables rejectedFormatClearing;
    rejectedFormatClearing = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.REJECTED_FORMAT);

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
      CdtTransaction10 reverseCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, foundReverse.getIdTxExterno(), originalCdtTransaction.getCdtTransactionTypeReverse());
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

  private ResolutionPreparedVariables prepareTest(String merchantCode, ReconciliationStatusType tecnocomStatus, PrepaidMovementStatus movementStatus, AccountingStatusType clearingStatus) throws Exception {
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

    AccountingData10 accountingData = buildRandomAccouting();
    accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData.setIdTransaction(prepaidMovement.getId());
    accountingData.setType(AccountingTxType.RETIRO_WEB);
    accountingData.setStatus(AccountingStatusType.PENDING);
    accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);

    ClearingData10 clearingData = new ClearingData10();
    clearingData.setAccountingId(accountingData.getId());
    clearingData.setStatus(clearingStatus);
    clearingData.setUserBankAccount(userAccount);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

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
}
