package cl.multicaja.prepaid.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class CalculationsHelper {

  private static Log log = LogFactory.getLog(CalculationsHelper.class);

  public static final int ONE_HUNDRED = 100;

  // TODO: externalizar estos porcentajes?
  public static final BigDecimal TOPUP_POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  public static final BigDecimal TOPUP_WEB_COMMISSION_PERCENTAGE = new BigDecimal(0);
  public static final BigDecimal TOPUP_WEB_COMMISSION_AMOUNT = new BigDecimal(0);

  public static final BigDecimal WITHDRAW_POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  public static final BigDecimal WITHDRAW_WEB_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  public static final BigDecimal WITHDRAW_WEB_COMMISSION_AMOUNT = new BigDecimal(100);


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
   * @param comissionPercentage
   * @return
   */
  public static BigDecimal calculateComission(BigDecimal amount, BigDecimal comissionPercentage) {
    double percentage = (amount.longValue() * comissionPercentage.doubleValue() / 100);
    double max = Math.max(ONE_HUNDRED, percentage);
    BigDecimal result = BigDecimal.valueOf(Math.round(max * IVA));
    log.info("Amount: " + amount + ", comissionPercentage: " + comissionPercentage + ", percentage calculated: " + percentage + ", max: " + max + ", result with iva: " + result);
    return result;
  }

  /**
   * Calcula el: para cuanto alcanza
   *
   * @param amount
   * @return
   */
  public static BigDecimal calculatePca(BigDecimal amount) {
    return BigDecimal.valueOf((amount.doubleValue()-240)/1.022);
  }

  /**
   * Calcula el: eed
   *
   * @param amount
   * @return
   */
  public static BigDecimal calculateEed(BigDecimal amount) {
    double pca = calculatePca(amount).doubleValue();
    return BigDecimal.valueOf(pca / USD_VALUE);
  }
}
