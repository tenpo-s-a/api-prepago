package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.users.model.v10.NameStatus;
import cl.multicaja.users.model.v10.Timestamps;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_getPrepaidUser_v10 extends TestBaseUnitApi{

  /**
   *
   * @param userIdMc
   * @return
   */
  private HttpResponse getPrepaidUser(Long userIdMc) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s", userIdMc));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn400_UserId0() {

    HttpResponse resp = getPrepaidUser(0L);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_UserMcNull() throws Exception {

    HttpResponse resp = getPrepaidUser(numberUtils.random(999L, 9999L) + numberUtils.random(999L, 9999L));

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", 102001, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_PrepaidUserNull() throws Exception {

    User user = registerUser();

    HttpResponse resp = getPrepaidUser(user.getId());

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

    HttpResponse resp = getPrepaidUser(user.getId());

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
  }

  @Test
  public void shouldReturn200_PrepaidUserDisabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = getPrepaidUser(user.getId());

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
  }

  @Test
  public void shouldReturn200_PrepaidUserLevel1() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.UNVERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = getPrepaidUser(user.getId());

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
  }

  @Test
  public void shouldReturn200_PrepaidUserLevel2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    HttpResponse resp = getPrepaidUser(user.getId());

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
  }

}
