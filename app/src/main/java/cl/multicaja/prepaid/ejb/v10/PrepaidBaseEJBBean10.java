package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.calculatorParameter10;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.users.utils.ParametersUtil;

/**
 * @autor vutreras
 */
public abstract class PrepaidBaseEJBBean10 {

  protected static NumberUtils numberUtils = NumberUtils.getInstance();

  protected static ParametersUtil parametersUtil = ParametersUtil.getInstance();

  private static ConfigUtils configUtils;

  private static EncryptUtil encryptUtil;

  private static DBUtils dbUtils;

  private static DateUtils dateUtils;

  public final static String APP_NAME = "prepaid.appname";

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }


  public calculatorParameter10 getPercentage(){
    return CalculationsHelper.getInstance().getPercentage10();
  }
  public CalculationsHelper getCalculationsHelper(){
    return CalculationsHelper.getInstance();
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
  public static EncryptUtil getEncryptUtil() {
    if (encryptUtil == null) {
      encryptUtil = EncryptUtil.getInstance();
    }
    return encryptUtil;
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
