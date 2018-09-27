package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.util.PGTimestamp;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement;


public class Test_20180925154245_create_sp_mc_prp_actualiza_no_conciliados_switch_v10 extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_no_conciliados_switch_v10";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @Test
  public void expireSwitchStatus() throws SQLException
  {
    Map<String, Object> mapMovimiento = insertRandomMovement();
    changeMovementDate(mapMovimiento.get("_id"), "2018-09-25 15:00:32");

    mapMovimiento = insertRandomMovement();
    changeMovementDate(mapMovimiento.get("_id"), "2018-08-03 21:14:09");

    mapMovimiento = insertRandomMovement();
    changeMovementDate(mapMovimiento.get("_id"), "2018-08-03 17:43:54");

    mapMovimiento = insertRandomMovement();
    changeMovementDate(mapMovimiento.get("_id"), "2018-08-01 11:52:10");

    mapMovimiento = insertRandomMovement();
    changeMovementDate(mapMovimiento.get("_id"), "2018-09-14 19:21:06");

    mapMovimiento = insertRandomMovement();
    changeMovementDate(mapMovimiento.get("_id"), "2018-09-04 09:05:31");

    Timestamp firstDate = Timestamp.valueOf("2018-08-01 14:00:00");
    Timestamp lastDate = Timestamp.valueOf("2018-09-04 23:59:59");

    PGTimestamp firstDatePG = new PGTimestamp(firstDate.getTime());
    PGTimestamp lastDatePG = new PGTimestamp(lastDate.getTime());

    Object[] params = {
      "2018-08-01 14:00:00",
      "2018-09-04 23:59:59"
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    List result = (List)resp.get("result");
    System.out.println(result);

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener 3 elementos", 3, result.size());

    for (Object object: result) {
      Map<String, Object> movement = (Map<String, Object>) object;
      Timestamp thisTimestamp = (Timestamp) movement.get("_fecha_creacion");
      boolean included = !(thisTimestamp.before(firstDate)) && !(thisTimestamp.after(lastDate));
      Assert.assertTrue("Debe ser entre 2018-08-01 14:00:00 y 2018-09-04 23:59:59", included);
    }
  }

  private void changeMovementDate(Object idMovimiento, String newDate)  {
    //dbUtils.getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento WHERE ID = %s", SCHEMA, idMovimiento.toString()));
    dbUtils.getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET fecha_creacion = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')"
        + " WHERE ID = " + idMovimiento.toString());
      /*
      "INSERT INTO " + SCHEMA + ".prp_movimiento (fecha_creacion) VALUES ("
      + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS'))"
      + " WHERE ID = " + idMovimiento.toString());
      */
  }
}
