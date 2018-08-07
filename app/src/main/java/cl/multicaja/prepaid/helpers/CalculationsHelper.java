package cl.multicaja.prepaid.helpers;

import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.calculatorParameter10;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @autor vutreras
 */
public class CalculationsHelper {


  private static CalculationsHelper instance;
  private static Log log = LogFactory.getLog(CalculationsHelper.class);
  private static final int ONE_HUNDRED = 100;
  private static calculatorParameter10 calculatorParameter10;
  public CalculationsHelper() {

  }

  /**
   * retorna la instancia unica como singleton
   * @return
   */
  public static CalculationsHelper getInstance() {
    if (instance == null) {
      instance = new CalculationsHelper();
      try {
        calculatorParameter10 = getParametersUtil().getObject("api-prepaid", "calculator_percentage", "v10", calculatorParameter10.class);
      }catch (Exception e){

      }
    }
    return instance;
  }

  public static int getOneHundred() {
    return ONE_HUNDRED;
  }

  public calculatorParameter10 getPercentage10() {
    return calculatorParameter10;
  }


  /**
   * Calcula comision en la formula: MAX(100; 0,5% * amount) + IVA
   *
   * @param amount
   * @param feePercentage
   * @return
   */

  public BigDecimal calculateFee(BigDecimal amount, BigDecimal feePercentage) {
    BigDecimal percentage = (amount.multiply(feePercentage)).divide(BigDecimal.valueOf(ONE_HUNDRED));

    BigDecimal max = BigDecimal.valueOf(100).max(percentage);

    BigDecimal result = max.multiply(BigDecimal.valueOf(calculatorParameter10.getIVA()));
    // Se redondea de la mitad hacia arriba
    BigDecimal rounded = result.setScale(0, RoundingMode.HALF_UP);
    log.info("Amount: " + amount + ", feePercentage: " + feePercentage + ", percentage calculated: " + percentage + ", max: " + max + ", with iva: " + result + ", final: " + rounded);
    return rounded;
  }

  /**
   * Calcula el: para cuanto alcanza (pca)
   *
   * @param amount
   * @return
   */
  private static double _calculatePca(BigDecimal amount) {
    if (amount == null) {
      return 0d;
    }
    return (amount.doubleValue() - 240) / 1.022;
  }

  /**
   * Calcula el: para cuanto alcanza (pca)
   *
   * @param amount
   * @return
   */
  public static BigDecimal calculatePca(BigDecimal amount) {
    return BigDecimal.valueOf(_calculatePca(amount)).setScale(2, RoundingMode.CEILING);
  }

  /**
   * Calcula el: equivalente en dolares (eed)
   *
   * @param amount
   * @return
   */
  public static BigDecimal calculateEed(BigDecimal amount) {
    if (amount == null) {
      return BigDecimal.valueOf(0d);
    }
    double pca = _calculatePca(amount);
    return BigDecimal.valueOf(pca / getUsdValue()).setScale(2, RoundingMode.CEILING);
  }

  /**
   * @return
   */
  public static Integer getUsdValue() {
    //TODO quizas se saca de algun servicio externo
    return 645;
  }

  /**
   *
   * @param balance
   * @return
   */
  public static NewAmountAndCurrency10 calculatePcaMain(NewAmountAndCurrency10 balance) {
    //https://www.pivotaltracker.com/story/show/158367667
    if (balance == null) {
      return null;
    }
    //por defecto debe ser 0
    NewAmountAndCurrency10 pcaMain = new NewAmountAndCurrency10(BigDecimal.valueOf(0L));
    //solamente se debe calcular el pca si el saldo es mayor a 0
    if (balance.getValue().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal pca = calculatePca(balance.getValue());
      if (pca.compareTo(BigDecimal.ZERO) > 0) {
        pcaMain.setValue(pca);
      }
    }
    return pcaMain;
  }

  /**
   *
   * @param balance
   * @param pcaMain
   * @return
   */
  public static NewAmountAndCurrency10 calculatePcaSecondary(NewAmountAndCurrency10 balance, NewAmountAndCurrency10 pcaMain) {
    //https://www.pivotaltracker.com/story/show/158367667
    if (balance == null || pcaMain == null) {
      return null;
    }
    //por defecto debe ser 0
    NewAmountAndCurrency10 pcaSecondary = new NewAmountAndCurrency10(BigDecimal.valueOf(0d).setScale(2, RoundingMode.CEILING), CodigoMoneda.USA_USN);
    //solamente se debe calcular el pca si el saldo es mayor a 0 y el pcaMain es mayor a 0
    if (balance.getValue().compareTo(BigDecimal.ZERO) > 0 && pcaMain.getValue().compareTo(BigDecimal.ZERO) > 0) {
      pcaSecondary.setValue(calculateEed(balance.getValue()));
    }
    return pcaSecondary;
  }

  public static ParametersUtil getParametersUtil() {
    return ParametersUtil.getInstance();
  }
}
