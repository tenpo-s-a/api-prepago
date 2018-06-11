package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;

import java.math.BigDecimal;
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
   * @param userId
   * @param status
   * @throws Exception
   */
  void updatePrepaidUserStatus(Map<String, Object> headers, Long userId, PrepaidUserStatus status) throws Exception;

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
   * Retorna el saldo del cliente prepago
   *
   * @param headers
   * @param userId
   * @return
   */
  PrepaidBalance10 getPrepaidUserBalance(Map<String, Object> headers, Long userId) throws Exception;

  /**
   * Actualiza el saldo del cliente prepago
   *
   * @param headers
   * @param userId
   * @param balance
   * @throws Exception
   */
  void updatePrepaidUserBalance(Map<String, Object> headers, Long userId, BigDecimal balance) throws Exception;
}
