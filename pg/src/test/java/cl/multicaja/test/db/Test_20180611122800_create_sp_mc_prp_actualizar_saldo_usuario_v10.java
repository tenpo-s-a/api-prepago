package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180510152848_create_sp_mc_prp_crear_usuario_v10.insertUser;
import static cl.multicaja.test.db.Test_20180510152942_create_sp_mc_prp_buscar_usuarios_v10.searchUsers;

/**
 * @autor vutreras
 */
public class Test_20180611122800_create_sp_mc_prp_actualizar_saldo_usuario_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualizar_saldo_usuario_v10";

  @Test
  public void updateBalanceOk() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    String balance = "{}";
    Long balanceExpiration = 1000L;

    Object[] params = {
      obj1.get("id"), //id
      balance, //saldo_info
      balanceExpiration,// saldo_expiracion
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
    Assert.assertEquals("Debe contener el nuevo saldo_info", balance, ((Map)result.get(0)).get("_saldo_info"));
    Assert.assertEquals("Debe contener el nuevo saldo_expiracion", balanceExpiration, ((Map)result.get(0)).get("_saldo_expiracion"));
  }

  @Test
  public void updateBalanceNoOk() throws SQLException {

    {
      Object[] params = {
        new NullParam(Types.BIGINT), //id
        "{}", //saldo_info
        1L,// saldo_expiracion
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
        new NullParam(Types.VARCHAR), //saldo_info
        1L, //saldo_expiracion
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "MC002", resp.get("_error_code"));
    }

    {
      Object[] params = {
        1L, //id
        "{}", //saldo_info
        new NullParam(Types.BIGINT), //saldo_expiracion
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "MC003", resp.get("_error_code"));
    }

    {
      Object[] params = {
        1L, //id
        "{}", //saldo_info
        -1, //saldo_expiracion, no puede ser expiracion menor a 0
        new OutParam("_error_code", Types.VARCHAR),
        new OutParam("_error_msg", Types.VARCHAR)
      };

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser 0", "MC003", resp.get("_error_code"));
    }
  }
}
