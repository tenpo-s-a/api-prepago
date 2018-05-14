package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.OutParam;
import org.apache.commons.lang3.RandomStringUtils;
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
public class Test_20180510152848_create_sp_mc_prp_crear_usuario_v10 extends TestDbBase {

  protected static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_usuario", SCHEMA));
  }

  /**
   *
   * @param status
   * @return
   */
  protected Object[] buildUserData(String status) {
    Object[] params = {
      new Long(getUniqueInteger()), //id_usuario_mc
      getUniqueRutNumber(), //rut
      status, //estado
      RandomStringUtils.randomAlphabetic(20), //_contrato
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }

  /**
   *
   * @param status
   * @return
   * @throws SQLException
   */
  protected Map<String, Object> insertUserOk(String status) throws SQLException {

    Object[] params = buildUserData(status);

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    Map<String, Object> map = new HashMap<>();
    map.put("id", resp.get("_r_id"));
    map.put("id_usuario_mc", params[0]);
    map.put("rut", params[1]);
    map.put("estado", params[2]);
    map.put("contrato", params[3]);
    return map;
  }

  @Test
  public void insertUserOk() throws SQLException {

    /**
     * Caso de prueba para registrar un usuario nuevo, debe ser exitoso
     */

    Object[] params = buildUserData("ACTIVO");

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);
  }

  @Test
  public void insertUserNotOk() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registrar exactamente el mismo usuario
     */

    Object[] params = buildUserData("ACTIVO");

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    /**
     * se intenta registrar exactamente el mismo usuario y debe fallar
     */
    resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", 0,numberUtils.toLong(resp.get("_r_id"), 0));
  }

  @Test
  public void insertUserNotOkBy_id_usuario_mc_exists() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registar otro diferente pero con
     * el mismo id_usuario_multicaja
     */

    Long idUsuarioMc = new Long(getUniqueInteger());

    Object[] params = buildUserData("ACTIVO");

    params[0] = idUsuarioMc;

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo id_usuario_multiaja, debe fallar
     */

    Object[] params2 = buildUserData("ACTIVO");

    params2[0] = idUsuarioMc;

    resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params2);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", 0,numberUtils.toLong(resp.get("_r_id"), 0));
  }

  @Test
  public void insertUserNotOkBy_rut_exists() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registar otro diferente pero con
     * el mismo rut
     */

    Integer rut = getUniqueRutNumber();

    Object[] params = buildUserData("ACTIVO");

    params[1] = rut;

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo rut, debe fallar
     */

    Object[] params2 = buildUserData("ACTIVO");

    params2[1] = rut;

    resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_usuario_v10", params2);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", 0,numberUtils.toLong(resp.get("_r_id"), 0));
  }
}
