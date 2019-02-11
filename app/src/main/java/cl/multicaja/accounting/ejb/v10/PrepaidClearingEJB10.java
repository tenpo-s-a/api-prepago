package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;

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
  ClearingData10 insertClearingData(Map<String, Object> header, ClearingData10 clearing10) throws Exception;

  /**
   *
   * @param header
   * @param id
   * @param status
   * @return
   * @throws Exception
   */
  ClearingData10 updateClearingData(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception;

  /**
   *
   * @param header
   * @param fileId
   * @param status
   * @return
   * @throws Exception
   */
  ClearingData10 updateClearingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status) throws Exception;

  /**
   *
   * @param header
   * @param id
   * @param status
   * @return
   * @throws Exception
   */
  List<ClearingData10> searchClearingData(Map<String, Object> header, Long id, AccountingStatusType status, Long accountingId)throws Exception;

  /**
   *
   * @param header
   * @param id
   * @return
   * @throws Exception
   */
  ClearingData10 searchClearingDataById(Map<String, Object> header, Long id) throws Exception;

  /**
   *
   * @param header
   * @param accountingId
   * @return
   * @throws Exception
   */
  ClearingData10 searchClearingDataByAccountingId(Map<String, Object> header, Long accountingId) throws Exception;

  /**
   * Busca los retiros web para procesar conciliacion:
   *  - El movimiento debe tener respuesta del banco sobre el proceso de clearing
   *  - El movimiento debe haber pasado el procesado de conciliacion con Tecnocom
   *  - El movimiento no debe no ha sido conciliado
   * @param headers
   * @return
   * @throws Exception
   */
  List<ClearingData10> getWebWithdrawForReconciliation(Map<String, Object> headers) throws Exception;

}
