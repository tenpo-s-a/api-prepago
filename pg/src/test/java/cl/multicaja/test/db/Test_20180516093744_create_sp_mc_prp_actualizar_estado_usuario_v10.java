package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180510152848_create_sp_mc_prp_crear_usuario_v10.insertUser;
import static cl.multicaja.test.db.Test_20180510152942_create_sp_mc_prp_buscar_usuarios_v10.searchUsers;

/**
 * @autor vutreras
 */
public class Test_20180516093744_create_sp_mc_prp_actualizar_estado_usuario_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualizar_estado_usuario_v10";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_usuario", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_usuario", SCHEMA));
  }

  @Test
  public void updateStatusOk() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    String newStatus = "INACTIVO";

    Object[] params = {
      obj1.get("id"), //id
      newStatus, //estado
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));

    Map<String, Object> resp2 = searchUsers((long) obj1.get("id"), null, null, null);

    List result = (List)resp2.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());
    Assert.assertEquals("Debe contener ser el nuevo estado",  newStatus, ((Map)result.get(0)).get("_estado"));
  }

  @Test
  public void updateStatusNotOk() throws SQLException {

    {
      Object[] params = {
        new NullParam(Types.BIGINT), //id
        "INACTIVO", //estado
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "MC001", resp.get("_error_code"));
    }

    {
      Object[] params = {
        1L, //id
        new NullParam(Types.VARCHAR), //estado
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "MC002", resp.get("_error_code"));
    }
  }
}
