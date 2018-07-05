package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.users.utils.ParametersUtil;

/**
 * @autor vutreras
 */
public abstract class PrepaidBaseEJBBean10 {

  protected static NumberUtils numberUtils = NumberUtils.getInstance();

  protected static ParametersUtil parametersUtil = ParametersUtil.getInstance();

  private static ConfigUtils configUtils;

  private static DBUtils dbUtils;

  private static DateUtils dateUtils;

  public final static String APP_NAME = "prepaid.appname";

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }

  /**
   *
   * @return
   */
  public static ConfigUtils getConfigUtils() {
    if (configUtils == null) {
      configUtils = new ConfigUtils("api-prepaid");
    }
    return configUtils;
  }

  /**
   *
   * @return
   */
  public static DBUtils getDbUtils() {
    if (dbUtils == null) {
      dbUtils = new DBUtils(getConfigUtils());
    }
    return dbUtils;
  }

  /**
   *
   * @return
   */
  public static String getSchema() {
    return getConfigUtils().getProperty("schema");
  }

  public static DateUtils getDateUtils(){
    if (dateUtils == null) {
      dateUtils = new DateUtils();
    }
    return dateUtils;
  }
}
