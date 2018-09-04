package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_20180725121258_create_function_get_app_file_v10 extends TestDbBasePg {

  private static List<Long> fileIds = new ArrayList<>();

  @AfterClass
  public static void afterClass() {
    fileIds.stream()
      .forEach(id -> {
        dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_app_file WHERE id = %d", SCHEMA, id));
      });
  }

  private static Long appFileId;
  private static String name = getRandomString(5, 25);
  private static String version = "v1.0";
  private static String description = getRandomString(5, 100);
  private static String mimetype = getRandomString(5, 25);
  private static String location  = getRandomString(5, 100);

  private static Long appFileId2;
  private static String name2 = name;
  private static String version2 = "v1.1.1";
  private static String description2 = getRandomString(5, 100);
  private static String mimetype2 = getRandomString(5, 25);
  private static String location2  = getRandomString(5, 100);

  private static Long appFileId3;
  private static String name3 = getRandomString(5, 25);
  private static String version3 = "v1.1";
  private static String description3 = getRandomString(5, 100);
  private static String mimetype3 = getRandomString(5, 25);
  private static String location3  = getRandomString(5, 100);

  private static Long appFileId4;
  private static String name4 = getRandomString(5, 25);
  private static String version4 = "v2.0";
  private static String description4 = getRandomString(5, 100);
  private static String mimetype4 = getRandomString(5, 25);
  private static String location4  = getRandomString(5, 100);

  private static Long appFileId5;
  private static String name5 = name;
  private static String version5 = "v2.0";
  private static String description5 = getRandomString(5, 100);
  private static String mimetype5 = getRandomString(5, 25);
  private static String location5  = getRandomString(5, 100);

  @BeforeClass
  public static void beforeClass() throws Exception {
    Map<String,Object> map = createAppFile(name, version, description, mimetype, location);
    Assert.assertNotNull("Debe retornar un id", map.get("_id_app_file"));
    Long id1 = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotEquals("Debe retornar un id diferent de 0", Long.valueOf(0), id1);
    appFileId = id1;
    fileIds.add(id1);

    map = createAppFile(name2, version2, description2, mimetype2, location2);
    Assert.assertNotNull("Debe retornar un id", map.get("_id_app_file"));
    Long id2 = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotEquals("Debe retornar un id diferent de 0", Long.valueOf(0), id2);
    appFileId2 = id2;
    fileIds.add(id2);

    map = createAppFile(name3, version3, description3, mimetype3, location3);
    Assert.assertNotNull("Debe retornar un id", map.get("_id_app_file"));
    Long id3 = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotEquals("Debe retornar un id diferent de 0", Long.valueOf(0), id3);
    appFileId3 = id3;
    fileIds.add(id3);

    map = createAppFile(name4, version4, description4, mimetype4, location4);
    Assert.assertNotNull("Debe retornar un id", map.get("_id_app_file"));
    Long id4 = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotEquals("Debe retornar un id diferent de 0", Long.valueOf(0), id4);
    appFileId4 = id4;
    fileIds.add(id4);

    map = createAppFile(name5, version5, description5, mimetype5, location5);
    Assert.assertNotNull("Debe retornar un id", map.get("_id_app_file"));
    Long id5 = numberUtils.toLong(map.get("_id_app_file"));
    Assert.assertNotEquals("Debe retornar un id diferent de 0", Long.valueOf(0), id5);
    appFileId5 = id5;
    fileIds.add(id5);
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
  private static Map<String, Object> createAppFile(String name, String version, String description, String mimeType, String location) throws SQLException {
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

  private static Map<String, Object> getAppFile(Long id, String name, String version, Integer status) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      name != null ? name : new NullParam(Types.VARCHAR),
      version != null ? version : new NullParam(Types.VARCHAR),
      status != null ? status : new NullParam(Types.VARCHAR)
    };
    return dbUtils.execute(SCHEMA + ".mc_prp_get_app_file_v10", params);
  }

  @Test
  public void getAppFile_Ok() throws SQLException {

    Map<String, Object> resp = getAppFile( null, null, null,null);
    List result = (List)resp.get("result");
    Assert.assertEquals("debe tener 5 archivos", 6, result.size());
  }

  @Test
  public void getAppFile_Ok_by_id() throws SQLException {

    Map<String, Object> resp = getAppFile(appFileId3, null, null,null);
    List result = (List)resp.get("result");
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("debe tener 1 archivos", 1, result.size());
    Map map = (Map) result.get(0);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId3, numberUtils.toLong(map.get("_id")));

  }

  @Test
  public void getAppFile_Ok_by_version() throws SQLException {

    Map<String, Object> resp = getAppFile(null, null, "v2.0",null);
    List result = (List)resp.get("result");
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("debe tener 3 archivos", 2, result.size());
    Map map = (Map) result.get(0);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId5, numberUtils.toLong(map.get("_id")));
    map = (Map) result.get(1);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId4, numberUtils.toLong(map.get("_id")));

  }

  @Test
  public void getAppFile_Ok_by_name() throws SQLException {

    Map<String, Object> resp = getAppFile(null, name, null,null);
    List result = (List)resp.get("result");
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("debe tener 3 archivos", 3, result.size());
    Map map = (Map) result.get(0);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId5, numberUtils.toLong(map.get("_id")));
    Assert.assertEquals("tener la misma version", "v2.0", map.get("_version").toString());
    map = (Map) result.get(1);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId2, numberUtils.toLong(map.get("_id")));
    Assert.assertEquals("tener la misma version", "v1.1.1", map.get("_version").toString());
    map = (Map) result.get(2);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId, numberUtils.toLong(map.get("_id")));
    Assert.assertEquals("tener la misma version", "v1.0", map.get("_version").toString());
  }

  @Test
  public void getAppFile_Ok_by_name_and_version() throws SQLException {

    Map<String, Object> resp = getAppFile(null, name, version2,null);
    List result = (List)resp.get("result");
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("debe tener 3 archivos", 1, result.size());
    Map map = (Map) result.get(0);
    Assert.assertNotNull("debe retornar un resultado", map);
    Assert.assertEquals("tener el mismo id", appFileId2, numberUtils.toLong(map.get("_id")));
    Assert.assertEquals("tener la misma version", "v1.1.1", map.get("_version").toString());
  }

  @Test
  public void getAppFile_Ok_null() throws SQLException {
    Map<String, Object> resp = getAppFile(Long.MAX_VALUE, null, null,null);
    Assert.assertNull("La lista debe ser nula", resp.get("result"));
  }

}
