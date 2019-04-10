package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidBalance10;
import cl.multicaja.prepaid.model.v10.PrepaidBalanceInfo10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidUserEJB10 {

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
   * @param userId id de usuario prepago
   * @param userIdMc id de usuario multicaja
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
   * @param userId id de usuario prepago
   * @return
   * @throws Exception
   */
  PrepaidUser10 getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception;

  /**
   * permite buscar un usuario prepago por el id de usuario de multicaja
   *
   * @param headers
   * @param userIdMc id de usuario multicaja
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
   * @param userId id de usuario prepago
   * @param status
   * @throws Exception
   */
  void updatePrepaidUserStatus(Map<String, Object> headers, Long userId, PrepaidUserStatus status) throws Exception;

  /**
   *  Verifica el nivel del usuario
   *
   * @param oUser usuario multicaja
   * @param prepaidUser10 usuario prepago
   * @throws NotFoundException 102001 si el usuario MC es null
   * @throws ValidationException 101004 si el rut, status del rut o el nameStatus es null
   * @throws NotFoundException 302003 si el usuario prepago es null
   * @return el nivel del usuario
   */
  PrepaidUser10 getUserLevel(User oUser, PrepaidUser10 prepaidUser10) throws Exception;

  /**
   * Incrementa el contador de intentos de verificacion de identidad
   * @param headers
   * @param prepaidUser
   * @return
   * @throws Exception
   */
  PrepaidUser10 incrementIdentityVerificationAttempt(Map<String, Object> headers, PrepaidUser10 prepaidUser) throws Exception;

}
