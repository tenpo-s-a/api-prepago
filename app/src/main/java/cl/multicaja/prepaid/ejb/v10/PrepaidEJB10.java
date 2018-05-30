package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception;

  void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest);

  List<PrepaidTopup10> getUserTopups(Map<String, Object> headers, Long userId);

  PrepaidUserSignup10 initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup10 signupRequest);

  PrepaidUserSignup10 getUserSignup(Map<String, Object> headers, Long signupId);

  PrepaidCard10 issuePrepaidCard(Map<String, Object> headers, Long userId);

  /**
   * permite crear un usuario prepago
   *
   * @param headers
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  PrepaidUser10 createPrepaidUser(Map<String, Object> headers, PrepaidUser10 prepaidUser) throws Exception;

  /**
   * permite buscar usuarios prepago
   *
   * @param headers
   * @param userId
   * @param userIdMc
   * @param rut
   * @param status
   * @return
   * @throws Exception
   */
  List<PrepaidUser10> getPrepaidUsers(Map<String, Object> headers, Long userId, Long userIdMc, Integer rut, PrepaidUserStatus status) throws Exception;

  /**
   * permite buscar un usuario prepago por su id
   *
   * @param headers
   * @param userId
   * @return
   * @throws Exception
   */
  PrepaidUser10 getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception;

  /**
   * permite buscar un usuario prepago por el id de usuario de multicaja
   *
   * @param headers
   * @param userIdMc
   * @return
   * @throws Exception
   */
  PrepaidUser10 getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception;

  /**
   * permite buscar un usuario prepago por su rut
   *
   * @param headers
   * @param rut
   * @return
   * @throws Exception
   */
  PrepaidUser10 getPrepaidUserByRut(Map<String, Object> headers, Integer rut) throws Exception;

  /**
   * actualiza el estado del usuario prepago
   *
   * @param headers
   * @param id
   * @param status
   * @throws Exception
   */
  void updatePrepaidUserStatus(Map<String, Object> headers, Long id, PrepaidUserStatus status) throws Exception;

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
   *  Verifica el nivel del usuario
   * @param oUser usuario multicaja
   * @param prepaidUser10 usuario prepago
   * @throws NotFoundException 102001 si el usuario MC es null
   * @throws ValidationException 101000 si el rut o status del rut es null
   * @throws NotFoundException 302003 si el usuario prepago es null
   * @return el nivel del usuario
   */
  PrepaidUserLevel getUserLevel(User oUser, PrepaidUser10 prepaidUser10) throws Exception;

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
 *  Permite realizar la actualizacion de los datos de una tarjeta
 *
*/
  boolean updateCard(Map<String, Object> headers,Long cardId, Long userId, PrepaidCardStatus oldState, PrepaidCard10 prepaidCard) throws Exception;

}
