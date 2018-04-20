package cl.multicaja.prepago.ejb.v10;

import javax.ejb.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vutreras
 */
@Stateless
@Local(PrepaidEJB10.class)
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 implements PrepaidEJB10 {

  @EJB
  private UsersEJB10 usersEJB10;

  @EJB
  private HelpersEJB10 helpersEJB10;

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    map.put("ejb_users", this.usersEJB10.info());
    map.put("ejb_helpers", this.helpersEJB10.info());
    return map;
  }
}
