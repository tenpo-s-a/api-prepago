package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 */
public interface PrepaidAccountingEJB10 {

  List<Accounting10> searchAccountingData(Map<String, Object> header, Date dateToSearch) throws Exception;

  public List<Accounting10> saveAccountingData (Map<String, Object> header,List<Accounting10> accounting10s) throws Exception;

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
  List<Accounting10> processMovementForAccounting(Map<String, Object> headers, LocalDateTime date) throws Exception;

  /**
   * Busca los movimientos en accounting y genera un archivo csv que se envia por correo
   * @param headers
   * @param date la fecha recibida debe estar en UTC
   * @return
   * @throws Exception
   */
  void generateAccountingFile(Map<String, Object> headers, LocalDateTime date) throws Exception;
}
