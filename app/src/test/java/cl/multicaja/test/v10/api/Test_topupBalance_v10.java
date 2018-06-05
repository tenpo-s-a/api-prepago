package cl.multicaja.test.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.NewPrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_topupBalance_v10 extends TestBaseUnitApi {

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

  @Test
  public void shouldReturn422_OnMissingBody() {
    HttpResponse resp = apiPOST("/1.0/prepaid/topup", "{}");
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingRut() {

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setTransactionId("123456789");
    topupRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    topupRequest.setAmount(amount);

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    System.out.println(resp);
    System.out.println(resp.getResp());
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingTransactionId() {

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setRut(11111111);
    topupRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    topupRequest.setAmount(amount);

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingMerchantCode() {

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setTransactionId("123456789");
    topupRequest.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    topupRequest.setAmount(amount);

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingAmount() {

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setTransactionId("123456789");
    topupRequest.setRut(11111111);
    topupRequest.setMerchantCode("987654321");

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingAmountCurrencyCode() {

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setTransactionId("123456789");
    topupRequest.setRut(11111111);
    topupRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    topupRequest.setAmount(amount);

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnMissingAmountValue() {

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setTransactionId("123456789");
    topupRequest.setRut(11111111);
    topupRequest.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    topupRequest.setAmount(amount);

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
}
