package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.domain.*;
import cl.multicaja.helpers.ejb.v10.HelpersEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

  @EJB
  private UsersEJBBean10 usersEJB10;

  @EJB
  private HelpersEJBBean10 helpersEJB10;

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    map.put("ejb_users", this.usersEJB10.info());
    map.put("ejb_helpers", this.helpersEJB10.info());
    return map;
  }

  @Override
  public PrepaidTopup topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) {
    return null;
  }

  @Override
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) {

  }

  @Override
  public List<PrepaidTopup> getUserTopups(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidUserSignup initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup signupRequest) {
    return null;
  }

  @Override
  public PrepaidUserSignup getUserSignup(Map<String, Object> headers, Long signupId) {
    return null;
  }

  @Override
  public PrepaidCard issuePrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidCard getPrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }
}
