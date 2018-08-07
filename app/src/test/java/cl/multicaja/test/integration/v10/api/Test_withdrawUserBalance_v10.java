package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_withdrawUserBalance_v10 extends TestBaseUnitApi {

  /**
   *
   * @param newPrepaidWithdraw10
   * @return
   */
  private HttpResponse withdrawUserBalance(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void shouldReturn201_OnPosWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(user, BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());


    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
  }

  @Test
  public void shouldReturn201_OnWebWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(user, BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
  }

  @Test
  public void shouldReturn422_OnWithdraw_MinAmount() throws Exception {
    // POS
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser(password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
      prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108303", 108303, errorObj.get("code"));
    }

    //WEB
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser(password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108303", 108303, errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_OnWithdraw_MaxAmount() throws Exception {
    // POS
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser(password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
      prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(100001));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108302", 108302, errorObj.get("code"));
    }

    //WEB
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser(password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
      prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500001));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108301", 108301, errorObj.get("code"));
    }
  }

  @Test
  public void shouldReturn422_OnWithdraw_InsufficientFounds() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));


    InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(10000));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106001", 106001, errorObj.get("code"));

    // Verifica la transaccion
    PrepaidMovement10 movement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.ERROR_POS_WITHDRAW,
      PrepaidMovementStatus.ERROR_WEB_WITHDRAW,
      PrepaidMovementStatus.REVERSED);

    Assert.assertNotNull("Debe existir un movimiento", movement);
    Assert.assertEquals("Debe tener el mismo idTxExterno", prepaidWithdraw.getTransactionId(), movement.getIdTxExterno());
    Assert.assertEquals("Debe estar en status " + PrepaidMovementStatus.REVERSED, PrepaidMovementStatus.REVERSED, movement.getEstado());

  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = withdrawUserBalance(null);
    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingRut() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingTransactionId() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmount() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountCurrencyCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountValue() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingPassword() {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null);
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(null);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_McUserNull() {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null);
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(RandomStringUtils.randomNumeric(4));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102002", 102002, errorObj.get("code"));
  }
  @Test
  public void shouldReturn422_McUserListaNegra() throws Exception {

    User user = registerUserBlackListed();
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102016", 102016, errorObj.get("code"));
  }
  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {

    User user = registerUser();
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_InvalidPassword() throws Exception {

    User user = registerUser("1234");

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "4321");

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102053", 102053, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardNull() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardPending() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardExpired() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardHardLocked() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn500_TecnocomError_UserDoesntExists() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10(prepaidUser));

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106001", 106001, errorObj.get("code"));

    // Verifica la transaccion
    PrepaidMovement10 movement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.ERROR_POS_WITHDRAW,
      PrepaidMovementStatus.ERROR_WEB_WITHDRAW,
      PrepaidMovementStatus.REVERSED);

    Assert.assertNotNull("Debe existir un movimiento", movement);
    Assert.assertEquals("Debe tener el mismo idTxExterno", prepaidWithdraw.getTransactionId(), movement.getIdTxExterno());
    Assert.assertEquals("Debe estar en status " + PrepaidMovementStatus.REVERSED, PrepaidMovementStatus.REVERSED, movement.getEstado());

  }
}
