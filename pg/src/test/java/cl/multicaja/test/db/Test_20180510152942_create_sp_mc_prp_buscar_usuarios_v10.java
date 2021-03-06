package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cl.multicaja.test.db.Test_20180510152848_create_sp_mc_prp_crear_usuario_v10.insertUser;

/**
 * @autor vutreras
 */
public class Test_20180510152942_create_sp_mc_prp_buscar_usuarios_v10  extends TestDbBasePg {

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  /**
   *
   * @param map
   */
  private void checkColumns(Map<String, Object> map) {

    String[] columns = {
      "_id",
      "_id_usuario_mc",
      "_rut",
      "_estado",
      "_saldo_info",
      "_saldo_expiracion",
      "_intentos_validacion",
      "_fecha_creacion",
      "_fecha_actualizacion"
    };

    for (String column : columns) {
      Assert.assertTrue("Debe contener la columna " + column, map.containsKey(column));
    }

    Assert.assertEquals("Debe contener solamente las columnas definidas", columns.length, map.keySet().size());
  }

  /**
   * busca usuarios
   * @param id
   * @param idUsuarioMc
   * @param rut
   * @param estado
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> searchUsers(Long id, Long idUsuarioMc, Integer rut, String estado) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      idUsuarioMc != null ? idUsuarioMc : new NullParam(Types.BIGINT),
      rut != null ? rut : new NullParam(Types.INTEGER),
      estado != null ? estado : new NullParam(Types.VARCHAR)
    };
    return dbUtils.execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);
  }

  @Test
  public void searchUserByAllFields() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    Map<String, Object> resp = searchUsers((long) obj1.get("id"), (long) obj1.get("id_usuario_mc"), (int)obj1.get("rut"), (String) obj1.get("estado"));

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get("_" + k));
    }
  }

  @Test
  public void searchUserBy_id() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    Map<String, Object> resp = searchUsers((long) obj1.get("id"), null, null, null);

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get("_" + k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchUsers(((long) obj1.get("id")) + 1, null, null, null);

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }

  @Test
  public void searchUserBy_id_usuario_mc() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    Map<String, Object> resp = searchUsers(null, (long) obj1.get("id_usuario_mc"), null, null);

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get("_" + k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchUsers(null, ((long) obj1.get("id_usuario_mc")) + 1, null, null);

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }

  @Test
  public void searchUserBy_rut() throws SQLException {

    Map<String, Object> obj1 = insertUser("ACTIVO");

    Map<String, Object> resp = searchUsers(null, null, (int)obj1.get("rut"), null);

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser el mismo usuario", obj1.get(k), mUsu1.get("_" + k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchUsers(null, null, ((int)obj1.get("rut")) + 1, null);

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }

  @Test
  public void searchUserBy_estado() throws SQLException {

    String status = getRandomString(10) + numberUtils.random(11111, 99999);

    ArrayList<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();
    objectList.add(0, insertUser(status));
    objectList.add(1, insertUser(status));

    Map<String, Object> resp = searchUsers(null, null, null, status);

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener dos elementos", 2 , result.size());

    for (Map<String, Object> createdUser : objectList) { // Por cada usuario insertado
      for (Object foundUserObj : result) { // Por cada usuario insertado
        Map<String, Object> foundUserMap = (Map) foundUserObj;
        if (foundUserMap.get("_id").equals(createdUser.get("id"))) { // Buscar si tienen el mismo id
          checkColumns(foundUserMap);
          Set<String> keys = createdUser.keySet();
          for (String k : keys) { // Deberian tener lo mismo en el resto de sus campos
            System.out.println(createdUser.get(k) + " == "  + foundUserMap.get("_" + k));
            Assert.assertEquals("Debe ser el mismo usuario", createdUser.get(k), foundUserMap.get("_" + k));
          }
        }
      }
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchUsers(null, null, null, status + 1);

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }
}
