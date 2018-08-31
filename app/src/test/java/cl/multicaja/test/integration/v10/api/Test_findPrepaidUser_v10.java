package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class Test_findPrepaidUser_v10 extends TestBaseUnitApi {

  private HttpResponse findPrepaidUser(String rut) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid?rut=%s", rut));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn400_Rut0() {

    HttpResponse resp = findPrepaidUser("0");

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_RutEmpty() {

    HttpResponse resp = findPrepaidUser("");

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_UserMcNull() {

    HttpResponse resp = findPrepaidUser(getUniqueRutNumber().toString());

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", 102001, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_PrepaidUserNull() throws Exception {

    User user = registerUser();

    HttpResponse resp = findPrepaidUser(user.getRut().getValue().toString());

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn200_PrepaidUserActive() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = findPrepaidUser(user.getRut().getValue().toString());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidUser10 respUser = resp.toObject(PrepaidUser10.class);

    Assert.assertNotNull("debe tener usuario", respUser);
    Assert.assertEquals("debe tener el mismo id", prepaidUser.getId(), respUser.getId());
    Assert.assertEquals("debe tener el mismo user_id", prepaidUser.getUserIdMc(), respUser.getUserIdMc());
    Assert.assertEquals("debe tener el mismo rut", prepaidUser.getRut(), respUser.getRut());
    Assert.assertEquals("debe tener el mismo status", prepaidUser.getStatus(), respUser.getStatus());
    Assert.assertEquals("debe tener status = ACTIVE", PrepaidUserStatus.ACTIVE, respUser.getStatus());
    Timestamps timestamps = respUser.getTimestamps();
    Assert.assertNotNull("debe tener timestamps", timestamps);
    Assert.assertNotNull("debe tener timestamps.created_at", timestamps.getCreatedAt());
    Assert.assertNotNull("debe tener timestamps.updated_at", timestamps.getUpdatedAt());
    Assert.assertNotNull("debe tener user_level", respUser.getUserLevel());
    Assert.assertTrue("debe tener primer carga pentiente", respUser.isHasPendingFirstTopup());
  }

  @Test
  public void shouldReturn200_PrepaidUserDisabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = findPrepaidUser(user.getRut().getValue().toString());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidUser10 respUser = resp.toObject(PrepaidUser10.class);

    Assert.assertNotNull("debe tener usuario", respUser);
    Assert.assertEquals("debe tener el mismo id", prepaidUser.getId(), respUser.getId());
    Assert.assertEquals("debe tener el mismo user_id", prepaidUser.getUserIdMc(), respUser.getUserIdMc());
    Assert.assertEquals("debe tener el mismo rut", prepaidUser.getRut(), respUser.getRut());
    Assert.assertEquals("debe tener el mismo status", prepaidUser.getStatus(), respUser.getStatus());
    Assert.assertEquals("debe tener status = ACTIVE", PrepaidUserStatus.DISABLED, respUser.getStatus());
    Timestamps timestamps = respUser.getTimestamps();
    Assert.assertNotNull("debe tener timestamps", timestamps);
    Assert.assertNotNull("debe tener timestamps.created_at", timestamps.getCreatedAt());
    Assert.assertNotNull("debe tener timestamps.updated_at", timestamps.getUpdatedAt());
    Assert.assertNotNull("debe tener user_level", respUser.getUserLevel());
    Assert.assertTrue("debe tener primer carga pentiente", respUser.isHasPendingFirstTopup());
  }

  @Test
  public void shouldReturn200_PrepaidUserLevel1() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.UNVERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = findPrepaidUser(user.getRut().getValue().toString());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidUser10 respUser = resp.toObject(PrepaidUser10.class);

    Assert.assertNotNull("debe tener usuario", respUser);
    Assert.assertEquals("debe tener el mismo id", prepaidUser.getId(), respUser.getId());
    Assert.assertEquals("debe tener el mismo user_id", prepaidUser.getUserIdMc(), respUser.getUserIdMc());
    Assert.assertEquals("debe tener el mismo rut", prepaidUser.getRut(), respUser.getRut());
    Assert.assertEquals("debe tener el mismo status", prepaidUser.getStatus(), respUser.getStatus());
    Timestamps timestamps = respUser.getTimestamps();
    Assert.assertNotNull("debe tener timestamps", timestamps);
    Assert.assertNotNull("debe tener timestamps.created_at", timestamps.getCreatedAt());
    Assert.assertNotNull("debe tener timestamps.updated_at", timestamps.getUpdatedAt());
    Assert.assertNotNull("debe tener user_level", respUser.getUserLevel());
    Assert.assertEquals("debe tener user_level = LEVEL_1", PrepaidUserLevel.LEVEL_1, respUser.getUserLevel());
    Assert.assertTrue("debe tener primer carga pentiente", respUser.isHasPendingFirstTopup());
  }

  @Test
  public void shouldReturn200_PrepaidUserLevel2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = findPrepaidUser(user.getRut().getValue().toString());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidUser10 respUser = resp.toObject(PrepaidUser10.class);

    Assert.assertNotNull("debe tener usuario", respUser);
    Assert.assertEquals("debe tener el mismo id", prepaidUser.getId(), respUser.getId());
    Assert.assertEquals("debe tener el mismo user_id", prepaidUser.getUserIdMc(), respUser.getUserIdMc());
    Assert.assertEquals("debe tener el mismo rut", prepaidUser.getRut(), respUser.getRut());
    Assert.assertEquals("debe tener el mismo status", prepaidUser.getStatus(), respUser.getStatus());
    Timestamps timestamps = respUser.getTimestamps();
    Assert.assertNotNull("debe tener timestamps", timestamps);
    Assert.assertNotNull("debe tener timestamps.created_at", timestamps.getCreatedAt());
    Assert.assertNotNull("debe tener timestamps.updated_at", timestamps.getUpdatedAt());
    Assert.assertNotNull("debe tener user_level", respUser.getUserLevel());
    Assert.assertEquals("debe tener user_level = LEVEL_2", PrepaidUserLevel.LEVEL_2, respUser.getUserLevel());
    Assert.assertTrue("debe tener primer carga pentiente", respUser.isHasPendingFirstTopup());
  }
}
