package cl.multicaja.prepaid.helpers.tecnocom;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.utils.EnvironmentUtil;
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

  private static final String TECNOCOM_API_KEY = "TECNOCOM_API_KEY";
  private static final String TECNOCOM_API_URL = "TECNOCOM_API_URL";
  private static final String TECNOCOM_CHANNEL = "TECNOCOM_CHANNEL";
  private static final String TECNOCOM_CODENT = "TECNOCOM_CODENT";
  private static final String TECNOCOM_ORDER = "TECNOCOM_ORDER";
  private static final String TECNOCOM_MOCK_IMPL = "TECNOCOM_MOCK_IMPL";
  private static final String TECNOCOM_MOCK_DATA_PATH = "TECNOCOM_MOCK_DATA_PATH";

  public static TecnocomServiceHelper getInstance() {
    if (instance == null) {
      instance = new TecnocomServiceHelper();
    }
    return instance;
  }

  private ConfigUtils configUtils;
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

  /**
   *
   * @return
   */
  public synchronized TecnocomService getTecnocomService() {
    if (this.tecnocomService == null) {
      ConfigUtils config = getConfigUtils();
      String apiKey = EnvironmentUtil.getVariable(TECNOCOM_API_KEY, () ->
        config.getProperty("tecnocom.apiKey"));
      String apiUrl = EnvironmentUtil.getVariable(TECNOCOM_API_URL, () ->
        config.getProperty("tecnocom.apiUrl"));
      String channel = EnvironmentUtil.getVariable(TECNOCOM_CHANNEL, () ->
        config.getProperty("tecnocom.channel"));
      String codent = EnvironmentUtil.getVariable(TECNOCOM_CODENT, () ->
        config.getProperty("tecnocom.codEntity"));
      String order = EnvironmentUtil.getVariable(TECNOCOM_ORDER, () ->
        config.getProperty("tecnocom.order"));
      String useMock = EnvironmentUtil.getVariable(TECNOCOM_MOCK_IMPL, () ->
        config.getProperty("tecnocom.service.mock"));

      HashOrder hashOrder = order.equals("ASC") ? HashOrder.ASC : HashOrder.DESC;

      if (Boolean.valueOf(useMock)) {
        String mockPath =  EnvironmentUtil.getVariable(TECNOCOM_MOCK_DATA_PATH, () ->
          ".");

        this.tecnocomService = new TecnocomServiceMockImpl(apiKey, apiUrl, channel, codent, hashOrder, mockPath);
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
    CodigoMoneda clamondiv = CodigoMoneda.UNK;
    String numreffac = prepaidMovement.getId().toString(); // Se hace internamente en Tecnocomç
    String numaut = TecnocomServiceHelper.getNumautFromIdMov(prepaidMovement.getId().toString());

    ZonedDateTime chileDt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("America/Santiago"));

    Date fecfac = format.parse(chileDt.format(dtf));

    if(nomcomred.length() > 27){
      nomcomred = nomcomred.substring(0, 27);
    }

    log.info(String.format("LLamando a inclusion de movimientos para carga de saldo a contrato %s", contrato));

    return this.getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
      numreffac, impfac, numaut, codcom,
      nomcomred, codact, clamondiv,impfac, fecfac);
  }


}
