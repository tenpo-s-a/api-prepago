package cl.multicaja.prepaid.helpers.users;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.core.utils.http.HttpUtils;
import cl.multicaja.core.utils.json.JsonMapper;
import cl.multicaja.prepaid.helpers.users.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

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

  private JsonMapper getJsonMapper(){
    if(jsonMapper == null) {
      jsonMapper = new JsonMapper() ;
    }
    return jsonMapper;
  }

  public static UserClient getInstance() {
    if(instance == null){
      instance = new UserClient();
    }
    return instance;
  }

  private ConfigUtils getConfigUtils(){
    if (configUtils == null) {
      configUtils = new ConfigUtils("api-users");
    }
    return configUtils;
  }

  private HttpResponse apiPOST(String api_route, Object request) {
    System.out.println("request: "+getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.POST,null,TIMEOUT,TIMEOUT,api_url+api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiGET(String api_route) {
    return httpUtils.execute(HttpUtils.ACTIONS.GET,null,TIMEOUT,TIMEOUT,api_url+api_route, null, DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiDELETE(String api_route, Object request) {
    System.out.println("request: "+getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.DELETE,null,TIMEOUT,TIMEOUT,api_url+api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }

  private HttpResponse apiPATH(String api_route, Object request) {
    System.out.println("request: "+getJsonMapper().toJson(request));
    return httpUtils.execute(HttpUtils.ACTIONS.PATCH,null,TIMEOUT,TIMEOUT,api_url+api_route, jsonMapper.toJson(request).getBytes(), DEFAULT_HTTP_HEADERS);
  }


  public User getUserByRut(Map<String, Object> headers, Integer rut) {
    log.info("******** getUserByRut IN ********");
    HttpResponse httpResponse =  apiGET(String.format("%s?rut=%d",UserPath.GET_USERS,rut));
    httpResponse.setJsonParser(getJsonMapper());
    User response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      response = new User();
      return response;
    }
    response = httpResponse.toObject(User.class);
    log.info("******** getUserByRut OUT ********");
    return response;
  }

  public User getUserByEmail(Map<String, Object> headers, String email) {
    log.info("******** getUserByEmail IN ********");
    HttpResponse httpResponse =  apiGET(String.format("%s?email=%s",UserPath.GET_USERS,email));
    httpResponse.setJsonParser(getJsonMapper());
    User response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      response = new User();
      return response;
    }
    response = httpResponse.toObject(User.class);
    log.info("******** getUserByEmail OUT ********");
    return response;
  }

  public User getUserById(Map<String, Object> headers, Long userIdMc) {
    log.info("******** getUserById IN ********");
    HttpResponse httpResponse =  apiGET(UserPath.GET_USER_BY_ID(userIdMc));
    httpResponse.setJsonParser(getJsonMapper());
    User response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      response = new User();
      return response;
    }
    response = httpResponse.toObject(User.class);
    log.info("********getUserById OUT ********");
    return response;
  }

  public SignUp signUp(Map<String, Object> headers, SignUPNew signUPNew) {
    log.info("******** signUp IN ********");
    HttpResponse httpResponse =  apiPOST(UserPath.SOFT_SIGNUP,signUPNew);
    httpResponse.setJsonParser(getJsonMapper());
    SignUp response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      response = new SignUp();
      return response;
    }
    response = httpResponse.toObject(SignUp.class);
    log.info("******** signUp OUT ********");
    return response;
  }

  public User finishSignup(Map<String, Object> headers, Long userIdMc) {
    log.info("******** signUp IN ********");
    HttpResponse httpResponse =  apiPOST(UserPath.FINISH_SIGNUP(userIdMc),null);
    httpResponse.setJsonParser(getJsonMapper());
    User response;
    if(HttpError.TIMEOUT_CONNECTION.equals(httpResponse.getHttpError()) || HttpError.TIMEOUT_RESPONSE.equals(httpResponse.getHttpError())){
      response = new User();
      return response;
    }
    response = httpResponse.toObject(User.class);
    log.info("******** signUp OUT ********");
    return response;
  }

  public void sendMail(Map<String,Object> headers,Long userId,EmailBody content) {
    log.info("******** signUp IN ********");

  }
  public void checkPassword(Map<String, Object> headers,Long userId, UserPasswordNew userPasswordNew) {

  }

  public User updateNameStatus(Map<String, Object> headers,Long userId, NameStatus nameStatus) {
    return null;
  }

  public User fillUser(Map<String,Object> headers,User user) {

    return null;
  }

  public UserFile createUserFile(Map<String,Object> headers, Long userId, UserFile newFile) {

    return null;
  }

  public UserFile getUserFileById(Map<String,Object> headers,Long userId, Long fileId) {

    return null;
  }
  public List<UserFile> getUserFiles(Map<String,Object> headers, Long userId, String app, String name, String version) {

    return null;
  }
  public User updateUser(Map<String,Object> headers, Long userId, User user) {
    return null;
  }
}
