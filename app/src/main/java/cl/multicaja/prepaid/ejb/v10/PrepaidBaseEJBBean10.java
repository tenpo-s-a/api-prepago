package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @autor vutreras
 */
public abstract class PrepaidBaseEJBBean10 {

  protected NumberUtils numberUtils = NumberUtils.getInstance();
  protected ParametersUtil parametersUtil = ParametersUtil.getInstance();
  private ConfigUtils configUtils;

  private DBUtils dbUtils;

  public final static String APP_NAME = "prepaid.appname";

  public TecnocomService getTecnocomService() {
    return TecnocomServiceHelper.getInstance().getTecnocomService();
  }

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  /**
   *
   * @return
   */
  public DBUtils getDbUtils() {
    if (this.dbUtils == null) {
      this.dbUtils = new DBUtils(this.getConfigUtils());
    }
    return this.dbUtils;
  }

  /**
   *
   * @return
   */
  public String getSchema() {
    return this.getConfigUtils().getProperty("schema");
  }
}
