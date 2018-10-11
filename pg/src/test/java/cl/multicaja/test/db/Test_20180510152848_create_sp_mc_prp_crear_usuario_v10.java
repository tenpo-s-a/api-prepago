package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor vutreras
 */
public class Test_20180510152848_create_sp_mc_prp_crear_usuario_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_crear_usuario_v10";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_usuario", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_usuario", SCHEMA));
  }

  /**
   * Crea los datos de un usuario de forma aleatoria
   * @param status
   * @return
   */
  public static Object[] buildUser(String status) {
    Object[] params = {
      new Long(getUniqueInteger()), //id_usuario_mc
      getUniqueRutNumber(), //rut
      status, //estado
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }

  /**
   * inserta un nuevo usuario
   * @param status
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertUser(String status) throws SQLException {

    Object[] params = buildUser(status);

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(resp.get("_r_id")));
    map.put("id_usuario_mc", numberUtils.toLong(params[0]));
    map.put("rut", numberUtils.toInt(params[1]));
    map.put("estado", String.valueOf(params[2]));

    return map;
  }

  @Test
  public void insertUser() throws SQLException {

    /**
     * Caso de prueba para registrar un usuario nuevo, debe ser exitoso
     */

    Object[] params = buildUser("ACTIVO");

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);
  }

  @Test
  public void insertUserNotOk() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registrar exactamente el mismo usuario
     */

    Object[] params = buildUser("ACTIVO");

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se intenta registrar exactamente el mismo usuario y debe fallar
     */
    resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", Long.valueOf(0),numberUtils.toLong(resp.get("_r_id")));
  }

  @Test
  public void insertUserNotOkByParamsNull() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo pero pasando parametros en null
     */

    {
      Object[] params = buildUser("ACTIVO");

      params[0] = new NullParam(Types.BIGINT); //id_usuario_mc

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC001", resp.get("_error_code"));
    }

    {
      Object[] params = buildUser("ACTIVO");

      params[1] = new NullParam(Types.INTEGER); //rut

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC002", resp.get("_error_code"));
    }

    {
      Object[] params = buildUser("ACTIVO");

      params[2] = new NullParam(Types.VARCHAR); //estado

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC003", resp.get("_error_code"));
    }
  }

  @Test
  public void insertUserNotOkBy_id_usuario_mc_exists() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registar otro diferente pero con
     * el mismo id_usuario_multicaja
     */

    Long idUsuarioMc = new Long(getUniqueInteger());

    Object[] params = buildUser("ACTIVO");

    params[0] = idUsuarioMc;

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo id_usuario_multiaja, debe fallar
     */

    Object[] params2 = buildUser("ACTIVO");

    params2[0] = idUsuarioMc;

    resp = dbUtils.execute(SP_NAME, params2);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", Long.valueOf(0),numberUtils.toLong(resp.get("_r_id")));
  }

  @Test
  public void insertUserNotOkBy_rut_exists() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registar otro diferente pero con
     * el mismo rut
     */

    Integer rut = getUniqueRutNumber();

    Object[] params = buildUser("ACTIVO");

    params[1] = rut;

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo rut, debe fallar
     */

    Object[] params2 = buildUser("ACTIVO");

    params2[1] = rut;

    resp = dbUtils.execute(SP_NAME, params2);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", Long.valueOf(0),numberUtils.toLong(resp.get("_r_id")));
  }
}
