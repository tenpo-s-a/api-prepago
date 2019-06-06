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

import static cl.multicaja.test.db.Test_20180925154245_create_sp_mc_prp_actualiza_no_conciliados_switch_v10.searchAllMovements;

//TODO: Revisar despues
@Ignore
public class Test_20180927170320_create_sp_mc_prp_actualiza_no_conciliados_tecnocom_v10 extends TestDbBasePg {
  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_no_conciliados_tecnocom_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }
  /*
  @Test
  public void expireTecnocomStatus() throws SQLException
  {
    Test_20180925154245_create_sp_mc_prp_actualiza_no_conciliados_switch_v10.fillDb();

    String startDate = "20180803";
    String endDate = "20180803";
    Integer tipofac = 3001;
    Integer indnorcor = 1;

    Object[] params = {
      startDate,
      endDate,
      tipofac,
      indnorcor,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    System.out.println("Msg error: " + resp.get("_error_msg"));

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser  0", "0", resp.get("_error_code"));

    Timestamp startDateTs = Timestamp.valueOf("2018-08-03 00:00:00");
    Timestamp endDateTs = Timestamp.valueOf("2018-08-03 23:59:59");

    List lstMov = searchAllMovements();

    int notReconciliateCount = 0;
    for (Object object: lstMov) {
      Map<String, Object> movement = (Map<String, Object>) object;
      Timestamp movementCreationDate = (Timestamp) movement.get("fecha_creacion");
      Integer movementTipoFac = ((BigDecimal)movement.get("tipofac")).intValue();
      Integer movementIndNorCor = ((BigDecimal)movement.get("indnorcor")).intValue();

      String tecnocomStatus = (String) movement.get("estado_con_tecnocom");
      if (tecnocomStatus.equals("NO_CONCILIADO")) {
        boolean includedBetweenDates = !movementCreationDate.before(startDateTs) && !movementCreationDate.after(endDateTs);
        ////Assert.assertTrue("Debe estar adentro de las fechas [2018/08/03-2018/08/04[", includedBetweenDates);
        Assert.assertEquals("Debe ser tipo fac " + tipofac, tipofac, movementTipoFac);
        Assert.assertEquals("Debe tener indnorcor " + indnorcor, indnorcor, movementIndNorCor);
        notReconciliateCount++;
      }
      else {
        boolean excludedFromDates = movementCreationDate.before(startDateTs) || movementCreationDate.after(endDateTs);
        boolean wrongTipoFac = !tipofac.equals(movementTipoFac);
        boolean wrongIndNorCor = !indnorcor.equals(movementTipoFac);
        Assert.assertTrue("Debe estar fuera de fecha, distinto tipofac o distinto indnorcor.", excludedFromDates || wrongTipoFac || wrongIndNorCor);
      }
    }

    Assert.assertEquals("Debe haber 5 movimientos no conciliados", 6, notReconciliateCount);
  }

  @Test
  public void updateTecnocomStatusNotOkByStartDateNull()throws SQLException {
    String endDate = "20180803";

    Object[] params = {
      new NullParam(Types.VARCHAR),
      endDate,
      3001,
      1,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateTecnocomStatusNotOkByEndDateNull()throws SQLException {
    String startDate = "20180803";

    Object[] params = {
      startDate,
      new NullParam(Types.VARCHAR),
      3001,
      1,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateTecnocomStatusNotOkByTipoFacNull()throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      new NullParam(Types.NUMERIC),
      1,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateTecnocomStatusNotOkByIndNorCorNull()throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      3001,
      new NullParam(Types.NUMERIC),
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateTecnocomStatusNotOkByIndNorCorOutOfRange()throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      3001,
      2,
      "NO_CONCILIADO",
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }

  @Test
  public void updateTecnocomStatusNotOkByNewStateNull()throws SQLException {
    String startDate = "20180803";
    String endDate = "20180803";

    Object[] params = {
      startDate,
      endDate,
      3001,
      1,
      new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  } */
}
