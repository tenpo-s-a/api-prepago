package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
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

  PrepaidUserSignup10 initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup10 signupRequest) throws Exception;

  PrepaidUser10 finishSignup(Map<String, Object> headers, Long userIdMc) throws Exception;


  /**
   * Retorna la informacion de la ultima tarjeta del usuario
   *
   * @param headers
   * @param userIdMc ID usuario MC
   * @return tarjeta prepago
   * @throws BadRequestException cuando userIdMc es null o 0
   * @throws NotFoundException cuando el usuario no existe
   * @throws ValidationException cuando el usuario esta bloqueado o borrado
   * @throws NotFoundException cuando el usuario no esta registrado en prepago
   * @throws ValidationException cuando el usuario prepago esta bloqueado o borrado
   * @throws ValidationException cuando el usuario prepago tiene la primera carga pendiente
   * @throws ValidationException cuando el usuario prepago tiene la primera carga en proceso
   */
  PrepaidCard10 getPrepaidCard(Map<String, Object> headers, Long userIdMc) throws Exception;

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
   *
   * @param headers
   * @param prepaidUser10
   * @param simulationNew
   * @return
   * @throws Exception
   */
  SimulationTopup10 topupSimulation(Map<String, Object> headers, PrepaidUser10 prepaidUser10, SimulationNew10 simulationNew)throws Exception;
  /**
   *
   * @param headers
   * @param userIdMc
   * @param simulationNew
   * @return
   * @throws Exception
   */
  SimulationTopupGroup10 topupSimulationGroup(Map<String,Object> headers, Long userIdMc, SimulationNew10 simulationNew) throws Exception;

  /**
   * Calculadora de retiro
   *
   * @param headers
   * @param userIdMc id de usuario multicaja
   * @param simulationNew
   * @return
   * @throws Exception
   */
  SimulationWithdrawal10 withdrawalSimulation(Map<String,Object> headers, Long userIdMc, SimulationNew10 simulationNew) throws Exception;

  /**
   *
   * @param headers
   * @param userIdMc
   * @return
   * @throws Exception
   */
  PrepaidUser10 getPrepaidUser(Map<String, Object> headers, Long userIdMc) throws Exception;


  /**
   *
   * @param headers
   * @param userIdMc
   * @param startDate
   * @param endDate
   * @param count
   * @return
   * @throws Exception
   */
  List<PrepaidTransaction10> getTransactions(Map<String,Object> headers, Long userIdMc, String startDate, String endDate, Integer count) throws Exception;
  /**
   *
   * @param headers
   * @param rut
   * @return
   * @throws Exception
   */
  PrepaidUser10 findPrepaidUser(Map<String, Object> headers, Integer rut) throws Exception;

  /**
   *
   * @param headers
   * @param userIdMc
   * @throws Exception
   */
  void lockPrepaidCard(Map<String, Object> headers, Long userIdMc) throws Exception;

  /**
   *
   * @param headers
   * @param userIdMc
   * @throws Exception
   */
  void unlockPrepaidCard(Map<String, Object> headers, Long userIdMc) throws Exception;

}
