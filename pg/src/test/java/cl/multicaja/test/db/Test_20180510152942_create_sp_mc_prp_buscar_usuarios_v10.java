package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @autor vutreras
 */
public class Test_20180510152942_create_sp_mc_prp_buscar_usuarios_v10  extends TestDbBase {

  public Map<String, Object> insertUserOk() throws SQLException {
    return insertUserOk("ACTIVO");
  }

  public Map<String, Object> insertUserOk(String estado) throws SQLException {

    /**
     * Caso de prueba para registrar un usuario nuevo, debe ser exitoso
     */

    Object[] params = {
      new Long(getUniqueInteger()),
      getUniqueRutNumber(),
      estado,
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_crear_usuario_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    Map<String, Object> map = new HashMap<>();

    map.put("id", resp.get("_r_id"));
    map.put("id_usuario_mc", params[0]);
    map.put("rut", params[1]);
    map.put("estado", params[2]);

    return map;
  }

  @Test
  public void searchUserByAllFields() throws SQLException {

    /**
     * se crean un usuario y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */
    Map<String, Object> u1 = insertUserOk();

    Object[] params = {
      numberUtils.toLong(u1.get("id"), 0),
      numberUtils.toLong(u1.get("id_usuario_mc"), 0),
      numberUtils.toInt(u1.get("rut"), 0),
      String.valueOf(u1.get("estado")),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);

    System.out.println("Mu:" + mUsu1.get("fecha_actualizacion").getClass());

    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id"), mUsu1.get("id"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id_usuario_mc"), mUsu1.get("id_usuario_mc"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("rut"), mUsu1.get("rut"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("estado"), mUsu1.get("estado"));
  }

  @Test
  public void searchUserBy_id() throws SQLException {

    /**
     * se crean un usuario y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */
    Map<String, Object> u1 = insertUserOk();

    Object[] params = {
      numberUtils.toLong(u1.get("id"), 0),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id"), mUsu1.get("id"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id_usuario_mc"), mUsu1.get("id_usuario_mc"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("rut"), mUsu1.get("rut"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("estado"), mUsu1.get("estado"));;
  }

  @Test
  public void searchUserBy_id_usuario_mc() throws SQLException {

    /**
     * se crean un usuario y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */
    Map<String, Object> u1 = insertUserOk();

    Object[] params = {
      new NullParam(Types.BIGINT),
      numberUtils.toLong(u1.get("id_usuario_mc"), 0),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id"), mUsu1.get("id"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id_usuario_mc"), mUsu1.get("id_usuario_mc"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("rut"), mUsu1.get("rut"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("estado"), mUsu1.get("estado"));
  }

  @Test
  public void searchUserBy_rut() throws SQLException {

    /**
     * se crean un usuario y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */
    Map<String, Object> u1 = insertUserOk();

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      numberUtils.toInt(u1.get("rut"), 0),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id"), mUsu1.get("id"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id_usuario_mc"), mUsu1.get("id_usuario_mc"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("rut"), mUsu1.get("rut"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("estado"), mUsu1.get("estado"));
  }

  @Test
  public void searchUserBy_estado() throws SQLException {

    /**
     * se creann 2 usuarios y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */
    String estado = "ACTIVO" + numberUtils.random(1111,9999);

    Map<String, Object> u1 = insertUserOk(estado);
    Map<String, Object> u2 = insertUserOk(estado);

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      estado,
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute("prepago.mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id"), mUsu1.get("id"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("id_usuario_mc"), mUsu1.get("id_usuario_mc"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("rut"), mUsu1.get("rut"));
    Assert.assertEquals("Debe ser el mismo usuario", u1.get("estado"), mUsu1.get("estado"));

    Map mUsu2 = (Map)result.get(1);
    Assert.assertEquals("Debe ser el mismo usuario", u2.get("id"), mUsu2.get("id"));
    Assert.assertEquals("Debe ser el mismo usuario", u2.get("id_usuario_mc"), mUsu2.get("id_usuario_mc"));
    Assert.assertEquals("Debe ser el mismo usuario", u2.get("rut"), mUsu2.get("rut"));
    Assert.assertEquals("Debe ser el mismo usuario", u2.get("estado"), mUsu2.get("estado"));
  }
}
