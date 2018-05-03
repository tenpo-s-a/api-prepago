package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.dto.*;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  void processorNotification(NewRawTransactionDTO trx) throws Exception;

  PrepaidCardDTO emitPrepaid(String userId, NewPrepaidTransactionDTO trx) throws Exception;

  PrepaidUserDataDTO getPrepaid(String userId) throws Exception;

  BalanceDTO getBalance(String userId) throws Exception;

  List<PrepaidTransactionDTO> getTransactions(String userId) throws Exception;

  NewTransactionResponseDTO topupBalance(String userId, NewPrepaidTransactionDTO trx) throws Exception;
  NewTransactionResponseDTO withdrawBalance(String userId, NewPrepaidTransactionDTO trx) throws Exception;

  PrepaidCardDTO lockCard(String userId) throws Exception;
  PrepaidCardDTO unlockCard(String userId) throws Exception;

  void sendPrepaidCard(String userId) throws Exception;
}
