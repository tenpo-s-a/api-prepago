package cl.multicaja.test.v10.unit;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * @autor vutreras
 */
public class Test_CalculationsHelper extends TestBaseUnit {

  @Test
  public void testCalculateComission() {

    for (int i = 0; i < 20; i++) {

      BigDecimal amount = BigDecimal.valueOf(numberUtils.random(1000, 100000));

      // MAX(100; 0,5% * amont) + IVA
      BigDecimal commission = calculateComission(amount, TOPUP_POS_COMMISSION_PERCENTAGE);

      BigDecimal comissionOk = BigDecimal.valueOf(Math.round(Math.max(100, (amount.longValue() * 0.5 / 100)) * 1.19));

      Assert.assertEquals("deben ser iguales", comissionOk, commission);
    }
  }
}
