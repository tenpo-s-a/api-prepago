package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_getPrepaidCard_v10 extends TestBaseUnitApi {

  private static final String URL_PATH = "/1.0/prepaid/%s/card";

  @Test
  public void shouldReturn200_PrepaidCardActive() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId()));

    System.out.println("RESP:::" + resp.toMap());

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

  }

  @Test
  public void shouldReturn200_PrepaidCardLocked() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId()));

    System.out.println("RESP:::" + resp.toMap());

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
  }

  @Test
  public void shouldReturn200_PrepaidCardLockedHard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId()));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidCard10 card1 = resp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", PrepaidCardStatus.LOCKED_HARD, card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
    Assert.assertNull("no debe tener idUser", card1.getIdUser());
    Assert.assertNull("no debe tener encryptedPan", card1.getEncryptedPan());
    Assert.assertNull("no debe tener producto", card1.getProducto());
    Assert.assertNull("no debe tener numeroUnico", card1.getNumeroUnico());
  }

  @Test
  public void shouldReturn200_PrepaidCardExpired() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId()));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 200", 200, resp.getStatus());

    PrepaidCard10 card1 = resp.toObject(PrepaidCard10.class);

    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getId(), card1.getId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getProcessorUserId(), card1.getProcessorUserId());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getPan(), card1.getPan());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getExpiration(), card1.getExpiration());
    Assert.assertEquals("debe ser la misma tarjeta", PrepaidCardStatus.EXPIRED, card1.getStatus());
    Assert.assertEquals("debe ser la misma tarjeta", prepaidCard10.getNameOnCard(), card1.getNameOnCard());
    Assert.assertNull("no debe tener idUser", card1.getIdUser());
    Assert.assertNull("no debe tener encryptedPan", card1.getEncryptedPan());
    Assert.assertNull("no debe tener producto", card1.getProducto());
    Assert.assertNull("no debe tener numeroUnico", card1.getNumeroUnico());
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId() + 1));

    System.out.println("RESP:::" + resp.toMap());

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

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId()));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_FirstTopupPending() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId() ));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106007", 106007, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_FirstTopupInProcess() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard10);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    HttpResponse resp = apiGET(String.format(URL_PATH, prepaidUser10.getId() ));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106008", 106008, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_Id0() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup, prepaidCard10);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    HttpResponse resp = apiGET(String.format(URL_PATH, 0));

    System.out.println("RESP:::" + resp.toMap());

    Assert.assertEquals("status 400", 400, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
}
