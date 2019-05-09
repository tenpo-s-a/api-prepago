package cl.multicaja.test.integration.v10.api;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 */
public class Test_withdrawUserBalance_v10 extends TestBaseUnitApi {

  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private HttpResponse withdrawUserBalance(String extUserId, NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/cash_out",extUserId), toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse withdrawUserBalanceDefered(String extUserId, NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/defered_cash_out",extUserId), toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  private HttpResponse withdrawUserBalanceOld(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Before
  @After
  public void clearData() throws InterruptedException {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", getSchemaAccounting()));

    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_comision", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @Test
  public void shouldReturn201_OnPosWithdraw() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);
    PrepaidMovement10 dbTopup = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(),prepaidWithdraw);

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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());

    verifyFees(dbPrepaidMovement.getId(), dbPrepaidMovement.getCodcom());

    waitForAccountingToExist(dbTopup.getId());
    waitForAccountingToExist(dbPrepaidMovement.getId());
  }

  @Test
  public void shouldReturn201_OnPosWithdraw_merchantCode_5() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);
    PrepaidMovement10 dbTopup = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);

    String merchantCode = getRandomNumericString(5);
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(merchantCode);

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());
    Assert.assertEquals("El merchant code debe estar completado con 0", "0000000000" + merchantCode, dbPrepaidMovement.getCodcom());

    verifyFees(dbPrepaidMovement.getId(), dbPrepaidMovement.getCodcom());

    waitForAccountingToExist(dbTopup.getId());
    waitForAccountingToExist(dbPrepaidMovement.getId());
  }


  @Test
  public void shouldReturn201_OnPosWithdraw_merchantCode_18() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);


    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);
    PrepaidMovement10 dbTopup = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);

    String merchantCode = getRandomNumericString(15);
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2("000" + merchantCode);

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(),prepaidWithdraw);

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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());
    Assert.assertEquals("Debe tener el merchantCode truncado con los ultimos 15 digitos", merchantCode, dbPrepaidMovement.getCodcom());

    verifyFees(dbPrepaidMovement.getId(), dbPrepaidMovement.getCodcom());

    waitForAccountingToExist(dbTopup.getId());
    waitForAccountingToExist(dbPrepaidMovement.getId());
  }

  @Test
  public void shouldReturn201_OnWebWithdraw() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);


    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);
    PrepaidMovement10 dbTopup = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    HttpResponse resp = withdrawUserBalanceDefered(prepaidUser.getUuid(),prepaidWithdraw);

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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.IN_PROCESS, BusinessStatusType.IN_PROCESS, dbPrepaidMovement.getEstadoNegocio());

    verifyFees(dbPrepaidMovement.getId(), dbPrepaidMovement.getCodcom());

    waitForAccountingToExist(dbTopup.getId());
    waitForAccountingToExist(dbPrepaidMovement.getId());
  }

  @Test
  public void shouldReturn422_OnWithdraw_MinAmount() throws Exception {
    // POS
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(),prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108303", 108303, errorObj.get("code"));
    }

    //WEB
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = withdrawUserBalanceDefered(prepaidUser.getUuid(), prepaidWithdraw);

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
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);


      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(101585));

      HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108302", 108302, errorObj.get("code"));
    }

    //WEB
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);



      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500250));

      HttpResponse resp = withdrawUserBalanceDefered(prepaidUser.getUuid(), prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108301", 108301, errorObj.get("code"));
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_OnWithdraw_InsufficientFounds() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(10000));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106001", 106001, errorObj.get("code"));

    // Verifica la transaccion
    PrepaidMovement10 movement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.ERROR_POS_WITHDRAW,
      PrepaidMovementStatus.ERROR_WEB_WITHDRAW,
      PrepaidMovementStatus.REJECTED);

    Assert.assertNotNull("Debe existir un movimiento", movement);
    Assert.assertEquals("Debe tener el mismo idTxExterno", prepaidWithdraw.getTransactionId(), movement.getIdTxExterno());
    Assert.assertEquals("Debe estar en status " + PrepaidMovementStatus.REJECTED, PrepaidMovementStatus.REJECTED, movement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.REJECTED, BusinessStatusType.REJECTED, movement.getEstadoNegocio());
  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), null);

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

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

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

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMerchantCodeFormat() throws Exception {


    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(RandomStringUtils.randomNumeric(4));
    prepaidWithdraw.setMerchantCode(getRandomString(10));

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(getRandomNumericString(10), prepaidWithdraw);

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardPending() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardExpired() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_PrepaidCardHardLocked() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED_HARD);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_TecnocomError_UserDoesntExists() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = createRandomAccount(prepaidUser);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10();
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setAccountId(account.getId());
    prepaidCard10.setUuid(UUID.randomUUID().toString());
    prepaidCard10.setHashedPan(getRandomString(20));
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106001", 106001, errorObj.get("code"));

    // Verifica la transaccion
    PrepaidMovement10 movement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.ERROR_POS_WITHDRAW,
      PrepaidMovementStatus.ERROR_WEB_WITHDRAW,
      PrepaidMovementStatus.REJECTED);

    Assert.assertNotNull("Debe existir un movimiento", movement);
    Assert.assertEquals("Debe tener el mismo idTxExterno", prepaidWithdraw.getTransactionId(), movement.getIdTxExterno());
    Assert.assertEquals("Debe estar en status " + PrepaidMovementStatus.REJECTED, PrepaidMovementStatus.REJECTED, movement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.REJECTED, BusinessStatusType.REJECTED, movement.getEstadoNegocio());
  }


  @Test
  public void shouldReturn422_OnWithdraw_Reversed() throws Exception {
    // POS
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 130005", REVERSA_MOVIMIENTO_REVERSADO.getValue(), errorObj.get("code"));

      List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null,
        IndicadorNormalCorrector.NORMAL, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movements);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movements.size());
      PrepaidMovement10 prepaidMovement10 = movements.get(0);

      Assert.assertEquals("Debe tener el mismo id externo", prepaidWithdraw.getTransactionId(), prepaidMovement10.getIdTxExterno());
      Assert.assertEquals("Debe tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());
      Assert.assertEquals("Debe tener businessStatus REVERSED", BusinessStatusType.REVERSED, prepaidMovement10.getEstadoNegocio());
      Assert.assertEquals("Debe tener conTecnocom RECONCILIED", ReconciliationStatusType.RECONCILED, prepaidMovement10.getConTecnocom());
    }

    // WEB
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = withdrawUserBalanceDefered(prepaidUser.getUuid(), prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 130005", REVERSA_MOVIMIENTO_REVERSADO.getValue(), errorObj.get("code"));

      List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.NORMAL, TipoFactura.RETIRO_TRANSFERENCIA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movements);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movements.size());
      PrepaidMovement10 prepaidMovement10 = movements.get(0);

      Assert.assertEquals("Debe tener el mismo id externo", prepaidWithdraw.getTransactionId(), prepaidMovement10.getIdTxExterno());
      Assert.assertEquals("Debe tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());
      Assert.assertEquals("Debe tener businessStatus REVERSED", BusinessStatusType.REVERSED, prepaidMovement10.getEstadoNegocio());
      Assert.assertEquals("Debe tener conTecnocom RECONCILIED", ReconciliationStatusType.RECONCILED, prepaidMovement10.getConTecnocom());
    }

  }

  @Test
  public void shouldReturn422_OnWithdraw_AlreadyReceived() throws Exception {
    // POS
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);


      // se hace una carga
      topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

      PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
      Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));

      HttpResponse resp = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

      Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
      Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
      Assert.assertNotNull("Deberia tener id", withdraw.getId());
      Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
      Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
      Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());

      // Segunda vez
      HttpResponse resp1 = withdrawUserBalance(prepaidUser.getUuid(), prepaidWithdraw);
      Assert.assertEquals("status 422", 422, resp1.getStatus());
      Map<String, Object> errorObj1 = resp1.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj1);
      Assert.assertEquals("Deberia tener error code = 108000", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), errorObj1.get("code"));
      Assert.assertTrue("Deberia tener error message = Transacción duplicada", errorObj1.get("message").toString().contains("Transacción duplicada"));

    }

    // WEB
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      // se hace una carga
      topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

      PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
      Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      HttpResponse resp = withdrawUserBalanceDefered(prepaidUser.getUuid(), prepaidWithdraw);

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

      // Segunda vez
      HttpResponse resp1 = withdrawUserBalanceDefered(prepaidUser.getUuid(), prepaidWithdraw);
      Assert.assertEquals("status 422", 422, resp1.getStatus());
      Map<String, Object> errorObj1 = resp1.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj1);
      Assert.assertEquals("Deberia tener error code = 108000", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), errorObj1.get("code"));
      Assert.assertTrue("Deberia tener error message = Transacción duplicada", errorObj1.get("message").toString().contains("Transacción duplicada"));
    }
  }
  //TODO: Verificarcuando se haga la reversa.
  @Ignore
  @Test
  public void shouldReturn201_OnWithdraw_Reversed_DifferentAmount_POS() throws Exception {
   /* String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement.setImpfac(prepaidMovement.getImpfac().add(BigDecimal.TEN));
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

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
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());

    */
  }

  //TODO: Verificar esto cuando se trabaje la reversa.
  @Ignore
  @Test
  public void shouldReturn201_OnWithdraw_Reversed_DifferentAmount_WEB() throws Exception {
    /* String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement.setImpfac(prepaidMovement.getImpfac().add(BigDecimal.TEN));
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

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
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.IN_PROCESS, BusinessStatusType.IN_PROCESS, dbPrepaidMovement.getEstadoNegocio());
    */

  }


  @Test
  public void shouldReturn201_OnPosWithdrawOldVersion() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);
    PrepaidMovement10 dbTopup = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    prepaidWithdraw.setRut(Integer.parseInt(prepaidUser.getDocumentNumber()));

    HttpResponse resp = withdrawUserBalanceOld(prepaidWithdraw);

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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());

    waitForAccountingToExist(dbTopup.getId());
    waitForAccountingToExist(dbPrepaidMovement.getId());
  }

  private void verifyFees(Long movementId, String codcom) throws BaseException {
    // Verificar que existan las fees almacenadas en BD
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(movementId);
    Assert.assertEquals("Debe tener 2 fees", 2, prepaidMovementFee10List.size());

    if (NewPrepaidWithdraw10.WEB_MERCHANT_CODE.equals(codcom)) {
      PrepaidMovementFee10 withdrawFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.WITHDRAW_WEB_FEE.equals(f.getFeeType())).findAny().orElse(null);
      Assert.assertEquals("Debe tener una fee de withdraw con valor: 84", new BigDecimal(84L), withdrawFee.getAmount().setScale(0, RoundingMode.HALF_UP));

      PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
      Assert.assertEquals("Debe tener una fee de iva con valor: 16", new BigDecimal(16L), ivaFee.getAmount().stripTrailingZeros());
    } else {
      PrepaidMovementFee10 withdrawFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.WITHDRAW_POS_FEE.equals(f.getFeeType())).findAny().orElse(null);
      Assert.assertEquals("Debe tener una fee de withdraw con valor: 200", new BigDecimal(200L), withdrawFee.getAmount().setScale(0, RoundingMode.HALF_UP));

      PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
      Assert.assertEquals("Debe tener una fee de iva con valor: 38", new BigDecimal(38L), ivaFee.getAmount().stripTrailingZeros());
    }
  }

  private void waitForAccountingToExist(Long movementId) throws Exception {
    for (int i = 0; i < 20; i++) {
      AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, movementId);
      if (accountingData10 != null) {
        return;
      }
      Thread.sleep(500);
    }
  }
}
