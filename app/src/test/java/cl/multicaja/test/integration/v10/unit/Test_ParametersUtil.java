package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.model.v10.Parameter;
import cl.multicaja.test.integration.v10.model.TestModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class Test_ParametersUtil extends TestBaseUnit {

  private static Log log = LogFactory.getLog(Test_ParametersUtil.class);

  private static final String INSERT_SP_NAME = getSchema() + ".mc_crear_parametro_v10";

  private static final String app = "APP";

  @BeforeClass
  public static void beforeClass() {
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.mc_parametro where aplicacion like %s", getSchema(), "'APP%'"));
  }

  private Object[] buildParam(String app, String name, String version, String value, Long expiration) {
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

  private String getValue(Object value) {
    String val;
    if(value instanceof String){
      val = (String) value;
    } else {
      val = JsonUtils.getJsonParser().toJson(value);
    }
    Parameter p = new Parameter(value.getClass().getSimpleName(), val);
    return JsonUtils.getJsonParser().toJson(p);
  }

  /**
   * inserta un nuevo parametro
   * @param name
   * @param version
   * @param value
   * @return
   * @throws SQLException
   */
  private Map<String, Object> insertParam(String app, String name, String version, String value, Long expiration) throws SQLException {

    Object[] params = buildParam(app, name, version, value, expiration);

    Map<String, Object> resp = getDbUtils().execute(INSERT_SP_NAME, params);

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

  private void updateParam(Long id, String value){
    log.debug(String.format("update parameter %d value to -> %s", id, value));
    getDbUtils().getJdbcTemplate().update(String.format("UPDATE %s.mc_parametro set valor = to_json(?::json) where id = ?", getSchema()), value, id);
  }

  private void insertNullParam(String app, String name, String version){
    String json = "{\"value\": null}";
    getDbUtils().getJdbcTemplate().update(String.format("INSERT INTO %s.mc_parametro (aplicacion, nombre, version, valor, expiracion, fecha_creacion) values(?,?,?,to_json(?::json), 1000, timezone('utc', now()))", getSchema()), app, name, version, json);
  }

  @Test
  public void getStringParameter() throws SQLException, InterruptedException {

    String name = "STRING_PARAM";
    String version = "v10";
    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue("valor1"), 1000L);

    String value = parametersUtil.getString(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = valor1", "valor1", value);

    // Actualizo valor del parametro

    this.updateParam((Long)param1.get("id"), getValue("valor2"));

    Thread.sleep(200);

    value = parametersUtil.getString(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = valor1", "valor1", value);

    Thread.sleep(200);

    value = parametersUtil.getString(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = valor1", "valor1", value);

    Thread.sleep(200);

    value = parametersUtil.getString(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = valor1", "valor1", value);

    Thread.sleep(200);

    value = parametersUtil.getString(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = valor1", "valor1", value);

    Thread.sleep(200);

    value = parametersUtil.getString(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = valor1", "valor2", value);

  }

  @Test
  public void getStringParameter_Null() throws SQLException {

    String name = "STRING_NULL_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    String value = parametersUtil.getString(app, name, version);
    Assert.assertEquals("Deberia ser vacio", "", value);
  }

  @Test
  public void getStringParameter_Default() throws SQLException {

    String name = "STRING_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    String value = parametersUtil.getString(app, name, version, "default");
    Assert.assertEquals("Deberia ser vacio", "default", value);
  }

  @Test
  public void getLongParameter() throws SQLException, InterruptedException {

    String name = "LONG_PARAM";
    String version = "v10";
    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue(1L), 1000L);

    Long value = parametersUtil.getLong(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Long.valueOf(1), value);

    // Actualizo valor del parametro

    this.updateParam((Long)param1.get("id"), getValue(2L));

    Thread.sleep(200);

    value = parametersUtil.getLong(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Long.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getLong(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Long.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getLong(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Long.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getLong(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Long.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getLong(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 2", Long.valueOf(2), value);

  }

  @Test
  public void getLongParameter_Null() throws SQLException {

    String name = "LONG_NULL_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Long value = parametersUtil.getLong(app, name, version);
    Assert.assertEquals("Deberia ser 0", Long.valueOf(0), value);
  }

  @Test
  public void getLongParameter_Default() throws SQLException {

    String name = "LONG_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Long value = parametersUtil.getLong(app, name, version, 10L);
    Assert.assertEquals("Deberia ser 0", Long.valueOf(10), value);
  }

  @Test
  public void getIntegerParameter() throws SQLException, InterruptedException {

    String name = "INTEGER_PARAM";
    String version = "v10";
    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue(1), 1000L);

    Integer value = parametersUtil.getInteger(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Integer.valueOf(1), value);

    // Actualizo valor del parametro

    this.updateParam((Long)param1.get("id"), getValue(2));

    Thread.sleep(200);

    value = parametersUtil.getInteger(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Integer.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getInteger(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Integer.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getInteger(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Integer.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getInteger(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Integer.valueOf(1), value);

    Thread.sleep(200);

    value = parametersUtil.getInteger(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1", Integer.valueOf(2), value);

  }

  @Test
  public void getIntegerParameter_Default() throws SQLException {

    String name = "INTEGER_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Integer value = parametersUtil.getInteger(app, name, version, 5);
    Assert.assertEquals("Deberia ser 0", Integer.valueOf(5), value);
  }

  @Test
  public void getDoubleParameter() throws SQLException, InterruptedException {

    String name = "DOUBLE_PARAM";
    String version = "v10";
    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue(Double.valueOf("1.5")), 1000L);

    Double value = parametersUtil.getDouble(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Double.valueOf("1.5"), value);

    // Actualizo valor del parametro

    this.updateParam((Long)param1.get("id"), getValue( Double.valueOf("2.5")));

    Thread.sleep(200);

    value = parametersUtil.getDouble(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Double.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getDouble(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Double.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getDouble(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Double.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getDouble(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Double.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getDouble(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 2.5", Double.valueOf("2.5"), value);

  }

  @Test
  public void getDoubleParameter_Null() throws SQLException {

    String name = "DOUBLE_NULL_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Double value = parametersUtil.getDouble(app, name, version);
    Assert.assertEquals("Deberia ser 0", Double.valueOf(0), value);
  }

  @Test
  public void getDoubleParameter_Default() throws SQLException {

    String name = "DOUBLE_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Double value = parametersUtil.getDouble(app, name, version, Double.valueOf("5.5"));
    Assert.assertEquals("Deberia ser 0", Double.valueOf("5.5"), value);
  }

  @Test
  public void getFloatParameter() throws SQLException, InterruptedException {

    String name = "FLOAT_PARAM";
    String version = "v10";
    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue(Float.valueOf("1.5")), 1000L);

    Float value = parametersUtil.getFloat(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Float.valueOf("1.5"), value);

    // Actualizo valor del parametro

    this.updateParam((Long)param1.get("id"), getValue(Float.valueOf("2.5")));

    Thread.sleep(200);

    value = parametersUtil.getFloat(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Float.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getFloat(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Float.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getFloat(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Float.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getFloat(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 1.5", Float.valueOf("1.5"), value);

    Thread.sleep(200);

    value = parametersUtil.getFloat(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = 2.5", Float.valueOf("2.5"), value);

  }

  @Test
  public void getFloatParameter_Null() throws SQLException {

    String name = "FLOAT_NULL_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Float value = parametersUtil.getFloat(app, name, version);
    Assert.assertEquals("Deberia ser 0", Float.valueOf(0), value);
  }

  @Test
  public void getFloatParameter_Default() throws SQLException {

    String name = "FLOAT_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Float value = parametersUtil.getFloat(app, name, version, Float.valueOf("2.5"));
    Assert.assertEquals("Deberia ser 0", Float.valueOf("2.5"), value);
  }

  @Test
  public void getBooleanParameter() throws SQLException, InterruptedException {

    String name = "BOOLEAN_PARAM";
    String version = "v10";
    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue(Boolean.TRUE), 1000L);

    Boolean value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = true", Boolean.TRUE, value);

    // Actualizo valor del parametro

    this.updateParam((Long)param1.get("id"), getValue(Boolean.FALSE));

    Thread.sleep(200);

    value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = true", Boolean.TRUE, value);

    Thread.sleep(200);

    value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = true", Boolean.TRUE, value);

    Thread.sleep(200);

    value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = true", Boolean.TRUE, value);

    Thread.sleep(200);

    value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = true", Boolean.TRUE, value);

    Thread.sleep(200);

    value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia tener valor = false", Boolean.FALSE, value);

  }

  @Test
  public void getBooleanParameter_Null() throws SQLException {

    String name = "BOOLEAN_NULL_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Boolean value = parametersUtil.getBoolean(app, name, version);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertEquals("Deberia ser false", Boolean.FALSE, value);
  }

  @Test
  public void getBooleanParameter_Default() throws SQLException {

    String name = "BOOLEAN_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    Boolean value = parametersUtil.getBoolean(app, name, version, Boolean.TRUE);
    Assert.assertEquals("Deberia ser false", Boolean.TRUE, value);
  }

  @Test
  public void getObjectParameter() throws SQLException, InterruptedException {

    String name = "OBJECT_PARAM";
    String version = "v10";

    TestModel model = new TestModel(1, "test");

    // Inserta un parametro
    Map<String, Object> param1 = insertParam(app,name, version, getValue(model), 1000L);

    TestModel value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNotNull("Deberia existir un value", value);
    Assert.assertEquals("Deberia tener id = 1", model.getId(), value.getId());
    Assert.assertEquals("Deberia tener name = test", model.getName(), value.getName());
    // Actualizo valor del parametro
    TestModel model2 = new TestModel(2, "test");
    this.updateParam((Long)param1.get("id"), getValue(model2));

    Thread.sleep(200);

    value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNotNull("Deberia existir un value", value);
    Assert.assertEquals("Deberia tener id = 1", model.getId(), value.getId());
    Assert.assertEquals("Deberia tener name = test", model.getName(), value.getName());

    Thread.sleep(200);

    value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNotNull("Deberia existir un value", value);
    Assert.assertEquals("Deberia tener id = 1", model.getId(), value.getId());
    Assert.assertEquals("Deberia tener name = test", model.getName(), value.getName());

    Thread.sleep(200);

    value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNotNull("Deberia existir un value", value);
    Assert.assertEquals("Deberia tener id = 1", model.getId(), value.getId());
    Assert.assertEquals("Deberia tener name = test", model.getName(), value.getName());

    Thread.sleep(200);

    value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNotNull("Deberia existir un value", value);
    Assert.assertEquals("Deberia tener id = 1", model.getId(), value.getId());
    Assert.assertEquals("Deberia tener name = test", model.getName(), value.getName());

    Thread.sleep(200);

    value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNotNull("Deberia existir un value", value);
    Assert.assertEquals("Deberia tener id = 2", model2.getId(), value.getId());
    Assert.assertEquals("Deberia tener name = test", model2.getName(), value.getName());

  }

  @Test
  public void getObjectParameter_Null() throws SQLException {

    String name = "OBJECT_NULL_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    TestModel value = parametersUtil.getObject(app, name, version, TestModel.class);
    log.debug(String.format("parameter: %s %s %s -> %s", app, name, version, value));
    Assert.assertNull("Deberia ser false", value);
  }

  @Test
  public void getObjectParameter_Default() throws SQLException {

    String name = "OBJECT_DEFAULT_PARAM";
    String version = "v10";

    // Inserta un parametro
    this.insertNullParam(app, name, version);

    TestModel model = new TestModel(1, "test");

    TestModel value = parametersUtil.getObject(app, name, version, TestModel.class, model);
    Assert.assertNotNull("Deberia retornar un valor", value);
    Assert.assertEquals("Deberia tener id 1", model.getId(), value.getId());
    Assert.assertEquals("Deberia tener name test", model.getName(), value.getName());
  }
}
