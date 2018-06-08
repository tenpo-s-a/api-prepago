package cl.multicaja.test.v10.unit;


import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.ErrorUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_withdrawalCalculator extends TestBaseUnit {

  @Test
  public void calculatorWithErrorParamsNull() throws Exception {

    final Integer codErrorParamNull = 101004;

    {
      AmountAndCurrency10 amount = new AmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(null);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      AmountAndCurrency10 amount = new AmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(null);
      calculatorRequest.setRut(1);

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
      calculatorRequest.setRut(1);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      AmountAndCurrency10 amount = new AmountAndCurrency10();
      amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
      amount.setValue(null);

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(null);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
    {
      AmountAndCurrency10 amount = new AmountAndCurrency10();
      amount.setCurrencyCode(null);
      amount.setValue(BigDecimal.valueOf(3000));

      CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
      calculatorRequest.setAmount(amount);
      calculatorRequest.setPaymentMethod(numberUtils.random() ? TransactionOriginType.WEB : TransactionOriginType.POS);
      calculatorRequest.setRut(null);

      try {
        getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);
      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser error de validacion de parametros", codErrorParamNull, vex.getCode());
      }
    }
  }

  @Test
  public void calculatorOk_WEB() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(3000));

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.WEB);
    calculatorRequest.setRut(user.getRut().getValue());

    //TODO falta registrar los datos en tecnocom

    CalculatorWithdrawalResponse10 resp = getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);

    Assert.assertNotNull("debe retornar una respuesta", resp);
    Assert.assertNotNull("debe retornar un monto", resp.getAmount());
    Assert.assertNotNull("debe retornar un monto a descontar", resp.getAmountToDiscount());
    Assert.assertNotNull("debe retornar una comision", resp.getComission());

    //calculo d la comision
    BigDecimal comissionOk = BigDecimal.valueOf(100);

    Assert.assertEquals("deben ser las mismas comisiones", comissionOk, resp.getComission());
    Assert.assertEquals("debe ser el mismo monto", amount, resp.getAmount());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", comissionOk.add(amount.getValue()), resp.getAmountToDiscount().getValue());
  }

  @Test
  public void calculatorOk_POS() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(3000));

    CalculatorRequest10 calculatorRequest = new CalculatorRequest10();
    calculatorRequest.setAmount(amount);
    calculatorRequest.setPaymentMethod(TransactionOriginType.POS);
    calculatorRequest.setRut(user.getRut().getValue());

    //TODO falta registrar los datos en tecnocom

    CalculatorWithdrawalResponse10 resp = getPrepaidEJBBean10().withdrawalCalculator(null, calculatorRequest);

    //calculo d la comision
    BigDecimal comissionOk = BigDecimal.valueOf(Math.round(Math.max(100, (amount.getValue().longValue() * 0.5 / 100)) * 1.19));

    Assert.assertEquals("deben ser las mismas comisiones", comissionOk, resp.getComission());
    Assert.assertEquals("debe ser el mismo monto", amount, resp.getAmount());
    Assert.assertEquals("debe ser el mismo monto a retirar (monto + comision)", comissionOk.add(amount.getValue()), resp.getAmountToDiscount().getValue());

    Locale.setDefault(Constants.DEFAULT_LOCALE);

    ErrorUtils err = ErrorUtils.getInstance();

    System.out.println(err.merge(err.getError(109000, Constants.DEFAULT_LOCALE), new KeyValue("value", 500)));
  }
}
