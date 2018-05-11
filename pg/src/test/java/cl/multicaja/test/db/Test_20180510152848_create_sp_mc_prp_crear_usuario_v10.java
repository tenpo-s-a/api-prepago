package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * @autor vutreras
 */
public class Test_20180510152848_create_sp_mc_prp_crear_usuario_v10 extends TestDbBase {

  @Test
  public void insertUserOk() throws SQLException {

    /**
     * Caso de prueba para registrar un usuario nuevo, debe ser exitoso
     */

    Object[] params = {
      new Long(getUniqueInteger()),
      getUniqueRutNumber(),
      "ACTIVO",
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);
  }

  @Test
  public void insertUserNotOk() throws SQLException {

    /**
     * Caso de prueba donde se registra un usuario nuevo y luego se intenta registrar exactamente el mismo usuario
     */

    Object[] params = {
      new Long(getUniqueInteger()),
      getUniqueRutNumber(),
      "ACTIVO",
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    /**
     * se intenta registrar exactamente el mismo usuario y debe fallar
     */
    resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params);

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

    Object[] params = {
      idUsuarioMc,
      getUniqueRutNumber(),
      "ACTIVO",
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo id_usuario_multiaja, debe fallar
     */

    Object[] params2 = {
      idUsuarioMc,
      getUniqueRutNumber(),
      "ACTIVO",
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params2);

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

    Object[] params = {
      new Long(getUniqueInteger()),
      rut,
      "ACTIVO",
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    /**
     * se intenta registrar un nuevo usuario pero con el mismo rut, debe fallar
     */

    Object[] params2 = {
      new Long(getUniqueInteger()),
      rut,
      "ACTIVO",
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params2);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", 0,numberUtils.toLong(resp.get("_r_id"), 0));
  }
}
