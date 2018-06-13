package cl.multicaja.test.v10.api;


import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * @autor vutreras
 */
public class Test_withdrawalCalculator_v10 extends TestBaseUnitApi {

  /**
   *
   * @param userId
   * @param calculatorRequest
   * @return
   */
  private HttpResponse postWithdrawalCalculator(Long userId, CalculatorRequest10 calculatorRequest) {
    HttpResponse respHttp = apiPOST(String.format("/1.0/prepaid/%s/calculator/withdrawal", userId), toJson(calculatorRequest));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Test
  public void withdrawalCalculator_with_error_in_params_null() throws Exception {

    final Integer codErrorParamNull = 101004;

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalCalculator(null, calculatorRequest);

      Assert.assertEquals("status 500", 500, respHttp.getStatus());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(null);

      HttpResponse respHttp = postWithdrawalCalculator(1L, calculatorRequest);

      ValidationException vex = respHttp.toObject(ValidationException.class);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(null);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalCalculator(1L, calculatorRequest);

      ValidationException vex = respHttp.toObject(ValidationException.class);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(null);

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalCalculator(1L, calculatorRequest);

      ValidationException vex = respHttp.toObject(ValidationException.class);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(null);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);

      HttpResponse respHttp = postWithdrawalCalculator(1L, calculatorRequest);

      ValidationException vex = respHttp.toObject(ValidationException.class);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());
      Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
    }
  }

  @Test
  public void withdrawalCalculator_ok_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(10000); //se carga 10.000 en tecnocom como saldo del usuario

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(8000)); //intento retirar 8.000

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.WEB);

    HttpResponse respHttp = postWithdrawalCalculator(prepaidUser10.getId(), calculatorRequest);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    CalculatorWithdrawalResponse10 resp = respHttp.toObject(CalculatorWithdrawalResponse10.class);

    System.out.println("respuesta calculo: " + resp);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    BigDecimal feeOk = CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT;

    Assert.assertEquals("deben ser las mismas comisiones", feeOk, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", feeOk.add(amount.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalCalculator_ok_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(10000); //se carga 10.000 en tecnocom como saldo del usuario

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(8000)); //intento retirar 8.000

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.POS);

    HttpResponse respHttp = postWithdrawalCalculator(prepaidUser10.getId(), calculatorRequest);

    Assert.assertEquals("status 200", 200, respHttp.getStatus());

    CalculatorWithdrawalResponse10 resp = respHttp.toObject(CalculatorWithdrawalResponse10.class);

    System.out.println("respuesta calculo: " + resp);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getFee());

    //calculo de la comision
    BigDecimal feeOk = calculateFee(calculatorRequest.getAmount().getValue(), CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE);

    Assert.assertEquals("deben ser las mismas comisiones", feeOk, resp.getFee());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", feeOk.add(amount.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void withdrawalCalculator_not_ok_insufficient_balance_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(10000); //se carga 10.000 en tecnocom como saldo del usuario

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(10000)); //intento retirar 10.000

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.WEB);

    try {

      //debe lanzar excepcion de saldo insuficiente dado que intenta retirar 10.000 al cual se le agrega la comision de
      //retiro WEB  y eso supera el saldo inicial de 10.000
      HttpResponse respHttp = postWithdrawalCalculator(prepaidUser10.getId(), calculatorRequest);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", Integer.valueOf(109001), vex.getCode());
    }
  }

  @Test
  public void withdrawalCalculator_not_ok_insufficient_balance_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(10000); //se carga 10.000 en tecnocom como saldo del usuario

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(10000)); //intento retirar 10.000

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.POS);

    try {

      //debe lanzar excepcion de saldo insuficiente dado que intenta retirar 10.000 al cual se le agrega la comision de
      //retiro POS  y eso supera el saldo inicial de 10.000
      HttpResponse respHttp = postWithdrawalCalculator(prepaidUser10.getId(), calculatorRequest);

      Assert.assertEquals("status 422", 422, respHttp.getStatus());

      ValidationException vex = respHttp.toObject(ValidationException.class);

      if (vex != null) {
        throw vex;
      }

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de saldo insuficiente", Integer.valueOf(109001), vex.getCode());
    }
  }
}
