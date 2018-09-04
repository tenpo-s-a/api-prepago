package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.NewTermsAndConditions10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 **/
public class Test_acceptTermsAndConditions_v10 extends TestBaseUnitApi {

  private HttpResponse acceptTermsAndConditions(Long userIdMc, NewTermsAndConditions10 tac) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/signup/tac", userIdMc), toJson(tac));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn400_When_McUserIdNull() {
    HttpResponse resp = acceptTermsAndConditions(Long.valueOf(0), null);
    Assert.assertEquals("resp -> 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertEquals("userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn400_When_TermsAndConditionsNull() {

    HttpResponse resp = acceptTermsAndConditions(Long.MAX_VALUE, null);
    Assert.assertEquals("resp -> 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertEquals("tac null", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn400_When_TermsAndConditions_Version_Null() {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();

    HttpResponse resp = acceptTermsAndConditions(Long.MAX_VALUE, tac);
    Assert.assertEquals("resp -> 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertEquals("tac.version null", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn400_When_TermsAndConditions_Version_Empty() {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("");

    HttpResponse resp = acceptTermsAndConditions(Long.MAX_VALUE, tac);
    Assert.assertEquals("resp -> 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertEquals("tac.version empty", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn404_When_McUserNull() {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    HttpResponse resp = acceptTermsAndConditions(Long.MAX_VALUE, tac);
    Assert.assertEquals("resp -> 404", 404, resp.getStatus());

    NotFoundException bex = resp.toObject(NotFoundException.class);
    Assert.assertEquals("user null", CLIENTE_NO_EXISTE.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn422_When_McUserDisabled() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    updateUser(user);

    HttpResponse resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 422", 422, resp.getStatus());

    ValidationException bex = resp.toObject(ValidationException.class);
    Assert.assertEquals("user null", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn422_When_McUserLocked() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    updateUser(user);

    HttpResponse resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 422", 422, resp.getStatus());

    ValidationException bex = resp.toObject(ValidationException.class);
    Assert.assertEquals("user null", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn422_When_McUserDeleted() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    updateUser(user);

    HttpResponse resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 422", 422, resp.getStatus());

    ValidationException bex = resp.toObject(ValidationException.class);
    Assert.assertEquals("user null", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn422_When_VersionMismatch() throws Exception {

    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v0.9");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);

    HttpResponse resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 422", 422, resp.getStatus());

    ValidationException bex = resp.toObject(ValidationException.class);
    Assert.assertEquals("user null", VERSION_TERMINOS_Y_CONDICIONES_NO_COINCIDEN.getValue(), bex.getCode());
  }

  @Test
  public void shouldReturn200_AcceptTermsAndConditions() throws Exception {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);

    HttpResponse resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 200", 200, resp.getStatus());

    UserFile userFile = getUserClient().getUserFiles(null, user.getId(), "api-prepaid", "TERMS_AND_CONDITIONS", tac.getVersion(), null)
      .stream().findFirst()
      .get();

    Assert.assertNotNull("Debe tener un registro", userFile);
    Assert.assertEquals("Debe ser del usuario", user.getId(), userFile.getUserId());
    Assert.assertEquals("Debe ser de prepago", "api-prepaid", userFile.getApp());
    Assert.assertEquals("Debe ser TERMS_AND_CONDITIONS", "TERMS_AND_CONDITIONS", userFile.getName());
    Assert.assertEquals("Debe tener version v1.0", "v1.0", userFile.getVersion());
  }

  @Test
  public void shouldReturn200_When_TermsAndConditionsAlreadyAccepted() throws Exception {
    NewTermsAndConditions10 tac = new NewTermsAndConditions10();
    tac.setVersion("v1.0");
    tac.setBenefitsAccepted(Boolean.FALSE);

    User user = registerUser();
    updateUser(user);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);

    HttpResponse resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 200", 200, resp.getStatus());

    resp = acceptTermsAndConditions(user.getId(), tac);
    Assert.assertEquals("resp -> 200", 200, resp.getStatus());

    List<UserFile> files = getUserClient().getUserFiles(null, user.getId(), "api-prepaid", "TERMS_AND_CONDITIONS", tac.getVersion(), null);

    Assert.assertEquals("Debe tener solo 1", 1, files.size());
    UserFile userFile = files.get(0);
    Assert.assertNotNull("Debe tener un registro", userFile);
    Assert.assertEquals("Debe ser del usuario", user.getId(), userFile.getUserId());
    Assert.assertEquals("Debe ser de prepago", "api-prepaid", userFile.getApp());
    Assert.assertEquals("Debe ser TERMS_AND_CONDITIONS", "TERMS_AND_CONDITIONS", userFile.getName());
    Assert.assertEquals("Debe tener version v1.0", "v1.0", userFile.getVersion());
  }
}
