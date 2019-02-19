package cl.multicaja.test.db;

import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.sql.SQLException;
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
    for(int i = 0; i < 10; i++) {
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "Status");
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "SUSCRIPTION", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);
      allMovements.add(0, newMovement);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 6) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
      } else {
        System.out.println("idx: " + i);
        Assert.assertEquals("Del 7 en adelante deben tener el estado EXPIRED", "EXPIRED", storedMovement.get("estado").toString());
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
    for(int i = 0; i < 10; i++) {
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "Status");
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), "PURCHASE", "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);
      allMovements.add(0, newMovement);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());

      if(i <= 6) {
        Assert.assertEquals("Los primeros deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
      } else {
        Assert.assertEquals("Del 7 en adelante deben tener el estado EXPIRED", "EXPIRED", storedMovement.get("estado").toString());
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
    for(int i = 0; i < 10; i++) {
      Test_20181218135154_create_sp_mc_acc_create_ipm_file_v10.createIpmFile(String.format("FileName%d", i), "FileId", 10, "Status");
      Map<String, Object> newMovement = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement(idMovimientoRef, idUsuario, getUniqueLong().toString(), getRandomString(8), "PENDING", "API", getRandomNumericString(10), 152, 0, 3001);
      allMovements.add(0, newMovement);
      Thread.sleep(10);
    }

    dbUtils.execute(SP_NAME);

    for(int i = 0; i < 10; i++) {
      Map<String, Object> movement = allMovements.get(i);
      Map<String, Object> storedMovement = getMovement(numberUtils.toLong(movement.get("_id")));
      Assert.assertEquals("Debe tener el mismo id", movement.get("_id").toString(), storedMovement.get("id").toString());
      Assert.assertEquals("Todos deben tener el estado PENDING", "PENDING", storedMovement.get("estado").toString());
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
}
