package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_withdrawBalance_v10 extends TestBaseUnitApi {

  private static final String URL_PATH = "/1.0/prepaid/withdrawal";
  /*
  @Test
  public void shouldReturn200_OnTopupUserBalance() throws Exception {

    User user = registerUser();

    System.out.println(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    System.out.println(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);

    String json = toJson(prepaidTopup);

    System.out.println(json);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 200", 200, resp.getStatus());
    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
    Assert.assertNotNull("Deberia tener userId", topup.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(topup.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", topup.getStatus());
  }
  */

  @Test
  public void shouldReturn422_OnMissingBody() {
    HttpResponse resp = apiPOST(URL_PATH, "{}");
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingRut() {

    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    withdrawRequest.setTransactionId("123456789");
    withdrawRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    withdrawRequest.setAmount(amount);

    String json = toJson(withdrawRequest);

    HttpResponse resp = apiPOST(URL_PATH, json);
    System.out.println(resp);
    System.out.println(resp.getResp());
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingTransactionId() {

    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    withdrawRequest.setRut(11111111);
    withdrawRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    withdrawRequest.setAmount(amount);

    String json = toJson(withdrawRequest);

    HttpResponse resp = apiPOST(URL_PATH, json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingMerchantCode() {

    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    withdrawRequest.setTransactionId("123456789");
    withdrawRequest.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    withdrawRequest.setAmount(amount);

    String json = toJson(withdrawRequest);

    HttpResponse resp = apiPOST(URL_PATH, json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingAmount() {

    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    withdrawRequest.setTransactionId("123456789");
    withdrawRequest.setRut(11111111);
    withdrawRequest.setMerchantCode("987654321");

    String json = toJson(withdrawRequest);

    HttpResponse resp = apiPOST(URL_PATH, json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingAmountCurrencyCode() {

    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    withdrawRequest.setTransactionId("123456789");
    withdrawRequest.setRut(11111111);
    withdrawRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    withdrawRequest.setAmount(amount);

    String json = toJson(withdrawRequest);

    HttpResponse resp = apiPOST(URL_PATH, json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingAmountValue() {

    NewPrepaidWithdraw10 withdrawRequest = new NewPrepaidWithdraw10();
    withdrawRequest.setTransactionId("123456789");
    withdrawRequest.setRut(11111111);
    withdrawRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    withdrawRequest.setAmount(amount);

    String json = toJson(withdrawRequest);

    HttpResponse resp = apiPOST(URL_PATH, json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_McUserNull() {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null);
    prepaidWithdraw.setRut(getUniqueRutNumber());

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", 102001, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_McUserDeleted() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    user = updateUser(user);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_McUserLocked() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    user = updateUser(user);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_McUserDisabled() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    user = updateUser(user);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {

    User user = registerUser();
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser.setStatus(PrepaidUserStatus.DISABLED);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardNull() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardPending() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardExpired() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardHardLocked() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    System.out.println(json);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

}
