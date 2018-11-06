package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserIdentityStatus;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_topupUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param newPrepaidTopup10
   * @return
   */
  private HttpResponse topupUserBalance(NewPrepaidTopup10 newPrepaidTopup10) {
    System.out.println(toJson(newPrepaidTopup10));
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup", toJson(newPrepaidTopup10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn200_OnTopupUserBalance() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
    Assert.assertNotNull("Deberia tener userId", topup.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(topup.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", topup.getStatus());
    Assert.assertNull("No deberia tener rut", topup.getRut());

    Assert.assertNotNull("Deberia tener el tipo de voucher", topup.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", topup.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", topup.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, topup.getMcVoucherData().size());

    Map<String, String> variableData = topup.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = topup.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));
  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = topupUserBalance(null);
    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingRut() {

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingTransactionId() {

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setRut(11111111);
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantCode() {

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmount() {

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(11111111);
    prepaidTopup.setMerchantCode("987654321");

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountCurrencyCode() {

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(11111111);
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountValue() {

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(11111111);
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnTopup_UserBlacklisted() throws Exception {
    // POS
    {
      User user = registerUser(UserIdentityStatus.TERRORIST);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", 102015, errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser(UserIdentityStatus.TERRORIST);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", 102015, errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_OnTopup_MinAmount() throws Exception {
    // POS
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108203", 108203, errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108203", 108203, errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_OnTopup_MaxAmount() throws Exception {
    // POS
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(100001));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108202", 108202, errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500001));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108201", 108201, errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_OnTopup_MonthlyAmount() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    for(int i = 0; i < 10; i++) {

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(100000));

      HttpResponse resp = topupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());
    }

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(100000));

    HttpResponse resp = topupUserBalance(prepaidTopup);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 108204", 108204, errorObj.get("code"));
  }

}
