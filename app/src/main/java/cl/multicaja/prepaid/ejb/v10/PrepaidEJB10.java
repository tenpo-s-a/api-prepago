package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.domain.*;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  PrepaidTopup topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) throws Exception;

  void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest);

  List<PrepaidTopup> getUserTopups(Map<String, Object> headers, Long userId);

  PrepaidUserSignup initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup signupRequest);

  PrepaidUserSignup getUserSignup(Map<String, Object> headers, Long signupId);

  PrepaidCard issuePrepaidCard(Map<String, Object> headers, Long userId);

  PrepaidCard getPrepaidCard(Map<String, Object> headers, Long userId) throws Exception;

  PrepaidUser createPrepaidUser(Map<String, Object> headers, PrepaidUser prepaidUser) throws Exception;

  List<PrepaidUser> getPrepaidUsers(Map<String, Object> headers, Long userId, Long userIdMc, Integer rut, PrepaidUserStatus status) throws Exception;

  PrepaidUser getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception;

  PrepaidUser getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception;

  PrepaidUser getPrepaidUserByRut(Map<String, Object> headers, Integer rut) throws Exception;


  PrepaidCard createPrepaidCard(Map<String, Object> headers, PrepaidCard prepaidCard) throws Exception;

  List<PrepaidCard> getPrepaidCards(Map<String, Object> headers, Long id, Long userId, Integer expiration, PrepaidCardStatus status, String processorUserId) throws Exception;
  
}
