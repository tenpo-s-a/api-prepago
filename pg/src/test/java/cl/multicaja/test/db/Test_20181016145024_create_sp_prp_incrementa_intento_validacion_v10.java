package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180510152848_create_sp_mc_prp_crear_usuario_v10.insertUser;

/**
 * @author abarazarte
 **/
public class Test_20181016145024_create_sp_prp_incrementa_intento_validacion_v10 extends TestDbBasePg {

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario CASCADE", SCHEMA));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario CASCADE", SCHEMA));
  }

  /**
   * busca usuarios
   * @param id
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> incrementIdentityVerification(Long id) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT)
    };
    return dbUtils.execute(SCHEMA + ".mc_prp_incrementa_intento_validacion_v10", params);
  }

  @Test
  public void incrementIdentityVerificacion_ok() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    {
      Map<String, Object> resp = incrementIdentityVerification((long) obj1.get("id"));

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1 , result.size());

      Map<String, Object> mUsu1 = (Map)result.get(0);

      Assert.assertEquals("Debe ser el mismo usuario", Long.valueOf(1), numberUtils.toLong(mUsu1.get("_intentos_validacion")));
    }

    {
      Map<String, Object> resp = incrementIdentityVerification((long) obj1.get("id"));

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1 , result.size());

      Map<String, Object> mUsu1 = (Map)result.get(0);

      Assert.assertEquals("Debe ser el mismo usuario", Long.valueOf(2), numberUtils.toLong(mUsu1.get("_intentos_validacion")));
    }

    {
      Map<String, Object> resp = incrementIdentityVerification((long) obj1.get("id"));

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1 , result.size());

      Map<String, Object> mUsu1 = (Map)result.get(0);

      Assert.assertEquals("Debe ser el mismo usuario", Long.valueOf(3), numberUtils.toLong(mUsu1.get("_intentos_validacion")));
    }


  }

  @Test
  public void incrementIdentityVerificacion_not_ok() throws SQLException {

    {
      Map<String, Object> resp = incrementIdentityVerification(null);

      Assert.assertNotNull("Debe retornar respuesta", resp);

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1 , result.size());

      Map<String, Object> mUsu1 = (Map)result.get(0);

      Assert.assertNotNull("Debe retornar respuesta", mUsu1);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC001", mUsu1.get("_error_code"));
    }

    {
      Map<String, Object> resp = incrementIdentityVerification(Long.MAX_VALUE);

      Assert.assertNotNull("Debe retornar respuesta", resp);

      List result = (List)resp.get("result");

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1 , result.size());

      Map<String, Object> mUsu1 = (Map)result.get(0);

      Assert.assertNotNull("Debe retornar respuesta", mUsu1);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "MC002", mUsu1.get("_error_code"));
    }

  }
}
