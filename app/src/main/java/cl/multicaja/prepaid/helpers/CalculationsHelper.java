package cl.multicaja.prepaid.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @autor vutreras
 */
public class CalculationsHelper {

  private static Log log = LogFactory.getLog(CalculationsHelper.class);

  public static final int ONE_HUNDRED = 100;

  // TODO: externalizar estos porcentajes?
  public static final BigDecimal TOPUP_POS_FEE_PERCENTAGE = new BigDecimal(0.5);
  public static final BigDecimal TOPUP_WEB_FEE_PERCENTAGE = new BigDecimal(0);
  public static final BigDecimal TOPUP_WEB_FEE_AMOUNT = new BigDecimal(0);

  public static final BigDecimal WITHDRAW_POS_FEE_PERCENTAGE = new BigDecimal(0.5);
  public static final BigDecimal WITHDRAW_WEB_FEE_PERCENTAGE = new BigDecimal(0.5);
  public static final BigDecimal WITHDRAW_WEB_FEE_AMOUNT = new BigDecimal(100);

  public static final double IVA = 1.19;

  /**
   * Calcula comision en la formula: MAX(100; 0,5% * amount) + IVA
   * @param amount
   * @param feePercentage
   * @return
   */
  public static BigDecimal calculateFee(BigDecimal amount, BigDecimal feePercentage) {
    BigDecimal percentage = (amount.multiply(feePercentage)).divide(BigDecimal.valueOf(ONE_HUNDRED));

    BigDecimal max = BigDecimal.valueOf(100).max(percentage);

    BigDecimal result = max.multiply(BigDecimal.valueOf(IVA));

    BigDecimal rounded = result.setScale(0, RoundingMode.DOWN);
    log.info("Amount: " + amount + ", feePercentage: " + feePercentage + ", percentage calculated: " + percentage + ", max: " + max + ", with iva: " + result + ", final: " + rounded);
    return rounded;
  }


}
