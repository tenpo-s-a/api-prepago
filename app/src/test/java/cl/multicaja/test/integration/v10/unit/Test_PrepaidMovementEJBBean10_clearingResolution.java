package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
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

    // RETIRO + WEB + OK Tecnocom + NO Conciliado + process OK + Clearing (OK, Invalid information, not in file, rejected, rejected_format)

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables allOk;
    allOk = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // Preparar Test: Es RETIRO + No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables notWeb;
    notWeb = prepareTest(getUniqueLong().toString(), ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // Preparar Test: Es RETIRO + Es WEB + No Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables notTecnocom;
    notTecnocom = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.NOT_RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // Preparar Test: Es RETIRO + Es WEB + Pending Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables pendingTecnocom;
    pendingTecnocom = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.PENDING, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: no ok + Clearing OK
    ResolutionPreparedVariables movementRejected;
    movementRejected = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.REJECTED, AccountingStatusType.OK);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing NotInFile
    ResolutionPreparedVariables notInFile;
    notInFile = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.NOT_IN_FILE);

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing InvalidInformatio
    ResolutionPreparedVariables invalidInformation;
    invalidInformation = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.INVALID_INFORMATION);

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
      accountingData.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
      accountingData.setIdTransaction(prepaidMovement.getId());
      accountingData.setType(AccountingTxType.RETIRO_WEB);
      accountingData.setStatus(AccountingStatusType.PENDING);
      accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData);

      ClearingData10 clearingData = new ClearingData10();
      clearingData.setAccountingId(accountingData.getId());
      clearingData.setStatus(AccountingStatusType.OK);
      clearingData.setUserBankAccount(userAccount);
      getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

      notWithdraw.accountingData10 = accountingData;
      notWithdraw.cdtTransaction10 = cdtTransaction;
      notWithdraw.prepaidMovement10 = prepaidMovement;
      notWithdraw.clearingData10 = clearingData;
    }

    // Preparar Test: Es RETIRO + Es WEB + OK Tecnocom + Ya Conciliado en BD + MovStatus: process OK + Clearing OK
    ResolutionPreparedVariables reconciledMovement;
    reconciledMovement = prepareTest(NewPrepaidWithdraw10.WEB_MERCHANT_CODE, ReconciliationStatusType.RECONCILED, PrepaidMovementStatus.PROCESS_OK, AccountingStatusType.OK);
    getPrepaidMovementEJBBean10().createMovementConciliate(null, reconciledMovement.prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    getPrepaidMovementEJBBean10().clearingResolution();

    // Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
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
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(allOk.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado reconciled", ReconciliationStatusType.RECONCILED, reconciliedMovement.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion none", ReconciliationActionType.NONE, reconciliedMovement.getActionType());
    }

    // Chequea test: Es RETIRO + No es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
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
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(notWeb.prepaidMovement10.getId());
      Assert.assertNull("Debe tener estado reconciled", reconciliedMovement);

      // No debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idMov=%d", notWeb.prepaidMovement10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
      reconciliedResearch = getResearchMovement(String.format("idClearing=%d", notWeb.clearingData10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
    }

    // Chequea test: Es RETIRO + Es WEB + No Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notTecnocom.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio confirmed", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notTecnocom.accountingData10.getId());
      Assert.assertEquals("Debe tener estado pending", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado para que no vuelva a ser procesado
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(notTecnocom.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement.getActionType());

      // Debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idMov=%d", notTecnocom.prepaidMovement10.getId()));
      Assert.assertEquals("Debe estar en research", 1, reconciliedResearch.size());
    }

    // Chequea test: Es RETIRO + Es WEB + PENDING Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
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
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(pendingTecnocom.prepaidMovement10.getId());
      Assert.assertNull("No debe tener estado reconciled", reconciliedMovement);

      // No debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idMov=%d", pendingTecnocom.prepaidMovement10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
      reconciliedResearch = getResearchMovement(String.format("idClearing=%d", pendingTecnocom.clearingData10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
    }

    // Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: rejected + Clearing OK
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
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(movementRejected.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement.getActionType());

      // Debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idMov=%d", movementRejected.prepaidMovement10.getId()));
      Assert.assertEquals("Debe estar en research", 1, reconciliedResearch.size());
    }

    // Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing notInFile
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
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(notInFile.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement.getActionType());

      // Debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idClearing=%d", notInFile.clearingData10.getId()));
      Assert.assertEquals("Debe estar en research", 1, reconciliedResearch.size());
    }

    // Chequea test: Es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process_ok + Clearing invalidInformation
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
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(invalidInformation.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado need verif", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement.getReconciliationStatusType());
      Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement.getActionType());

      // Debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idClearing=%d", invalidInformation.clearingData10.getId()));
      Assert.assertEquals("Debe estar en research", 1, reconciliedResearch.size());
    }

    // Chequea test: No es RETIRO + Es WEB + OK Tecnocom + NO Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, notWithdraw.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(notWithdraw.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio confirmed", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(notWithdraw.accountingData10.getId());
      Assert.assertEquals("Debe tener estado OK", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento no debe quedar conciliado
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(notWithdraw.prepaidMovement10.getId());
      Assert.assertNull("Debe tener estado reconciled", reconciliedMovement);

      // No debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idMov=%d", notWithdraw.prepaidMovement10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
      reconciliedResearch = getResearchMovement(String.format("idClearing=%d", notWithdraw.clearingData10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
    }

    // Chequea test: Es RETIRO + Es WEB + Ok Tecnocom + Conciliado en BD + MovStatus: process OK + Clearing OK
    {
      // Debido a que no fue tomado, ninguno de sus estados debe haber cambiado
      // Revisar que no haya confirmado en el cdt
      CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, reconciledMovement.prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
      Assert.assertNull("No debe existir la confirmacion en el cdt", foundCdtTransation);

      // Revisar que no se confirme el estado del negocio
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(reconciledMovement.prepaidMovement10.getId());
      Assert.assertEquals("Debe tener estado de nogocio sin cambio", BusinessStatusType.IN_PROCESS, foundMovement.getEstadoNegocio());

      // Revisar que el estado de accounting no haya cambiado
      AccountingData10 foundAccounting = getAccountingData(reconciledMovement.accountingData10.getId());
      Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());

      // El movimiento debe quedar conciliado
      ReconciliedMovement reconciliedMovement = getReconciliedMovement(reconciledMovement.prepaidMovement10.getId());
      Assert.assertNotNull("Debe tener estado reconciled", reconciliedMovement);

      // No debe estar en research
      List<ReconciliedResearch> reconciliedResearch = getResearchMovement(String.format("idMov=%d", reconciledMovement.prepaidMovement10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
      reconciliedResearch = getResearchMovement(String.format("idClearing=%d", reconciledMovement.clearingData10.getId()));
      Assert.assertEquals("No debe estar en research", 0, reconciliedResearch.size());
    }
  }


  private List<ReconciliedResearch> getResearchMovement(String movId) {
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedResearch reconciliedResearch = new ReconciliedResearch();
      reconciliedResearch.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedResearch.setIdRef(String.valueOf(rs.getString("mov_ref")));
      reconciliedResearch.setNombre_archivo(String.valueOf(rs.getString("nombre_archivo")));
      reconciliedResearch.setOrigen(String.valueOf(rs.getString("origen")));
      return reconciliedResearch;
    };
    List<ReconciliedResearch> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where mov_ref LIKE '%s'", getSchema(), movId), rowMapper);
    return data;
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
    ReconciliedMovement reconciliedMovement = data.size() == 0 ? null : data.get(0);
    return reconciliedMovement;
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
    clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData);

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
