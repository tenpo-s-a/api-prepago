package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor abarazarte
 */
public class Test_PrepaidEJBBean10_withdrawUserBalance extends TestBaseUnit {

  @Test
  public void PosWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));


    PrepaidWithdraw10 withdraw = null;

    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);

    } catch(Exception vex) {
      Assert.fail("No debe pasar por acá");
    }

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());

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
  public void WebWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw = null;

    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);

    } catch(Exception vex) {
      Assert.fail("No debe pasar por acá");
    }

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());

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
  public void shouldReturnExceptionWhen_OnWithdraw_MinAmount() throws Exception {
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

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108303), vex.getCode());
      }
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

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108303), vex.getCode());
      }
    }
  }

  @Test
  public void shouldReturnExceptionWhen_OnWithdraw_MaxAmount() throws Exception {
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

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108302), vex.getCode());
      }
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

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108301), vex.getCode());
      }
    }
  }
  // No se puede ejecutar por que ahora tecnocomMock tiene limite de saldo al igual que el real
  @Ignore
  @Test
  public void shouldReturnExceptionWhen_OnWithdraw_MaxMonthlyAmount() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    for(int i = 0; i < 3; i++){
      InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(500000));
      Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    }

    for(int i = 0; i < 10; i++) {

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(100000));
      try {
        getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      } catch(ValidationException vex) {
        Assert.fail("No debe pasar por acá.     i = " + i);
      }
    }

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(100000));

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108304), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_OnWithdraw_InsufficientFounds() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

    InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(10000));

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(RunTimeValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", Integer.valueOf(106001), vex.getCode());
    }

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
  public void shouldReturnExceptionWhen_WithdrawRequestNull() throws Exception {

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, null);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingRut() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);
    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingTransactionId() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingMerchantCode() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingAmount() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingAmountCurrencyCode() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingAmountValue() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingPassword() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null);
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(amount);
    prepaidWithdraw.setPassword(null);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserNull() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null);
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(RandomStringUtils.randomNumeric(4));

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(NotFoundException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserDeleted() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DELETED);
    user = updateUser(user);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserLocked() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.LOCKED);
    user = updateUser(user);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_McUserDisabled() throws Exception {

    User user = registerUser();
    user.setGlobalStatus(UserStatus.DISABLED);
    user = updateUser(user);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidUserNull() throws Exception {

    User user = registerUser();
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(NotFoundException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidUserDisabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser.setStatus(PrepaidUserStatus.DISABLED);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_InvalidPassword() throws Exception {

    User user = registerUser("1234");

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "4321");

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLAVE_NO_COINCIDE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardNull() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardPending() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardExpired() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardHardLocked() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_TecnocomError_UserDoesntExists() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser(password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10(prepaidUser));

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null, prepaidWithdraw);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(RunTimeValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_ERROR_GENERICO_$VALUE.getValue(), vex.getCode());
    }

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