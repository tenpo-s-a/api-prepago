package cl.multicaja.test.v10.unit;

import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.model.v10.Percentage10;
import cl.multicaja.users.utils.ParametersUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static cl.multicaja.prepaid.helpers.CalculationsHelper.TOPUP_POS_FEE_PERCENTAGE;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.calculateFee;

/**
 * @autor vutreras
 */
public class Test_CalculationsHelper extends TestBaseUnit {

  private ParametersUtil parameterUtil = ParametersUtil.getInstance();
  @Test
  public void example(){

    Percentage10 percentage10 = new Percentage10();
    percentage10.setTOPUP_POS_FEE_PERCENTAGE(new BigDecimal(0.5));
    percentage10.setTOPUP_WEB_FEE_PERCENTAGE(new BigDecimal(0));
    percentage10.setTOPUP_WEB_FEE_AMOUNT(new BigDecimal(0));
    percentage10.setWITHDRAW_POS_FEE_PERCENTAGE(new BigDecimal(0.5));
    percentage10.setWITHDRAW_WEB_FEE_PERCENTAGE(new BigDecimal(0.5));
    percentage10.setWITHDRAW_WEB_FEE_AMOUNT(new BigDecimal(100));
    percentage10.setCALCULATOR_TOPUP_WEB_FEE_AMOUNT(new BigDecimal(0));
    percentage10.setCALCULATOR_TOPUP_POS_FEE_PERCENTAGE(new BigDecimal(0.5));
    percentage10.setCALCULATOR_WITHDRAW_WEB_FEE_AMOUNT(new BigDecimal(100));
    percentage10.setCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE(new BigDecimal(0.5));
    percentage10.setOPENING_FEE(new BigDecimal(990));
    percentage10.setIVA(1.19);
    percentage10.setMAX_AMOUNT_BY_USER(500000);
    System.out.println(JsonUtils.getJsonParser().toJson(percentage10));
  }

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

  @Test
  public void testCalculateFee_20999() {

    BigDecimal amount = BigDecimal.valueOf(20999);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculateFee(amount, TOPUP_POS_FEE_PERCENTAGE);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(124), commission);
  }

  @Test
  public void testCalculateFee_199999() {

    BigDecimal amount = BigDecimal.valueOf(199999);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculateFee(amount, TOPUP_POS_FEE_PERCENTAGE);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(1189), commission);
  }

  @Test
  public void testCalculateFee_167899() {

    BigDecimal amount = BigDecimal.valueOf(167899);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculateFee(amount, TOPUP_POS_FEE_PERCENTAGE);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(998), commission);
  }

  @Test
  public void testCalculateFee_168067() {

    BigDecimal amount = BigDecimal.valueOf(168067);

    // MAX(100; 0,5% * amount) + IVA
    BigDecimal commission = calculateFee(amount, TOPUP_POS_FEE_PERCENTAGE);

    Assert.assertEquals("deben ser iguales", BigDecimal.valueOf(999), commission);
  }

}
