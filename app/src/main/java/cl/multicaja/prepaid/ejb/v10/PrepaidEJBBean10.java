package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.helpers.ejb.v10.HelpersEJB10;
import cl.multicaja.prepaid.dto.*;
import cl.multicaja.users.ejb.v10.UsersEJB10;
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
  public PrepaidCardDTO emitPrepaid(String userId, NewPrepaidTransactionDTO trx) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrepaidUserDataDTO getPrepaid(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public BalanceDTO getBalance(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<PrepaidTransactionDTO> getTransactions(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public NewTransactionResponseDTO topupBalance(String userId, NewPrepaidTransactionDTO trx) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public NewTransactionResponseDTO withdrawBalance(String userId, NewPrepaidTransactionDTO trx) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrepaidCardDTO lockCard(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public PrepaidCardDTO unlockCard(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendPrepaidCard(String userId) throws Exception {
    throw new UnsupportedOperationException();
  }
}
