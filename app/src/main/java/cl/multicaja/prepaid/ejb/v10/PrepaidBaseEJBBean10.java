package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.*;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.model.v10.CalculatorParameter10;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;

import java.util.Map;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

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
  public UserClient getUserClient(){
    return UserClient.getInstance();
  }

  public CalculatorParameter10 getPercentage(){
    return CalculationsHelper.getInstance().getCalculatorParameter10();
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
  public static String getSchemaAccounting() {
    return getConfigUtils().getProperty("schema.acc");
  }
  public static DateUtils getDateUtils(){
    if (dateUtils == null) {
      dateUtils = new DateUtils();
    }
    return dateUtils;
  }
  public Long verifiUserAutentication(Map<String, Object> headers) throws BaseException {
    if(headers != null && headers.containsKey("X-Authenticated-Userid")) {
      return numberUtils.toLong(headers.get("X-Authenticated-Userid"));
    }
    throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
  }

}
