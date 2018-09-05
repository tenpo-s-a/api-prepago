package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_20180725095504_create_function_create_app_file_v10 extends TestDbBasePg {

  private static List<Long> fileIds = new ArrayList<>();

  @AfterClass
  public static void afterClass() {
    fileIds.stream()
      .forEach(id -> {
        dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_app_file WHERE id = %d", SCHEMA, id));
      });
  }

  /**
   *
   * @param name
   * @param description
   * @param mimeType
   * @param location
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> createAppFile(String name, String version, String description, String mimeType, String location) throws SQLException {
    Object[] params = {
      name != null ? name : new NullParam(Types.VARCHAR),
      version != null ? version : new NullParam(Types.VARCHAR),
      description != null ? description : new NullParam(Types.VARCHAR),
      mimeType != null ? mimeType : new NullParam(Types.VARCHAR),
      location != null ? location: new NullParam(Types.LONGVARCHAR),
      new OutParam("_id_app_file", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR) };
    return dbUtils.execute(SCHEMA + ".mc_prp_create_app_file_v10", params);
  }

  @Test
  public void createAppFile_Ok() throws SQLException {

    String name = getRandomString(5, 10);
    String version = "v1.0";
    String description = getRandomString(5, 100);
    String mimeType = getRandomString(5, 25);
    String location = getRandomString(5, 100);
    Map<String,Object> map = createAppFile(name, version, description, mimeType, location);

    Long id = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotNull("Debe retornar un id", id);
    Assert.assertNotEquals("Debe retornar un id diferent de 0",  Long.valueOf(0), id);
    fileIds.add(id);
  }

  @Test
  public void createAppFile_NotOk_ParamsNull() throws SQLException {

    String name = getRandomString(5, 10);
    String version = "v1.0";
    String description = getRandomString(5, 100);
    String mimeType = getRandomString(5, 25);
    String location = getRandomString(5, 100);

    //name null
    {
      Map<String,Object> map1 = createAppFile(null, version, description, mimeType, location);

      Assert.assertNotNull("Debe retornar un id", map1.get("_id_app_file"));
      Assert.assertEquals("Debe retornar un id diferent de 0",  0, numberUtils.toInt(map1.get("_id_app_file")));
    }

    //version null
    {
      Map<String,Object> map1 = createAppFile(name, null, description, mimeType, location);

      Assert.assertNotNull("Debe retornar un id", map1.get("_id_app_file"));
      Assert.assertEquals("Debe retornar un id diferent de 0",  0, numberUtils.toInt(map1.get("_id_app_file")));
    }

    //description null
    {
      Map<String,Object> map1 = createAppFile(name, version, null, mimeType, location);

      Assert.assertNotNull("Debe retornar un id", map1.get("_id_app_file"));
      Assert.assertEquals("Debe retornar un id diferent de 0",  0, numberUtils.toInt(map1.get("_id_app_file")));
    }

    //mimeType null
    {
      Map<String,Object> map1 = createAppFile(name, version, description, null, location);

      Assert.assertNotNull("Debe retornar un id", map1.get("_id_app_file"));
      Assert.assertEquals("Debe retornar un id diferent de 0",  0, numberUtils.toInt(map1.get("_id_app_file")));
    }

    //description null
    {
      Map<String,Object> map1 = createAppFile(name, version, description, mimeType, null);

      Assert.assertNotNull("Debe retornar un id", map1.get("_id_app_file"));
      Assert.assertEquals("Debe retornar un id diferent de 0",  0, numberUtils.toInt(map1.get("_id_app_file")));
    }
  }

  @Test
  public void createAppFile_NotOk_DuplicatedFile() throws SQLException {

    String name = getRandomString(5, 10);
    String version = "v1.0";
    String description = getRandomString(5, 100);
    String mimeType = getRandomString(5, 25);
    String location = getRandomString(5, 100);
    Map<String,Object> map = createAppFile(name, version, description, mimeType, location);

    Long id = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotNull("Debe retornar un id", id);
    Assert.assertNotEquals("Debe retornar un id diferent de 0",  Long.valueOf(0), id);
    fileIds.add(id);

    Map<String,Object> map1 = createAppFile(name, version, description, mimeType, location);

    Long id2 = numberUtils.toLong(map1.get("_id_app_file"));
    Assert.assertNotNull("Debe retornar un id", id2);
    Assert.assertEquals("Debe retornar un id diferent de 0",  Long.valueOf(0), id2);
    fileIds.add(id2);
  }


}
