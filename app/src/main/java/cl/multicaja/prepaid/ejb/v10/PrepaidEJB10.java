package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.model.v10.*;
import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  /**
   *
   * @return
   * @throws Exception
   */
  Map<String, Object> info() throws Exception;

  /**
   * V2 Usuario tempo
   * @param headers
   * @param userId
   * @param topupRequest
   * @param fromEndPoint
   * @return
   * @throws Exception
   */
  PrepaidTopup10 topupUserBalance(Map<String, Object> headers, String userId, NewPrepaidTopup10 topupRequest, Boolean fromEndPoint) throws Exception;

  /**
   *
   * @param headers
   * @param topupRequest
   * @throws Exception
   */
  void reverseTopupUserBalance(Map<String, Object> headers, String userId, NewPrepaidTopup10 topupRequest,Boolean fromEndPoint) throws Exception;

  /**
   *
   * @param headers
   * @param withdrawRequest
   * @return
   * @throws Exception
   */
  PrepaidWithdraw10 withdrawUserBalance(Map<String, Object> headers, String userId, NewPrepaidWithdraw10 withdrawRequest, Boolean fromEndpoint) throws Exception;

  /**
   *
   * @param headers
   * @param withdrawRequest
   * @throws Exception
   */
  void reverseWithdrawUserBalance(Map<String, Object> headers,String extUserId, NewPrepaidWithdraw10 withdrawRequest, Boolean fromEndPoint) throws Exception;

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
  IPrepaidTransaction10 calculateFeeAndTotal(IPrepaidTransaction10 transaction) throws Exception;

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
  @Deprecated
  SimulationTopup10 topupSimulation(Map<String, Object> headers, PrepaidUser10 prepaidUser10, SimulationNew10 simulationNew)throws Exception;
  /**
   *
   * @param headers
   * @param userIdMc
   * @param simulationNew
   * @return
   * @throws Exception
   */
  @Deprecated
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
  @Deprecated
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
  PrepaidTransactionExtend10 getTransactions(Map<String,Object> headers, Long userIdMc, String startDate, String endDate, Integer count) throws Exception;
  /**
   *
   * @param headers
   * @param rut
   * @return
   * @throws Exception
   */
  PrepaidUser10 findPrepaidUser(Map<String, Object> headers, Integer rut) throws Exception;

  /**
   * Busca en las colas erroneas el mensaje por el id y lo vuelve a inyectar para ser reprocesado.
   * @param headers
   * @param reprocesQueue
   * @throws Exception
   */
  String reprocessQueue(Map<String, Object> headers, ReprocesQueue reprocesQueue) throws Exception;

}
