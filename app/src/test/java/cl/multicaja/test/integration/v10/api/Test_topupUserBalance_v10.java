package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

//TODO: Esto verifica el endopint antiguo
@Ignore
public class Test_topupUserBalance_v10 extends TestBaseUnitApi {


  private HttpResponse topupUserBalanceLocal(NewPrepaidTopup10 newPrepaidTopup10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup", toJson(newPrepaidTopup10));
    return respHttp;
  }

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.accounting cascade", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.clearing cascade", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimiento_conciliado cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimiento cascade", getSchema()));
  }

  @Test
  public void shouldReturn201_OnWebtopupUserBalanceLocal() throws Exception {

    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
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
    //Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));
  }

  @Test
  public void shouldReturn201_OnPostopupUserBalanceLocal() throws Exception {


    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode(getRandomNumericString(15));

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
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
    //Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));
  }

  @Test
  public void shouldReturn201_OnPostopupUserBalanceLocal_merchantCode_5() throws Exception {


    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    String merchantCode = getRandomNumericString(5);
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode(merchantCode);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
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
    //Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(topup.getId());
    Assert.assertEquals("Debe tener el merchantCode completado con 0 a la izquierda", "0000000000" + merchantCode, dbMovement.getCodcom());
  }

  @Test
  public void shouldReturn201_OnPostopupUserBalanceLocal_merchantCode_18() throws Exception {

    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    String merchantCode = getRandomNumericString(15);
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode("000" + merchantCode);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

    Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
    Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
    Assert.assertNotNull("Deberia tener id", topup.getId());
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
    //Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(topup.getId());
    Assert.assertEquals("Debe tener el merchantCode truncado con los ultimos 15 digitos", merchantCode, dbMovement.getCodcom());
  }

  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = topupUserBalanceLocal(null);
    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }


  @Test
  public void shouldReturn400_OnMissingTransactionId()throws Exception {


    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingMerchantCode() throws Exception{


    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(prepaidUser.getRut());
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmount() throws Exception{


    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode("987654321");

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountCurrencyCode() throws Exception{


    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMissingAmountValue() throws Exception{

    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = new NewPrepaidTopup10();
    prepaidTopup.setTransactionId("123456789");
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CLP);
    prepaidTopup.setAmount(amount);

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }

  @Test
  public void shouldReturn400_OnMerchantCodeFormat() throws Exception {

    PrepaidUser10 prepaidUser =buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.setMerchantCode(getRandomString(10));

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101007", PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue(), errorObj.get("code"));
  }

  @Test
  public void shouldReturn422_OnTopup_MinAmount() throws Exception {
    // POS
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(getRandomNumericString(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidTopup.setRut(prepaidUser.getRut());

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108203", 108203, errorObj.get("code"));
    }

    //WEB
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));
      prepaidTopup.setRut(prepaidUser.getRut());

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108203", 108203, errorObj.get("code"));
    }
  }

  //TODO: Se debe corregir problema por primera monto primera carga.
  @Ignore
  @Test
  public void shouldReturn422_OnTopup_MaxAmount() throws Exception {
    // POS
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(getRandomNumericString(15));
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(101586));
      prepaidTopup.setRut(prepaidUser.getRut());

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108202", 108202, errorObj.get("code"));
    }

    //WEB
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500001));
      prepaidTopup.setRut(prepaidUser.getRut());

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108201", 108201, errorObj.get("code"));
    }
  }

  //TODO: Revisar este test.
  @Ignore
  @Test
  public void shouldReturn422_OnTopup_MonthlyAmount() throws Exception {


    PrepaidUser10 prepaidUser =buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    for(int i = 0; i < 10; i++) {

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());

      if(i == 0 ){
        prepaidTopup.getAmount().setValue(BigDecimal.valueOf(5000));
      }else{
        prepaidTopup.getAmount().setValue(BigDecimal.valueOf(100000));
      }

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);
      if(i == 0 )
        Thread.sleep(3000);
      Assert.assertEquals("status 201", 201, resp.getStatus());
    }

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setRut(prepaidUser.getRut());
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(100000));

    HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 108204", 108204, errorObj.get("code"));
  }

  //TODO: revisar este fallo
  @Ignore
  @Test
  public void shouldReturn422_OnTopup_Reversed() throws Exception {
    // POS
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());
      prepaidTopup.setMerchantCode(getRandomNumericString(15));

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 130005", REVERSA_MOVIMIENTO_REVERSADO.getValue(), errorObj.get("code"));


      List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, null, null, null, IndicadorNormalCorrector.NORMAL, TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movements);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movements.size());
      PrepaidMovement10 prepaidMovement10 = movements.get(0);

      Assert.assertEquals("Debe tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());
      Assert.assertEquals("Debe tener businessStatus REVERSED", BusinessStatusType.REVERSED, prepaidMovement10.getEstadoNegocio());
      Assert.assertEquals("Debe tener conTecnocom RECONCILIED", ReconciliationStatusType.RECONCILED, prepaidMovement10.getConTecnocom());
    }

    //WEB
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 130005", REVERSA_MOVIMIENTO_REVERSADO.getValue(), errorObj.get("code"));

      List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, null, null, null, IndicadorNormalCorrector.NORMAL, TipoFactura.CARGA_TRANSFERENCIA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movements);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movements.size());
      PrepaidMovement10 prepaidMovement10 = movements.get(0);

      Assert.assertEquals("Debe tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());
      Assert.assertEquals("Debe tener businessStatus REVERSED", BusinessStatusType.REVERSED, prepaidMovement10.getEstadoNegocio());
      Assert.assertEquals("Debe tener conTecnocom RECONCILIED", ReconciliationStatusType.RECONCILED, prepaidMovement10.getConTecnocom());
    }
  }

  @Test
  public void shouldReturn422_OnTopup_alreadyReceived() throws Exception {
    // POS
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());
      prepaidTopup.setMerchantCode(getRandomNumericString(15));

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

      Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
      Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
      Assert.assertNotNull("Deberia tener id", topup.getId());
      Assert.assertNull("No deberia tener rut", topup.getRut());

      // Segunda vez
      HttpResponse resp1 = topupUserBalanceLocal(prepaidTopup);
      Assert.assertEquals("status 422", 422, resp1.getStatus());
      Map<String, Object> errorObj1 = resp1.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj1);
      Assert.assertEquals("Deberia tener error code = 108000", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), errorObj1.get("code"));
      Assert.assertTrue("Deberia tener error message = Transaccion duplicada", errorObj1.get("message").toString().contains("Transaccion duplicada"));

    }

    //WEB
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

      Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
      Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
      Assert.assertNotNull("Deberia tener id", topup.getId());
      Assert.assertNull("No deberia tener rut", topup.getRut());

      // Segunda vez
      HttpResponse resp1 = topupUserBalanceLocal(prepaidTopup);
      Assert.assertEquals("status 422", 422, resp1.getStatus());
      Map<String, Object> errorObj1 = resp1.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj1);
      Assert.assertEquals("Deberia tener error code = 108000", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), errorObj1.get("code"));
      Assert.assertTrue("Deberia tener error message = Transaccion duplicada", errorObj1.get("message").toString().contains("Transaccion duplicada"));
    }
  }

  @Test
  public void shouldReturn201_OnTopup_Reversed_differentAmount() throws Exception {
    // POS
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());
      prepaidTopup.setMerchantCode(getRandomNumericString(15));

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement.setImpfac(prepaidMovement.getImpfac().add(BigDecimal.TEN));
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

      Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
      Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
      Assert.assertNotNull("Deberia tener id", topup.getId());
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
      //Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
      //Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));
    }

    // WEB
    {

      PrepaidUser10 prepaidUser =buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
      prepaidTopup.setRut(prepaidUser.getRut());
      prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement.setImpfac(prepaidMovement.getImpfac().add(BigDecimal.TEN));
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = topupUserBalanceLocal(prepaidTopup);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidTopup10 topup = resp.toObject(PrepaidTopup10.class);

      Assert.assertNotNull("Deberia ser un PrepaidTopup10",topup);
      Assert.assertNotNull("Deberia tener timestamps", topup.getTimestamps());
      Assert.assertNotNull("Deberia tener id", topup.getId());
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
      //Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidTopup.getRut(), null), rutData.get("value"));
    }
  }
}
