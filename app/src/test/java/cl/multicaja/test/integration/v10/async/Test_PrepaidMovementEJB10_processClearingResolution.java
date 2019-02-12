package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;

public class Test_PrepaidMovementEJB10_processClearingResolution extends TestBaseUnitAsync {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
  }

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
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.REJECTED); // Banco rechaza deposito
    clearingData10.setUserBankAccount(userAccount);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    // Banco rechaza retiro
    getPrepaidMovementEJBBean10().processClearingResolution(prepaidMovement10, clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement reconciliedMovement = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement);
    Assert.assertEquals("Debe estar en estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement.getActionType());

    PrepaidMovement10 foundMovement = null;
    for(int i = 0; i < 20; i++) {
      Thread.sleep(500); // Esperar que el async ejecute la reversa
      System.out.println("Buscando...");

      foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
        break;
      }
    }

    Assert.assertNotNull("Debe encontrarse el movimiento", foundMovement);
    Assert.assertEquals("Debe tener estado reversado", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());

    PrepaidMovement10 foundReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidMovement10.getIdTxExterno(), prepaidMovement10.getTipoMovimiento(), IndicadorNormalCorrector.CORRECTORA);
    Assert.assertNotNull("Debe existir una reversa", foundReverse);

    // Chequear que exista el cdt confirmado
    CdtTransaction10 originalCdtTransaction = getCdtEJBBean10().buscaMovimientoReferencia(null, prepaidMovement10.getIdMovimientoRef());
    CdtTransaction10 foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, prepaidMovement10.getIdTxExterno(), cdtTransaction.getCdtTransactionTypeConfirm());
    Assert.assertNotNull("Debe existir la confirmacion del movimiento cdt", foundCdtTransaction);
    Assert.assertEquals("Debe referenciar al movimiento original", originalCdtTransaction.getId(), foundCdtTransaction.getTransactionReference());

    // Chequear que exista la reversa del cdt
    CdtTransaction10 reverseCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, foundReverse.getIdTxExterno(), originalCdtTransaction.getCdtTransactionTypeReverse());
    Assert.assertNotNull("Debe existir la reversa en el cdt", reverseCdtTransaction);

    // Debe existir la confirmacion de la reversa
    CdtTransaction10 reverseConfirm = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, prepaidMovement10.getIdTxExterno(), reverseCdtTransaction.getCdtTransactionTypeConfirm());
    Assert.assertNotNull("Debe exitir la confirmacion de la reversa", reverseConfirm);
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
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.REJECTED_FORMAT); // MC rechaza archivo
    clearingData10.setUserBankAccount(userAccount);
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    // Rechazo formato
    getPrepaidMovementEJBBean10().processClearingResolution(prepaidMovement10, clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement reconciliedMovement = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement);
    Assert.assertEquals("Debe estar en estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement.getActionType());

    PrepaidMovement10 foundMovement = null;
    for(int i = 0; i < 20; i++) {
      Thread.sleep(500); // Esperar que el async ejecute la reversa
      System.out.println("Buscando...");

      foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
        break;
      }
    }

    Assert.assertNotNull("Debe encontrarse el movimiento", foundMovement);
    Assert.assertEquals("Debe tener estado reversado", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());

    PrepaidMovement10 foundReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidMovement10.getIdTxExterno(), prepaidMovement10.getTipoMovimiento(), IndicadorNormalCorrector.CORRECTORA);
    Assert.assertNotNull("Debe existir una reversa", foundReverse);

    // Chequear que exista el cdt confirmado
    CdtTransaction10 originalCdtTransaction = getCdtEJBBean10().buscaMovimientoReferencia(null, prepaidMovement10.getIdMovimientoRef());
    CdtTransaction10 foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, prepaidMovement10.getIdTxExterno(), cdtTransaction.getCdtTransactionTypeConfirm());
    Assert.assertNotNull("Debe existir la confirmacion del movimiento cdt", foundCdtTransaction);
    Assert.assertEquals("Debe referenciar al movimiento original", originalCdtTransaction.getId(), foundCdtTransaction.getTransactionReference());

    // Chequear que exista la reversa del cdt
    CdtTransaction10 reverseCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, foundReverse.getIdTxExterno(), originalCdtTransaction.getCdtTransactionTypeReverse());
    Assert.assertNotNull("Debe existir la reversa en el cdt", reverseCdtTransaction);

    // Debe existir la confirmacion de la reversa
    CdtTransaction10 reverseConfirm = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, prepaidMovement10.getIdTxExterno(), reverseCdtTransaction.getCdtTransactionTypeConfirm());
    Assert.assertNotNull("Debe exitir la confirmacion de la reversa", reverseConfirm);
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
