package cl.multicaja.test.integration.v10.api;

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
public class Test_reverseTopupUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param newPrepaidTopup10
   * @return
   */
  private HttpResponse reverseTopupUserBalance(NewPrepaidTopup10 newPrepaidTopup10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup/reverse", toJson(newPrepaidTopup10), headers);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private void updateCreationDate(Long id) {
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.prp_movimiento set fecha_creacion = ((fecha_creacion - INTERVAL '1 DAY')) WHERE id = %s", getPrepaidEJBBean10().getSchema(), id));
  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = reverseTopupUserBalance(null);
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

    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_UserNull() throws Exception {
    // POS
    {
      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(null);
      prepaidTopup.setRut(Integer.MAX_VALUE);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_EXISTE.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(null);
      prepaidTopup.setRut(Integer.MAX_VALUE);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_CARGAR.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser(UserIdentityStatus.TERRORIST);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_CARGAR.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_UserDisabled() throws Exception {
    // POS
    {
      User user = registerUser(UserStatus.DISABLED);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser(UserStatus.DISABLED);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser(UserStatus.LOCKED);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser(UserStatus.DELETED);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser(UserStatus.PREREGISTERED);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

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
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
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

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());


      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
    }

  }

  @Test
  public void shouldReturn201_OriginalTopupNull() throws Exception {
    // POS
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
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

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa
      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
    }

  }

  @Test
  public void shouldReturn410_OriginalTopupReverseTimeExpired() throws Exception {
    // POS
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
      originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
      originalTopup.setMonto(prepaidTopup.getAmount().getValue());
      originalTopup = createPrepaidMovement10(originalTopup);

      Assert.assertNotNull("Debe tener id", originalTopup.getId());
      Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

      this.updateCreationDate(originalTopup.getId());

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 410", 410, resp.getStatus());

      // Se verifica que se tenga un registro de reversa

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA);

      Assert.assertNull("No debe tener movimientos de reversa", movement);
    }

    //WEB
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
      originalTopup.setMonto(prepaidTopup.getAmount().getValue());
      originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
      originalTopup = createPrepaidMovement10(originalTopup);

      Assert.assertNotNull("Debe tener id", originalTopup.getId());
      Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

      this.updateCreationDate(originalTopup.getId());

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 410", 410, resp.getStatus());

      // Se verifica que se tenga un registro de reversa
      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA);

      Assert.assertNull("No debe tener movimientos de reversa", movement);
    }

  }

  @Test
  public void shouldReturn201_ReverseAccepted() throws Exception {
    // POS
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
      originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
      originalTopup.setMonto(prepaidTopup.getAmount().getValue());
      originalTopup = createPrepaidMovement10(originalTopup);

      Assert.assertNotNull("Debe tener id", originalTopup.getId());
      Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa

      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA);

      Assert.assertNotNull("Debe tener movimientos de reversa", movement);
      Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
    }

    //WEB
    {
      User user = registerUser();

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
      originalTopup.setMonto(prepaidTopup.getAmount().getValue());
      originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
      originalTopup = createPrepaidMovement10(originalTopup);

      Assert.assertNotNull("Debe tener id", originalTopup.getId());
      Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

      HttpResponse resp = reverseTopupUserBalance(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      // Se verifica que se tenga un registro de reversa
      List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA);

      Assert.assertNotNull("Debe tener movimientos de reversa", movement);
      Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
    }

  }
}
