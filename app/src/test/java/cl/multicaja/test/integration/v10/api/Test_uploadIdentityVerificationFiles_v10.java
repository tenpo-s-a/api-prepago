package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 **/
public class Test_uploadIdentityVerificationFiles_v10 extends TestBaseUnitApi {

  //TODO: Esto se eliminara
  /**
   *
   * @param userIdMc
   * @param files
   * @return
   */
  private HttpResponse uploadFiles(Long userIdMc, Map<String, UserFile> files) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/identity/files", userIdMc), toJson(files));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private UserFile f = new UserFile();

  @Ignore
  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_user_not_found() throws Exception {

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    HttpResponse respHttp = uploadFiles(Long.MAX_VALUE, files);

    Assert.assertEquals("status 404", 404, respHttp.getStatus());
    NotFoundException vex = respHttp.toObject(NotFoundException.class);

    Assert.assertEquals("debe ser error de userMc null", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
  }

  @Ignore
  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_prepaid_user_not_found() throws Exception {

    User user = registerUser();

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    HttpResponse respHttp = uploadFiles(user.getId(), files);

    Assert.assertEquals("status 404", 404, respHttp.getStatus());
    NotFoundException vex = respHttp.toObject(NotFoundException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
  }

  @Ignore
  @Test
  public void uploadIdentityVerificationFiles_not_ok_by_prepaid_user_disabled() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    createPrepaidUser10(prepaidUser);

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    HttpResponse respHttp = uploadFiles(user.getId(), files);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Ignore
  @Test
  public void uploadIdentityVerificationFiles() throws Exception {
    User user = registerUser();
    user.setBirthday(LocalDate.now());
    user.setNameStatus(NameStatus.UNVERIFIED);
    user = updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    Map<String, UserFile> files = new HashMap<>();
    UserFile idFront = new UserFile();
    idFront.setMimeType("asdsadsad");
    idFront.setLocation("sadsadsa");
    files.put("USER_ID_FRONT", idFront);

    UserFile idBack= new UserFile();
    idBack.setMimeType("fasdas");
    idBack.setLocation("fasdasd");
    files.put("USER_ID_BACK", idBack);

    UserFile selfie = new UserFile();
    selfie.setMimeType("dgdsgdsg");
    selfie.setLocation("werewrewr");
    files.put("USER_SELFIE", selfie);

    HttpResponse respHttp = uploadFiles(user.getId(), files);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    Map<String, Object> u = respHttp.toObject(Map.class);
    Assert.assertEquals("Debe tener nameStatus IN_REVIEW", NameStatus.IN_REVIEW, NameStatus.valueOf(u.get("name_status").toString()));
  }

  @Ignore
  @Test
  public void uploadIdentityVerificationFiles_attempts() throws Exception {
    User user = registerUser();
    user.setBirthday(LocalDate.now());
    user.setNameStatus(NameStatus.UNVERIFIED);
    user = updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    Map<String, UserFile> files = new HashMap<>();
    UserFile idFront = new UserFile();
    idFront.setMimeType("asdsadsad");
    idFront.setLocation("sadsadsa");
    files.put("USER_ID_FRONT", idFront);

    UserFile idBack= new UserFile();
    idBack.setMimeType("fasdas");
    idBack.setLocation("fasdasd");
    files.put("USER_ID_BACK", idBack);

    UserFile selfie = new UserFile();
    selfie.setMimeType("dgdsgdsg");
    selfie.setLocation("werewrewr");
    files.put("USER_SELFIE", selfie);

    HttpResponse respHttp = uploadFiles(user.getId(), files);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    Map<String, Object> u = respHttp.toObject(Map.class);
    Assert.assertEquals("Debe tener nameStatus IN_REVIEW", NameStatus.IN_REVIEW, NameStatus.valueOf(u.get("name_status").toString()));

    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 1", Integer.valueOf(1), prepaidUser.getIdentityVerificationAttempts());

    // Intento 2
    respHttp = uploadFiles(user.getId(), files);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());
    prepaidUser = getPrepaidUserEJBBean10().getPrepaidUserById(null, prepaidUser.getId());

    Assert.assertEquals("Debe tener intento de validacion = 2", Integer.valueOf(2), prepaidUser.getIdentityVerificationAttempts());

  }

}
