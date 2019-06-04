package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.test.integration.v10.async.Test_PrepaidMovementEJB10_clearingResolution;
import cl.multicaja.test.integration.v10.async.Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;

public class Test_PrepaidMovementEJBBean10_clearingResolution extends TestBaseUnit {

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
  public void clearingResolution_All() throws Exception {

    // 1. Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables allOk;
    allOk = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // 2. Preparar Test: No es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing Ok
    ResolutionPreparedVariables notWithdraw = new ResolutionPreparedVariables();
    {

      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard = createPrepaidCardV2(prepaidCard);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
      prepaidTopup.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
      prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
      prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

      CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
      cdtTransaction = createCdtTransaction10(cdtTransaction);

      PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP,false);
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
      prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      AccountingData10 accountingData = buildRandomAccouting();
      accountingData.setAccountingMovementType(AccountingMovementType.CARGA_WEB);
      accountingData.setIdTransaction(prepaidMovement.getId());
      accountingData.setType(AccountingTxType.CARGA_WEB);
      accountingData.setStatus(AccountingStatusType.PENDING);
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setAccountingId(accountingData.getId());
      clearingData.setIdTransaction(prepaidMovement.getId());
      clearingData.setStatus(AccountingStatusType.OK);

      UserAccount userAccount = new UserAccount();
      userAccount.setRut(getUniqueRutNumber().toString());
      userAccount.setAccountType("Corriente");
      userAccount.setBankId(1L);
      userAccount.setAccountNumber(getUniqueLong());

      clearingData.setUserBankAccount(userAccount);
      getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

      notWithdraw.accountingData10 = accountingData;
      notWithdraw.cdtTransaction10 = cdtTransaction;
      notWithdraw.prepaidMovement10 = prepaidMovement;
      notWithdraw.clearingData10 = clearingData;
    }

    // 3. Preparar Test: Es RETIRO + >>No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables notWeb;
    notWeb = prepareTest(0L, getUniqueLong().toString(), ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // 4. Preparar Test: Es RETIRO + Es WEB + >>Tecnocom: NOT_RECONCILED + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables notTecnocom;
    notTecnocom = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.NOT_RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // 5. Preparar Test: Es RETIRO + Es WEB + >>Tecnocom: PENDING + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables pendingTecnocom;
    pendingTecnocom = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.PENDING, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // 6. Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + >> Ya Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables alreadyReconciledMovement;
    alreadyReconciledMovement = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);
    getPrepaidMovementEJBBean10().createMovementConciliate(null, alreadyReconciledMovement.prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    // 7. Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + >> MovStatus: distinto de ok + Clearing OK
    ResolutionPreparedVariables movementRejected;
    movementRejected = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.REJECTED, AccountingStatusType.OK);

    // 8. Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >> Clearing NotInFile
    ResolutionPreparedVariables notInFile;
    notInFile = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.NOT_IN_FILE);

    // 9. Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >> Clearing InvalidInformation
    ResolutionPreparedVariables invalidInformation;
    invalidInformation = prepareTest(0L, NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.INVALID_INFORMATION);

    // Corre resolution
    getPrepaidMovementEJBBean10().clearingResolution();

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
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.PENDING, foundAccounting.getStatus());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundAccounting.getAccountingStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(allOk.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado reconciled", ReconciliationStatusType.RECONCILED, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion none", ReconciliationActionType.NONE, reconciliedMovement10.getActionType());
    }

    // 2. Chequea test: No es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notWithdraw.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notWithdraw.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio confirmed", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notWithdraw.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(notWithdraw.prepaidMovement10.getId());
      Assert.assertNull("No debe existir reconciled", reconciliedMovement10);

      // No debe estar en research
      List<ResearchMovement10> researchMovement = getResearchMovement(notWithdraw.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, researchMovement.size());
      researchMovement = getResearchMovement(notWithdraw.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, researchMovement.size());

    }

    // 3. Chequea test: Es RETIRO + >> No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notWeb.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_POS_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notWeb.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio confirmed", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notWeb.accountingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(notWeb.prepaidMovement10.getId());
      Assert.assertNull("Debe tener estado reconciled", reconciliedMovement10);

      // No debe estar en research
      List<ResearchMovement10> reconciliedResearch = getResearchMovement(notWeb.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
      reconciliedResearch = getResearchMovement(notWeb.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());

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
      Assert.assertEquals("Debe tener estado pending", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(notTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      List<ResearchMovement10> researchMovement = getResearchMovement(notTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("Debe estar en research", 1, researchMovement.size());
    }

    // 5. Chequea test: Es RETIRO + Es WEB + >> PENDING Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, pendingTecnocom.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio sin cambio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(pendingTecnocom.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertNull("No debe tener estado reconciled", reconciliedMovement10);

      // No debe estar en research
      List<ResearchMovement10> researchMovement = getResearchMovement(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, researchMovement.size());
      researchMovement = getResearchMovement(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, researchMovement.size());
    }

    // 6. Chequea test: Es RETIRO + Es WEB + Ok Tecnocom + >> Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Debido a que no fue tomado, ninguno de sus estados debe haber cambiado
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, alreadyReconciledMovement.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(alreadyReconciledMovement.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio sin cambio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(alreadyReconciledMovement.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(alreadyReconciledMovement.prepaidMovement10.getId());
      Assert.assertNotNull("Debe tener estado reconciled", reconciliedMovement10);

      // No debe estar en research
      List<ResearchMovement10> reconciliedResearch = getResearchMovement(alreadyReconciledMovement.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
      reconciliedResearch = getResearchMovement(alreadyReconciledMovement.prepaidMovement10.getId());
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
    }

    // 7. Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + >> MovStatus: distinto de ok + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, movementRejected.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(movementRejected.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio sin cambio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(movementRejected.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(movementRejected.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      List<ResearchMovement10> researchMovement = getResearchMovement(movementRejected.prepaidMovement10.getId());
      Assert.assertEquals("Debe estar en research", 1, researchMovement.size());
    }

    // 8. Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing notInFile
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notInFile.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notInFile.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio sin cambio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notInFile.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(notInFile.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      List<ResearchMovement10> researchMovement = getResearchMovement(notInFile.prepaidMovement10.getId());
      Assert.assertEquals("Debe estar en research", 1, researchMovement.size());
    }

    // Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + >> Clearing invalidInformation
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, invalidInformation.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(invalidInformation.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio sin cambio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(invalidInformation.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(invalidInformation.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

      // Debe estar en research
      List<ResearchMovement10> researchMovement = getResearchMovement(invalidInformation.prepaidMovement10.getId());
      Assert.assertEquals("Debe estar en research", 1, researchMovement.size());
    }
  }

  private List<ResearchMovement10> getResearchMovement(Long movId) throws Exception{
    return getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movId));
  }

  private ReconciliedMovement10 getReconciliedMovement(Long idMov) {
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedMovement10 reconciliedMovement10 = new ReconciliedMovement10();
      reconciliedMovement10.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedMovement10.setIdMovRef(numberUtils.toLong(rs.getLong("id_mov_ref")));
      reconciliedMovement10.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(rs.getString("estado"))));
      reconciliedMovement10.setActionType(ReconciliationActionType.valueOf(String.valueOf(rs.getString("accion"))));
      return reconciliedMovement10;
    };
    List<ReconciliedMovement10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_conciliado where id_mov_ref = %d", getSchema(), idMov), rowMapper);
    ReconciliedMovement10 reconciliedMovement10 = data.size() == 0 ? null : data.get(0);
    return reconciliedMovement10;
  }

  private AccountingData10 getAccountingData(Long idMov) {
    RowMapper rowMapper = (rs, rowNum) -> {
      AccountingData10 accountingData10 = new AccountingData10();
      accountingData10.setId(numberUtils.toLong(rs.getLong("id")));
      accountingData10.setStatus(AccountingStatusType.fromValue(String.valueOf(rs.getString("status"))));
      accountingData10.setAccountingStatus(AccountingStatusType.fromValue(String.valueOf(rs.getString("accounting_status"))));
      return accountingData10;
    };
    List<AccountingData10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT id, status, accounting_status FROM %s.accounting where id = %d", getSchemaAccounting(), idMov), rowMapper);
    AccountingData10 accountingData10 = data.get(0);
    return accountingData10;
  }

  private Test_PrepaidMovementEJB10_clearingResolution.ResolutionPreparedVariables prepareTest(Long fileId, String merchantCode, ReconciliationStatusType tecnocomStatus, PrepaidMovementStatus movementStatus, AccountingStatusType clearingStatus) throws Exception {
    return new Test_PrepaidMovementEJB10_clearingResolution().prepareTest(fileId, merchantCode, tecnocomStatus, movementStatus, clearingStatus);
  }
}
