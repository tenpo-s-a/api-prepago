package cl.multicaja.test.v10.unit;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * @autor vutreras
 */
public class Test_CalculationsHelper extends TestBaseUnit {

  @Test
  public void testCalculatePosTopupFee() {

    for (int i = 0; i < 50; i++) {

      BigDecimal amount = BigDecimal.valueOf(numberUtils.random(1000, 100000));

      // MAX(100; 0,5% * amount) + IVA
      BigDecimal commission = calculateFee(amount, TOPUP_POS_FEE_PERCENTAGE);

      BigDecimal feeOk = BigDecimal.valueOf(100).max((amount.multiply(TOPUP_POS_FEE_PERCENTAGE).divide(BigDecimal.valueOf(100)))).multiply(BigDecimal.valueOf(1.19)).setScale(0, RoundingMode.DOWN);

      Assert.assertEquals("deben ser iguales", feeOk, commission);
    }
  }

  @Test
  public void testCalculatePosWithdrawFee() {

    for (int i = 0; i < 50; i++) {

      BigDecimal amount = BigDecimal.valueOf(numberUtils.random(1000, 100000));

      // MAX(100; 0,5% * amount) + IVA
      BigDecimal commission = calculateFee(amount, TOPUP_POS_FEE_PERCENTAGE);

      BigDecimal feeOk = BigDecimal.valueOf(100).max((amount.multiply(TOPUP_POS_FEE_PERCENTAGE).divide(BigDecimal.valueOf(100)))).multiply(BigDecimal.valueOf(1.19)).setScale(0, RoundingMode.DOWN);

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
      BigDecimal commission = calculateFee(amount, BigDecimal.valueOf(random));

      BigDecimal feeOk = BigDecimal.valueOf(100).max((amount.multiply(BigDecimal.valueOf(random)).divide(BigDecimal.valueOf(100)))).multiply(BigDecimal.valueOf(1.19)).setScale(0, RoundingMode.DOWN);

      Assert.assertEquals("deben ser iguales", feeOk, commission);
    }
  }
}
