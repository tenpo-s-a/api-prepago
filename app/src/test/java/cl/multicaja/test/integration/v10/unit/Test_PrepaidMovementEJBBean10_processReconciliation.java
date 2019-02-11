package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Test_PrepaidMovementEJBBean10_processReconciliation extends TestBaseUnit {

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
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
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
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
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
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
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

    // verifica movimiento conciliado
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());
    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
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
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());
    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());
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
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());
    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());
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
    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);
    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

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

    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());

    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());

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

    ReconciliedMovement movConciliado = getMovimientoConciliado(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento conciliado",movConciliado);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId(),movConciliado.getIdMovRef());
    Assert.assertEquals("Estado debe ser Conciliado","NEED_VERIFICATION",movConciliado.getReconciliationStatusType().getValue());
    Assert.assertEquals("La accion debe ser","INVESTIGACION",movConciliado.getActionType().toString());

    ReconciliedResearch reconciliedResearch = getMovimientoInvestigarMotor(prepaidMovement10.getId());

    Assert.assertNotNull("Debe contener un movimiento a investigar",reconciliedResearch);
    Assert.assertEquals("Los id deben coincidir",prepaidMovement10.getId().toString(),reconciliedResearch.getIdRef().replace("idMov=",""));
    Assert.assertEquals("El Origen debe ser Motot","MOTOR",reconciliedResearch.getOrigen());
    Assert.assertEquals("El Nombre archivo debe ser vacio","",reconciliedResearch.getNombre_archivo());

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

  private ReconciliedMovement getMovimientoConciliado(Long idMovRef){
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedMovement reconciliedMovement = new ReconciliedMovement();
      reconciliedMovement.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedMovement.setIdMovRef(numberUtils.toLong(rs.getLong("id_mov_ref")));
      reconciliedMovement.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(rs.getString("estado"))));
      reconciliedMovement.setActionType(ReconciliationActionType.valueOf(String.valueOf(rs.getString("accion"))));
      reconciliedMovement.setFechaRegistro(rs.getTimestamp("fecha_registro"));
      return reconciliedMovement;
    };
    List<ReconciliedMovement> data =getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_conciliado where id_mov_ref = %s",getSchema(),idMovRef),rowMapper);
    return data.get(0);
  }
  private ReconciliedResearch getMovimientoInvestigarMotor(Long idMovRef){
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedResearch reconciliedResearch = new ReconciliedResearch();
      reconciliedResearch.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedResearch.setIdRef(String.valueOf(rs.getString("mov_ref")));
      reconciliedResearch.setNombre_archivo(String.valueOf(rs.getString("nombre_archivo")));
      reconciliedResearch.setOrigen(String.valueOf(rs.getString("origen")));
      return reconciliedResearch;
    };
    List<ReconciliedResearch> data =getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where mov_ref LIKE 'idMov=%s'",getSchema(),idMovRef),rowMapper);
    return data.get(0);
  }
}

class ReconciliedResearch {
  private Long id;
  private String idRef;
  private String nombre_archivo;
  private String origen;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdRef() {
    return idRef;
  }

  public void setIdRef(String idRef) {
    this.idRef = idRef;
  }

  public String getNombre_archivo() {
    return nombre_archivo;
  }

  public void setNombre_archivo(String nombre_archivo) {
    this.nombre_archivo = nombre_archivo;
  }

  public String getOrigen() {
    return origen;
  }

  public void setOrigen(String origen) {
    this.origen = origen;
  }

}
