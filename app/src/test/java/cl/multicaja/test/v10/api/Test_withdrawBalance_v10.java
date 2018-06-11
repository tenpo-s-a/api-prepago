package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_withdrawBalance_v10 extends TestBaseUnitApi {

  private static final String URL_PATH = "/1.0/prepaid/withdrawal";

  @Test
  public void shouldReturn200_OnPosWithdraw() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));

    String json = toJson(prepaidWithdraw);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 200", 200, resp.getStatus());
    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertTrue("Deberia tener el data", withdraw.getMcVoucherData().size() > 0);

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    List<PrepaidMovement10> dbPrepaidMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUser(prepaidUser.getId());
    Assert.assertEquals("Deberia tener un movimiento", 1, dbPrepaidMovements.size());
    PrepaidMovementStatus ok = PrepaidMovementStatus.PROCESS_OK;
    for(PrepaidMovement10 m : dbPrepaidMovements) {
      if(m.getIdMovimientoRef().equals(withdraw.getId()) && m.getIdTxExterno().equals(withdraw.getTransactionId())){
        Assert.assertEquals("Deberia estar en status " + ok, ok, m.getEstado());
      } else {
        Assert.assertTrue("Deberia ser false", Boolean.FALSE);
      }
    }
  }

  @Test
  public void shouldReturn200_OnWebWithdraw() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    String json = toJson(prepaidWithdraw);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 200", 200, resp.getStatus());
    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertTrue("Deberia tener el data", withdraw.getMcVoucherData().size() > 0);

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    List<PrepaidMovement10> dbPrepaidMovements = getPrepaidMovementEJBBean10().getPrepaidMovementByIdPrepaidUser(prepaidUser.getId());
    Assert.assertEquals("Deberia tener un movimiento", 1, dbPrepaidMovements.size());
    PrepaidMovementStatus ok = PrepaidMovementStatus.PROCESS_OK;
    for(PrepaidMovement10 m : dbPrepaidMovements) {
      if(m.getIdMovimientoRef().equals(withdraw.getId()) && m.getIdTxExterno().equals(withdraw.getTransactionId())){
        Assert.assertEquals("Deberia estar en status " + ok, ok, m.getEstado());
      } else {
        Assert.assertTrue("Deberia ser false", Boolean.FALSE);
      }
    }
  }

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

  @Test
  public void shouldReturn500_TecnocomError_UserDoesntExists() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10(prepaidUser));

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    String json = toJson(prepaidWithdraw);

    HttpResponse resp = apiPOST(URL_PATH, json);

    System.out.println("resp:: " + resp);

    Assert.assertEquals("status 500", 500, resp.getStatus());
  }
}
