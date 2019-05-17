package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import cl.multicaja.prepaid.external.freshdesk.FreshdeskService;
import cl.multicaja.prepaid.external.freshdesk.FreshdeskServiceImpl;
import cl.multicaja.core.utils.ConfigUtils;


public class FreshdeskServiceHelper {

  private final String API_URL = "https://multicaja.freshdesk.com";
  private final String API_USER = "CEFMK1T4XuXYrBhbuN";
  private final String API_PASSWORD = "X";
  private final Boolean API_IS_ENABLED = Boolean.TRUE;

  private FreshdeskService freshdeskService;
  private ConfigUtils configUtils;

  private static FreshdeskServiceHelper instance;

  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  public FreshdeskService getFreshdeskService() {
    return freshdeskService;
  }

  private void setFreshdeskService(FreshdeskService freshdeskService) {
    this.freshdeskService = freshdeskService;
  }

  public FreshdeskServiceHelper(){
    String apiUrl = getConfigUtils().getProperty("freshdesk.endpoint") == null ? this.API_URL : getConfigUtils().getProperty("freshdesk.endpoint");
    String apiUser = getConfigUtils().getProperty("freshdesk.user") == null ? this.API_USER : getConfigUtils().getProperty("freshdesk.user");
    String apiPassword = getConfigUtils().getProperty("freshdesk.password") == null ? this.API_PASSWORD : getConfigUtils().getProperty("freshdesk.password");
    Boolean apiIsEnabled = Boolean.valueOf(getConfigUtils().getProperty("freshdesk.enabled") == null ? this.API_IS_ENABLED.toString() : getConfigUtils().getProperty("freshdesk.enabled"));
    setFreshdeskService(new FreshdeskServiceImpl(apiUrl,apiUser,apiPassword,apiIsEnabled));
  }

  public  static FreshdeskServiceHelper getInstance() {
    if (instance == null) {
      instance = new FreshdeskServiceHelper();
    }
    return instance;
  }

  public Boolean isClosedOrResolved(Long status) {
    return (status.equals(StatusType.CLOSED) || status.equals(StatusType.RESOLVED));
  }

}
