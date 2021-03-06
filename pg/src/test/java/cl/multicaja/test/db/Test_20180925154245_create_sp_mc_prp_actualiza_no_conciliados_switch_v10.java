package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

//TODO: Revisar despues
@Ignore
public class Test_20180925154245_create_sp_mc_prp_actualiza_no_conciliados_switch_v10 extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_no_conciliados_switch_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  @Test
  public void expireSwitchStatus() throws SQLException
  {
    //fillDb();

    String startDate = "20180803000000";
    String endDate = "20180803235959";
    String tipoMovimiento = "CARGA";
    Integer indnorcor = 1;

    Object[] params = {
      startDate,
      endDate,
      tipoMovimiento,
      indnorcor,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    System.out.println("Mensaje error: " + resp.get("_error_msg"));
    Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

    Timestamp startDateTs = Timestamp.valueOf("2018-08-03 00:00:00");
    Timestamp endDateTs = Timestamp.valueOf("2018-08-03 23:59:59");

    List lstMov = searchAllMovements();
    int notReconciliateCount = 0;
    int i = 0;
    for (Object object: lstMov) {
      i++;
      Map<String, Object> movement = (Map<String, Object>) object;
      Timestamp movementCreationDate = (Timestamp) movement.get("fecha_creacion");
      System.out.println(i + "] " + movementCreationDate);
      String movementTipoMov = (String) movement.get("tipo_movimiento");
      Integer movementIndNorCor = ((BigDecimal)movement.get("indnorcor")).intValue();

      String switchStatus = (String) movement.get("estado_con_switch");
      if (switchStatus.equals("NO_CONCILIADO")) {
        boolean includedBetweenDates = !movementCreationDate.before(startDateTs) && !movementCreationDate.after(endDateTs);
        //Assert.assertTrue("Debe estar adentro de las fechas [2018/08/03 04:00:00 - 2018/08/04 03:59:59[", includedBetweenDates);
        Assert.assertEquals("Debe ser tipo mov " + tipoMovimiento, tipoMovimiento, movementTipoMov);
        Assert.assertEquals("Debe tener indnorcor " + indnorcor, indnorcor, movementIndNorCor);
        notReconciliateCount++;
        //System.out.println("In, before: " + movementCreationDate.before(startDateTs) + ", after: " + movementCreationDate.after(endDateTs));
      }
      else {
        boolean excludedFromDates = movementCreationDate.before(startDateTs) || movementCreationDate.after(endDateTs);
        boolean wrongTipoMov = !tipoMovimiento.equals(movementTipoMov);
        boolean wrongIndNorCor = !indnorcor.equals(movementIndNorCor);
        Assert.assertTrue("Debe estar fuera de fecha, distinto tipofac o distinto indnorcor.", excludedFromDates || wrongTipoMov || wrongIndNorCor);
      }
    }

    Assert.assertEquals("Debe haber 4 movimientos no conciliados", 5, notReconciliateCount);
  }

  @Test
  public void updateSwitchStatusNotOkByStartDateNull() throws SQLException {
    String endDate = "20180803";

    Object[] params = {
      new NullParam(Types.VARCHAR),
      endDate,
      "CARGA",
      1,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateSwitchStatusNotOkByEndDateNull() throws SQLException {
    String startDate = "20180803";

    Object[] params = {
      startDate,
      new NullParam(Types.VARCHAR),
      "CARGA",
      1,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateSwitchStatusNotOkByTipoFacNull() throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      new NullParam(Types.VARCHAR),
      1,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateSwitchStatusNotOkByIndNorCorNull() throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      "CARGA",
      new NullParam(Types.NUMERIC),
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateSwitchStatusNotOkByIndNorCorOutOfRange() throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      "CARGA",
      2,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateSwitchStatusNotOkByNewStateNull() throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      "CARGA",
      1,
      new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }
  /*
  public static void fillDb() {
    try {
      Map<String, Object> mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-03 21:14:09", "CARGA", 1); // Dentro

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-03 17:43:54", "CARGA", 1); // Dentro

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-03 00:00:00", "CARGA", 1); // Dentro, limite

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-03 23:59:59", "CARGA", 1); // Dentro, limite

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-02 23:59:59", "CARGA", 1); // Fuera, por fecha limite

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-04 00:00:00", "CARGA", 1); // Fuera, por fecha limite

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-03 11:52:10", "RETIRO", 1); // Fuera, por tipo movimiento

      mapMovimiento = insertRandomMovement();
      changeMovement(mapMovimiento.get("_id"), "2018-08-03 07:43:54", "CARGA", 0); // Fuera, por indnorcor
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  } */

  static public void changeMovement(Object idMovimiento, String newDate, String tipoMovimiento, Integer indnorcor)  {
    dbUtils.getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET fecha_creacion = "
        + "(TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone), "
        + "indnorcor = " + indnorcor + ", "
        + "tipo_movimiento = '" + tipoMovimiento + "' "
        + "WHERE ID = " + idMovimiento.toString());
  }

  static public List searchAllMovements()  {
    return dbUtils.getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento", SCHEMA));
  }
}
