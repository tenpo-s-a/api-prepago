package cl.multicaja.prepaid.helpers.fees;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.core.utils.http.HttpUtils;
import cl.multicaja.core.utils.json.JsonMapper;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.helpers.tenpo.TenpoApiCall;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.concurrent.TimeoutException;

public class FeeService {

  private static final Log LOG = LogFactory.getLog(FeeService.class);

  private static FeeService instance;
  private HttpUtils httpUtils;
  private JsonMapper jsonMapper;
  private ConfigUtils configUtils;
  private static final int TIMEOUT = 15000;
  private String apiUrl = this.getConfigUtils().getProperty("apis.fees.url");

  private static final HttpHeader[] DEFAULT_HTTP_HEADERS = {
    new HttpHeader("Content-Type", "application/json"),
  };

  private JsonMapper getJsonMapper() {
    if(jsonMapper == null) {
      jsonMapper = new JsonMapper();
    }
    return jsonMapper;
  }

  public static FeeService getInstance() {
    if(instance == null) {
      instance = new FeeService();
    }
    return instance;
  }

  private ConfigUtils getConfigUtils() {
    if(configUtils == null) {
      configUtils = new ConfigUtils("api-prepaid");
    }
    return configUtils;
  }

  public HttpUtils getHttpUtils() {
    if (httpUtils == null) {
      httpUtils = HttpUtils.getInstance();
    }
    return httpUtils;
  }

  public void setHttpUtils(HttpUtils httpUtils) {
    this.httpUtils = httpUtils;
  }

  private String getApiUrl() {
    return this.apiUrl;
  }

  public void setApiUrl(String url){
    this.apiUrl = url;
  }

  public Fee calculateFees(PrepaidMovementType prepaidMovementType, CodigoMoneda clamon, Long amountInCLP) throws TimeoutException, BaseException {
    LOG.info("******** calculateFees IN ********");

    // Convierte movementType a codigo del servicio
    Integer transactionType = getTransactionType(prepaidMovementType);

    // Prepara la ruta
    final String URL = String.format(getApiUrl(), transactionType.toString(), clamon.getValue().toString(), amountInCLP.toString());
    LOG.info("request route: " + URL);

    // Hace la llamada al servicio
    HttpResponse httpResponse = getHttpUtils().execute(HttpUtils.ACTIONS.GET, null, TIMEOUT, TIMEOUT, URL, null, DEFAULT_HTTP_HEADERS);
    httpResponse.setJsonParser(getJsonMapper());
    LOG.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new TimeoutException();
    }
    LOG.info(String.format("Status: %d", httpResponse.getStatus()));
    LOG.info(String.format("Response: %s", httpResponse.getResp()));

    switch (httpResponse.getStatus()) {
      case 200:
      case 201:
        LOG.info(String.format("******** %s OUT ********", "calculateFees"));
        return httpResponse.toObject(Fee.class);
      case 400:
        BadRequestException brex = httpResponse.toObject(BadRequestException.class);
        brex.setStatus(httpResponse.getStatus());
        LOG.error(brex);
        throw  brex;
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        nfe.setStatus(404);
        LOG.error(nfe);
        throw  nfe;
      case 422:
        return null;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        bex.setStatus(500);
        LOG.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  public Integer getTransactionType(PrepaidMovementType prepaidMovementType) {
    switch (prepaidMovementType) {
      case PURCHASE:
        return 1;
      case SUSCRIPTION:
        return 55;
      default:
        return -1;
    }
  }
}

