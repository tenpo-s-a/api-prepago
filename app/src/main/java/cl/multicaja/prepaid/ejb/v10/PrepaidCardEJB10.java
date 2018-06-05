package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;

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
   * permite buscar una tarjeta prepago por su id de usuario prepago
   *
   * @param headers
   * @param userId
   * @param status
   * @return
   * @throws Exception
   */
  PrepaidCard10 getPrepaidCardByUserId(Map<String, Object> headers, Long userId, PrepaidCardStatus status) throws Exception;

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
   *  Permite realizar la actualizacion de los datos de una tarjeta
   *
  */
  boolean updatePrepaidCard(Map<String, Object> headers, Long cardId, Long userId, PrepaidCardStatus oldState, PrepaidCard10 prepaidCard) throws Exception;
}
