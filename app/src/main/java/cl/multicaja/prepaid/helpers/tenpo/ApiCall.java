package cl.multicaja.prepaid.helpers.tenpo;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.core.utils.http.HttpUtils;
import cl.multicaja.core.utils.json.JsonMapper;
import cl.multicaja.prepaid.helpers.tenpo.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.util.*;
import java.util.concurrent.TimeoutException;

public class ApiCall {

  private static cl.multicaja.prepaid.helpers.users.UserClient instance;
  private static final Log LOG = LogFactory.getLog(cl.multicaja.prepaid.helpers.users.UserClient.class);
  private HttpUtils httpUtils = HttpUtils.getInstance();
  private JsonMapper jsonMapper;
  private ConfigUtils configUtils;
  private static final int TIMEOUT = 15000;
  private String apiUrl = this.getConfigUtils().getProperty("apis.user.url");

  private static final HttpHeader[] DEFAULT_HTTP_HEADERS = {
    new HttpHeader("Content-Type", "application/json"),
  };

  private JsonMapper getJsonMapper() {
    if(jsonMapper == null) {
      jsonMapper = new JsonMapper();
    }
    return jsonMapper;
  }

  public static cl.multicaja.prepaid.helpers.users.UserClient getInstance() {
    if(instance == null) {
      instance = new cl.multicaja.prepaid.helpers.users.UserClient();
    }
    return instance;
  }

  private ConfigUtils getConfigUtils() {
    if(configUtils == null) {
      configUtils = new ConfigUtils("api-prepaid");
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
    final String URI = "user";
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
        return null;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        vex.setStatus(422);
        LOG.error(vex);
        throw  vex;
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

