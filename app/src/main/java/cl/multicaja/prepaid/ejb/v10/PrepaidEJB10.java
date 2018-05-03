package cl.multicaja.prepaid.ejb.v10;

import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  // TODO: cambiar el tipo de retorno de los metodos

  void processorNotification() throws Exception;
  void emitPrepaid(String userId) throws Exception;
  void getPrepaid(String userId) throws Exception;
  void getBalance(String userId) throws Exception;
  void getTransactions(String userId) throws Exception;
  void topupBalance(String userId) throws Exception;
  void withdrawBalance(String userId) throws Exception;
  void lockCard(String userId) throws Exception;
  void unlockCard(String userId) throws Exception;
  void sendPrepaidCard(String userId) throws Exception;
}
