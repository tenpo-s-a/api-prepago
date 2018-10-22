package cl.multicaja.prepaid.utils;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.HashMapCache;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.model.v10.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * @autor abarazarte
 */
public final class ParametersUtil {

  private static Log log = LogFactory.getLog(ParametersUtil.class);

  private final String SP_NAME = this.getSchema() + ".mc_buscar_parametro_v10";

  private static ParametersUtil instance;

  private ConfigUtils configUtils;

  private DBUtils dbUtils;

  private NumberUtils numberUtils = NumberUtils.getInstance();

  private HashMapCache<String, Object> cache = new HashMapCache<>();

  private String getSchema() {
    return this.getConfigUtils().getProperty("schema");
  }

  private ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  private DBUtils getDbUtils() {
    if (this.dbUtils == null) {
      this.dbUtils = new DBUtils(this.getConfigUtils());
    }
    return this.dbUtils;
  }

  /**
   * retorna la instancia unica como singleton
   * @return
   */
  public static ParametersUtil getInstance() {
    if (instance == null) {
      instance = new ParametersUtil();
    }
    return instance;
  }

  public ParametersUtil() {
    super();
  }

  private Object getParameter(String application, String name, String version) throws SQLException {
    String key = application + "+" + name + "+" + version;
    if(cache.get(key) != null){
      return cache.get(key);
    }
    else{
      Map<String, Object> params = this.searchParameter(application, name, version);
      List result = (List)params.get("result");
      if(result == null || result.size() == 0){
        return null;
      }
      Map param = (Map)result.get(0);
      Parameter paramValue = JsonUtils.getJsonParser().fromJson((String)param.get("_valor"), Parameter.class);
      Object value = paramValue.getValue();
      if(value == null) {
        return null;
      }
      Long exp = (Long)param.get("_expiracion");

      cache.put(key, value, exp, HashMapCache.TypeExpire.MILLISECONDS);

      return value;
    }
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @param clazz clase para parsear el objeto
   * @param <T>
   * @return parametro
   * @throws SQLException
   */
  public <T> T getObject(String application, String name, String version, Class<T> clazz) throws SQLException {
    return this.getParameter(application, name, version) != null ? JsonUtils.getJsonParser().fromJson((String)this.getParameter(application, name, version),clazz) : null;
  }

  public <T> T getObject(String application, String name, String version, Class<T> clazz, T defaultValue) throws SQLException {
    T val = this.getObject(application, name, version, clazz);
    return val != null ? val : defaultValue;
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return parametro
   * @throws SQLException
   */
  public Integer getInteger(String application, String name, String version) throws SQLException {
    return numberUtils.toInteger(this.getParameter(application, name, version));
  }

  public Integer getInteger(String application, String name, String version, Integer defaultValue) throws SQLException {
    return numberUtils.toInteger(this.getParameter(application, name, version), defaultValue);
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return parametro
   * @throws SQLException
   */
  public Long getLong(String application, String name, String version) throws SQLException {
    return numberUtils.toLong(this.getParameter(application, name, version));
  }

  public Long getLong(String application, String name, String version, Long defaultValue) throws SQLException {
    return numberUtils.toLong(this.getParameter(application, name, version), defaultValue);
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return parametro
   * @throws SQLException
   */
  public Boolean getBoolean(String application, String name, String version) throws SQLException {
    return numberUtils.toBoolean(this.getParameter(application, name, version));
  }

  public Boolean getBoolean(String application, String name, String version, Boolean defaultValue) throws SQLException {
    return numberUtils.toBoolean(this.getParameter(application, name, version), defaultValue);
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return parametro
   * @throws SQLException
   */
  public Double getDouble(String application, String name, String version) throws SQLException {
    return numberUtils.toDouble(this.getParameter(application, name, version));
  }

  public Double getDouble(String application, String name, String version, Double defaultValue) throws SQLException {
    return numberUtils.toDouble(this.getParameter(application, name, version), defaultValue);
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return parametro
   * @throws SQLException
   */
  public Float getFloat(String application, String name, String version) throws SQLException {
    return numberUtils.toFloat(this.getParameter(application, name, version));
  }

  public Float getFloat(String application, String name, String version, Float defaultValue) throws SQLException {
    return numberUtils.toFloat(this.getParameter(application, name, version), defaultValue);
  }

  /**
   *
   * @param application aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return parametro
   * @throws SQLException
   */
  public String getString(String application, String name, String version) throws SQLException {
    return this.getParameter(application, name, version) != null ? String.valueOf(this.getParameter(application, name, version)) : "";
  }

  public String getString(String application, String name, String version, String defaultValue) throws SQLException {
    String val = this.getString(application, name, version);
    return !val.equals("") ? val: defaultValue;
  }

  /**
   * Busca el Parametro en la BD
   * @param app aplicacion del parametro
   * @param name nombre del parametro
   * @param version version del parametro
   * @return
   * @throws SQLException
   */
  protected Map<String, Object> searchParameter(String app, String name, String version) throws SQLException {
    Object[] params = {
      !StringUtils.isBlank(app) ? app : new NullParam(Types.VARCHAR),
      !StringUtils.isBlank(name) ? name : new NullParam(Types.VARCHAR),
      !StringUtils.isBlank(version) ? version : new NullParam(Types.VARCHAR),
    };
    return getDbUtils().execute(SP_NAME, params);
  }


  public String replaceDataHTML(String template, Map<String, String> data) {
    for (Map.Entry<String, String> entry : data.entrySet())
    {
      template = template.replace(entry.getKey(), entry.getValue());
    }
    return template;
  }

}
