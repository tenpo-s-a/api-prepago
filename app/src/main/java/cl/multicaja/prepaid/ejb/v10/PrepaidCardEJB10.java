package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.*;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidCardEJB10 {

  /**
   * permite crear una tarjeta prepago
   *
   * @param headers
   * @param prepaidCard
   * @return
   * @throws Exception
   */
  PrepaidCard10 createPrepaidCard(Map<String, Object> headers, PrepaidCard10 prepaidCard) throws Exception;

  /**
   * permite buscar tarjetas prepago
   *
   * @param headers
   * @param id
   * @param userId
   * @param expiration
   * @param status
   * @param processorUserId
   * @return
   * @throws Exception
   */
  List<PrepaidCard10> getPrepaidCards(Map<String, Object> headers, Long id, Long userId, Integer expiration, PrepaidCardStatus status, String processorUserId) throws Exception;

  /**
   * permite buscar una tarjeta prepago por su id
   *
   * @param headers
   * @param id
   * @return
   * @throws Exception
   */
  PrepaidCard10 getPrepaidCardById(Map<String, Object> headers, Long id) throws Exception;

  /**
   * permite buscar la ultima tarjeta prepago por su id de usuario prepago y estado
   *
   * @param headers
   * @param userId
   * @param status
   * @return
   * @throws Exception
   */
  PrepaidCard10 getLastPrepaidCardByUserIdAndStatus(Map<String, Object> headers, Long userId, PrepaidCardStatus status) throws Exception;

  /**
   * permite buscar la ultima tarjeta prepago por su id de usuario prepago y uno de los estado
   *
   * @param headers
   * @param userId
   * @param status
   * @return
   * @throws Exception
   */
  PrepaidCard10 getLastPrepaidCardByUserIdAndOneOfStatus(Map<String, Object> headers, Long userId, PrepaidCardStatus... status) throws Exception;

  /**
   * permite buscar la ultima tarjeta prepago por su id de usuario prepago
   *
   * @param headers
   * @param userId
   * @return
   * @throws Exception
   */
  PrepaidCard10 getLastPrepaidCardByUserId(Map<String, Object> headers, Long userId) throws Exception;

  /**
   * actualiza el estado de la tarjeta
   *
   * @param headers
   * @param id
   * @param status
   * @throws Exception
   */
  void updatePrepaidCardStatus(Map<String, Object> headers, Long id, PrepaidCardStatus status) throws Exception;

  /**
   * Permite realizar la actualizacion de los datos de una tarjeta
   *
   * @param headers
   * @param cardId
   * @param userId
   * @param oldStatus
   * @param prepaidCard
   * @throws Exception
   */
  void updatePrepaidCard(Map<String, Object> headers, Long cardId, Long userId, PrepaidCardStatus oldStatus, PrepaidCard10 prepaidCard) throws Exception;

  /**
   * Activa la tarjeta mastercard (level 1 a level 2)
   *
   * @param headers
   * @param userUuid
   * @param accountUuid
   * @return
   * @throws Exception
   */
  PrepaidCardResponse10 upgradePrepaidCard(Map<String, Object> headers, String userUuid, String accountUuid) throws Exception;

  /**
   *  busca una tarjeta por pan y numero de contrato
   * @param headers
   * @param pan pan encriptado
   * @param processorUserId numero de contrato
   * @return
   * @throws Exception
   */
  PrepaidCard10 getPrepaidCardByPanAndProcessorUserId(Map<String, Object> headers, String pan, String processorUserId) throws Exception;

  /**
   *  Busca una tarjeta por id y publica evento de tarjeta creada
   * @param cardId id interno de la tarjeta
   * @throws Exception
   */
  void publishCardEvent(String externalUserId, String accountUuid, Long cardId, String endpoint) throws Exception;
}
