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
import java.util.Set;

import static cl.multicaja.test.db.Test_20190401112103_create_sp_mc_prp_crear_usuario_v11.buildUser;
import static cl.multicaja.test.db.Test_20190401112103_create_sp_mc_prp_crear_usuario_v11.insertUser;

public class Test_20190401160011_create_sp_mc_prp_buscar_usuarios_v11  extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_buscar_usuarios_v11";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  public void resetTables(){
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
      "_rut",
      "_estado",
      "_fecha_creacion",
      "_fecha_actualizacion",
      "_nombre",
      "_apellido",
      "_numero_documento",
      "_nivel",
      "_uiid"
    };

    for (String column : columns) {
      Assert.assertTrue("Debe contener la columna " + column, map.containsKey(column));
    }

    Assert.assertEquals("Debe contener solamente las columnas definidas", columns.length, map.keySet().size());
  }

  /**
   *
   * @param id
   * @param uiid
   * @param rut
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> searchUsers(Long id, String uiid, Integer rut) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      rut != null ? rut : new NullParam(Types.INTEGER),
      uiid != null ? uiid : new NullParam(Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }

  @Test
  public void searchUserByAllFields() throws SQLException {

    resetTables();

    Map<String, Object> obj1 = insertUser(buildUser());

    Map<String, Object> resp = searchUsers((long) obj1.get("id"), (String)obj1.get("uiid"), (int)obj1.get("rut"));
    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    String obj1String;
    String mUsu1String;
    for (String k : keys) {
      if(k != "_error_msg" && k != "_r_id" && k!= "_error_code") {
        obj1String = obj1.get(k).toString();
        mUsu1String = mUsu1.get("_" + k).toString();
        Assert.assertEquals("Debe ser el mismo dato", obj1String, mUsu1String);
      }
    }
  }

  @Test
  public void searchUserBy_id() throws SQLException {

    resetTables();

    Map<String, Object> obj1 = insertUser(buildUser());

    Map<String, Object> resp = searchUsers((long) obj1.get("id"), null, null);

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    String obj1String;
    String mUsu1String;
    for (String k : keys) {
      if(k != "_error_msg" && k != "_r_id" && k!= "_error_code"){
        obj1String = obj1.get(k).toString();
        mUsu1String = mUsu1.get("_" + k).toString();
        Assert.assertEquals("Debe ser el mismo dato", obj1String, mUsu1String);
      }
    }

    //Caso en donde no deberia encontrar un registro
    Map<String, Object> resp2 = searchUsers(((long) obj1.get("id")) + 1, null, null);

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }



  @Test
  public void searchUserBy_uiid() throws SQLException {

    resetTables();

    Map<String, Object> obj1 = insertUser(buildUser());

    Map<String, Object> resp = searchUsers(null, ((String)obj1.get("uiid")),null);

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    String obj1String;
    String mUsu1String;
    for (String k : keys) {
      if(k != "_error_msg" && k != "_r_id" && k!= "_error_code") {
        obj1String = obj1.get(k).toString();
        mUsu1String = mUsu1.get("_" + k).toString();
        Assert.assertEquals("Debe ser el mismo dato", obj1String, mUsu1String);
      }
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchUsers(null, ((String)obj1.get("uiid")) + 1 ,null);

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }

  @Test
  public void searchUserBy_rut() throws SQLException {

    resetTables();

    Map<String, Object> obj1 = insertUser(buildUser());

    Map<String, Object> resp = searchUsers(null,null, (int)obj1.get("rut"));

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map<String, Object> mUsu1 = (Map)result.get(0);

    checkColumns(mUsu1);

    Set<String> keys = obj1.keySet();
    String obj1String;
    String mUsu1String;
    for (String k : keys) {
      if(k != "_error_msg" && k != "_r_id" && k!= "_error_code") {
        obj1String = obj1.get(k).toString();
        mUsu1String = mUsu1.get("_" + k).toString();
        Assert.assertEquals("Debe ser el mismo dato", obj1String, mUsu1String);
      }
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchUsers(null,null, (int)obj1.get("rut") + 1 );

    Assert.assertNull("no debe retornar una lista", resp2.get("result"));
  }

}
