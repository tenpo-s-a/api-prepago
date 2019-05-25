package cl.multicaja.prepaid.ejb.v10;

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
   *
   * @param headers
   * @param user
   * @return
   * @throws Exception
   */
  PrepaidUser10 updatePrepaidUser(Map<String,Object> headers, PrepaidUser10 user)throws Exception;


}
