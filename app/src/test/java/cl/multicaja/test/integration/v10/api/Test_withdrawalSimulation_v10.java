package cl.multicaja.test.integration.v10.api;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.*;

/**
 * @autor vutreras
 */
public class Test_withdrawalSimulation_v10 extends TestBaseUnitApi {

  @Before
  public void before() {
    getTecnocomService().setAutomaticError(Boolean.FALSE);
    getTecnocomService().setRetorno(null);
  }

  /**
   *
   * @param userIdMc
   * @param simulationNew
   * @return
   */
  private HttpResponse withdrawalSimulation(Long userIdMc, SimulationNew10 simulationNew) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/simulation/withdrawal", userIdMc), toJson(simulationNew));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void withdrawalSimulation_not_ok_by_params_null() throws Exception {

    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(null, simulationNew);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(TransactionOriginType.WEB);

      HttpResponse respHttp = withdrawalSimulation(Long.MAX_VALUE, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 404", 404, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
    }
    {
      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(null);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));
      amount.setCurrencyCode(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = withdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_not_found() throws Exception {

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(Long.MAX_VALUE, simulationNew);

    Assert.assertEquals("status 404", 404, respHttp.getStatus());
    NotFoundException vex = respHttp.toObject(NotFoundException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_NO_EXISTE.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_disabled() throws Exception {

    User user = registerUser(UserStatus.DISABLED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_deleted() throws Exception {

    User user = registerUser(UserStatus.DELETED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_locked() throws Exception {

    User user = registerUser(UserStatus.LOCKED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_user_preregistered() throws Exception {

    User user = registerUser(UserStatus.PREREGISTERED);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_prepaid_user_not_found() throws Exception {

    User user = registerUser();

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 404", 404, respHttp.getStatus());
    NotFoundException vex = respHttp.toObject(NotFoundException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_NO_TIENE_PREPAGO.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_prepaid_user_disabled() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUser10(prepaidUser);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_ok_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    // se hace una carga
    topupUserBalance(user, BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationWithdrawal10 resp = respHttp.toObject(SimulationWithdrawal10.class);

    System.out.println("respuesta calculo: " + resp);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(BigDecimal.valueOf(
      getCalculationsHelper().addIva(getPercentage().getCALCULATOR_WITHDRAW_WEB_FEE_AMOUNT()).intValue()
    ));

    Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalSimulation_ok_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    // se hace una carga
    topupUserBalance(user, BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationWithdrawal10 resp = respHttp.toObject(SimulationWithdrawal10.class);

    System.out.println("respuesta calculo: " + resp);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(getCalculationsHelper().calculateFee(simulationNew.getAmount().getValue(), getPercentage().getCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE()));

    Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalSimulation_not_ok_insufficient_balance_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 10.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(10000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    try {

      //debe lanzar excepcion de saldo insuficiente dado que intenta retirar 10.000 al cual se le agrega la comision de
      //retiro WEB  y eso supera el saldo inicial de 10.000
      HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", SALDO_INSUFICIENTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_insufficient_balance_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    //se carga 10.000 en tecnocom como saldo del usuario
    BigDecimal impfac = BigDecimal.valueOf(10000);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    //se intenta retirar 10.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(10000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    try {

      //debe lanzar excepcion de saldo insuficiente dado que intenta retirar 10.000 al cual se le agrega la comision de
      //retiro POS  y eso supera el saldo inicial de 10.000
      HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", SALDO_INSUFICIENTE_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_not_ok_by_min_amount_web() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(499));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_min_amount_pos() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(5000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(999));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_max_amount_web() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(500001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_UN_RETIRO_WEB.getValue(), vex.getCode());
  }

  @Test
  public void withdrawalSimulation_not_ok_by_max_amount_pos() throws Exception {
    User user = registerUser();
    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(0);

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(100001));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);
    HttpResponse respHttp = withdrawalSimulation(user.getId(), simulationNew);

    Assert.assertEquals("status 422", 422, respHttp.getStatus());
    ValidationException vex = respHttp.toObject(ValidationException.class);

    Assert.assertEquals("debe ser error de supera saldo", EL_RETIRO_SUPERA_EL_MONTO_MAXIMO_DE_UN_RETIRO_POS.getValue(), vex.getCode());
  }


}
