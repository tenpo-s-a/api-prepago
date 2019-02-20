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
import java.sql.Timestamp;
import java.time.*;
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
    accountingData10.setAmount(new NewAmountAndCurrency10(prepaidMovement10.getImpfac()));
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.REJECTED); // Banco rechaza deposito
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setIdTransaction(prepaidMovement10.getId());
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    // Banco rechaza retiro
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estar en estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement10.getActionType());

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

    Thread.sleep(5000);

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

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement10.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.REJECTED, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertNotEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado REJECTED", AccountingStatusType.REJECTED, clearing10.getStatus());
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
    accountingData10.setAmount(new NewAmountAndCurrency10(prepaidMovement10.getImpfac()));
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    accountingData10.setAccountingStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.REJECTED_FORMAT); // MC rechaza archivo
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setIdTransaction(prepaidMovement10.getId());
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    // Rechazo formato
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estar en estado counter movement", ReconciliationStatusType.COUNTER_MOVEMENT, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion reversa retiro", ReconciliationActionType.REVERSA_RETIRO, reconciliedMovement10.getActionType());

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

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement10.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());

    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));


    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.REJECTED_FORMAT, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertNotEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado REJECTED_FORMAT", AccountingStatusType.REJECTED_FORMAT, clearing10.getStatus());
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
    ReconciliedMovement10 reconciliedMovement10 = data.get(0);
    return reconciliedMovement10;
  }

  private Boolean checkReconciliationDate(Timestamp ts) {

    Instant instant = Instant.now();
    ZoneId z = ZoneId.of( "UTC" );
    ZonedDateTime nowUtc = instant.atZone(z);

    LocalDateTime localDateTime = ts.toLocalDateTime();
    ZonedDateTime reconciliationDateUtc = localDateTime.atZone(ZoneOffset.UTC);

    return reconciliationDateUtc.isBefore(nowUtc);
  }
}
