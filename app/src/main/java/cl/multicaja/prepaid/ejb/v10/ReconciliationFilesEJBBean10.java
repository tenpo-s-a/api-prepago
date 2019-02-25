package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.FileStatus;
import cl.multicaja.prepaid.model.v10.ReconciliationFileInfo;
import cl.multicaja.prepaid.model.v10.ReconciliationFileType;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;

import java.util.List;
import java.util.Map;

public class ReconciliationFilesEJBBean10 implements ReconciliationFilesEJB10 {

  @Override
  public ReconciliationFileInfo createReconciliationFile(Map<String, Object> headers, ReconciliationFileInfo reconciliationFileInfo) throws Exception {
    //Todo
    return null;
  }

  @Override
  public List<ReconciliationFileInfo> getReconciliationFile(Map<String, Object> headers, String fileName, ReconciliationOriginType process, ReconciliationFileType fileType, FileStatus fileStatus) throws Exception {
    //Todo
    return null;
  }

  @Override
  public void updateFileStatus(Map<String, Object> headers, Long fileId, FileStatus newFileStatus) throws Exception {
    //Todo
  }
}
