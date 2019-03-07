package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;

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
  ReconciliationFile10 processFile(InputStream inputStream, String fileName) throws Exception;

  /**
   * Agrega un movimiento switch
   *
   * @param header
   * @param newSwitchMovement
   * @return
   * @throws Exception
   */
  McRedReconciliationFileDetail addFileMovement(Map<String,Object> header, McRedReconciliationFileDetail newSwitchMovement) throws Exception;

  /**
   * Borra de la tabla switch_movement todos los registros de cierto archivo
   * @param header
   * @param fileId
   * @throws Exception
   */
  void deleteFileMovementsByFileId(Map<String,Object> header, Long fileId) throws Exception;
}
