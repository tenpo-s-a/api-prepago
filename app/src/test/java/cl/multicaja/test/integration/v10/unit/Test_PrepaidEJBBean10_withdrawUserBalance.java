package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor abarazarte
 */
//TODO: Hacer test withdrawUserBalance fromEndPoint False
public class Test_PrepaidEJBBean10_withdrawUserBalance extends TestBaseUnit {


  @Test
  public void PosWithdraw() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(),prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    PrepaidWithdraw10 withdraw = null;

    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);

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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());
  }

  @Test
  public void WebWithdraw() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(),prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidWithdraw10 withdraw = null;

    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);

    } catch(Exception vex) {
      vex.printStackTrace();
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

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.IN_PROCESS, BusinessStatusType.IN_PROCESS, dbPrepaidMovement.getEstadoNegocio());
  }

  @Test
  public void shouldReturnExceptionWhen_OnWithdraw_MinAmount() throws Exception {
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

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108303), vex.getCode());
      }
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

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
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
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(101560));

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108302), vex.getCode());
      }
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
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500201));

      try {
        getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108301), vex.getCode());
      }
    }
  }

  @Test
  public void shouldReturnExceptionWhen_OnWithdraw_MaxMonthlyAmount() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    // Se cargan 500000
    {
      InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(),prepaidCard10, BigDecimal.valueOf(500000));
      Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    }
    // Se retiran 450000
    {
      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(490000));
      try {
        getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      } catch(ValidationException vex) {
        Assert.fail("No debe pasar por acá");
      }
    }

    // Se cargan 500000
    {
      InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(),prepaidCard10, BigDecimal.valueOf(490000));
      Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    }
    // Se retiran 450000
    {
      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(490000));
      try {
        getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      } catch(ValidationException vex) {
        Assert.fail("No debe pasar por acá");
      }
    }

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(100000));

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", Integer.valueOf(108304), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_OnWithdraw_InsufficientFounds() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(),prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(10000));

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(RunTimeValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", Integer.valueOf(106001), vex.getCode());
    }

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
  public void shouldReturnExceptionWhen_WithdrawRequestNull() throws Exception {

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,getRandomString(10),null,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingTransactionId() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingMerchantCode() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingAmount() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingAmountCurrencyCode() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_MissingAmountValue() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(amount);

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(BadRequestException vex) {
      Assert.assertEquals("debe ser error de validacion", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidUserNull() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,getRandomString(10), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(NotFoundException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidUserDisabled() throws Exception {


    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardNull() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardPending() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardExpired() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_PrepaidCardHardLocked() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10.setStatus(PrepaidCardStatus.LOCKED_HARD);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void shouldReturnExceptionWhen_TecnocomError_UserDoesntExists() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();

    try {
      getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");
    } catch(RunTimeValidationException vex) {
      Assert.assertEquals("debe ser error de validacion", TARJETA_ERROR_GENERICO_$VALUE.getValue(), vex.getCode());
    }


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

}
