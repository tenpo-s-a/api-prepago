package cl.multicaja.test.v10.unit;


import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_topupCalculator extends TestBaseUnit {

  @Test
  public void testCalculatorErrorParamsNull() throws Exception {

    final Integer codErrorParamNull = 101004;

    {
      AmountAndCurrency10 amountAndCurrency10 = new AmountAndCurrency10();
      amountAndCurrency10.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amountAndCurrency10.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amountAndCurrency10);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setUserRut(null);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      AmountAndCurrency10 amountAndCurrency10 = new AmountAndCurrency10();
      amountAndCurrency10.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amountAndCurrency10.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amountAndCurrency10);
      calculatorRequest.setPaymentMethod(null);
      calculatorRequest.setUserRut(1);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(null);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setUserRut(1);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      AmountAndCurrency10 amountAndCurrency10 = new AmountAndCurrency10();
      amountAndCurrency10.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amountAndCurrency10.setValue(null);

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amountAndCurrency10);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setUserRut(null);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      AmountAndCurrency10 amountAndCurrency10 = new AmountAndCurrency10();
      amountAndCurrency10.setCurrencyCode(null);
      amountAndCurrency10.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amountAndCurrency10);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setUserRut(null);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
  }

  @Test
  public void testCalculatorOk() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    AmountAndCurrency10 amountAndCurrency10 = new AmountAndCurrency10();
    amountAndCurrency10.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amountAndCurrency10.setValue(BigDecimal.valueOf(3000));

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amountAndCurrency10);
    calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
    calculatorRequest.setUserRut(user.getRut().getValue());

    //TODO falta registrar los datos en tecnocom

    CalculatorTopupResponse10 resp = getPrepaidEJBBean10().topupCalculator(null, calculatorRequest);

    //TODO falta completar el test
  }

}
