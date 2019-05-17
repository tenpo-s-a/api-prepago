package cl.multicaja.prepaid.helpers.tenpo;


import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.core.utils.http.HttpUtils;
import cl.multicaja.core.utils.json.JsonMapper;
import cl.multicaja.prepaid.helpers.tenpo.model.User;
import cl.multicaja.prepaid.utils.EnvironmentUtil;

public class ApiCall {

  private static final Log LOG = LogFactory.getLog(ApiCall.class);
  private static final String TENPO_USER_API_URL = "TENPO_USER_API_URL";
  private static final int TIMEOUT = 15000;
  private static final HttpHeader[] DEFAULT_HTTP_HEADERS = {
    new HttpHeader("Content-Type", "application/json"),
  };

  private static ApiCall instance;
  private static JsonMapper jsonMapper;
  private static ConfigUtils configUtils;
  
  private HttpUtils httpUtils = HttpUtils.getInstance();
  private String apiUrl = EnvironmentUtil.getVariable(TENPO_USER_API_URL, () -> 
    this.getConfigUtils().getProperty("apis.user.url"));

  private ApiCall() {}

  private JsonMapper getJsonMapper() {
    if(jsonMapper == null) {
      synchronized(ApiCall.class) {
        if(jsonMapper == null) {
          jsonMapper = new JsonMapper();
        }
      }
    }
    return jsonMapper;
  }

  public static ApiCall getInstance() {
    if(instance == null) {
      synchronized(ApiCall.class) {
        if(instance == null) {
          instance = new ApiCall();
        }
      }
    }
    return instance;
  }

  private ConfigUtils getConfigUtils() {
    if(configUtils == null) {
      synchronized(ApiCall.class) {
        if(configUtils == null) {
          configUtils = new ConfigUtils("api-prepaid");
        }
      }
    }
    return configUtils;
  }

  private String getApiUrl() {
    return this.apiUrl;
  }

  public void setApiUrl(String url){
    this.apiUrl = url;
  }




  public User getUserById(UUID userId) throws TimeoutException, BaseException {
    final String URI = "users";
    final String URL = String.format("%s/%s/%s", getApiUrl(), URI, userId);
    LOG.info("request route: " + URL);
    LOG.info("******** getUserById IN ********");
    HttpResponse httpResponse = httpUtils.execute(HttpUtils.ACTIONS.GET, null, TIMEOUT, TIMEOUT, URL, null, DEFAULT_HTTP_HEADERS);
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
        LOG.info(String.format("******** %s OUT ********", "getUserById"));
        return httpResponse.toObject(User.class);
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

}

