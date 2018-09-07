package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @autor abarazarte
 */
public class Test_20180516121151_create_sp_mc_buscar_parametro_v10 extends Test_20180516120330_create_sp_mc_crear_parametro_v10 {

  private static final String SP_NAME = SCHEMA + ".mc_buscar_parametro_v10";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.mc_parametro where aplicacion like %s", SCHEMA, "'APP%'"));
  }

  /**
   * busca parametros
   * @param app
   * @param name
   * @param version
   * @return
   * @throws SQLException
   */
  protected Map<String, Object> searchParameter(String app, String name, String version) throws SQLException {
    Object[] params = {
      !StringUtils.isBlank(app) ? app : new NullParam(Types.VARCHAR),
      !StringUtils.isBlank(name) ? name : new NullParam(Types.VARCHAR),
      !StringUtils.isBlank(version) ? version : new NullParam(Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }

  /**
   *  Deberia buscar un parametro
   */
  @Test
  public void searchParamByAppNameAndVersion() throws SQLException {

    Map<String, Object> obj1 = insertParam("APP_5","PARAMETRO_5", "v10", getValue("valor5"), Long.valueOf(10));

    Map<String, Object> resp = searchParameter((String) obj1.get("aplicacion"), (String) obj1.get("nombre"), (String)obj1.get("version"));

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mUsu1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo parametro", obj1.get(k), mUsu1.get("_" + k));
    }
  }

  /**
   * No deberia retornar parametro
   */
  @Test
  public void searchParamByAppNameAndVersionNotExists() throws SQLException {

    Map<String, Object> resp = searchParameter("APP_6", "PARAMETRO_6", "v10");

    List result = (List)resp.get("result");

    Assert.assertNull("No debe retornar una lista", result);
  }
}
