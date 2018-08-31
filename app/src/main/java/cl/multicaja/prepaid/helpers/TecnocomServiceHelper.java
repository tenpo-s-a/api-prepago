package cl.multicaja.prepaid.helpers;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.TecnocomServiceImpl;
import cl.multicaja.tecnocom.TecnocomServiceMockImpl;
import cl.multicaja.tecnocom.constants.HashOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper para inicializar el servicio de tecnocom
 *
 * @autor vutreras
 */
public final class TecnocomServiceHelper {

  private static Log log = LogFactory.getLog(TecnocomServiceHelper.class);

  private static TecnocomServiceHelper instance;

  public static TecnocomServiceHelper getInstance() {
    if (instance == null) {
      instance = new TecnocomServiceHelper();
    }
    return instance;
  }

  private ConfigUtils configUtils;
  private ParametersUtil parametersUtil;
  private TecnocomService tecnocomService;

  /**
   *
   * @return
   */
  private ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  private ParametersUtil getParametersUtil() {
    if (parametersUtil == null) {
      parametersUtil = ParametersUtil.getInstance();
    }
    return parametersUtil;
  }

  /**
   *
   * @return
   */
  public synchronized TecnocomService getTecnocomService() {
    if (this.tecnocomService == null) {
      ConfigUtils config = getConfigUtils();
      String apiKey = config.getProperty("tecnocom.apiKey");
      String apiUrl = config.getProperty("tecnocom.apiUrl");
      String channel = config.getProperty("tecnocom.channel");
      String codent = null;
      try {
        codent = getParametersUtil().getString("api-prepaid", "cod_entidad", "v10");
      } catch (Exception e) {
        log.error("Error al cargar parametro cod_entidad");
        codent = config.getProperty("tecnocom.codEntity");
      }
      boolean useMock = config.getPropertyBoolean("tecnocom.service.mock", false);
      if (useMock) {
        this.tecnocomService = new TecnocomServiceMockImpl(apiKey, apiUrl, channel, codent, HashOrder.ASC);
      } else {
        this.tecnocomService = new TecnocomServiceImpl(apiKey, apiUrl, channel, codent, HashOrder.ASC);
      }
    }
    return tecnocomService;
  }
}
