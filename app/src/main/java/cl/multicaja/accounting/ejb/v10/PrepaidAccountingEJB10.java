package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.prepaid.model.v10.PrepaidAccountingMovement;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 */
public interface PrepaidAccountingEJB10 {

  AccountingData10 searchAccountingByIdTrx(Map<String, Object> header, Long  idTrx) throws Exception;

  List<AccountingData10> searchAccountingData(Map<String, Object> header, LocalDateTime dateToSearch) throws Exception;

  List<AccountingData10> saveAccountingData (Map<String, Object> header, List<AccountingData10> accounting10s) throws Exception;

  /**
   * Busca los movimientos conciliados para agregarlos en la tabla de contabilidad.
   *
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  List<PrepaidAccountingMovement> getReconciledPrepaidMovementsForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception;

  /**
   * Procesa los movimientos conciliados para agregarlos en la tabla de contabilidad
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  List<AccountingData10> processMovementForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception;

  /**
   * Agrega registro de archivo IPM
   * @param headers
   * @param file
   * @return
   * @throws Exception
   */
  IpmFile saveIpmFileRecord(Map<String, Object> headers, IpmFile file) throws Exception;

  /**
   * Actualiza registro de archivo IPM
   * @param headers
   * @param file
   * @return
   * @throws Exception
   */
  IpmFile updateIpmFileRecord(Map<String, Object> headers, IpmFile file) throws Exception;

  /**
   * Convierte un archivo IPM en CSV con la libreria python https://github.com/adelosa/mciutil
   * @param ipmFileName
   * @throws Exception
   */
  void convertIpmFileToCsv(String ipmFileName) throws Exception;

  /**
   * Procesa el archivo CSV y guarda las transacciones
   * @param file
   * @param ipmFile
   * @throws Exception
   */
  IpmFile processIpmFile(Map<String, Object> headers, File file, IpmFile ipmFile) throws Exception;

  /**
   * Procesa las transacciones obtenias del archivo csv
   * @param headers
   * @param ipmFile
   * @throws Exception
   */
  void processIpmFileTransactions(Map<String, Object> headers, IpmFile ipmFile) throws Exception;

  /**
   * Obtiene el tipo de transaccion del mensaje IPM
   * @param trx
   * @return
   * @throws Exception
   */
  AccountingTxType getTransactionType(IpmMessage trx) throws Exception;

  /**
   * Verifica si el merchant name califica para ser una suscripcion
   * @param merchantName
   * @return
   * @throws Exception
   */
  Boolean isSubscriptionMerchant(final String merchantName) throws Exception;

  /**
   * Actualiza el fileId y estado
   *
   * @param header
   * @param id
   * @param fileId
   * @param status
   * @throws Exception
   */
  void updateAccountingData(Map<String, Object> header, Long id, Long fileId, AccountingStatusType status) throws Exception;

  /**
   * Actualiza el AccountingStatus
   * @param header
   * @param id
   * @param accountingStatus
   * @throws Exception
   */
  void updateAccountingStatus(Map<String, Object> header, Long id, AccountingStatusType accountingStatus) throws Exception;

  /**
   * Actualiza es estado
   * @param header
   * @param id
   * @param status
   * @throws Exception
   */
  void updateStatus(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception;

  /**
   * Actualiza accounting status y fecha de conciliacion
   * @param header
   * @param id
   * @param accountingStatus
   * @param conciliationDate
   * @throws Exception
   */
  void updateAccountingStatusAndConciliationDate(Map<String, Object> header, Long id, AccountingStatusType accountingStatus, String conciliationDate) throws Exception;
}
