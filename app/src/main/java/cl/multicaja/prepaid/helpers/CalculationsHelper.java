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


  public static final BigDecimal CALCULATOR_TOPUP_WEB_COMMISSION_AMOUNT = new BigDecimal(0);
  public static final BigDecimal CALCULATOR_TOPUP_POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);

  public static final BigDecimal CALCULATOR_WITHDRAW_WEB_COMMISSION_AMOUNT = new BigDecimal(100);
  public static final BigDecimal CALCULATOR_WITHDRAW_POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);

  public static final double IVA = 1.19;

  //TODO: Valor dolar debe ser obtenido desde algun servicio.
  public static final Integer USD_VALUE = 645;

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

  /**
   * Calcula el: para cuanto alcanza (pca)
   *
   * @param amount
   * @return
   */
  public static double calculatePca(BigDecimal amount) {
    if (amount == null) {
      return 0;
    }
    return (amount.doubleValue() - 240) / 1.022;
  }

  /**
   * Calcula el: equivalente en dolares (eed)
   *
   * @param amount
   * @return
   */
  public static double calculateEed(BigDecimal amount) {
    if (amount == null) {
      return 0;
    }
    double pca = calculatePca(amount);
    return pca / USD_VALUE;
  }

}
