package cl.multicaja.test.db;

import cl.multicaja.core.utils.json.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @autor vutreras
 */
public class Test_20180523113509_insert_parameter_cod_entidad_tecnocom extends TestDbBasePg {

  @Test
  public void exists_cod_entidad() {

    String codEntidad = configUtils.getProperty("tecnocom.cod_entidad");

    String query = String.format("select valor from %s.mc_parametro where aplicacion = ? and nombre = ? and version = ?;", SCHEMA_PARAMETERS);

    String[] params = {
      "api-prepaid",
      "cod_entidad",
      "v10"
    };

    String json = dbUtils.getJdbcTemplate().queryForObject(query, params, String.class);

    Map map = JsonUtils.getJsonParser().fromJson(json, HashMap.class);

    Assert.assertEquals("Debe coincidir el valor", codEntidad, map.get("value"));
  }

}
