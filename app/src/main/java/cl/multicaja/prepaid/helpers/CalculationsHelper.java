package cl.multicaja.prepaid.helpers;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.ejb.v10.MastercardCurrencyUpdateEJBBean10;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.CalculatorParameter10;
import cl.multicaja.prepaid.model.v11.IvaType;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor vutreras
 */
public class CalculationsHelper {


  private static CalculationsHelper instance;
  private static Log log = LogFactory.getLog(CalculationsHelper.class);
  private final int ONE_HUNDRED = 100;
  private static CalculatorParameter10 calculatorParameter10;

  @EJB
  private MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10;

  public MastercardCurrencyUpdateEJBBean10 getMastercardCurrencyUpdateEJBBean10() {
    if(mastercardCurrencyUpdateEJBBean10 == null) {
      mastercardCurrencyUpdateEJBBean10 = new MastercardCurrencyUpdateEJBBean10();
    }
    return mastercardCurrencyUpdateEJBBean10;
  }

  public void setMastercardCurrencyUpdateEJBBean10(MastercardCurrencyUpdateEJBBean10 mastercardCurrencyUpdateEJBBean10) {
    this.mastercardCurrencyUpdateEJBBean10 = mastercardCurrencyUpdateEJBBean10;
  }

  public CalculationsHelper() {

  }

  /**
   * retorna la instancia unica como singleton
   * @return
   */
  public  static CalculationsHelper getInstance() {
    if (instance == null) {
      instance = new CalculationsHelper();
      try {
        calculatorParameter10 = getParametersUtil().getObject("api-prepaid", "calculator_percentage", "v10", CalculatorParameter10.class);
      }catch (Exception e){

      }
    }
    return instance;
  }

  public  int getOneHundred() { return ONE_HUNDRED; }

  public CalculatorParameter10 getCalculatorParameter10() {
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
   * Dado un monto total y un tipo de iva ("iva incluido" o "mas iva") calcula los dos valores, monto e iva.
   * @param totalFee
   * @param ivaType
   * @return
   */
  public Map<String, BigDecimal> calculateFeeAndIva(BigDecimal totalFee, IvaType ivaType) {
    BigDecimal iva = BigDecimal.ZERO;
    BigDecimal fee = totalFee;

    if(IvaType.IVA_INCLUDED.equals(ivaType)) {
      iva = calculateIncludedIva(totalFee);
      fee = totalFee.subtract(iva);
    } else if (IvaType.PLUS_IVA.equals(ivaType)) {
      fee = totalFee;
      iva = calculateIva(totalFee);
    }

    Map<String, BigDecimal> amountAndIva = new HashMap<>();
    amountAndIva.put("fee", fee.setScale(0, RoundingMode.HALF_UP));
    amountAndIva.put("iva", iva.setScale(0, RoundingMode.HALF_UP));
    return amountAndIva;
  }

  /**
   * Calcula el iva dado el monto entregado: (iva = amount * 0.19)
   * @param amount
   * @return
   */
  public BigDecimal calculateIva(BigDecimal amount){
    BigDecimal result = amount.multiply(BigDecimal.valueOf(calculatorParameter10.getIVA())).subtract(amount);
    BigDecimal rounded = result.setScale(0, RoundingMode.HALF_UP);
    return rounded;
  }

  /**
   * Calcula el iva cuando el totalAmount entregado es el valor con el iva incluido (totalAmount = iva + monto inicial).
   * @param totalAmount
   * @return
   */
  public BigDecimal calculateIncludedIva(BigDecimal totalAmount) {
    BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(calculatorParameter10.getIVA()), 0, RoundingMode.HALF_UP);
    return totalAmount.subtract(baseAmount);
  }

  /**
   * Agrega el IVA al monto
   *
   * @param amount
   * @return
   */
  public BigDecimal addIva(BigDecimal amount) {
    BigDecimal iva = amount.multiply(BigDecimal.valueOf(calculatorParameter10.getIVA()));
    log.info(String.format("Amount: [%s], Iva: [%s]", amount, iva));
    return amount.intValue() > 0 ? iva : BigDecimal.ZERO;
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
  public BigDecimal calculateEed(BigDecimal amount) throws Exception {
    if (amount == null) {
      return BigDecimal.valueOf(0d);
    }
    double pca = _calculatePca(amount);
    return BigDecimal.valueOf(pca / getUsdValue()).setScale(2, RoundingMode.CEILING);
  }

  /**
   * Calcula el monto a partir del equivalente en dolares (eed)
   * @param eed
   * @return
   */
  public BigDecimal calculateAmountFromEed(BigDecimal eed) throws Exception {
    if (eed == null) {
      return BigDecimal.valueOf(0d);
    }

    BigDecimal amount = BigDecimal.valueOf(((eed.doubleValue() * getUsdValue()) * 1.022) + 240).setScale(0, RoundingMode.CEILING);

    // Se redondea el monto al 10 mas cercano
    if((amount.intValue() % 10 ) > 0) {
      amount = BigDecimal.valueOf(amount.intValue() + ( 10 - ((amount.intValue() % 10))));
    }

    return amount;
  }

  /**
   * @return
   */
  public Double getUsdValue() throws Exception {
    if(ConfigUtils.isEnvTest() || ConfigUtils.isEnvCI()) {
      return Double.valueOf(645);
    }
    else {
      return getMastercardCurrencyUpdateEJBBean10().getCurrencyUsd().getSellCurrencyConvertion();
    }
  }
  
  /**
   * Para uso en calculo del dolar valor día.
   */
  public static final Double dayCurrencyVariation = 1.025;

  /**
   *
   * @param balance
   * @return
   */
  public NewAmountAndCurrency10 calculatePcaMain(NewAmountAndCurrency10 balance) {
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
  public NewAmountAndCurrency10 calculatePcaSecondary(NewAmountAndCurrency10 balance, NewAmountAndCurrency10 pcaMain) throws Exception {
    //https://www.pivotaltracker.com/story/show/158367667
    if (balance == null || pcaMain == null) {
      return null;
    }
    //por defecto debe ser 0
    NewAmountAndCurrency10 pcaSecondary = new NewAmountAndCurrency10(BigDecimal.valueOf(0d).setScale(2, RoundingMode.CEILING), CodigoMoneda.USA_USD);
    //solamente se debe calcular el pca si el saldo es mayor a 0 y el pcaMain es mayor a 0
    if (balance.getValue().compareTo(BigDecimal.ZERO) > 0 && pcaMain.getValue().compareTo(BigDecimal.ZERO) > 0) {
      pcaSecondary.setValue(calculateEed(balance.getValue()));
    }
    return pcaSecondary;
  }

  public static ParametersUtil getParametersUtil() {
    return ParametersUtil.getInstance();
  }

  /**
   * Calcula el monto de un porcentaje
   * @param amount
   * @return
   */
  public BigDecimal calculatePercentageValue(BigDecimal amount, BigDecimal percentage) throws Exception {
    if(amount == null) {
      throw new Exception("Amount is null");
    }
    if(percentage == null) {
      throw new Exception("Percentage is null");
    }

    if(percentage.doubleValue() > 1){
      percentage = percentage.subtract(BigDecimal.ONE);
    }
    return amount.multiply(percentage);
  }

}
