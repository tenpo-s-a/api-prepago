package cl.multicaja.test.api;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.NewPrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_topupBalance_v10 extends TestApiBase {

  @Ignore
  @Test
  public void shouldReturn200_OnTopupUserBalance() {

    final String transactionId = getUniqueInteger().toString();
    final Integer rut = getUniqueRutNumber();
    final String merchantCode = "987654321";
    final CodigoMoneda currencyCode = CodigoMoneda.CHILE_CLP;
    final BigDecimal value = new BigDecimal("9999.99");

    NewPrepaidTopup10 topupRequest = new NewPrepaidTopup10();
    topupRequest.setTransactionId(transactionId);
    topupRequest.setRut(rut);
    topupRequest.setMerchantCode(merchantCode);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(currencyCode);
    amount.setValue(value);
    topupRequest.setAmount(amount);

    String json = toJson(topupRequest);

    HttpResponse resp = apiPOST("/1.0/prepaid/topup", json);
    Assert.assertEquals("status 200", 200, resp.getStatus());
    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
    Assert.assertNotNull("Deberia tener userId", topup.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(topup.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", topup.getStatus());
    Assert.assertEquals(String.format("Deberia tener transactionId = %s", transactionId), transactionId, topup.getTransactionId());
    Assert.assertEquals(String.format("Deberia tener rut = %d", rut), rut, topup.getRut());
    Assert.assertEquals(String.format("Deberia tener merchantCode = %s", merchantCode), merchantCode, topup.getMerchantCode());
    Assert.assertNotNull("Deberia tener amount", topup.getAmount());
    Assert.assertEquals(String.format("Deberia tener amount.currencyCode = %d", currencyCode), currencyCode, topup.getAmount().getCurrencyCode());
    Assert.assertEquals(String.format("Deberia tener amount.value = %s", value.toString()), value, topup.getAmount().getValue());
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
