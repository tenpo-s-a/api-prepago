package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Test_PrepaidMovementEJBBean10_processReconciliation extends TestBaseUnit {

  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private DateTimeFormatter conciliationDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
  }

  // Movimiento conciliado OK
  @Test
  public void processReconciliationOk_PosTopup() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setMerchantCode(getRandomString(15));
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement10);

    // procesa la conciliacion
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    // verifica movimiento conciliado
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());


    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement10.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener status PENDING", AccountingStatusType.PENDING, accounting10.getStatus());
    Assert.assertEquals("Debe tener accountingStatus OK", AccountingStatusType.OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertEquals("debe tener la misma fecha de transaccion", prepaidMovement10.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));
    Assert.assertEquals("debe tener la misma fecha de conciliacion", movConciliado.getFechaRegistro().toLocalDateTime().format(conciliationDateTimeFormatter), accounting10.getConciliationDate().toLocalDateTime().format(conciliationDateTimeFormatter));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado PENDING", AccountingStatusType.PENDING, clearing10.getStatus());
  }

  @Test
  public void processReconciliationOk_WebTopup() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement10);

    // procesa la conciliacion
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    // verifica movimiento conciliado
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());


    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement10.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.CARGA_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.CARGA_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener status PENDING", AccountingStatusType.PENDING, accounting10.getStatus());
    Assert.assertEquals("Debe tener accountingStatus OK", AccountingStatusType.OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertEquals("debe tener la misma fecha de transaccion", prepaidMovement10.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));
    Assert.assertEquals("debe tener la misma fecha de conciliacion", movConciliado.getFechaRegistro().toLocalDateTime().format(conciliationDateTimeFormatter), accounting10.getConciliationDate().toLocalDateTime().format(conciliationDateTimeFormatter));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado PENDING", AccountingStatusType.PENDING, clearing10.getStatus());
  }

  @Test
  public void processReconciliationOk_PosWithdraw() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(getRandomString(15));
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement10);

    // procesa la conciliacion
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    // verifica movimiento conciliado
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());


    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement10.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.RETIRO_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener status PENDING", AccountingStatusType.PENDING, accounting10.getStatus());
    Assert.assertEquals("Debe tener accountingStatus OK", AccountingStatusType.OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertEquals("debe tener la misma fecha de transaccion", prepaidMovement10.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));
    Assert.assertEquals("debe tener la misma fecha de conciliacion", movConciliado.getFechaRegistro().toLocalDateTime().format(conciliationDateTimeFormatter), accounting10.getConciliationDate().toLocalDateTime().format(conciliationDateTimeFormatter));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado PENDING", AccountingStatusType.PENDING, clearing10.getStatus());
  }

  @Test
  public void processReconciliationOk_WebWithdraw() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement10, AccountingStatusType.PENDING);

    // procesa la conciliacion
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());


    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    Long movId = prepaidMovement10.getId();

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", accounting10);
    Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener status PENDING", AccountingStatusType.PENDING, accounting10.getStatus());
    Assert.assertEquals("Debe tener accountingStatus OK", AccountingStatusType.PENDING, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertEquals("debe tener la misma fecha de transaccion", prepaidMovement10.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));
    Assert.assertNotEquals("No debe tener fecha de conciliacion", movConciliado.getFechaRegistro().toLocalDateTime().format(conciliationDateTimeFormatter), accounting10.getConciliationDate().toLocalDateTime().format(conciliationDateTimeFormatter));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimientos de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.PENDING, clearing10.getStatus());
  }

  //Movimiento No Conciliado tecnocom, conciliado Switch status ok
  @Test
  public void processReconciliationCase3() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());
    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    //Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdArchivoOrigen().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    //Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());
  }
  // Movimiento no conciliado por ninguno, procesado ok
  @Test
  public void processReconciliationCase4() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());
    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    //Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdArchivoOrigen().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    //Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());
  }
  // Conciliado por todos, con error en nuestra tabla
  @Test
  public void processReconciliationCase5() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement10, AccountingStatusType.PENDING);


    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);
    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","RECONCILED",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","NONE",movConciliado.getActionType().toString());
  }
  // Conciliado por todos con error nuestro tipo retiro.
  @Test
  public void processReconciliationCase8() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());

    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    //Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdArchivoOrigen().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    //Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());

  }
  // Movimientos con status pendiente o en proceso.
  @Test
  public void processReconciliationCase19_24() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PENDING);

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    ReconciliedMovement10 movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());

    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    //Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdArchivoOrigen().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    //Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());

  }

  private void addAccountingAndClearing(PrepaidMovement10 prepaidMovement, AccountingStatusType clearingStatus) throws Exception {
    PrepaidAccountingMovement pam = new PrepaidAccountingMovement();
    pam.setPrepaidMovement10(prepaidMovement);
    pam.setReconciliationDate(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toInstant()));

    AccountingData10 accounting = getPrepaidAccountingEJBBean10().buildAccounting10(pam, AccountingStatusType.PENDING, AccountingStatusType.PENDING);

    accounting = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting);

    // Insertar en clearing
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(clearingStatus);
    clearing10.setUserBankAccount(null);
    clearing10.setAccountingId(accounting.getId());
    getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
  }

  private void addAccountingAndClearing(PrepaidMovement10 prepaidMovement) throws Exception {
    addAccountingAndClearing(prepaidMovement, AccountingStatusType.INITIAL);
  }

  private ReconciliedMovement10 getMovimientoConciliado(Long idMovRef){
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedMovement10 reconciliedMovement10 = new ReconciliedMovement10();
      reconciliedMovement10.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedMovement10.setIdMovRef(numberUtils.toLong(rs.getLong("id_mov_ref")));
      reconciliedMovement10.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(rs.getString("estado"))));
      reconciliedMovement10.setActionType(ReconciliationActionType.valueOf(String.valueOf(rs.getString("accion"))));
      reconciliedMovement10.setFechaRegistro(rs.getTimestamp("fecha_registro"));
      return reconciliedMovement10;
    };
    List<ReconciliedMovement10> data =getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_conciliado where id_mov_ref = %s",getSchema(), idMovRef), rowMapper);
    return data.get(0);
  }
  
  private ReconciliedResearch getMovimientoInvestigarMotor(Long idMovRef){
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedResearch reconciliedResearch = new ReconciliedResearch();
      reconciliedResearch.setId(numberUtils.toLong(rs.getLong("id")));
      //reconciliedResearch.setIdRef(String.valueOf(rs.getString("mov_ref")));
      reconciliedResearch.setIdArchivoOrigen(String.valueOf(rs.getString("id_archivo_origen")));
      reconciliedResearch.setNombreArchivo(String.valueOf(rs.getString("nombre_archivo")));
      reconciliedResearch.setOrigen(String.valueOf(rs.getString("origen")));
      return reconciliedResearch;
    };
    //List<ReconciliedResearch> data =getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where mov_ref = 'idMov=%s'",getSchema(),idMovRef),rowMapper);
    List<ReconciliedResearch> data =getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where id_archivo_origen = 'idMov=%s'",getSchema(),idMovRef),rowMapper);
    return data.get(0);
  }
}

class ReconciliedResearch {
  private Long id;
  //private String idRef;
  private String idArchivoOrigen;
  private String origen;
  private String nombreArchivo;
  private Timestamp fechaRegistro;
  private Timestamp fechaDeTransaccion;
  private String responsable;
  private String descripcion;
  private Long movRef;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdArchivoOrigen() {
    return idArchivoOrigen;
  }

  public void setIdArchivoOrigen(String idArchivoOrigen) {
    this.idArchivoOrigen = idArchivoOrigen;
  }

  public String getOrigen() {
    return origen;
  }

  public void setOrigen(String origen) {
    this.origen = origen;
  }

  public String getNombreArchivo() {
    return nombreArchivo;
  }

  public void setNombreArchivo(String nombreArchivo) {
    this.nombreArchivo = nombreArchivo;
  }

  public Timestamp getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(Timestamp fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public Timestamp getFechaDeTransaccion() {
    return fechaDeTransaccion;
  }

  public void setFechaDeTransaccion(Timestamp fechaDeTransaccion) {
    this.fechaDeTransaccion = fechaDeTransaccion;
  }

  public String getResponsable() {
    return responsable;
  }

  public void setResponsable(String responsable) {
    this.responsable = responsable;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Long getMovRef() {
    return movRef;
  }

  public void setMovRef(Long movRef) {
    this.movRef = movRef;
  }
}
