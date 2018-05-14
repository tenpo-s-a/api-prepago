package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
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

/**
 * @autor vutreras
 */
public class Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10 extends TestDbBase {

  protected static final String SCHEMA = "prepago";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
  }

  protected Object[] buildCardData(String status) {

    int expiryYear = numberUtils.random(2018, 2025);
    int expiryMonth = numberUtils.random(1, 12);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"), 0);

    Object[] params = {
      new Long(getUniqueInteger()), //_id_usuario
      RandomStringUtils.randomNumeric(16), //_pan
      RandomStringUtils.randomAlphabetic(50), //_pan_encriptado
      RandomStringUtils.randomAlphabetic(20), //_contrato
      expiryDate, //_fecha_expiracion
      status, //_estado
      "Tarjeta de: " + RandomStringUtils.randomAlphabetic(5), //_nombre_tarjeta
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }

  protected Map<String, Object> insertCardOk(String status) throws SQLException {

    /**
     * Caso de prueba para registrar una tarjeta nueva, debe ser exitoso
     */

    Object[] params = buildCardData(status);

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_tarjeta_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);

    Map<String, Object> map = new HashMap<>();

    map.put("id", resp.get("_r_id"));
    map.put("id_usuario", params[0]);
    map.put("pan", params[1]);
    map.put("pan_encriptado", params[2]);
    map.put("contrato", params[3]);
    map.put("fecha_expiracion", params[4]);
    map.put("estado", params[5]);
    map.put("nombre_tarjeta", params[6]);

    return map;
  }

  @Test
  public void insertCardOk() throws SQLException {

    /**
     * Caso de prueba para registrar una tarjeta nueva, debe ser exitoso
     *
     */
    Object[] params = buildCardData("ACTIVA");

    Map<String, Object> resp = dbUtils.execute(SCHEMA + ".mc_prp_crear_tarjeta_v10", params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), 0) > 0);
  }
}
