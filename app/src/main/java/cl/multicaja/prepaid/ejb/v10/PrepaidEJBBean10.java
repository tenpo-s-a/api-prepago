package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.helpers.ejb.v10.HelpersEJB10;
import cl.multicaja.users.ejb.v10.UsersEJB10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

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

  @Override
  public void processorNotification() throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitPrepaid(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getPrepaid(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getBalance(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getTransactions(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void topupBalance(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void withdrawBalance(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void lockCard(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlockCard(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendPrepaidCard(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }
}
