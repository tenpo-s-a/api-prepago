package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserIdentityStatus;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 */
public class Test_reverseWithdrawUserBalance_v10 extends TestBaseUnitApi {
  /**
   *
   * @param newPrepaidWithdraw10
   * @return
   */
  private HttpResponse reverseWithdrawUserBalance(String extUserId, NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/cash_out/reverse", extUserId), toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse reverseWithdrawUserBalanceOld(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal/reverse", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private void updateCreationDate(Long id) {
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.prp_movimiento set fecha_creacion = ((fecha_creacion - INTERVAL '1 DAY')) WHERE id = %s", getSchema(), id));
  }


  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), null);
    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: request", bex.getMessage().contains("withdrawRequest"));
  }

  @Test
  public void shouldReturn400_OnMissingAmount() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

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

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

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

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: amount.currency_code", bex.getMessage().contains("amount.currency_code"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(0);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setPassword("1235");

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

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
    prepaidWithdraw.setMerchantCode("1235");
    prepaidWithdraw.setPassword("1235");

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

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
    prepaidWithdraw.setMerchantCode("1235");
    prepaidWithdraw.setMerchantName("asdasdasd");
    prepaidWithdraw.setPassword("1235");

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

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
    prepaidWithdraw.setMerchantCode("1235");
    prepaidWithdraw.setMerchantName("asdasdasd");
    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setPassword("1235");

    HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    BadRequestException bex = resp.toObject(BadRequestException.class);
    Assert.assertNotNull("Deberia tener error", bex);
    Assert.assertEquals("Deberia tener error code = 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), bex.getCode());
    Assert.assertTrue("Debe tener error: transaction_id", bex.getMessage().contains("transaction_id"));
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {
    // POS
    {


      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidTopup);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(getRandomString(10), prepaidTopup);

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
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidWithdraw10 prepaidTopup = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn202_ReverseAlreadyReceived_POS() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    createPrepaidMovement10(prepaidMovement);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130003", REVERSA_RECIBIDA_PREVIAMENTE.getValue(), errorObj.get("code"));

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn202_ReverseAlreadyReceived_WEB() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    createPrepaidMovement10(prepaidMovement);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130003", REVERSA_RECIBIDA_PREVIAMENTE.getValue(), errorObj.get("code"));

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn202_OriginalWithdrawNull_POS() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130001", REVERSA_MOVIMIENTO_ORIGINAL_NO_RECIBIDO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn202_OriginalWithdrawNull_WEB() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130001", REVERSA_MOVIMIENTO_ORIGINAL_NO_RECIBIDO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn422_differentAmount_POS() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
    Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

    prepaidWithdraw.getAmount().setValue(prepaidWithdraw.getAmount().getValue().add(BigDecimal.TEN));

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130002", REVERSA_INFORMACION_NO_CONCUERDA.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

    Assert.assertNull("No debe tener movimiento de reversa", movement);
  }

  @Test
  public void shouldReturn422_differentAmount_WEB() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
    originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
    Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

    prepaidWithdraw.getAmount().setValue(prepaidWithdraw.getAmount().getValue().add(BigDecimal.TEN));

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130002", REVERSA_INFORMACION_NO_CONCUERDA.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

    Assert.assertNull("No debe tener movimiento de reversa", movement);
  }

  @Test
  public void shouldReturn410_OriginalWithdrawReverseTimeExpired() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
    Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

    this.updateCreationDate(originalWithdraw.getId());

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 410", 410, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130004", REVERSA_TIEMPO_EXPIRADO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

    Assert.assertNull("No debe tener movimientos de reversa", movement);

  }

  @Test
  public void shouldReturn410_OriginalWithdrawReverseTimeExpired_WEB() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
    originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
    Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

    this.updateCreationDate(originalWithdraw.getId());

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 410", 410, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130004", REVERSA_TIEMPO_EXPIRADO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

    Assert.assertNull("No debe tener movimientos de reversa", movement);
  }

  @Test
  public void shouldReturn201_ReverseAccepted_POS() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
    Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    // Se verifica que se tenga un registro de reversa

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

    Assert.assertNotNull("Debe tener movimientos de reversa", movement);
    Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
  }

  @Test
  public void shouldReturn201_ReverseAccepted_WEB() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidWithdraw.setPassword("1235");

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalWithdraw.setMonto(prepaidWithdraw.getAmount().getValue());
    originalWithdraw.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);


    Assert.assertNotNull("Debe tener id", originalWithdraw.getId());
    Assert.assertTrue("Debe tener id", originalWithdraw.getId() > 0);

    HttpResponse resp = reverseWithdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_RETIRO_TRANSFERENCIA, null, null);

    Assert.assertNotNull("Debe tener movimientos de reversa", movement);
    Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
  }

}
