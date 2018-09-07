package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.json.JsonMapper;
import cl.multicaja.core.utils.json.JsonParser;
import cl.multicaja.test.model.TestModelObject;
import cl.multicaja.test.model.TestParam;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @autor abarazarte
 */
public class Test_20180516120330_create_sp_mc_crear_parametro_v10 extends Test_20180516115015_create_table_mc_parametro_v10 {

  private static final String SP_NAME = SCHEMA + ".mc_crear_parametro_v10";

  private JsonParser jsonParser = null;

  protected JsonParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = new JsonMapper();
    }

    return this.jsonParser;
  }

  protected String toJson(Object obj) {
    return this.getJsonParser().toJson(obj);
  }

  protected <T> T fromJson(String json, Class<T> cls) {
    return this.getJsonParser().fromJson(json, cls);
  }

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.mc_parametro where aplicacion like %s", SCHEMA, "'APP%'"));
  }

  /**
   * Crea los datos de un parametro
   * @param app aplicacion que utiliza el parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @param value expiracion del parametro en milisegundos
   * @return
   */
  protected Object[] buildParam(String app, String name, String version, String value, Long expiration) {
    Object[] params = {
      app, //aplicacion
      name, //nombre
      version, //version
      value, //valor
      expiration != null ? expiration : 0L, //expiracion
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }

  /**
   * inserta un nuevo parametro
   * @param name
   * @param version
   * @param value
   * @return
   * @throws SQLException
   */
  protected Map<String, Object> insertParam(String app, String name, String version, String value, Long expiration) throws SQLException {

    Object[] params = buildParam(app, name, version, value, expiration);

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    Map<String, Object> map = new HashMap<>();
    map.put("id", numberUtils.toLong(resp.get("_r_id")));
    map.put("aplicacion",params[0]);
    map.put("nombre",params[1]);
    map.put("version", params[2]);
    map.put("valor", params[3]);
    map.put("expiracion", numberUtils.toLong(params[4]));

    return map;
  }

  protected String getValue(Object value){

    TestParam p = new TestParam();
    p.setType(value.getClass().getSimpleName());
    p.setValue(toJson(value));
    return toJson(p);
  }

  /**
   * Caso de prueba donde se inserta un parametro
   */
  @Test
  public void insertParam() throws SQLException {
    Object[] params = buildParam("APP_1","PARAMETRO_1", "v10", getValue("valor1"), Long.valueOf(10));

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);
  }

  /**
   * Caso de prueba donde se inserta un parametro nuevo y luego se inserta otro con el mismo nombre y otra version
   */
  @Test
  public void insertParamNewVersionOk() throws SQLException {

    Object[] params = buildParam("APP_2","PARAMETRO_2", "v10", getValue("valor2"), Long.valueOf(10));

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se inserta una nueva version
     */
    params[2] = "v11"; //version
    resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);
  }

  /**
   * Caso de prueba donde se inserta un parametro nuevo y luego se intenta exactamente el mismo parametro
   */
  @Test
  public void insertParamNotOk() throws SQLException {

    Object[] params = buildParam("APP_3","PARAMETRO_3", "v10", getValue("valor3"), Long.valueOf(10));

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id")) > 0);

    /**
     * se intenta registrar exactamente el mismo parametro y debe fallar
     */
    params[3] = "valor3.1"; //valor
    resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error no debe ser 0", "0", resp.get("_error_code"));
    Assert.assertEquals("debe retornar un id 0", Long.valueOf(0),numberUtils.toLong(resp.get("_r_id")));
  }

  /**
   * Caso de prueba donde se inserta un parametro nuevo pero pasando parametros en null
   */
  @Test
  public void insertParamNotOkByParamsNull() throws SQLException {
    String value = getValue("valor4");
    {
      Object[] params = buildParam("APP_4","PARAMETRO_4", "v10", value, Long.valueOf(10));

      params[0] = new NullParam(Types.VARCHAR); //aplicacion

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "101000", resp.get("_error_code"));
    }

    {
      Object[] params = buildParam("APP_4","PARAMETRO_4", "v10", value, Long.valueOf(10));

      params[1] = new NullParam(Types.VARCHAR); //nombre

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "101000", resp.get("_error_code"));
    }

    {
      Object[] params = buildParam("APP_4","PARAMETRO_4", "v10", value, Long.valueOf(10));

      params[2] = new NullParam(Types.VARCHAR); //version

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "101000", resp.get("_error_code"));
    }

    {
      Object[] params = buildParam("APP_4","PARAMETRO_4", "v10", value, Long.valueOf(10));

      params[3] = new NullParam(Types.VARCHAR); //valor

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "101000", resp.get("_error_code"));
    }

    {
      Object[] params = buildParam("APP_4","PARAMETRO_4", "v10", value, null);

      Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

      Assert.assertNotNull("Debe retornar respuesta", resp);
      Assert.assertEquals("Codigo de error debe ser distinto de 0", "101000", resp.get("_error_code"));
    }
  }

  /**
   * Caso de prueba donde se inserta un parametro -> Objeto json
   */
  @Test
  public void insertParam_Object() throws SQLException {
    TestModelObject obj = new TestModelObject(1L, "Test");

    Object[] params = buildParam("APP_1","OBJECT", "v10", getValue(obj), Long.valueOf(10));

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(resp.get("_r_id"), Long.valueOf(0)) > 0);

    Map<String, Object> p = getParameter(numberUtils.toLong(resp.get("_r_id"), Long.valueOf(0))).get(0);
    TestParam inserted = fromJson((String)p.get("valor"), TestParam.class);
    TestModelObject modelObject = fromJson(inserted.getValue(), TestModelObject.class);

    Assert.assertEquals("Debe ser de tipo ", obj.getName(), modelObject.getName());
  }

  /**
   * Caso de prueba donde se inserta un parametro -> Objeto json mal formado
   */
  @Test
  public void insertParam_InvalidObject() throws SQLException {
    Object[] params = buildParam("APP_1","OBJECT", "v10", "{\"type\"}", Long.valueOf(10));

    Map<String,Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser distinto de 0", "0", resp.get("_error_code"));
  }

  private List<Map<String, Object>> getParameter(Long id) {
    return dbUtils.getJdbcTemplate().queryForList(
      " SELECT " +
        "     id, " +
        "     aplicacion, " +
        "     nombre, " +
        "     version, " +
        "     valor::text, " +
        "     expiracion " +
        " FROM " +
        "   "+ SCHEMA +".mc_parametro"+
        " WHERE " +
        " id = "+ id
    );
  }
}
