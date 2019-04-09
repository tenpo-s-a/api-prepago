package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.http.HttpHeader;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
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
  private HttpResponse reverseTopupUserBalance(String externalUserId, NewPrepaidTopup10 newPrepaidTopup10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/cash_in/reverse",externalUserId), toJson(newPrepaidTopup10), headers);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse reverseTopupUserBalanceTmp(NewPrepaidTopup10 newPrepaidTopup10) {
    HttpHeader[] headers = new HttpHeader[]{
      new HttpHeader("Content-Type", "application/json"),
      new HttpHeader(Constants.HEADER_USER_TIMEZONE, "America/Santiago")
    };
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup/reverse", toJson(newPrepaidTopup10), headers);
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private void updateCreationDate(Long id) {
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.prp_movimiento set fecha_creacion = ((fecha_creacion - INTERVAL '1 DAY')) WHERE id = %s", getSchema(), id));
  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = reverseTopupUserBalance(getRandomString(10),null);
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

    HttpResponse resp = reverseTopupUserBalance(getRandomString(10),prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(getRandomString(10),prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(getRandomString(10),prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(getRandomString(10), prepaidTopup);

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

    HttpResponse resp = reverseTopupUserBalance(getRandomString(10), prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {
    // POS
    {


      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(getRandomNumericString(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(getRandomString(10),prepaidTopup);

      Assert.assertEquals("status 404", 404, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_NO_TIENE_PREPAGO.getValue(), errorObj.get("code"));
    }

    //WEB
    {
      User user = registerUser();

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(getRandomString(10),prepaidTopup);

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

      PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(getRandomNumericString(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }

    //WEB
    {

      PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
      prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 102015", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn202_ReverseAlreadyReceived_POS() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(5000));

    
    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(),prepaidTopup);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130003", REVERSA_RECIBIDA_PREVIAMENTE.getValue(), errorObj.get("code"));

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn202_ReverseAlreadyReceived_WEB() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130003", REVERSA_RECIBIDA_PREVIAMENTE.getValue(), errorObj.get("code"));

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, null, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn202_OriginalTopupNull_POS() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(),prepaidTopup);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130001", REVERSA_MOVIMIENTO_ORIGINAL_NO_RECIBIDO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn202_OriginalTopupNull_WEB() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

    Assert.assertEquals("status 202", 202, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130001", REVERSA_MOVIMIENTO_ORIGINAL_NO_RECIBIDO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA, null, null);

    Assert.assertNotNull("Debe tener 1 movimiento de reversa", movement);
    Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movement.size());
  }

  @Test
  public void shouldReturn410_OriginalTopupReverseTimeExpired_POS() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    this.updateCreationDate(originalTopup.getId());

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

    Assert.assertEquals("status 410", 410, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130004", REVERSA_TIEMPO_EXPIRADO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);

    Assert.assertNull("No debe tener movimientos de reversa", movement);
  }

  @Test
  public void shouldReturn410_OriginalTopupReverseTimeExpired_WEB() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    this.updateCreationDate(originalTopup.getId());

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

    Assert.assertEquals("status 410", 410, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130004", REVERSA_TIEMPO_EXPIRADO.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA, null, null);

    Assert.assertNull("No debe tener movimientos de reversa", movement);
  }

  @Test
  public void shouldReturn201_ReverseAccepted_POS() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(),prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    // Se verifica que se tenga un registro de reversa

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);

    Assert.assertNotNull("Debe tener movimientos de reversa", movement);
    Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
  }

  @Test
  public void shouldReturn201_ReverseAccepted_WEB() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_TRANSFERENCIA, null, null);

    Assert.assertNotNull("Debe tener movimientos de reversa", movement);
    Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
  }

  @Test
  public void shouldReturn422_differentAmount_POS() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    prepaidTopup.getAmount().setValue(prepaidTopup.getAmount().getValue().add(BigDecimal.TEN));

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(),prepaidTopup);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130002", REVERSA_INFORMACION_NO_CONCUERDA.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

    Assert.assertNull("No debe tener movimiento de reversa", movement);
  }

  @Test
  public void shouldReturn422_differentAmount_WEB() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    prepaidTopup.getAmount().setValue(prepaidTopup.getAmount().getValue().add(BigDecimal.TEN));

    HttpResponse resp = reverseTopupUserBalance(prepaidUser.getUuid(), prepaidTopup);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 130002", REVERSA_INFORMACION_NO_CONCUERDA.getValue(), errorObj.get("code"));

    // Se verifica que se tenga un registro de reversa
    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PROCESS_OK, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.RETIRO_TRANSFERENCIA, null, null);

    Assert.assertNull("No debe tener movimiento de reversa", movement);
  }


  /**
   * Test que verifica el manejo del endpoint antiguo
   * @throws Exception
   */
  @Test
  public void shouldReturn201_ReverseTmpAccepted_POS() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());
    Assert.assertNotNull("La Cuenta debe ser != null ", account);


    PrepaidCard10 prepaidCard10 = buildPrepaidCardByAccountNumber(prepaidUser,account.getAccountNumber());
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);
    Assert.assertNotNull("La tarjeta debe ser != null ", prepaidCard10);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));
    prepaidTopup.setRut(Integer.parseInt(prepaidUser.getDocumentNumber()));


    PrepaidMovement10 originalTopup = buildPrepaidMovement10(prepaidUser, new PrepaidTopup10(prepaidTopup));
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup = createPrepaidMovement10(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    HttpResponse resp = reverseTopupUserBalanceTmp(prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    // Se verifica que se tenga un registro de reversa

    List<PrepaidMovement10> movement = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
      prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, PrepaidMovementStatus.PENDING, null, null, IndicadorNormalCorrector.CORRECTORA, TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);

    Assert.assertNotNull("Debe tener movimientos de reversa", movement);
    Assert.assertEquals("Debe tener movimientos de reversa", 1,movement.size());
  }

}
