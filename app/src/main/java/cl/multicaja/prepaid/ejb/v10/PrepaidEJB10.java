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
   * Calcula la comision total y total segun el tipo (TOPUP/WITHDRAW)
   *
   * @param transaction
   * @param feeList
   * @return
   * @throws Exception
   */
  IPrepaidTransaction10 calculateFeeAndTotal(IPrepaidTransaction10 transaction, List<PrepaidMovementFee10> feeList) throws Exception;

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
   * @param userIdMc
   * @return
   * @throws Exception
   */
  PrepaidUser10 getPrepaidUser(Map<String, Object> headers, Long userIdMc) throws Exception;


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
