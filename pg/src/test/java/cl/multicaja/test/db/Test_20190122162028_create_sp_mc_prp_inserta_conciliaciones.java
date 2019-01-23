package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

public class Test_20190122162028_create_sp_mc_prp_inserta_conciliaciones extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_inserta_conciliaciones_v10";

  /*
    IN _id_movimiento     BIGINT,
    IN _tipo              VARCHAR,
    IN _status            VARCHAR,
    OUT _r_id             BIGINT,
    OUT _error_code       VARCHAR,
    OUT _error_msg        VARCHAR
   */

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_conciliaciones cascade", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
  }

  public static Map<String, Object> creaConciliaciones(Long idMovimiento, String tipo, String status) throws SQLException {
    Object[] params = {
      idMovimiento != null ? idMovimiento : new NullParam(Types.BIGINT),
      tipo != null ? tipo : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }


  @Test
  public void insertConciliacionesOK() throws Exception {

    Map<String, Object> data = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Long idMovimiento = numberUtils.toLong(data.get("_id"));
    System.out.println("idMovimiento: "+idMovimiento);
    Map<String, Object> dataConciliaciones = creaConciliaciones(idMovimiento,"TECNOCOM","OK");
    System.out.println(String.format("Num Err: %s Msj: %s",dataConciliaciones.get("_error_code"),dataConciliaciones.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","0", dataConciliaciones.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","", dataConciliaciones.get("_error_msg"));
  }

  @Test
  public void insertConciliaciones_fail1() throws SQLException {
    Map<String, Object> data = creaConciliaciones(null,"TECNOCOM","OK");
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC001", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _id_movimiento es obligatorio", data.get("_error_msg"));
  }

  @Test
  public void insertConciliaciones_fail2() throws SQLException {
    Map<String, Object> data = creaConciliaciones(1L,null,"OK");
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC002", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _tipo es obligatorio", data.get("_error_msg"));
  }

  @Test
  public void insertConciliaciones_fail3() throws SQLException {
    Map<String, Object> data = creaConciliaciones(1L,"TECNOCOM",null);
    System.out.println(String.format("Num Err: %s Msj: %s",data.get("_error_code"),data.get("_error_msg")));
    Assert.assertEquals("Codigo de error tiene que ser","MC003", data.get("_error_code"));
    Assert.assertEquals("Codigo de error tiene que ser","El _status es obligatorio", data.get("_error_msg"));
  }

}
