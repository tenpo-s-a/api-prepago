package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180510152848_create_sp_mc_prp_crear_usuario_v10.insertUser;

/**
 * @autor vutreras
 */
public class Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_crear_tarjeta_v10";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
  }

  /**
   * Crea los datos de una tarjeta de forma aleatoria
   * @param status
   * @return
   */
  public static Object[] buildCard(String status) throws SQLException {

    int expiryYear = numberUtils.random(1000, 9999);
    int expiryMonth = numberUtils.random(1, 99);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"), 0);

    //la tarjeta requiere de la existencia de un usuario en la BD
    Map<String, Object> mapUser = insertUser("ACTIVO");

    Object[] params = {
      mapUser.get("id"), //_id_usuario
      RandomStringUtils.randomNumeric(16), //_pan
      RandomStringUtils.randomAlphabetic(50), //_pan_encriptado
      RandomStringUtils.randomAlphabetic(20), //_contrato
      expiryDate, //_expiracion
      status, //_estado
      "Tarjeta de: " + RandomStringUtils.randomAlphabetic(5), //_nombre_tarjeta
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }
  /**
   * Crea los datos de una tarjeta Pendiente
   * @param status
   * @return
   */
  public static Object[] buildEmptyCard(String contrato,String status) throws SQLException {

   //la tarjeta requiere de la existencia de un usuario en la BD
    Map<String, Object> mapUser = insertUser("ACTIVO");

    Object[] params = {
      mapUser.get("id"), //_id_usuario
      new NullParam(Types.VARCHAR), //_pan
      new NullParam(Types.VARCHAR), //_pan_encriptado
      contrato, //_contrato
      0, //_expiracion
      status, //_estado
      new NullParam(Types.VARCHAR), //_nombre_tarjeta
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }
  /**
   * inserta una nueva tarjeta
   * @param status
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertCard(String status) throws SQLException {

    Object[] params = buildCard(status);

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(resp.get("_r_id") ));
    map.put("id_usuario", numberUtils.toLong(params[0] ));
    map.put("pan", String.valueOf(params[1]));
    map.put("pan_encriptado", String.valueOf(params[2]));
    map.put("contrato", String.valueOf(params[3]));
    map.put("expiracion", numberUtils.toInt(params[4]));
    map.put("estado", String.valueOf(params[5]));
    map.put("nombre_tarjeta", String.valueOf(params[6]));

    return map;
  }

  /**
   * inserta una nueva pendiente
   * @param status
   * @return
   * @throws SQLException
   */

  public static Map<String, Object> insertEmptyCard(String contrato, String status) throws SQLException {

    Object[] params = buildEmptyCard(contrato,status);

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(resp.get("_r_id") ));
    map.put("id_usuario", numberUtils.toLong(params[0] ));
    map.put("pan", String.valueOf(params[1]));
    map.put("pan_encriptado", String.valueOf(params[2]));
    map.put("contrato", String.valueOf(params[3]));
    map.put("expiracion", numberUtils.toInt(params[4]));
    map.put("estado", String.valueOf(params[5]));
    map.put("nombre_tarjeta", String.valueOf(params[6]));

    return map;
  }

  @Test
  public void insertCardOk() throws SQLException {

    /**
     * Caso de prueba para registrar una tarjeta nueva, debe ser exitoso
     *
     */
    Object[] params = buildCard("ACTIVA");

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
    System.out.println(resp);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);
  }

  @Test
  public void insertCardNotOkByParamsNull() throws SQLException {

    /**
     * Caso de prueba donde se registra una tarjeta nueva pero pasando parametros en null
     */
    {
      Object[] params = buildCard("ACTIVA");

      params[0] = new NullParam(Types.BIGINT); //_id_usuario

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC001", resp.get("_error_code"));
    }


    {
      Object[] params = buildCard("ACTIVA");

      params[5] = new NullParam(Types.VARCHAR); //_estado

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC002", resp.get("_error_code"));
    }

  }
}
