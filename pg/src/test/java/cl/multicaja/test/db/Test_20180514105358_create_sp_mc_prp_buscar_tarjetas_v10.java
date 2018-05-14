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
public class Test_20180514105358_create_sp_mc_prp_buscar_tarjetas_v10 extends Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10 {

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
  }

  @Test
  public void searchCardByAllFields() throws SQLException {

    Map<String, Object> obj1 = insertCardOk("ACTIVA");

    Object[] params = {
      numberUtils.toLong(obj1.get("id"), 0),
      numberUtils.toLong(obj1.get("id_usuario"), 0),
      numberUtils.toInt(obj1.get("fecha_expiracion"), 0),
      String.valueOf(obj1.get("estado")),
      String.valueOf(obj1.get("contrato")),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }
  }

  @Test
  public void searchUserBy_id() throws SQLException {

    Map<String, Object> obj1 = insertCardOk("ACTIVA");

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

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
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

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_id_usuario() throws SQLException {

    Map<String, Object> obj1 = insertCardOk("ACTIVA");

    Object[] params = {
      new NullParam(Types.BIGINT),
      numberUtils.toLong(obj1.get("id_usuario"), 0),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      numberUtils.toLong(obj1.get("id_usuario"), 0) + 1,
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_fecha_expiracion() throws SQLException {

    Map<String, Object> obj1 = insertCardOk("ACTIVA");

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      numberUtils.toInt(obj1.get("fecha_expiracion"), 0),
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      numberUtils.toInt(obj1.get("fecha_expiracion"), 0) + 100,
      new NullParam(Types.VARCHAR),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params2);
    System.out.println("RESP2:::::::::::::::::::" + resp2);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_estado() throws SQLException {

    /**
     * se crean un registro y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */

    String status = "ACTIVA" + numberUtils.random(1111,9999);

    Map<String, Object> obj1 = insertCardOk(status);
    Map<String, Object> obj2 = insertCardOk(status);

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      String.valueOf(obj1.get("estado")),
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    Map mCard2 = (Map)result.get(1);
    Set<String> keys2 = obj2.keySet();
    for (String k : keys2) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj2.get(k), mCard2.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      String.valueOf(obj1.get("estado")) + "1",
      new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_contrato() throws SQLException {

    Map<String, Object> obj1 = insertCardOk("ACTIVA");

    Object[] params = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      String.valueOf(obj1.get("contrato")),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Object[] params2 = {
      new NullParam(Types.BIGINT),
      new NullParam(Types.BIGINT),
      new NullParam(Types.INTEGER),
      new NullParam(Types.VARCHAR),
      String.valueOf(obj1.get("contrato")) + "1",
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp2 = dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params2);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }
}
