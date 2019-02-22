package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.ReconciliationMcRed10;

import java.io.InputStream;
import java.util.Map;

public interface McRedReconciliationEJB10 {

  /**
   * Procesa archivo de conciliacion McRed
   * @param inputStream
   * @param fileName
   * @throws Exception
   */
  void processFile(InputStream inputStream, String fileName) throws Exception;

  /**
   * Agrega un movimiento switch
   *
   * @param header
   * @param newSwitchMovement
   * @return
   * @throws Exception
   */
  ReconciliationMcRed10 addFileMovement(Map<String,Object> header, ReconciliationMcRed10 newSwitchMovement) throws Exception;
}
