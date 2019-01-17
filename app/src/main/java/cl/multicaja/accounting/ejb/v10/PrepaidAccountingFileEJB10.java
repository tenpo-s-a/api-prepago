package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingFiles10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;

import java.util.List;
import java.util.Map;

public interface PrepaidAccountingFileEJB10 {

  /**
   *
   * @param header
   * @param accountingFiles10
   * @return
   * @throws Exception
   */
  AccountingFiles10 insertAccountingFile(Map<String, Object> header,AccountingFiles10 accountingFiles10) throws Exception;

  /**
   *
   * @param header
   * @param id
   * @return
   * @throws Exception
   */
   List<AccountingFiles10> searchAccountingFile(Map<String, Object> header, Long id, String fileName, String fileId, AccountingStatusType status) throws Exception;

  /**
   *
   * @param header
   * @param id
   * @return
   * @throws Exception
   */
  AccountingFiles10 searchAccountingFileById(Map<String, Object> header, Long id) throws Exception;

    /**
     *
     * @param header
     * @param status
     * @return
     * @throws Exception
     */
  AccountingFiles10 updateAccountingFile(Map<String, Object> header, Long id,String fileId,String Url, AccountingStatusType status) throws Exception;
}
