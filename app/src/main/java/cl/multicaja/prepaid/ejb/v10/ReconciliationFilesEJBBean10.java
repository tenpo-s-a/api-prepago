package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.model.v10.FileStatus;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import cl.multicaja.prepaid.model.v10.ReconciliationFileType;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class ReconciliationFilesEJBBean10 extends PrepaidBaseEJBBean10 implements ReconciliationFilesEJB10 {
  private static Log log = LogFactory.getLog(ReconciliationFilesEJBBean10.class);

  @Override
  public ReconciliationFile10 createReconciliationFile(Map<String, Object> headers, ReconciliationFile10 reconciliationFile10) throws Exception {
    if(reconciliationFile10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reconciliationFile10"));
    }
    if(reconciliationFile10.getFileName() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reconciliationFiileInfo.fileName"));
    }
    if (reconciliationFile10.getProcess() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reconciliationFile10.process"));
    }
    if (reconciliationFile10.getType() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reconciliationFile10.type"));
    }
    if (reconciliationFile10.getStatus() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reconciliationFile10.status"));
    }

    Object[] params = {
      new InParam(reconciliationFile10.getFileName(), Types.VARCHAR),
      new InParam(reconciliationFile10.getProcess(), Types.VARCHAR),
      new InParam(reconciliationFile10.getType(), Types.VARCHAR),
      new InParam(reconciliationFile10.getStatus(), Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_inserta_archivo_conciliacion", params);

    if ("0".equals(resp.get("_error_code"))) {
      reconciliationFile10.setId(getNumberUtils().toLong(resp.get("_r_id")));
      List<ReconciliationFile10> reconciliationFile10List = this.getReconciliationFile(null, reconciliationFile10.getFileName(), null, null, null);
      return reconciliationFile10List.get(0);
    } else {
      log.error("addReconciliationFile resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public List<ReconciliationFile10> getReconciliationFile(Map<String, Object> headers, String fileName, ReconciliationOriginType process, ReconciliationFileType fileType, FileStatus fileStatus) throws Exception {
    Object[] params = {
      fileName != null ? new InParam(fileName, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      process != null ? new InParam(process.toString(), Types.VARCHAR) : new NullParam(Types.VARCHAR),
      fileType != null ? new InParam(fileType.toString(), Types.VARCHAR) : new NullParam(Types.VARCHAR),
      fileStatus != null ? new InParam(fileStatus.toString(), Types.VARCHAR) : new NullParam(Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      try {
        ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
        reconciliationFile10.setId(getNumberUtils().toLong(row.get("_id")));
        reconciliationFile10.setFileName(String.valueOf(row.get("_nombre_de_archivo")));
        reconciliationFile10.setProcess(ReconciliationOriginType.valueOf(String.valueOf(row.get("_proceso"))));
        reconciliationFile10.setType(ReconciliationFileType.valueOf(String.valueOf(row.get("_tipo"))));
        reconciliationFile10.setStatus(FileStatus.valueOf(String.valueOf(row.get("_status"))));
        Timestamps timestamps = new Timestamps();
        timestamps.setCreatedAt((Timestamp)row.get("_created_at"));
        timestamps.setUpdatedAt((Timestamp)row.get("_updated_at"));
        reconciliationFile10.setTimestamps(timestamps);
        return reconciliationFile10;
      } catch(Exception e) {
        e.printStackTrace();
        log.info("RowMapper Error: "+e);
        return null;
      }
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_busca_archivo_conciliacion", rm, params);
    log.info("Respuesta Busca archivo conciliacion: "+resp);
    return (List)resp.get("result");
  }

  @Override
  public void updateFileStatus(Map<String, Object> headers, Long fileId, FileStatus newFileStatus) throws Exception {
    if(fileId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }
    if(newFileStatus == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newFileStatus"));
    }

    Object[] params = {
      new InParam(fileId, Types.BIGINT),
      new InParam(newFileStatus.toString(), Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_actualiza_archivo_conciliacion", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updateReconciliatinFileStatus resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }
}
