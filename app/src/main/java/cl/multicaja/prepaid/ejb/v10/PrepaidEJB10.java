package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.*;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception;

  void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest);

  PrepaidWithdraw10 withdrawUserBalance(Map<String, Object> headers, NewPrepaidWithdraw10 withdrawRequest) throws Exception;

  List<PrepaidTopup10> getUserTopups(Map<String, Object> headers, Long userId);

  PrepaidUserSignup10 initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup10 signupRequest);

  PrepaidUserSignup10 getUserSignup(Map<String, Object> headers, Long signupId);

  PrepaidCard10 issuePrepaidCard(Map<String, Object> headers, Long userId);

  /**
   *  Calcula la comision y total a cargar segun el el tipo de carga (POS/WEB)
   *
   * @param topup con el cual se calculara la comision y total
   * @throws IllegalStateException cuando el topup es null
   * @throws IllegalStateException cuando el topup.amount es null
   * @throws IllegalStateException cuando el topup.amount.value es null
   * @throws IllegalStateException cuando el topup.merchantCode es null o vacio
   */
  void calculateTopupFeeAndTotal(PrepaidTopup10 topup) throws Exception;

  /**
   *  Agrega la informacion para el voucher requerida por el POS/Switch
   *
   * @param topup al que se le agregara el voucher
   * @throws IllegalStateException si el topup es null
   * @throws IllegalStateException si el topup.amount es null
   * @throws IllegalStateException si el topup.amount.value es null
   */
  void addVoucherData(PrepaidTopup10 topup) throws Exception;

  /**
   * Calculadora de carga
   *
   * @param header
   * @param calculatorRequest
   * @return
   * @throws Exception
   */
  CalculatorTopupResponse10 topupCalculator(Map<String,Object> header, CalculatorRequest10 calculatorRequest) throws Exception;

  /**
   * Calculadora de retiro
   * @param header
   * @param calculatorRequest
   * @return
   * @throws Exception
   */
  CalculatorWithdrawalResponse10 withdrawalCalculator(Map<String,Object> header, CalculatorRequest10 calculatorRequest) throws Exception;
}
