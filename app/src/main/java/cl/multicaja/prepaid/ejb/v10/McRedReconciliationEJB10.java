package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.ReconciliationMcRed10;

import java.io.InputStream;
import java.util.List;
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

  /**
   * Busca la lista de movimientos que pertenecen a este fileId
   * @param header
   * @param fileId
   * @return
   * @throws Exception
   */
  List<ReconciliationMcRed10> getFileMovements(Map<String,Object> header, Long fileId, Long movementId, String mcId) throws Exception;

  /**
   * Borra de la tabla switch_movement todos los registros de cierto archivo
   * @param header
   * @param fileId
   * @throws Exception
   */
  void deleteFileMovementsByFileId(Map<String,Object> header, Long fileId) throws Exception;
}
