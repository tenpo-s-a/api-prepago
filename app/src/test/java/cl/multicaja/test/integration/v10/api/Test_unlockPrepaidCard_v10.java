package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

public class Test_unlockPrepaidCard_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userIdMc
   * @return
   */
  private HttpResponse getPrepaidCard(Long userIdMc) {
    HttpResponse respHttp = apiGET(String.format("/1.0/prepaid/%s/card", userIdMc));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse unlockPrepaidCard(Long userIdMc) {
    HttpResponse respHttp = apiPUT(String.format("/1.0/prepaid/%s/card/unlock", userIdMc), null);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn200_PrepaidCardLocked() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(user.getId());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidCard10 card1 = resp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", PrepaidCardStatus.LOCKED, card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
    Assert.assertNull("no debe tener idUser", card1.getIdUser());
    Assert.assertNull("no debe tener encryptedPan", card1.getEncryptedPan());
    Assert.assertNull("no debe tener producto", card1.getProducto());
    Assert.assertNull("no debe tener numeroUnico", card1.getNumeroUnico());
    Timestamps timestamps = card1.getTimestamps();
    Assert.assertNotNull("deberia tener timestamps", timestamps);
    Assert.assertNotNull("deberia tener fecha de creacion", timestamps.getCreatedAt());
    Assert.assertNotNull("deberia tener fecha de actualizacion", timestamps.getUpdatedAt());

    HttpResponse lockResp = unlockPrepaidCard(user.getId());
    Assert.assertEquals("status 200", 200, lockResp.getStatus());
    card1 = lockResp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", PrepaidCardStatus.ACTIVE, card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
    Assert.assertNull("no debe tener idUser", card1.getIdUser());
    Assert.assertNull("no debe tener encryptedPan", card1.getEncryptedPan());
    Assert.assertNull("no debe tener producto", card1.getProducto());
    Assert.assertNull("no debe tener numeroUnico", card1.getNumeroUnico());
    timestamps = card1.getTimestamps();
    Assert.assertNotNull("deberia tener timestamps", timestamps);
    Assert.assertNotNull("deberia tener fecha de creacion", timestamps.getCreatedAt());
    Assert.assertNotNull("deberia tener fecha de actualizacion", timestamps.getUpdatedAt());

  }

  @Test
  public void shouldReturn200_PrepaidCardActive() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(user.getId());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidCard10 card1 = resp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", PrepaidCardStatus.ACTIVE, card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
    Assert.assertNull("no debe tener idUser", card1.getIdUser());
    Assert.assertNull("no debe tener encryptedPan", card1.getEncryptedPan());
    Assert.assertNull("no debe tener producto", card1.getProducto());
    Assert.assertNull("no debe tener numeroUnico", card1.getNumeroUnico());
    Timestamps timestamps = card1.getTimestamps();
    Assert.assertNotNull("deberia tener timestamps", timestamps);
    Assert.assertNotNull("deberia tener fecha de creacion", timestamps.getCreatedAt());
    Assert.assertNotNull("deberia tener fecha de actualizacion", timestamps.getUpdatedAt());

    HttpResponse lockResp = unlockPrepaidCard(user.getId());
    Assert.assertEquals("status 200", 200, lockResp.getStatus());
    card1 = lockResp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", PrepaidCardStatus.ACTIVE, card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
    Assert.assertNull("no debe tener idUser", card1.getIdUser());
    Assert.assertNull("no debe tener encryptedPan", card1.getEncryptedPan());
    Assert.assertNull("no debe tener producto", card1.getProducto());
    Assert.assertNull("no debe tener numeroUnico", card1.getNumeroUnico());
    timestamps = card1.getTimestamps();
    Assert.assertNotNull("deberia tener timestamps", timestamps);
    Assert.assertNotNull("deberia tener fecha de creacion", timestamps.getCreatedAt());
    Assert.assertNotNull("deberia tener fecha de actualizacion", timestamps.getUpdatedAt());

  }

  @Test
  public void shouldReturn404_McUserNull() {

    HttpResponse resp = unlockPrepaidCard(Long.MAX_VALUE);

    Assert.assertEquals("status 404", 404, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", 102001, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_McUserDisabled() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    updateUser(user);

    HttpResponse resp = unlockPrepaidCard(user.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_McUserLocked() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    updateUser(user);

    HttpResponse resp = unlockPrepaidCard(user.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_McUserDeleted() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    updateUser(user);

    HttpResponse resp = unlockPrepaidCard(user.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {

    User user = registerUser();

    HttpResponse resp = unlockPrepaidCard(user.getId());

    Assert.assertEquals("status 404", 404, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);
    prepaidUser10.setStatus(PrepaidUserStatus.DISABLED);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = unlockPrepaidCard(user.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_FirstTopupPending() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    HttpResponse resp = unlockPrepaidCard(user.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106007", 106007, errorObj.get("code"));
  }
}
