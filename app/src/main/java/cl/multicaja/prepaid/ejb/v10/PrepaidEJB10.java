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

  PrepaidTopup topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) throws ValidationException;

  void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest);

  List<PrepaidTopup> getUserTopups(Map<String, Object> headers, Long userId);

  PrepaidUserSignup initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup signupRequest);

  PrepaidUserSignup getUserSignup(Map<String, Object> headers, Long signupId);

  PrepaidCard issuePrepaidCard(Map<String, Object> headers, Long userId);

  PrepaidCard getPrepaidCard(Map<String, Object> headers, Long userId);

}
