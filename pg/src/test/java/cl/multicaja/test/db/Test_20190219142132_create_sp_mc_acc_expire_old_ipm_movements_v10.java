package cl.multicaja.test.db;

import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_20190219142132_create_sp_mc_acc_expire_old_ipm_movements_v10 extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA_ACCOUNTING + ".create_sp_mc_acc_expire_old_ipm_movements_v10";

  @Before
  @After
  public void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.ipm_file cascade", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", SCHEMA));
  }

  @Test
  public void expireOldSuscriptionMovements() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> allMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> allAccountings = new ArrayList<>();
    ArrayList<Map<String, Object>> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar un archivo nuevo
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "PROCESSED");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "SUSCRIPTION", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);

      // Insertar un movimiento de accounting en estado pending
      Map<String, Object> newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting(numberUtils.toLong(newMovement.get("_id")));
      if (i % 2 == 0) {
        newAccounting.put("status", "PENDING");
      } else {
        newAccounting.put("status", "SENT_PENDING_CON");
      }
      newAccounting.put("accounting_status", "OK");
      newAccounting.put("conciliation_date", "4000-06-20 00:00:00");
      newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(newAccounting);

      // Insertar un movimiento de clearing en estado PENDING
      Map<String, Object> newClearing = Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData(numberUtils.toLong(newAccounting.get("id")), 0L, 0L, "PENDING");

      allMovements.add(0, newMovement);
      allAccountings.add(0, newAccounting);
      allClearings.add(0, newClearing);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      Map<String, Object> accounting = allAccountings.get(i);
      Map<String, Object> storedAccounting = getAccounting(numberUtils.toLong(accounting.get("id")));

      Map<String, Object> clearing = allClearings.get(i);
      Map<String, Object> storedClearing = getClearing(numberUtils.toLong(clearing.get("_r_id")));


      if(i <= 6) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
        Assert.assertEquals("Los primeros no cambian estado", accounting.get("status"), storedAccounting.get("status").toString());
        Assert.assertEquals("Los primeros deben tener el estado OK", "OK", storedAccounting.get("accounting_status").toString());
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedClearing.get("status").toString());
        Timestamp conciliationDate = (Timestamp) storedAccounting.get("conciliation_date");
        Assert.assertEquals("Debe tener año de conciliacion 4000", 4000, conciliationDate.toLocalDateTime().getYear());
      } else {
        Assert.assertEquals("Del 7 en adelante deben tener el estado EXPIRED", "EXPIRED", storedMovement.get("estado").toString());
        if (accounting.get("status").toString().equals("PENDING")) {
          Assert.assertEquals("Del 7 en adelante deben tener el estado NOT_SEND", "NOT_SEND", storedAccounting.get("status").toString());
        } else {
          Assert.assertEquals("Del 7 en adelante deben tener el estado SENT_PENDING_CON", "SENT_PENDING_CON", storedAccounting.get("status").toString());
        }
        Assert.assertEquals("Del 7 en adelante deben tener el estado NOT_OK", "NOT_OK", storedAccounting.get("accounting_status").toString());
        Assert.assertEquals("Del 7 en adelante deben tener el estado NO_CONFIRMADA", "NO_CONFIRMADA", storedClearing.get("status").toString());
        Timestamp conciliationDate = (Timestamp) storedAccounting.get("conciliation_date");
        LocalDateTime today = LocalDateTime.now(ZoneId.of("UTC"));
        Assert.assertEquals("Debe tener fecha de conciliacion actual", today.getDayOfYear(), conciliationDate.toLocalDateTime().getDayOfYear());
      }
    }
  }

  @Test
  public void expireOldPurchaseMovements() throws SQLException, InterruptedException {
    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> allMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> allAccountings = new ArrayList<>();
    ArrayList<Map<String, Object>> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar un archivo nuevo
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "PROCESSED");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "PURCHASE", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);

      // Insertar un movimiento de accounting en estado pending
      Map<String, Object> newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting(numberUtils.toLong(newMovement.get("_id")));
      if (i % 2 == 0) {
        newAccounting.put("status", "SENT_PENDING_CON");
      } else {
        newAccounting.put("status", "PENDING");
      }
      newAccounting.put("accounting_status", "OK");
      newAccounting.put("conciliation_date", "4000-06-20 00:00:00");
      newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(newAccounting);

      // Insertar un movimiento de clearing en estado PENDING
      Map<String, Object> newClearing = Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData(numberUtils.toLong(newAccounting.get("id")), 0L, 0L, "PENDING");

      allMovements.add(0, newMovement);
      allAccountings.add(0, newAccounting);
      allClearings.add(0, newClearing);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      Map<String, Object> accounting = allAccountings.get(i);
      Map<String, Object> storedAccounting = getAccounting(numberUtils.toLong(accounting.get("id")));

      Map<String, Object> clearing = allClearings.get(i);
      Map<String, Object> storedClearing = getClearing(numberUtils.toLong(clearing.get("_r_id")));


      if(i <= 6) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
        Assert.assertEquals("Los primeros no cambian estado", accounting.get("status"), storedAccounting.get("status").toString());
        Assert.assertEquals("Los primeros deben tener el estado OK", "OK", storedAccounting.get("accounting_status").toString());
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedClearing.get("status").toString());
        Timestamp conciliationDate = (Timestamp) storedAccounting.get("conciliation_date");
        Assert.assertEquals("Debe tener año de conciliacion 4000", 4000, conciliationDate.toLocalDateTime().getYear());
      } else {
        Assert.assertEquals("Del 7 en adelante deben tener el estado EXPIRED", "EXPIRED", storedMovement.get("estado").toString());
        if (accounting.get("status").toString().equals("PENDING")) {
          Assert.assertEquals("Del 7 en adelante deben tener el estado NOT_SEND", "NOT_SEND", storedAccounting.get("status").toString());
        } else {
          Assert.assertEquals("Del 7 en adelante deben tener el estado SENT_PENDING_CON", "SENT_PENDING_CON", storedAccounting.get("status").toString());
        }
        Assert.assertEquals("Del 7 en adelante deben tener el estado NOT_OK", "NOT_OK", storedAccounting.get("accounting_status").toString());
        Assert.assertEquals("Del 7 en adelante deben tener el estado NO_CONFIRMADA", "NO_CONFIRMADA", storedClearing.get("status").toString());
        Timestamp conciliationDate = (Timestamp) storedAccounting.get("conciliation_date");
        LocalDateTime today = LocalDateTime.now(ZoneId.of("UTC"));
        Assert.assertEquals("Debe tener fecha de conciliacion actual", today.getDayOfYear(), conciliationDate.toLocalDateTime().getDayOfYear());
      }
    }
  }

  @Test
  public void doNOTexpireOldAnyMovements() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> allMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> allAccountings = new ArrayList<>();
    ArrayList<Map<String, Object>> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar un archivo nuevo
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "PROCESSED");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), getRandomString(10), "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);

      // Insertar un movimiento de accounting en estado pending
      Map<String, Object> newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting(numberUtils.toLong(newMovement.get("_id")));
      if (i % 2 == 0) {
        newAccounting.put("status", "SENT_PENDING_CON");
      } else {
        newAccounting.put("status", "PENDING");
      }
      newAccounting.put("accounting_status", "OK");
      newAccounting.put("conciliation_date", "4000-06-20 00:00:00");
      newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(newAccounting);

      // Insertar un movimiento de clearing en estado PENDING
      Map<String, Object> newClearing = Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData(numberUtils.toLong(newAccounting.get("id")), 0L, 0L, "PENDING");

      allMovements.add(0, newMovement);
      allAccountings.add(0, newAccounting);
      allClearings.add(0, newClearing);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      Map<String, Object> accounting = allAccountings.get(i);
      Map<String, Object> storedAccounting = getAccounting(numberUtils.toLong(accounting.get("id")));

      Map<String, Object> clearing = allClearings.get(i);
      Map<String, Object> storedClearing = getClearing(numberUtils.toLong(clearing.get("_r_id")));

      Assert.assertEquals("Todos deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
      Assert.assertEquals("Todos no cambian estado", accounting.get("status"), storedAccounting.get("status").toString());
      Assert.assertEquals("Todos deben tener el estado OK", "OK", storedAccounting.get("accounting_status").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING", "PENDING", storedClearing.get("status").toString());
      Timestamp conciliationDate = (Timestamp) storedAccounting.get("conciliation_date");
      Assert.assertEquals("Todos deben tener año de conciliacion 4000", 4000, conciliationDate.toLocalDateTime().getYear());
    }
  }

  @Test
  public void doNotexpireSuscriptionMovements() throws SQLException, InterruptedException {

    // Preparar usuario
    Map<String, Object> mapCard = Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard("ACTIVA");
    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    ArrayList<Map<String, Object>> allMovements = new ArrayList<>();
    ArrayList<Map<String, Object>> allAccountings = new ArrayList<>();
    ArrayList<Map<String, Object>> allClearings = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar un archivo nuevo, pero en estado distinto de PROCESSED
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "OTHER_STATE");

      // Insertar el movimiento
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "SUSCRIPTION", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);

      // Insertar un movimiento de accounting en estado pending
      Map<String, Object> newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting(numberUtils.toLong(newMovement.get("_id")));
      if (i % 2 == 0) {
        newAccounting.put("status", "PENDING");
      } else {
        newAccounting.put("status", "SENT_PENDING_CON");
      }
      newAccounting.put("accounting_status", "OK");
      newAccounting.put("conciliation_date", "4000-06-20 00:00:00");
      newAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(newAccounting);

      // Insertar un movimiento de clearing en estado PENDING
      Map<String, Object> newClearing = Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData(numberUtils.toLong(newAccounting.get("id")), 0L, 0L, "PENDING");

      allMovements.add(0, newMovement);
      allAccountings.add(0, newAccounting);
      allClearings.add(0, newClearing);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    // Ninguno debe cambiar
    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      Map<String, Object> accounting = allAccountings.get(i);
      Map<String, Object> storedAccounting = getAccounting(numberUtils.toLong(accounting.get("id")));

      Map<String, Object> clearing = allClearings.get(i);
      Map<String, Object> storedClearing = getClearing(numberUtils.toLong(clearing.get("_r_id")));

      Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
      Assert.assertEquals("Los primeros no cambian estado", accounting.get("status"), storedAccounting.get("status").toString());
      Assert.assertEquals("Los primeros deben tener el estado OK", "OK", storedAccounting.get("accounting_status").toString());
      Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedClearing.get("status").toString());
      Timestamp conciliationDate = (Timestamp) storedAccounting.get("conciliation_date");
      Assert.assertEquals("Debe tener año de conciliacion 4000", 4000, conciliationDate.toLocalDateTime().getYear());
    }
  }

  private Map<String, Object> getMovement(Long id) {
    return dbUtils.getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     estado " +
        " FROM " +
        "   "+ SCHEMA +".prp_movimiento"+
        " WHERE " +
        " id = " + id
    ).get(0);
  }

  private Map<String, Object> getAccounting(Long id) {
    return dbUtils.getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     status, " +
        "     accounting_status, " +
        "     conciliation_date " +
        " FROM " +
        "   "+ SCHEMA_ACCOUNTING + ".accounting"+
        " WHERE " +
        " id = " + id
    ).get(0);
  }

  private Map<String, Object> getClearing(Long id) {
    return dbUtils.getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     status " +
        " FROM " +
        "   "+ SCHEMA_ACCOUNTING + ".clearing"+
        " WHERE " +
        " id = " + id
    ).get(0);
  }
}
