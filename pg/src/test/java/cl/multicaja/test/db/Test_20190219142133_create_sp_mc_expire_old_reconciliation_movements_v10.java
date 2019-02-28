package cl.multicaja.test.db;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;

public class Test_20190219142133_create_sp_mc_expire_old_reconciliation_movements_v10 extends TestDbBasePg {

  @Before
  @After
  public void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", SCHEMA));
  }

  @Test
  public void expireOldReconciledMovements() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> okMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherTipomovMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherIndnorcorMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> noPendingMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> purchasesAndSuscriptionsMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      // Insertar los archivos
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_1", i), "SWITCH", "SWITCH_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_2", i), "SWITCH", "SWITCH_REJECTED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_3", i), "SWITCH", "SWITCH_REVERSED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_4", i), "SWITCH", "SWITCH_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_5", i), "SWITCH", "SWITCH_REJECTED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_6", i), "SWITCH", "SWITCH_REVERSED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_7", i), "TECNOCOM", "TECNOCOM_FILE", "READING");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      okMovements.add(0, newMovement);

      // Insertar el movimiento con otro tipofac
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "WITHDRAW", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherTipomovMovements.add(0, newMovement);

      // Insertar el movimiento con otro indnorcor
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 1, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherIndnorcorMovements.add(0, newMovement);

      // Movimiento switch no pending
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "RECONCILED", "RECONCILED");
      noPendingMovements.add(0, newMovement);

      // Movimiento switch purchases and suscriptions
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), (i % 2 == 0) ? "SUSCRIPTION" : "PURCHASE", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      purchasesAndSuscriptionsMovements.add(0, newMovement);

      Thread.sleep(10);
    }

    Object[] params = {
      new InParam("estado_con_switch", Types.VARCHAR),
      new InParam("SWITCH_TOPUP", Types.VARCHAR),
      new InParam("TOPUP", Types.VARCHAR),
      new InParam(0, Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", params);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED tecnocom", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }


    // Expirar lo estado tecnocom
    Object[] paramsTc = {
      new InParam("estado_con_tecnocom", Types.VARCHAR),
      new InParam("TECNOCOM_FILE", Types.VARCHAR),
      new InParam("TOPUP", Types.VARCHAR),
      new InParam(0, Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", paramsTc);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }
  }

  @Test
  public void expireOldReconciledMovements_movementTypeNull() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> okMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherTipomovMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherIndnorcorMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> noPendingMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> purchasesAndSuscriptionsMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      // Insertar los archivos
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_1", i), "SWITCH", "SWITCH_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_2", i), "SWITCH", "SWITCH_REJECTED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_3", i), "SWITCH", "SWITCH_REVERSED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_4", i), "SWITCH", "SWITCH_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_5", i), "SWITCH", "SWITCH_REJECTED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_6", i), "SWITCH", "SWITCH_REVERSED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_7", i), "TECNOCOM", "TECNOCOM_FILE", "READING");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      okMovements.add(0, newMovement);

      // Insertar el movimiento con otro tipofac
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "WITHDRAW", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherTipomovMovements.add(0, newMovement);

      // Insertar el movimiento con otro indnorcor
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 1, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherIndnorcorMovements.add(0, newMovement);

      // Movimiento switch no pending
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "RECONCILED", "RECONCILED");
      noPendingMovements.add(0, newMovement);

      // Movimiento switch purchases and suscriptions
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), (i % 2 == 0) ? "SUSCRIPTION" : "PURCHASE", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      purchasesAndSuscriptionsMovements.add(0, newMovement);

      Thread.sleep(10);
    }

    Object[] params = {
      new InParam("estado_con_switch", Types.VARCHAR),
      new InParam("SWITCH_TOPUP", Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new InParam(0, Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", params);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED tecnocom", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }


    // Expirar lo estado tecnocom
    Object[] paramsTc = {
      new InParam("estado_con_tecnocom", Types.VARCHAR),
      new InParam("TECNOCOM_FILE", Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new InParam(0, Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", paramsTc);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }
  }

  @Test
  public void expireOldReconciledMovements_indnorcorNull() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> okMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherTipomovMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherIndnorcorMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> noPendingMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> purchasesAndSuscriptionsMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      // Insertar los archivos
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_1", i), "SWITCH", "SWITCH_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_2", i), "SWITCH", "SWITCH_REJECTED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_3", i), "SWITCH", "SWITCH_REVERSED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_4", i), "SWITCH", "SWITCH_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_5", i), "SWITCH", "SWITCH_REJECTED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_6", i), "SWITCH", "SWITCH_REVERSED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_7", i), "TECNOCOM", "TECNOCOM_FILE", "READING");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      okMovements.add(0, newMovement);

      // Insertar el movimiento con otro tipofac
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "WITHDRAW", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherTipomovMovements.add(0, newMovement);

      // Insertar el movimiento con otro indnorcor
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 1, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherIndnorcorMovements.add(0, newMovement);

      // Movimiento switch no pending
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "RECONCILED", "RECONCILED");
      noPendingMovements.add(0, newMovement);

      // Movimiento switch purchases and suscriptions
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), (i % 2 == 0) ? "SUSCRIPTION" : "PURCHASE", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      purchasesAndSuscriptionsMovements.add(0, newMovement);

      Thread.sleep(10);
    }

    Object[] params = {
      new InParam("estado_con_switch", Types.VARCHAR),
      new InParam("SWITCH_TOPUP", Types.VARCHAR),
      new InParam("TOPUP", Types.VARCHAR),
      new NullParam(Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", params);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED tecnocom", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }


    // Expirar lo estado tecnocom
    Object[] paramsTc = {
      new InParam("estado_con_tecnocom", Types.VARCHAR),
      new InParam("TECNOCOM_FILE", Types.VARCHAR),
      new InParam("TOPUP", Types.VARCHAR),
      new NullParam(Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", paramsTc);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }
  }

  @Test
  public void expireOldReconciledMovements_indnorcorAndMovementTypeNull() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> okMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherTipomovMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> otherIndnorcorMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> noPendingMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> purchasesAndSuscriptionsMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      // Insertar los archivos
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_1", i), "SWITCH", "SWITCH_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_2", i), "SWITCH", "SWITCH_REJECTED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_3", i), "SWITCH", "SWITCH_REVERSED_TOPUP", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_4", i), "SWITCH", "SWITCH_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_5", i), "SWITCH", "SWITCH_REJECTED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_6", i), "SWITCH", "SWITCH_REVERSED_WITHDRAW", "READING");
      Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(String.format("FileName%d_7", i), "TECNOCOM", "TECNOCOM_FILE", "READING");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      okMovements.add(0, newMovement);

      // Insertar el movimiento con otro tipofac
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "WITHDRAW", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherTipomovMovements.add(0, newMovement);

      // Insertar el movimiento con otro indnorcor
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 1, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      otherIndnorcorMovements.add(0, newMovement);

      // Movimiento switch no pending
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "TOPUP", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "RECONCILED", "RECONCILED");
      noPendingMovements.add(0, newMovement);

      // Movimiento switch purchases and suscriptions
      newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), (i % 2 == 0) ? "SUSCRIPTION" : "PURCHASE", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001, new Date(System.currentTimeMillis()), "", "PENDING", "PENDING");
      purchasesAndSuscriptionsMovements.add(0, newMovement);

      Thread.sleep(10);
    }

    Object[] params = {
      new InParam("estado_con_switch", Types.VARCHAR),
      new InParam("SWITCH_TOPUP", Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", params);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_switch").toString());
      }
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED tecnocom", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_switch").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING tecnocom", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }


    // Expirar lo estado tecnocom
    Object[] paramsTc = {
      new InParam("estado_con_tecnocom", Types.VARCHAR),
      new InParam("TECNOCOM_FILE", Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", paramsTc);
    System.out.println("Resp: " + resp);

    for(int i = 0; i < okMovements.size(); i++) {
      Map<String, Object> movement = okMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < otherTipomovMovements.size(); i++) {
      Map<String, Object> movement = otherTipomovMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < otherIndnorcorMovements.size(); i++) {
      Map<String, Object> movement = otherIndnorcorMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 1) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
      } else {
        Assert.assertEquals("Del 2 en adelante deben tener el estado NOT_RECONCILED", "NOT_RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
      }
    }

    for(int i = 0; i < noPendingMovements.size(); i++) {
      Map<String, Object> movement = noPendingMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar RECONCILED", "RECONCILED", storedMovement.get("estado_con_tecnocom").toString());
    }

    for(int i = 0; i < purchasesAndSuscriptionsMovements.size(); i++) {
      Map<String, Object> movement = purchasesAndSuscriptionsMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado sin cambiar PENDING", "PENDING", storedMovement.get("estado_con_tecnocom").toString());
    }
  }

  @Test
  public void expire_columnNameNull() throws Exception {
    Object[] paramsTc = {
      new NullParam(Types.VARCHAR),
      new InParam("TECNOCOM_FILE", Types.VARCHAR),
      new InParam("TOPUP", Types.VARCHAR),
      new InParam(0, Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", paramsTc);
    System.out.println("result: " + resp);

    Assert.assertEquals("Debe tener codigo de error MC001", "MC001", resp.get("_r_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", resp.get("_r_error_msg").toString());
  }

  @Test
  public void expire_fileTypeNull() throws Exception {
    Object[] paramsTc = {
      new InParam("estado_con_tecnocom", Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new InParam("TOPUP", Types.VARCHAR),
      new InParam(0, Types.NUMERIC),
      new OutParam("_r_error_code", Types.VARCHAR),
      new OutParam("_r_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_expire_old_reconciliation_movements_v10", paramsTc);
    System.out.println("result: " + resp);

    Assert.assertEquals("Debe tener codigo de error MC002", "MC002", resp.get("_r_error_code").toString());
    Assert.assertNotEquals("Debe tener mensaje de error", "", resp.get("_r_error_msg").toString());
  }

  private Map<String, Object> getMovement(Long id) {
    return dbUtils.getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     estado, " +
        "     estado_con_switch, " +
        "     estado_con_tecnocom " +
        " FROM " +
        "   "+ SCHEMA +".prp_movimiento"+
        " WHERE " +
        " id = " + id
    ).get(0);
  }
}
