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
   *  Calcula la comision y total segun el tipo (TOPUP/WITHDRAW) y el origen (POS/WEB)
   *
   * @param transaction con el cual se calculara la comision y total
   * @throws IllegalStateException cuando transaction es null
   * @throws IllegalStateException cuando transaction.amount es null
   * @throws IllegalStateException cuando transaction.amount.value es null
   * @throws IllegalStateException cuando transaction.merchantCode es null o vacio
   */
  void calculateFeeAndTotal(IPrepaidTransaction10 transaction) throws Exception;

  /**
   *  Agrega la informacion para el voucher requerida por el POS/Switch
   *
   * @param transaction al que se le agregara el voucher
   * @throws IllegalStateException si transaction es null
   * @throws IllegalStateException si transaction.amount es null
   * @throws IllegalStateException si transaction.amount.value es null
   */
  void addVoucherData(IPrepaidTransaction10 transaction) throws Exception;

  /**
   * Calculadora de carga
   *
   * @param header
   * @param userId
   * @param simulationNew
   * @return
   * @throws Exception
   */
  SimulationTopup10 topupSimulation(Map<String,Object> header, Long userId, SimulationNew10 simulationNew) throws Exception;

  /**
   * Calculadora de retiro
   *
   * @param header
   * @param userId
   * @param simulationNew
   * @return
   * @throws Exception
   */
  SimulationWithdrawal10 withdrawalSimulation(Map<String,Object> header, Long userId, SimulationNew10 simulationNew) throws Exception;

}
