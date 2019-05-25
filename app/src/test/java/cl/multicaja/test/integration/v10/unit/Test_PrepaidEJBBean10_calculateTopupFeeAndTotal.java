package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.IvaType;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author abarazarte
 */
public class Test_PrepaidEJBBean10_calculateTopupFeeAndTotal extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void calculateFeeAndTotal_nullTransaction() throws Exception {
    getPrepaidEJBBean10().calculateFeeAndTotal(null, new ArrayList<>());
  }

  @Test(expected = BadRequestException.class)
  public void calculateFeeAndTotal_nullFeeList() throws Exception {
    PrepaidTopup10 prepaidTopup10 = buildPrepaidTopup10();
    getPrepaidEJBBean10().calculateFeeAndTotal(prepaidTopup10, null);
  }

  @Test
  public void calculateFeeAndTotal_topup() throws Exception {
    PrepaidTopup10 prepaidTopup10 = buildPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(new BigDecimal(5000));
    prepaidTopup10.setAmount(amount);

    CalculatorParameter10 calculatorParameter10 = getPrepaidEJBBean10().getCalculationsHelper().getCalculatorParameter10();
    BigDecimal baseFee,percentageFee;
    PrepaidMovementFeeType feeType;
    if(PrepaidTopup10.WEB_MERCHANT_CODE.equals(prepaidTopup10.getMerchantCode())){
      baseFee = calculatorParameter10.getTOPUP_WEB_FEE_AMOUNT();
      percentageFee = calculatorParameter10.getTOPUP_WEB_FEE_PERCENTAGE();
      feeType = PrepaidMovementFeeType.TOPUP_WEB_FEE;
    }else {
      baseFee = calculatorParameter10.getTOPUP_POS_FEE_AMOUNT();
      percentageFee = calculatorParameter10.getTOPUP_POS_FEE_PERCENTAGE();
      feeType = PrepaidMovementFeeType.TOPUP_POS_FEE;
    }
    List<PrepaidMovementFee10> feeList = getPrepaidEJBBean10().calculateFeeList(prepaidTopup10.getAmount().getValue(),baseFee,percentageFee, IvaType.PLUS_IVA,feeType);
    prepaidTopup10 = (PrepaidTopup10) getPrepaidEJBBean10().calculateFeeAndTotal(prepaidTopup10, feeList);

    if(PrepaidTopup10.WEB_MERCHANT_CODE.equals(prepaidTopup10.getMerchantCode())){
      //TODO: Verificar cuanto tiene que ser la comision carga web
      //Assert.assertEquals("Debe tener fee total 15", new BigDecimal(152L), prepaidTopup10.getFee().getValue());
      //Assert.assertEquals("Debe tener total 5000 - 15 = 4985", new BigDecimal(4762L), prepaidTopup10.getTotal().getValue());

    }else {
      Assert.assertEquals("Debe tener fee total 238", new BigDecimal(238L), prepaidTopup10.getFee().getValue());
      Assert.assertEquals("Debe tener total 5000 - 238 = 4762", new BigDecimal(4762L), prepaidTopup10.getTotal().getValue());

    }
  }

  @Test
  public void calculateFeeAndTotal_withdraw() throws Exception {
    PrepaidWithdraw10 prepaidWithdraw10 = buildPrepaidWithdrawV2();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(new BigDecimal(5000));
    prepaidWithdraw10.setAmount(amount);

    CalculatorParameter10 calculatorParameter10 = getPrepaidEJBBean10().getCalculationsHelper().getCalculatorParameter10();

    BigDecimal baseFee,percentageFee;
    PrepaidMovementFeeType feeType;
    if(PrepaidTopup10.WEB_MERCHANT_CODE.equals(prepaidWithdraw10.getMerchantCode())){
      baseFee = calculatorParameter10.getWITHDRAW_WEB_FEE_AMOUNT();
      percentageFee = calculatorParameter10.getWITHDRAW_WEB_FEE_PERCENTAGE();
      feeType = PrepaidMovementFeeType.WITHDRAW_WEB_FEE;
    }else {
      baseFee = calculatorParameter10.getWITHDRAW_POS_FEE_AMOUNT();
      percentageFee = calculatorParameter10.getWITHDRAW_POS_FEE_PERCENTAGE();
      feeType = PrepaidMovementFeeType.WITHDRAW_POS_FEE;
    }
    List<PrepaidMovementFee10> feeList = getPrepaidEJBBean10().calculateFeeList(prepaidWithdraw10.getAmount().getValue(),baseFee,percentageFee, IvaType.PLUS_IVA,feeType);

    prepaidWithdraw10 = (PrepaidWithdraw10) getPrepaidEJBBean10().calculateFeeAndTotal(prepaidWithdraw10, feeList);
    if(PrepaidTopup10.WEB_MERCHANT_CODE.equals(prepaidWithdraw10.getMerchantCode())){
      //TODO: Verificar cuanto tiene que ser la comision carga web
      //Assert.assertEquals("Debe tener fee total 15", new BigDecimal(238), prepaidWithdraw10.getFee().getValue());
      //Assert.assertEquals("Debe tener total 5000 + 15 = 5015", new BigDecimal(5238L), prepaidWithdraw10.getTotal().getValue());
    }else {
      Assert.assertEquals("Debe tener fee total 238", new BigDecimal(238), prepaidWithdraw10.getFee().getValue());
      Assert.assertEquals("Debe tener total 5000 - 238 = 4762", new BigDecimal(5238L), prepaidWithdraw10.getTotal().getValue());

    }
  }


}
