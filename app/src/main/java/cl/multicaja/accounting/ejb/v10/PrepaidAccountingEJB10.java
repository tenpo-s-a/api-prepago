package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 */
public interface PrepaidAccountingEJB10 {

  List<AccountingData10> searchAccountingData(Map<String, Object> header, Date dateToSearch) throws Exception;

  List<AccountingData10> saveAccountingData (Map<String, Object> header, List<AccountingData10> accounting10s) throws Exception;

  /**
   * Busca los movimientos conciliados para agregarlos en la tabla de contabilidad.
   *
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  List<PrepaidMovement10> getReconciledPrepaidMovementsForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception;

  /**
   * Procesa los movimientos conciliados para agregarlos en la tabla de contabilidad
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  List<AccountingData10> processMovementForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception;

  /**
   * Busca los movimientos en accounting y genera un archivo csv que se envia por correo
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  void generateAccountingFile(Map<String, Object> headers, LocalDateTime date) throws Exception;

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

}
