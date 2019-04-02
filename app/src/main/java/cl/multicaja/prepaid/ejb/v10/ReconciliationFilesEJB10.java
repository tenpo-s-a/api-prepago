package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.FileStatus;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import cl.multicaja.prepaid.model.v10.ReconciliationFileType;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;

import java.util.List;
import java.util.Map;

public interface ReconciliationFilesEJB10 {

  /**
   * Inserta informacion de un archivos de conciliation en la tabla prp_archivos_conciliacion
   *
   * @param headers
   * @param reconciliationFile10
   * @return Devuelve los midmos datos con el ID asignado
   * @throws Exception
   */
  ReconciliationFile10 createReconciliationFile(Map<String, Object> headers, ReconciliationFile10 reconciliationFile10) throws Exception;

  /**
   * Busca la informacion de un archivo de conciliacion
   *
   * @param headers
   * @param fileName
   * @param process
   * @param fileType
   * @param fileStatus
   * @return
   * @throws Exception
   */
  List<ReconciliationFile10> getReconciliationFile(Map<String, Object> headers, Long fileId, String fileName, ReconciliationOriginType process, ReconciliationFileType fileType, FileStatus fileStatus) throws Exception;

  /**
   * Actualiza el estado del archivo
   *
   * @param headers
   * @param fileId
   * @param newFileStatus
   * @throws Exception
   */
  void updateFileStatus(Map<String, Object> headers, Long fileId, FileStatus newFileStatus) throws Exception;
}
