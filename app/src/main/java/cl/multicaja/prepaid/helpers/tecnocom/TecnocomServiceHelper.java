package cl.multicaja.prepaid.helpers.tecnocom;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.TecnocomServiceImpl;
import cl.multicaja.tecnocom.TecnocomServiceMockImpl;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.HashOrder;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
      String order = config.getProperty("tecnocom.order");

      HashOrder hashOrder = order.equals("ASC") ? HashOrder.ASC : HashOrder.DESC;
      log.info(hashOrder);
      try {
        codent = getParametersUtil().getString("api-prepaid", "cod_entidad", "v10");
      } catch (Exception e) {
        log.error("Error al cargar parametro cod_entidad");
        codent = config.getProperty("tecnocom.codEntity");
      }
      boolean useMock = config.getPropertyBoolean("tecnocom.service.mock", false);
      if (useMock) {
        this.tecnocomService = new TecnocomServiceMockImpl(apiKey, apiUrl, channel, codent, hashOrder);
      } else {
        this.tecnocomService = new TecnocomServiceImpl(apiKey, apiUrl, channel, codent, hashOrder);
      }
    }
    return tecnocomService;
  }

  public static String getNumautFromIdMov(String numreffac) {
    String numaut = StringUtils.leftPad(numreffac, 6, "0");
    numaut = numaut.substring(numaut.length() - 6);
    return numaut;
  }

  public InclusionMovimientosDTO issuanceFee(String contrato, String pan, String nomcomred, PrepaidMovement10 prepaidMovement) throws Exception {
    log.info(String.format("Issuance fee user -> [%s]", contrato));
    return includeMovement(contrato, pan, nomcomred, prepaidMovement);
  }

  public InclusionMovimientosDTO topup(String contrato, String pan, String nomcomred, PrepaidMovement10 prepaidMovement) throws Exception {
    log.info(String.format("Topup balance user -> [%s]", contrato));
    return includeMovement(contrato, pan, nomcomred, prepaidMovement);
  }

  public InclusionMovimientosDTO withdraw(String contrato, String pan, String nomcomred, PrepaidMovement10 prepaidMovement) throws Exception {
    log.info(String.format("Withdraw balance user -> [%s]", contrato));
    return includeMovement(contrato, pan, nomcomred, prepaidMovement);
  }

  public InclusionMovimientosDTO reverse(String contrato, String pan, String nomcomred, PrepaidMovement10 prepaidMovement) throws Exception {
    log.info(String.format("Reverse balance user -> [%s]", contrato));
    return includeMovement(contrato, pan, nomcomred, prepaidMovement);
  }

  private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private InclusionMovimientosDTO includeMovement(String contrato, String pan, String nomcomred, PrepaidMovement10 prepaidMovement) throws Exception {
    CodigoMoneda clamon = prepaidMovement.getClamon();
    IndicadorNormalCorrector indnorcor = prepaidMovement.getIndnorcor();
    TipoFactura tipofac = prepaidMovement.getTipofac();
    BigDecimal impfac = prepaidMovement.getImpfac();
    String codcom = prepaidMovement.getCodcom();
    Integer codact = prepaidMovement.getCodact();
    CodigoMoneda clamondiv = CodigoMoneda.NONE;
    String numreffac = prepaidMovement.getId().toString(); // Se hace internamente en Tecnocomç
    String numaut = TecnocomServiceHelper.getNumautFromIdMov(prepaidMovement.getId().toString());

    ZonedDateTime chileDt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("America/Santiago"));

    Date fecfac = format.parse(chileDt.format(dtf));

    log.info(String.format("LLamando a inclusion de movimientos para carga de saldo a contrato %s", contrato));

    return this.getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
      numreffac, impfac, numaut, codcom,
      nomcomred, codact, clamondiv,impfac, fecfac);
  }


}
