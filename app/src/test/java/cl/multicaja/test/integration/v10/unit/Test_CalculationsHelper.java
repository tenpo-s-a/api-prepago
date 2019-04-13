package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.model.v10.CalculatorParameter10;
import cl.multicaja.prepaid.model.v11.IvaType;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

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

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(119), commission);
  }

  @Test
  public void testCalculateFee_199999() {

    BigDecimal amount = BigDecimal.valueOf(199999);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(119), commission);
  }

  @Test
  public void testCalculateFee_167899() {

    BigDecimal amount = BigDecimal.valueOf(167899);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(119), commission);
  }

  @Test
  public void testCalculateFee_168067() {

    BigDecimal amount = BigDecimal.valueOf(168067);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculationsHelper.calculateFee(amount, calculationsHelper.getCalculatorParameter10().getTOPUP_POS_FEE_PERCENTAGE());

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(119), commission);
  }

  @Test
  public void testCalculateIva_plusIva() {
    BigDecimal originalFee = BigDecimal.valueOf(100L);
    BigDecimal iva = getCalculationsHelper().calculateIncludedIva(originalFee);
    Assert.assertEquals("Debe ser 16", BigDecimal.valueOf(16), iva);
  }

  @Test
  public void testCalculateIva_ivaIncluded() {
    BigDecimal originalFee = BigDecimal.valueOf(100L);
    BigDecimal iva = getCalculationsHelper().calculateIva(originalFee);
    Assert.assertEquals("Debe ser 19", BigDecimal.valueOf(19), iva);
  }

  @Test
  public void testCalculateAmountAndIva_IvaIncluded() {
    BigDecimal originalFee = new BigDecimal(100L);
    Map<String, BigDecimal> feeAndIva = getCalculationsHelper().calculateFeeAndIva(originalFee, IvaType.IVA_INCLUDED);

    BigDecimal fee = feeAndIva.get("fee");
    BigDecimal iva = feeAndIva.get("iva");
    Assert.assertEquals("Fee debe ser 84", BigDecimal.valueOf(84), fee);
    Assert.assertEquals("Iva debe ser 16", BigDecimal.valueOf(16), iva);
  }

  @Test
  public void testCalculateAmountAndIva_PlusIva() {
    BigDecimal originalFee = new BigDecimal(100L);
    Map<String, BigDecimal> feeAndIva = getCalculationsHelper().calculateFeeAndIva(originalFee, IvaType.PLUS_IVA);

    BigDecimal fee = feeAndIva.get("fee");
    BigDecimal iva = feeAndIva.get("iva");
    Assert.assertEquals("Fee debe ser 100", BigDecimal.valueOf(100), fee);
    Assert.assertEquals("Iva debe ser 19", BigDecimal.valueOf(19), iva);
  }

  @Test
  public void testCalculatePca() {

    BigDecimal amount = BigDecimal.valueOf(1000);

    BigDecimal pca = getCalculationsHelper().calculatePca(amount);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(743.64), pca);
  }

  @Test
  public void testCalculateEed() throws Exception {

    BigDecimal amount = BigDecimal.valueOf(1000);

    BigDecimal eed = getCalculationsHelper().calculateEed(amount);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1.16), eed);
  }


  @Test
  public void testCalculateAmountFromEed() throws Exception{
    {
      BigDecimal amount = BigDecimal.valueOf(1.16);

      BigDecimal baseAmount = getCalculationsHelper().calculateAmountFromEed(amount);

      Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1010), baseAmount);
    }

    {
      BigDecimal amount = BigDecimal.valueOf(1.15);

      BigDecimal baseAmount = getCalculationsHelper().calculateAmountFromEed(amount);

      Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1000), baseAmount);
    }
  }
}
