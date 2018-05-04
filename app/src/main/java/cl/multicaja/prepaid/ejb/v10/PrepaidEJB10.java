package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.domain.*;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  void processorNotification(NewRawTransaction trx) throws Exception;

  PrepaidCard emitPrepaid(String userId, NewPrepaidTransaction trx) throws Exception;

  PrepaidUserData getPrepaid(String userId) throws Exception;

  PrepaidCardBalance getBalance(String userId) throws Exception;

  List<PrepaidTransaction> getTransactions(String userId) throws Exception;

  NewPrepaidTransactionResponse topupBalance(String userId, NewPrepaidTransaction trx) throws Exception;
  NewPrepaidTransactionResponse withdrawBalance(String userId, NewPrepaidTransaction trx) throws Exception;

  PrepaidCard lockCard(String userId) throws Exception;
  PrepaidCard unlockCard(String userId) throws Exception;

  void sendPrepaidCard(String userId) throws Exception;
}
