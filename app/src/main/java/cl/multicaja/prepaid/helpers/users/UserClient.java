package cl.multicaja.prepaid.helpers.users;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.accounting.model.v10.UserAccountNew;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.core.utils.http.HttpUtils;
import cl.multicaja.core.utils.json.JsonMapper;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.NewTicket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.Ticket;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.accounting.model.v10.UserAccountNew;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class UserClient {

  private static UserClient instance;
  private Log log = LogFactory.getLog(UserClient.class);
  private HttpUtils httpUtils = HttpUtils.getInstance();
  private JsonMapper jsonMapper;
  private ConfigUtils configUtils;
  private int TIMEOUT = 15000;
  private String api_url;

  private final static HttpHeader[] DEFAULT_HTTP_HEADERS = {
    new HttpHeader("Content-Type", "application/json"),
  };

  private JsonMapper getJsonMapper() {
    if(jsonMapper == null) {
      jsonMapper = new JsonMapper();
    }
    return jsonMapper;
  }

  public static UserClient getInstance() {
    if(instance == null) {
      instance = new UserClient();
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
    return this.getConfigUtils().getProperty("apis.user.url");
  }

  private String getTestApiUrl() {
    return this.getConfigUtils().getProperty("apis.user_test.url");
  }

  private HttpResponse apiPOST(String api_route, Object request) {
    log.info("request route: " + api_route);
    log.info("request: " + getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.POST, null, TIMEOUT, TIMEOUT, api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiGET(String api_route) {
    log.info("request route: " + api_route);
    return httpUtils.execute(HttpUtils.ACTIONS.GET, null, TIMEOUT, TIMEOUT, api_route, null, DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiDELETE(String api_route, Object request) {
    log.info("request route: " + api_route);
    log.info("request: " + getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.DELETE, null, TIMEOUT, TIMEOUT, api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiPUT(String api_route, Object request) {
    log.info("request route: " + api_route);
    log.info("request: " + getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.PUT, null, TIMEOUT, TIMEOUT, api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiPATCH(String api_route, Object request) {
    log.info("request route: " + api_route);
    log.info("request: " + getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.PATCH, null, TIMEOUT, TIMEOUT, api_url + api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }

  /**
   * USERS
   */

  public User getUserByRut(Map<String, Object> headers, Integer rut) throws Exception {
    log.info("******** getUserByRut IN ********");
    HttpResponse httpResponse = apiGET(String.format("%s?rut=%d", getApiUrl(), rut));
    log.info("response: " + httpResponse.getResp());
    httpResponse.setJsonParser(getJsonMapper());
    User[] response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }

    int status = httpResponse.getStatus();
    switch (status) {
      case 200:
      case 201:
        response = httpResponse.toObject(User[].class);
        log.info("******** getUserByRuts OUT ********");
        return response != null ? response[0] : null;
      case 400:
        BadRequestException brex = httpResponse.toObject(BadRequestException.class);
        brex.setStatus(status);
        log.error(brex);
        throw brex;
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        nfe.setStatus(status);
        log.error(nfe);
        return null;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        vex.setStatus(status);
        log.error(vex);
        throw vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        bex.setStatus(status);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  public User getUserByEmail(Map<String, Object> headers, String email) throws Exception {
    log.info("******** getUserByEmail IN ********");
    HttpResponse httpResponse = apiGET(String.format("%s?email=%s", getApiUrl(), email));
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());
    User[] response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }

    int status = httpResponse.getStatus();
    switch (status) {
      case 200:
      case 201:
        response = httpResponse.toObject(User[].class);
        log.info("******** getUserByEmail OUT ********");
        return response != null ? response[0] : null;
      case 400:
        BadRequestException brex = httpResponse.toObject(BadRequestException.class);
        brex.setStatus(status);
        log.error(brex);
        throw brex;
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        nfe.setStatus(status);
        log.error(nfe);
        return null;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        vex.setStatus(status);
        log.error(vex);
        throw vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        bex.setStatus(status);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  public User getUserById(Map<String, Object> headers, Long userIdMc) throws Exception {
    log.info("******** getUserById IN ********");
    HttpResponse httpResponse = apiGET(String.format("%s/%d", getApiUrl(), userIdMc));
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());
    System.out.println(httpResponse.getResp());
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("getUserById", httpResponse, User.class);
  }

  public SignUp signUp(Map<String, Object> headers, SignUPNew signUPNew) throws Exception {
    log.info("******** signUp IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/soft_signup", getApiUrl()), signUPNew);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("signUp", httpResponse, SignUp.class);
  }

  public User finishSignup(Map<String, Object> headers, Long userIdMc, String product) throws Exception {
    log.info("******** finishSignup IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/%s/finish_signup", getApiUrl(), userIdMc), product);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());
    User response;
    if (HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      response = new User();
      return response;
    }

    return this.processResponse("finishSignup", httpResponse, User.class);
  }

  public void sendMail(Map<String, Object> headers, Long userId, EmailBody content) throws Exception {
    log.info("******** sendMail IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/%s/mail", getApiUrl(), userId), content);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }

    this.processResponse("sendMail", httpResponse, Map.class);
  }

  public void sendInternalMail(Map<String, Object> headers, EmailBody content) throws Exception {
    log.info("******** sendInternalMail IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/mail", getApiUrl()), content);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }

    this.processResponse("sendInternalMail", httpResponse, Map.class);
  }

  public void checkPassword(Map<String, Object> headers, Long userId, UserPasswordNew userPasswordNew) throws Exception {
    log.info("******** checkPassword IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/%s/check_password", getApiUrl(), userId), userPasswordNew);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());


    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }
    this.processResponse("checkPassword", httpResponse, Boolean.class);
  }

  public User initIdentityValidation(Map<String, Object> headers, Long userId) throws Exception {
    log.info("******** initIdentityValidation IN ********");
    HttpResponse httpResponse = apiPUT(String.format("%s/%s/init_identity_validation", getApiUrl(), userId), "{}");
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());


    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }
    return this.processResponse("initIdentityValidation", httpResponse, User.class);
  }

  public User finishIdentityValidation(Map<String, Object> headers, Long userId, Boolean success, Boolean isBlacklisted) throws Exception {
    log.info("******** initIdentityValidation IN ********");

    StringBuilder url = new StringBuilder(String.format("%s/%s/finish_identity_validation?", getApiUrl(), userId));
    if(success) {
      url.append("success=true&");
    }
    if(isBlacklisted) {
      url.append("blacklisted=true&");
    }

    HttpResponse httpResponse = apiPUT(url.toString(), "{}");
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());


    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }
    return this.processResponse("initIdentityValidation", httpResponse, User.class);
  }

  public User resetIdentityValidation(Map<String, Object> headers, Long userId) throws Exception {
    log.info("******** initIdentityValidation IN ********");
    HttpResponse httpResponse = apiPUT(String.format("%s/%s/reset_identity_validation", getApiUrl(), userId), "{}");
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());


    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }
    return this.processResponse("initIdentityValidation", httpResponse, User.class);
  }

  public User updatePersonalData(Map<String, Object> headers, Long userId, String name, String lastname) throws Exception {
    log.info("******** updatePersonalData IN ********");

    Map<String, String> data = new HashMap<>();
    data.put("name", name);
    data.put("lastname_1", lastname);

    HttpResponse httpResponse = apiPOST(String.format("%s/%s/update_personal_data", getApiUrl(), userId), data);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      throw new Exception("Error timeout");
    }
    return this.processResponse("updatePersonalData", httpResponse, User.class);
  }

  /**
   * FILES
   */

  public UserFile createUserFile(Map<String, Object> headers, Long userIdMc, UserFile newFile) throws Exception {
    log.info("******** createUserFile IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/%d/files", getApiUrl(), userIdMc), newFile);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("createUserFile", httpResponse, UserFile.class);
  }

  public UserFile getUserFileById(Map<String, Object> headers, Long userIdMc, Long fileId) throws Exception {
    log.info("******** getUserFileById IN ********");
    HttpResponse httpResponse = apiGET(String.format("%s/%d/files/%d", getApiUrl(), userIdMc, fileId));
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("getUserFileById", httpResponse, UserFile.class);
  }

  public List<UserFile> getUserFiles(Map<String, Object> headers, Long userId, String app, String name, String version, UserFileStatus fileStatus) throws Exception {
    log.info("******** getUserFiles IN ********");
    StringBuilder filter = new StringBuilder("");
    if(!StringUtils.isAllBlank(app)) {
      filter.append(String.format("app=%s&", app));
    }
    if(!StringUtils.isAllBlank(name)) {
      filter.append(String.format("name=%s&", name));
    }
    if(!StringUtils.isAllBlank(version)) {
      filter.append(String.format("version=%s&", version));
    }
    if(fileStatus != null) {
      filter.append(String.format("status=%s&", fileStatus.toString()));
    }

    HttpResponse httpResponse = apiGET(String.format("%s/%d/files?%s", getApiUrl(), userId, filter.toString()));
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());
    UserFile[] response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }

    int status = httpResponse.getStatus();
    switch (status) {
      case 200:
      case 201:
        response = httpResponse.toObject(UserFile[].class);
        log.info("******** getUserFiles OUT ********");
        return response != null ? Arrays.asList(response) : Collections.EMPTY_LIST;
      case 400:
        BadRequestException brex = httpResponse.toObject(BadRequestException.class);
        brex.setStatus(status);
        log.error(brex);
        throw brex;
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        nfe.setStatus(status);
        log.error(nfe);
        return null;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        vex.setStatus(status);
        log.error(vex);
        throw vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        bex.setStatus(status);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  /**
   * BACKOFFICE
   */
  public Ticket createFreshdeskTicket(Map<String, Object> headers, Long userIdMc, NewTicket newTicket) throws Exception {
    log.info("******** createFreshdeskTicket IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/%d/backoffice/ticket", getApiUrl(), userIdMc), newTicket);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("createFreshdeskTicket", httpResponse, Ticket.class);
  }

  /**
   * BANK ACCOUNTS
   */
  public UserAccount getUserBankAccountById(Map<String, Object> headers, Long userIdMc, Long accountId) throws Exception {
    log.info("******** getUserAccountById IN ********");
    HttpResponse httpResponse = apiGET(String.format("%s/%d/bank_accounts/%d", getApiUrl(), userIdMc, accountId));
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("getUserAccountById", httpResponse, UserAccount.class);
  }

  public UserAccount createUserBankAccount(Map<String, Object> headers, Long userIdMc, UserAccountNew newAccount) throws Exception {
    log.info("******** createUserAccount IN ********");
    HttpResponse httpResponse = apiPOST(String.format("%s/%d/bank_accounts", getApiUrl(), userIdMc), newAccount);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("createUserBankAccount", httpResponse, UserAccount.class);
  }

  public List<UserAccount> getUserBankAccounts(Map<String, Object> headers, Long userId) throws Exception {

    log.info("******** getUserBankAccounts IN ********");
    HttpResponse httpResponse = apiGET(String.format("%s/%d/bank_accounts", getApiUrl(), userId));
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());
    UserAccount[] response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }

    int status = httpResponse.getStatus();
    switch (status) {
      case 200:
      case 201:
        response = httpResponse.toObject(UserAccount[].class);
        log.info("******** getUserBankAccounts OUT ********");
        return response != null ? Arrays.asList(response) : Collections.EMPTY_LIST;
      case 400:
        BadRequestException brex = httpResponse.toObject(BadRequestException.class);
        brex.setStatus(status);
        log.error(brex);
        throw  brex;
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        nfe.setStatus(status);
        log.error(nfe);
        return null;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        vex.setStatus(status);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        bex.setStatus(status);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  public UserAccount deleteUserBankAccount(Map<String, Object> headers, Long userIdMc, UserAccount oldAccount) throws Exception {
    log.info("******** deleteUserAccount IN ********");
    HttpResponse httpResponse = apiDELETE(String.format("%s/%d/files/%sd", getApiUrl(), userIdMc, oldAccount.getBankId()), null);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())) {
      return null;
    }
    return this.processResponse("deleteUserAccount", httpResponse, UserAccount.class);
  }

  /**
   *  TEST HELPERS
   */
  private void validate() {
    if(ConfigUtils.isEnvProduction()) {
      throw new SecurityException("Este método no puede ser ejecutado en un ambiente de producción");
    }
  }

  //usersReset
  public void resetUsers(Map<String, Object> headers, Map<String, Object> body) throws Exception {
    log.info("******** resetUsers IN ********");
    validate();

    HttpResponse httpResponse =  apiPOST("/users/reset", body);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }
    if(httpResponse.getStatus() != 200) {
      BaseException bex = httpResponse.toObject(BaseException.class);
      log.error(bex);
      throw bex;
    }
    log.info("******** resetUsers OUT ********");
  }

  //createUser
  public User createUser(Map<String,Object> headers, User user) throws Exception {
    log.info("******** createUser IN ********");

    validate();

    HttpResponse httpResponse =  apiPOST(String.format("%s/users", getTestApiUrl()), user);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }

    switch (httpResponse.getStatus()) {
      case 200:
        User u = httpResponse.toObject(User.class);
        log.info("******** createUser OUT ********");
        return u;
      case 400:
        BadRequestException brex = httpResponse.toObject(BadRequestException.class);
        log.error(brex);
        throw  brex;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  //fillUser
  public User fillUser(Map<String,Object> headers,User user) throws Exception {
    log.info("******** fillUser IN ********");
    validate();

    HttpResponse httpResponse =  apiPUT(String.format("%s/users/fill", getTestApiUrl()), user);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }

    switch (httpResponse.getStatus()) {
      case 200:
        User u = httpResponse.toObject(User.class);
        log.info("******** fillUser OUT ********");
        return u;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  //updateUser
  public User updateUser(Map<String,Object> headers, Long userId, User user) throws Exception {
    log.info("******** updateUser IN ********");
    validate();

    HttpResponse httpResponse =  apiPUT(String.format("%s/user/%d", getTestApiUrl(), userId), user);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }

    switch (httpResponse.getStatus()) {
      case 200:
        User u = httpResponse.toObject(User.class);
        log.info("******** updateUser OUT ********");
        return u;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  //updateUserPassword
  public User updateUserPassword(Map<String,Object> headers, Long userId, UserPasswordNew password) throws Exception {
    log.info("******** updateUserPassword IN ********");
    validate();

    HttpResponse httpResponse =  apiPUT(String.format("%s/user/%d/password", getTestApiUrl(), userId), password);
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }

    return this.processResponse("updateUserPassword", httpResponse, User.class);
  }

  //getEmailCode
  public String getEmailCode(Map<String,Object> headers, Long userId) throws Exception {
    log.info("******** getEmailCode IN ********");
    validate();

    HttpResponse httpResponse =  apiGET("/user/{userId}/email_code");
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }

    switch (httpResponse.getStatus()) {
      case 200:
        Map<String, Object> map = httpResponse.toMap();
        log.info("******** getEmailCode OUT ********");
        return map.get("code").toString();
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        log.error(nfe);
        throw  nfe;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  //getSmsCode
  public String getSmsCode(Map<String,Object> headers, Long userId) throws Exception {
    log.info("******** getSmsCode IN ********");
    validate();

    HttpResponse httpResponse =  apiGET("/user/{userId}/sms_code");
    httpResponse.setJsonParser(getJsonMapper());
    log.info("response: " + httpResponse.getResp());

    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      throw new Exception("Error de conexion");
    }

    switch (httpResponse.getStatus()) {
      case 200:
        Map<String, Object> map = httpResponse.toMap();
        log.info("******** getSmsCode OUT ********");
        return map.get("code").toString();
      case 404:
        NotFoundException nfe = httpResponse.toObject(NotFoundException.class);
        log.error(nfe);
        throw  nfe;
      case 422:
        ValidationException vex = httpResponse.toObject(ValidationException.class);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = httpResponse.toObject(BaseException.class);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }

  private <T>T processResponse(String method, HttpResponse response, Class<T> clazz) throws Exception {

    int status = response.getStatus();
    log.info(String.format("Status: %d", status));
    log.info(String.format("Response: %s", response.getResp()));
    switch (status) {
      case 200:
      case 201:
        T u = response.toObject(clazz);
        log.info(String.format("******** %s OUT ********", method));
        return u;
      case 400:
        BadRequestException brex = response.toObject(BadRequestException.class);
        brex.setStatus(status);
        log.error(brex);
        throw  brex;
      case 404:
        NotFoundException nfe = response.toObject(NotFoundException.class);
        nfe.setStatus(status);
        log.error(nfe);
        throw  nfe;
      case 422:
        ValidationException vex = response.toObject(ValidationException.class);
        vex.setStatus(status);
        log.error(vex);
        throw  vex;
      case 500:
        BaseException bex = response.toObject(BaseException.class);
        bex.setStatus(status);
        log.error(bex);
        throw bex;
      default:
        throw new IllegalStateException();
    }
  }
}

