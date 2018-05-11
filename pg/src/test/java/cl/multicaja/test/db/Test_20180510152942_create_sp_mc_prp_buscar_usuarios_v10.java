package cl.multicaja.test.db;

import cl.multicaja.core.test.TestDbBase;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor vutreras
 */
public class Test_20180510152942_create_sp_mc_prp_buscar_usuarios_v10  extends TestDbBase {

  public Map<String, Object> insertUserOk() throws SQLException {

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

    Map<String, Object> map = new HashMap<>();

    map.put("id", resp.get("_r_id"));
    map.put("id_usuario_mc", params[0]);
    map.put("rut", params[1]);
    map.put("estado", params[2]);

    return map;
  }

  @Test
  public void searchUserById() throws SQLException {

    Map<String, Object> u1 = insertUserOk();
    Map<String, Object> u2 = insertUserOk();
  }
}
