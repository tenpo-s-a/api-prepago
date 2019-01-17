package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.Clearing10;

import java.util.List;
import java.util.Map;

public interface PrepaidClearingEJB10 {

  /**
   *
   * @param header
   * @param clearing10
   * @return
   * @throws Exception
   */
  Clearing10 insertClearingData(Map<String, Object> header, Clearing10 clearing10) throws Exception;

  /**
   *
   * @param header
   * @param fileId
   * @param status
   * @return
   * @throws Exception
   */
  Clearing10 updateClearingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status) throws Exception;

  /**
   *
   * @param header
   * @param id
   * @param status
   * @return
   * @throws Exception
   */
  List<Clearing10> searchClearingData(Map<String, Object> header,Long id, AccountingStatusType status)throws Exception;

  /**
   *
   * @param header
   * @param id
   * @return
   * @throws Exception
   */
  Clearing10 searchClearingDataById(Map<String, Object> header,Long id) throws Exception;

}
