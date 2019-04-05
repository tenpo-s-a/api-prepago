package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class Test_20190401112103_create_sp_mc_prp_crear_usuario_v11 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_crear_usuario_v11";

  @Before
  public void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  public static Object[] buildUserWithCustomStatus(String status){

    Object[] params = {
      numberUtils.random(1,11), //rut
      status, //estado
      getRandomString(10), //nombre
      getRandomString(10), //apellido
      getRandomString(10), //numero_documento
      getRandomString(10), //nivel
      getRandomString(10), //uuid
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return params;
  }

  /**
   *
   * @return
   */
  public static Object[] buildUser() {

    Object[] params = {
      numberUtils.random(1,11), //rut
      getRandomString(10), //estado
      getRandomString(10), //nombre
      getRandomString(10), //apellido
      getRandomString(10), //numero_documento
      getRandomString(10), //nivel
      getRandomString(10), //uuid
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return params;
  }

  /**
   *
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertUser(Object[] params) throws SQLException {

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(resp.get("_r_id")));
    map.put("rut", numberUtils.toInt(params[0]));
    map.put("estado", String.valueOf(params[1]));
    map.put("nombre", String.valueOf(params[2]));
    map.put("apellido", String.valueOf(params[3]));
    map.put("numero_documento", String.valueOf(params[4]));
    map.put("nivel", String.valueOf(params[5]));
    map.put("uuid", String.valueOf(params[6]));
    map.put("_r_id", resp.get("_r_id"));
    map.put("_error_code", resp.get("_error_code"));
    map.put("_error_msg", resp.get("_error_msg"));

    return map;
  }

  @Test
  public void insertUserOk() throws SQLException {

    

    /**
     * Caso de prueba para registrar un usuario nuevo, debe ser exitoso
     */
    Map<String, Object> resp = insertUser(buildUser());

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

  }

  @Test
  public void insertUserNotOk() throws SQLException {

    

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registrar exactamente el mismo usuario
     */

    Object[] params = buildUser();

    Map<String, Object> resp = insertUser(params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se intenta registrar exactamente el mismo usuario y debe fallar
     */
    resp = insertUser(params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser 0", 0, resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", Long.valueOf(0),numberUtils.toLong(resp.get("_r_id")));
  }

  @Test
  public void insertUserNotOkByParamsNull() throws SQLException {

    

    /**
     * Caso de prueba donde se registra un usuario nuevo pero pasando parametros en null
     */

    {
      Object[] params = buildUser();

      params[0] = new NullParam(Types.INTEGER); //rut

      Map<String, Object> resp = insertUser(params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC001", resp.get("_error_code"));
    }

    {
      Object[] params = buildUser();

      params[1] = new NullParam(Types.VARCHAR); //estado

      Map<String, Object> resp = insertUser(params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC002", resp.get("_error_code"));
    }

    {
      Object[] params = buildUser();

      params[6] = new NullParam(Types.VARCHAR); //uiid

      Map<String, Object> resp = insertUser(params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC003", resp.get("_error_code"));
    }
  }


  @Test
  public void insertUserNotOkBy_rut_exists() throws SQLException {

    

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registar otro diferente pero con
     * el mismo rut
     */

    Integer rut = getUniqueRutNumber();

    Object[] params = buildUser();

    params[0] = rut;

    Map<String, Object> resp = insertUser(params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo rut, debe fallar
     */

    Object[] params2 = buildUser();

    params2[0] = rut;

    resp = insertUser(params2);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", Long.valueOf(0),numberUtils.toLong(resp.get("_r_id")));
  }


}
