package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.CalculationsHelper;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @autor vutreras
 */
public class Test_CalculationsHelper extends TestBaseUnit {

  @Test
  public void testCalculatePosTopupFee() {

    for (int i = 0; i < 50; i++) {

      BigDecimal amount = BigDecimal.valueOf(numberUtils.random(1000, 100000));

      // MAX(100; 0,5% * amount) + IVA
      BigDecimal commission = getCalculationsHelper().calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

      BigDecimal feeOk = BigDecimal.valueOf(100).max((amount.multiply( calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE()).divide(BigDecimal.valueOf(100)))).multiply(BigDecimal.valueOf(1.19)).setScale(0, RoundingMode.HALF_UP);

      Assert.assertEquals("deben ser iguales", feeOk, commission);
    }
  }

  @Test
  public void testCalculatePosWithdrawFee() {

    for (int i = 0; i < 50; i++) {

      BigDecimal amount = BigDecimal.valueOf(numberUtils.random(1000, 100000));

      // MAX(100; 0,5% * amount) + IVA
      BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

      BigDecimal feeOk = BigDecimal.valueOf(100).max((amount.multiply(calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE()).divide(BigDecimal.valueOf(100)))).multiply(BigDecimal.valueOf(1.19)).setScale(0, RoundingMode.HALF_UP);

      Assert.assertEquals("deben ser iguales", feeOk, commission);
    }
  }

  @Test
  public void testCalculateFee_RandomPercentage() {

    numberUtils.random(0.1, 0.999999);

    for (int i = 0; i < 50; i++) {

      double random = numberUtils.random(0.1, 99.999999);

      BigDecimal amount = BigDecimal.valueOf(numberUtils.random(1000, 100000));

      // MAX(100; 0,5% * amount) + IVA
      BigDecimal commission = calculationsHelper.calculateFee(amount, BigDecimal.valueOf(random));

      BigDecimal feeOk = BigDecimal.valueOf(100).max((amount.multiply(BigDecimal.valueOf(random)).divide(BigDecimal.valueOf(100)))).multiply(BigDecimal.valueOf(1.19)).setScale(0, RoundingMode.HALF_UP);

      Assert.assertEquals("deben ser iguales", feeOk, commission);
    }
  }

  @Test
  public void testCalculateFee_20999() {

    BigDecimal amount = BigDecimal.valueOf(20999);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(125), commission);
  }

  @Test
  public void testCalculateFee_199999() {

    BigDecimal amount = BigDecimal.valueOf(199999);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1190), commission);
  }

  @Test
  public void testCalculateFee_167899() {

    BigDecimal amount = BigDecimal.valueOf(167899);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(999), commission);
  }

  @Test
  public void testCalculateFee_168067() {

    BigDecimal amount = BigDecimal.valueOf(168067);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1000), commission);
  }

  @Test
  public void testCalculatePca() {

    BigDecimal amount = BigDecimal.valueOf(1000);

    BigDecimal pca = CalculationsHelper.calculatePca(amount);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(743.64), pca);
  }

  @Test
  public void testCalculateEed() {

    BigDecimal amount = BigDecimal.valueOf(1000);

    BigDecimal eed = CalculationsHelper.calculateEed(amount);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1.16), eed);
  }


  @Test
  public void testCalculateAmountFromEed() {
    {
      BigDecimal amount = BigDecimal.valueOf(1.16);

      BigDecimal baseAmount = CalculationsHelper.calculateAmountFromEed(amount);

      Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1010), baseAmount);
    }

    {
      BigDecimal amount = BigDecimal.valueOf(1.15);

      BigDecimal baseAmount = CalculationsHelper.calculateAmountFromEed(amount);

      Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1000), baseAmount);
    }
  }
}
