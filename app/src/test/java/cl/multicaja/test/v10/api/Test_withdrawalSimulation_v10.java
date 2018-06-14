package cl.multicaja.test.v10.api;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.SALDO_INSUFICIENTE_$VALUE;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * @autor vutreras
 */
public class Test_withdrawalSimulation_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userId
   * @param simulationNew
   * @return
   */
  private HttpResponse postWithdrawalSimulation(Long userId, SimulationNew10 simulationNew) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/simulation/withdrawal", userId), toJson(simulationNew));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void withdrawalSimulation_with_params_null() throws Exception {

    final Integer codErrorParamNull = PARAMETRO_FALTANTE_$VALUE.getValue();

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalSimulation(null, simulationNew);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(3000));

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(null);

      HttpResponse respHttp = postWithdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(null);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(null);

      SimulationNew10 simulationNew = new SimulationNew10();
      simulationNew.setAmount(amount);
      simulationNew.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalSimulation(1L, simulationNew);

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

      HttpResponse respHttp = postWithdrawalSimulation(1L, simulationNew);

      BadRequestException vex = respHttp.toObject(BadRequestException.class);

      Assert.assertEquals("status 400", 400, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
  }

  @Test
  public void withdrawalSimulation_ok_WEB() throws Exception {

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

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB);

    HttpResponse respHttp = postWithdrawalSimulation(prepaidUser10.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationWithdrawal10 resp = respHttp.toObject(SimulationWithdrawal10.class);

    System.out.println("respuesta calculo: " + resp);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT);

    Assert.assertEquals("deben ser las mismas comisiones", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", amount.getValue().add(calculatedFee.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalSimulation_ok_POS() throws Exception {

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

    //se intenta retirar 8.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(8000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.POS);

    HttpResponse respHttp = postWithdrawalSimulation(prepaidUser10.getId(), simulationNew);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    SimulationWithdrawal10 resp = respHttp.toObject(SimulationWithdrawal10.class);

    System.out.println("respuesta calculo: " + resp);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    NewAmountAndCurrency10 calculatedFee = new NewAmountAndCurrency10(calculateFee(simulationNew.getAmount().getValue(), CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE));

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
      HttpResponse respHttp = postWithdrawalSimulation(prepaidUser10.getId(), simulationNew);

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
      HttpResponse respHttp = postWithdrawalSimulation(prepaidUser10.getId(), simulationNew);

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
}
