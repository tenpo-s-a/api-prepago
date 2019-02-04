package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;

public class Test_PrepaidMovementEJB10_processClearingResolution extends TestBaseUnitAsync {


  @Test
  public void processClearingResolution_BankStatus_Rejected() throws Exception {
    User user = registerUser();
    UserAccount userAccount = createBankAccount(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankAccountId(userAccount.getId());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setId(getUniqueLong());
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.REJECTED); // Banco rechaza deposito
    clearingData10.setUserBankAccount(userAccount);

    // No vino en el archivo del banco
    getPrepaidMovementEJBBean10().processClearingResolution(prepaidMovement10, clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement reconciliedMovement = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement);
    Assert.assertEquals("Debe estar en estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement.getActionType());

    Boolean reverseFound = false;
    Boolean movementFound = false;
    for(int i = 0; i < 20; i++) {
      Thread.sleep(500); // Esperar que el async envie la reversa
      System.out.println("Buscando...");
      if(!reverseFound) {
        PrepaidMovement10 foundReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidMovement10.getIdTxExterno(), prepaidMovement10.getTipoMovimiento(), IndicadorNormalCorrector.CORRECTORA);
        if(foundReverse != null) {
          System.out.println("Encontre reversa...");
          reverseFound = true;
        }
      }

      if(!movementFound) {
        PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
        if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
          System.out.println("Encontre movimiento con estado cambiado...");
          movementFound = true;
        }
      }

      if(movementFound && reverseFound) {
        break;
      }
    }
    Assert.assertTrue("Debe encontrarse la reversa", reverseFound);
    Assert.assertTrue("Debe encontrarse el movimiento con el estado cambiado", movementFound);
  }

  @Test
  public void processClearingResolution_BankStatus_RejectedFormat() throws Exception {
    User user = registerUser();
    UserAccount userAccount = createBankAccount(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankAccountId(userAccount.getId());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setId(getUniqueLong());
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.REJECTED_FORMAT); // MC rechaza archivo
    clearingData10.setUserBankAccount(userAccount);

    // No vino en el archivo del banco
    getPrepaidMovementEJBBean10().processClearingResolution(prepaidMovement10, clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement reconciliedMovement = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement);
    Assert.assertEquals("Debe estar en estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement.getActionType());

    Boolean reverseFound = false;
    Boolean movementFound = false;
    for(int i = 0; i < 20; i++) {
      Thread.sleep(500); // Esperar que el async envie la reversa
      System.out.println("Buscando...");
      if(!reverseFound) {
        PrepaidMovement10 foundReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidMovement10.getIdTxExterno(), prepaidMovement10.getTipoMovimiento(), IndicadorNormalCorrector.CORRECTORA);
        if(foundReverse != null) {
          System.out.println("Encontre reversa...");
          reverseFound = true;
        }
      }

      if(!movementFound) {
        PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
        if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
          System.out.println("Encontre movimiento con estado cambiado...");
          movementFound = true;
        }
      }

      if(movementFound && reverseFound) {
        break;
      }
    }
    Assert.assertTrue("Debe encontrarse la reversa", reverseFound);
    Assert.assertTrue("Debe encontrarse el movimiento con el estado cambiado", movementFound);
  }


  private ReconciliedMovement getReconciliedMovement(Long idMov) {
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedMovement reconciliedMovement = new ReconciliedMovement();
      reconciliedMovement.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedMovement.setIdMovRef(numberUtils.toLong(rs.getLong("id_mov_ref")));
      reconciliedMovement.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(rs.getString("estado"))));
      reconciliedMovement.setActionType(ReconciliationActionType.valueOf(String.valueOf(rs.getString("accion"))));
      return reconciliedMovement;
    };
    List<ReconciliedMovement> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_conciliado where id_mov_ref = %d", getSchema(), idMov), rowMapper);
    ReconciliedMovement reconciliedMovement = data.get(0);
    return reconciliedMovement;
  }
}
