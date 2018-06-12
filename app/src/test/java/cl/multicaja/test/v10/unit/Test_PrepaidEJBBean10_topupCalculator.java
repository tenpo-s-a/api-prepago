package cl.multicaja.test.v10.unit;


import cl.multicaja.core.exceptions.ValidationException;
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
public class Test_PrepaidEJBBean10_topupCalculator extends TestBaseUnit {

  @Test
  public void calculator_with_error_in_params_null() throws Exception {

    final Integer codErrorParamNull = 101004;

    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(null);

      try {

        getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(null);
      calculatorRequest.setRut(1);

      try {

        getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(null);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(1);

      try {

        getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(null);

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(null);

      try {

        getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setCurrencyCode(null);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(null);

      try {

        getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
  }

  @Test
  public void calculator_ok_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(3000));

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.WEB);
    calculatorRequest.setRut(user.getRut().getValue());

    System.out.println("Calcular carga WEB: " + calculatorRequest);

    CalculatorTopupResponse10 resp = getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

    System.out.println("respuesta calculo: " + resp);

    BigDecimal calculatedFee = CALCULATOR_TOPUP_WEB_FEE_AMOUNT;
    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee), CodigoMoneda.CHILE_CLP);

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getAmountToPay());

    BigDecimal calculatedPca = calculatePca(amount.getValue());
    BigDecimal calculatedEee = calculateEed(amount.getValue());

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getEed());
  }

  @Test
  public void calculator_ok_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(3000));

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.POS);
    calculatorRequest.setRut(user.getRut().getValue());

    System.out.println("Calcular carga POS: " + calculatorRequest);

    CalculatorTopupResponse10 resp = getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

    System.out.println("respuesta calculo: " + resp);

    BigDecimal calculatedFee = calculateFee(calculatorRequest.getAmount().getValue(), CALCULATOR_TOPUP_POS_FEE_PERCENTAGE);

    NewAmountAndCurrency10 calculatedAmount = new NewAmountAndCurrency10(amount.getValue().add(calculatedFee), CodigoMoneda.CHILE_CLP);

    Assert.assertEquals("debe ser comision para carga web", calculatedFee, resp.getFee());
    Assert.assertEquals("debe ser monto a pagar + comision", calculatedAmount, resp.getAmountToPay());

    BigDecimal calculatedPca = calculatePca(amount.getValue());
    BigDecimal calculatedEee = calculateEed(amount.getValue());

    Assert.assertEquals("debe ser el pca calculado", calculatedPca, resp.getPca());
    Assert.assertEquals("debe ser el eed calculado", calculatedEee, resp.getEed());
  }

  @Test
  public void calculator_not_ok_exceeds_balance() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(400000); //se agrega saldo de 450.000 en tecnocom

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);

    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(100001)); //se intenta cargar 100.001, debe dar error dado que el maximo es 500.000

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.WEB);
    calculatorRequest.setRut(user.getRut().getValue());

    System.out.println("Calcular carga WEB: " + calculatorRequest);

    try {

      getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      Assert.assertEquals("debe ser error de supera saldo", Integer.valueOf(109000), vex.getCode());
    }
  }
}
