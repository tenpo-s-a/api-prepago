package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserIdentityStatus;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 */
public class Test_reverseWithdrawUserBalance_v10 extends TestBaseUnitApi {
  //TODO: Hacer test withdrawUserBalance fromEndPoint False
  /**
   *
   * @param newPrepaidWithdraw10
   * @return
   */
  private HttpResponse reverseWithdrawUserBalance(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal/reverse", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private void updateCreationDate(Long id) {
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.prp_movimiento set fecha_creacion = ((fecha_creacion - INTERVAL '1 DAY')) WHERE id = %s", getPrepaidEJBBean10().getSchema(), id));
  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = reverseWithdrawUserBalance(null);
    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: request", bex.getMessage().contains("withdrawRequest"));
  }

  @Test
  public void shouldReturn400_OnMissingAmount() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: amount", bex.getMessage().contains("amount"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountValue() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: amount.value", bex.getMessage().contains("amount.value"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountCurrencyCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: amount.currency_code", bex.getMessage().contains("amount.currency_code"));
  }

  @Test
  public void shouldReturn400_OnMissingRut() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: rut", bex.getMessage().contains("rut"));
  }

  @Test
  public void shouldReturn400_OnMissingPassword() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setRut(0);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: password", bex.getMessage().contains("password"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(0);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setPassword("1234");

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: merchant_code", bex.getMessage().contains("merchant_code"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantName() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(0);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setMerchantCode("1234");
    prepaidWithdraw.setPassword("1234");

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: merchant_name", bex.getMessage().contains("merchant_name"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantCategory() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(0);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setMerchantCode("1234");
    prepaidWithdraw.setMerchantName("asdasdasd");
    prepaidWithdraw.setPassword("1234");

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: merchant_category", bex.getMessage().contains("merchant_category"));
  }

  @Test
  public void shouldReturn400_OnMissingTransactionId() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(0);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setMerchantCode("1234");
    prepaidWithdraw.setMerchantName("asdasdasd");
    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setPassword("1234");

    HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: transaction_id", bex.getMessage().contains("transaction_id"));
  }

  @Test
  public void shouldReturn404_UserNull() throws Exception {
    // POS
    {
      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null, "1234", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.setRut(Integer.MAX_VALUE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_EXISTE.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.setRut(Integer.MAX_VALUE);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_EXISTE.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_UserBlacklisted() throws Exception {
    // POS
    {
      User user = registerUser(UserIdentityStatus.TERRORIST);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1356", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_RETIRAR.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      user.setIdentityStatus(UserIdentityStatus.TERRORIST);
      updateUser(user);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_RETIRAR.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_UserDisabled() throws Exception {
    // POS
    {
      User user = registerUser(UserStatus.DISABLED);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      user.setGlobalStatus(UserStatus.DISABLED);
      updateUser(user);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_UserLocked() throws Exception {
    // POS
    {
      User user = registerUser(UserStatus.LOCKED);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      user.setGlobalStatus(UserStatus.LOCKED);
      updateUser(user);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_UserDeleted() throws Exception {
    // POS
    {
      User user = registerUser(UserStatus.DELETED);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1239", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1298", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      user.setGlobalStatus(UserStatus.DELETED);
      user = updateUser(user);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_UserPreregistered() throws Exception {
    // POS
    {
      User user = registerUser(UserStatus.PREREGISTERED);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      user.setGlobalStatus(UserStatus.PREREGISTERED);
      updateUser(user);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {
    // POS
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidTopup);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidTopup);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {
    // POS
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdraw10(user, "2356", RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn201_ReverseAlreadyReceived() throws Exception {
    // POS
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
      createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
    }

    //WEB
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1234", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
      createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());


      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
    }

  }

  @Test
  public void shouldReturn201_OriginalWithdrawNull() throws Exception {
    // POS
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
    }

    //WEB
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa
      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
    }

  }

  @Test
  public void shouldReturn410_OriginalWithdrawReverseTimeExpired() throws Exception {
    // POS
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
      originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
      originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
      originalWithdraw = createPrepaidMovement10(originalWithdraw);

      Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
      Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

      this.updateCreationDate(originalWithdraw.getId());

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 410", 410, resp.getStatus());

      // Se verifica que se tenga un registro de reversa

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

      Assert.assertNull("No debe tener movimientos de reversa", movement);
    }

    //WEB
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
      originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
      originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
      originalWithdraw = createPrepaidMovement10(originalWithdraw);

      Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
      Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

      this.updateCreationDate(originalWithdraw.getId());

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 410", 410, resp.getStatus());

      // Se verifica que se tenga un registro de reversa
      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

      Assert.assertNull("No debe tener movimientos de reversa", movement);
    }

  }

  @Test
  public void shouldReturn201_ReverseAccepted() throws Exception {
    // POS
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1234", RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
      originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
      originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
      originalWithdraw = createPrepaidMovement10(originalWithdraw);

      Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
      Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

      Assert.assertNotNull("Debe tener movimientos de reversa", movement);
      Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
    }

    //WEB
    {
      User user = registerUser("1234");
      user = updateUserPassword(user, "1234");

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1234", NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidWithdraw.setPassword("1234");

      PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
      originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
      originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
      originalWithdraw = createPrepaidMovement10(originalWithdraw);


      Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
      Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

      HttpResponse resp = reverseWithdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa
      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

      Assert.assertNotNull("Debe tener movimientos de reversa", movement);
      Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
    }

  }
}
