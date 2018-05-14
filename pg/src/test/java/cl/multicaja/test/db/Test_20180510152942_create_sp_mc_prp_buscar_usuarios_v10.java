package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @autor vutreras
 */
public class Test_20180510152942_create_sp_mc_prp_buscar_usuarios_v10  extends Test_20180510152848_create_sp_mc_prp_crear_usuario_v10 {

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_usuario", SCHEMA));
  }

  @Test
  public void searchUserByAllFields() throws SQLException {

    Map<String, Object> obj1 = insertUserOk("ACTIVO");

    Object[] params = {
      numberUtils.toLong(obj1.get("id"), 0),
      numberUtils.toLong(obj1.get("id_usuario_mc"), 0),
      numberUtils.toInt(obj1.get("rut"), 0),
      String.valueOf(obj1.get("estado")),
      String.valueOf(obj1.get("contrato")),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get(k));
    }
  }

  @Test
  public void searchUserBy_id() throws SQLException {

    Map<String, Object> obj1 = insertUserOk("ACTIVO");

    Object[] params = {
      numberUtils.toLong(obj1.get("id"), 0),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      numberUtils.toLong(obj1.get("id"), 0) + 1,
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_id_usuario_mc() throws SQLException {

    Map<String, Object> obj1 = insertUserOk("ACTIVO");

    Object[] params = {
      new NullParam(Types.BIGINT),
      numberUtils.toLong(obj1.get("id_usuario_mc"), 0),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      numberUtils.toLong(obj1.get("id_usuario_mc"), 0) + 1,
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_rut() throws SQLException {

    Map<String, Object> obj1 = insertUserOk("ACTIVO");

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      numberUtils.toInt(obj1.get("rut"), 0),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      numberUtils.toInt(obj1.get("rut"), 0) + 1,
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_estado() throws SQLException {

    String status = "ACTIVO" + numberUtils.random(1111,9999);

    Map<String, Object> obj1 = insertUserOk(status);
    Map<String, Object> obj2 = insertUserOk(status);

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      status,
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get(k));
    }

    Map mUsu2 = (Map)result.get(1);
    Set<String> keys2 = obj2.keySet();
    for (String k : keys2) {
      Assert.assertEquals("Debe ser el mismo usuario", obj2.get(k), mUsu2.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      status + "1",
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }
}
