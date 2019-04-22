package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_EXISTE;

/**
 * @author abarazarte
 */
public class Test_getPrepaidCard_v10 extends TestBaseUnitApi {

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

  //TODO: aca ya no existe get card
  @Ignore
  @Test
  public void shouldReturn200_PrepaidCardActive() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());


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
  }

  @Ignore
  @Test
  public void shouldReturn200_PrepaidCardLocked() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

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
  }

  @Ignore
  @Test
  public void shouldReturn200_PrepaidCardLockedHard() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

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
    Timestamps timestamps = card1.getTimestamps();
    Assert.assertNotNull("deberia tener timestamps", timestamps);
    Assert.assertNotNull("deberia tener fecha de creacion", timestamps.getCreatedAt());
    Assert.assertNotNull("deberia tener fecha de actualizacion", timestamps.getUpdatedAt());
  }

  @Ignore
  @Test
  public void shouldReturn200_PrepaidCardExpired() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

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
    Timestamps timestamps = card1.getTimestamps();
    Assert.assertNotNull("deberia tener timestamps", timestamps);
    Assert.assertNotNull("deberia tener fecha de creacion", timestamps.getCreatedAt());
    Assert.assertNotNull("deberia tener fecha de actualizacion", timestamps.getUpdatedAt());
  }

  @Ignore
  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {


    HttpResponse resp = getPrepaidCard(getUniqueLong());

    Assert.assertEquals("status 404", 404, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10.setStatus(PrepaidUserStatus.DISABLED);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_FirstTopupPending() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106007", 106007, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_FirstTopupInProcess_PrepaidCardPending() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106008", 106008, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_FirstTopupInProcess_MovementPending() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106008", 106008, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_FirstTopupInProcess_MovementInProcess() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
    prepaidMovement10.setEstado(PrepaidMovementStatus.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106008", 106008, errorObj.get("code"));
  }

  @Ignore
  @Test
  public void shouldReturn422_FirstTopupInProcess_MovementErrorAndPending() throws Exception {
    {
      PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
      prepaidUser10 = createPrepaidUserV2(prepaidUser10);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
      prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      prepaidTopup = buildPrepaidTopup10();
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
      prepaidMovement10.setEstado(PrepaidMovementStatus.PENDING);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 106008", 106008, errorObj.get("code"));
    }
    {
      PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
      prepaidUser10 = createPrepaidUserV2(prepaidUser10);


      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
      prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      prepaidTopup = buildPrepaidTopup10();
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
      prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      prepaidTopup = buildPrepaidTopup10();
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup);
      prepaidMovement10.setEstado(PrepaidMovementStatus.IN_PROCESS);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      HttpResponse resp = getPrepaidCard(prepaidUser10.getId());

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 106008", 106008, errorObj.get("code"));
    }
  }

  @Ignore
  @Test
  public void shouldReturn400_Id0() {

    HttpResponse resp = getPrepaidCard(0L);

    Assert.assertEquals("status 404", 404, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", CLIENTE_NO_EXISTE.getValue(), errorObj.get("code"));
  }
}
