package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class McRedReconciliationEJBBean10 extends PrepaidBaseEJBBean10 implements McRedReconciliationEJB10 {

  private static Log log = LogFactory.getLog(McRedReconciliationEJBBean10.class);

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10;

  private ReconciliationFilesEJBBean10 getReconciliationFilesEJBBean10() {
    return reconciliationFilesEJBBean10;
  }

  public void setReconciliationFilesEJBBean10(ReconciliationFilesEJBBean10 reconciliationFilesEJBBean10) {
    this.reconciliationFilesEJBBean10 = reconciliationFilesEJBBean10;
  }


  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return prepaidMovementEJBBean10;
  }

  public void setPrepaidMovementEJBBean10(PrepaidMovementEJBBean10 prepaidMovementEJBBean10) {
    this.prepaidMovementEJBBean10 = prepaidMovementEJBBean10;
  }

  @Override
  public ReconciliationFile10 processFile(InputStream inputStream, String fileName) throws Exception {

    log.info("[processFile IN]");
    ReconciliationFileType fileType = getReconciliationFileType(fileName);
    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();

    //Si es null se ignora por que son archivos de R.Rechazados o C.Rechazados
    if(fileType != null) {
      List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetails = getCsvData(fileName, inputStream);

      reconciliationFile10.setFileName(fileName);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);
      reconciliationFile10.setType(fileType);
      reconciliationFile10.setStatus(FileStatus.READING);
      reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

      for(McRedReconciliationFileDetail fileDetail : lstMcRedReconciliationFileDetails) {
        fileDetail.setFileId(reconciliationFile10.getId());
        try{
          this.addFileMovement(null,fileDetail);
        }catch (Exception e){
          e.printStackTrace();
        }
      }
      getReconciliationFilesEJBBean10().updateFileStatus(null,reconciliationFile10.getId(),FileStatus.OK);
    }
    log.info("[processFile OUT]");
    return reconciliationFile10;
  }

  private ReconciliationFileType getReconciliationFileType(String fileName){
    if (fileName.contains("rendicion_cargas_mcpsa_mc")) {
      return ReconciliationFileType.SWITCH_TOPUP;
    }else if (fileName.contains("rendicion_cargas_rechazadas_mcpsa_mc")) {
      return null;
    }
    else if (fileName.contains("rendicion_cargas_reversadas_mcpsa_mc")) {
      return ReconciliationFileType.SWITCH_REVERSED_TOPUP;
    }
    else if (fileName.contains("rendicion_retiros_mcpsa_mc")) {
      return ReconciliationFileType.SWITCH_WITHDRAW;
    }
    else if (fileName.contains("rendicion_retiros_rechazados_mcpsa_mc")) {
      return null;
    }
    else if (fileName.contains("rendicion_retiros_reversados_mcpsa_mc")) {
      return ReconciliationFileType.SWITCH_REVERSED_WITHDRAW;
    }
    else {
      return null;
    }
  }

  public void processSwitchData(ReconciliationFile10 reconciliationFile10) throws Exception {
    List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetails;
    switch (reconciliationFile10.getType()){
      case SWITCH_TOPUP: {
        lstMcRedReconciliationFileDetails = this.getFileMovements(null,reconciliationFile10.getId(),null,null);
        conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, reconciliationFile10.getFileName());
        break;
      }
      case SWITCH_REVERSED_TOPUP:{
        lstMcRedReconciliationFileDetails = this.getFileMovements(null,reconciliationFile10.getId(),null,null);
        conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA, reconciliationFile10.getFileName());
        break;
      }
      case SWITCH_WITHDRAW:{
        lstMcRedReconciliationFileDetails = this.getFileMovements(null,reconciliationFile10.getId(),null,null);
        conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,  reconciliationFile10.getFileName());
        break;
      }
      case SWITCH_REVERSED_WITHDRAW:{
        lstMcRedReconciliationFileDetails = this.getFileMovements(null,reconciliationFile10.getId(),null,null);
        this.conciliation(lstMcRedReconciliationFileDetails, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,  reconciliationFile10.getFileName());
        break;
      }
    }
    getPrepaidMovementEJBBean10().expireNotReconciledMovements(reconciliationFile10.getType());
    log.info("[processSwitchData OUT]");
  }

  private void conciliation(List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetails, PrepaidMovementType movementType, IndicadorNormalCorrector indicadorNormalCorrector, String fileName) throws Exception{
    try {
      for (McRedReconciliationFileDetail recTmp : lstMcRedReconciliationFileDetails) {
        PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode(),movementType,indicadorNormalCorrector);
        log.info(prepaidMovement10);
        if (prepaidMovement10 == null) {
          log.info("Movimiento no encontrado, no conciliado");
          // Construyendo un Id.
          StringBuilder researchId = new StringBuilder();
          researchId.append("ExtId:[");
          if (recTmp.getExternalId() != null) {
            researchId.append(recTmp.getExternalId().toString());
          } else {
            researchId.append("NoExternalId");
          }
          researchId.append("]-");
          researchId.append("McCode:[");
          researchId.append(recTmp.getMcCode());
          researchId.append("]");

          Long movRef = 0L;
          getPrepaidMovementEJBBean10().createMovementResearch(
            null,
            researchId.toString(),
            ReconciliationOriginType.SWITCH,
            fileName,
            recTmp.getDateTrx(),
            ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID,
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_BANC_AND_PROCESOR,
            movRef);
        }
        else
          {
            if (recTmp.getAmount().compareTo(prepaidMovement10.getMonto()) != 0) {
              log.error("No conciliado");
              getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ReconciliationStatusType.NOT_RECONCILED);
            }
            else {
              log.info("Conciliado");
              getPrepaidMovementEJBBean10().updateStatusMovementConSwitch(null, prepaidMovement10.getId(), ReconciliationStatusType.RECONCILED);
            }
        }
      }
    }catch (Exception e){
      e.printStackTrace();
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }


  /**
   * Lee los archivos CSV
   * @param fileName
   * @param is
   * @return
   */
  private List<McRedReconciliationFileDetail> getCsvData(String fileName, InputStream is) throws Exception {

    List<McRedReconciliationFileDetail> lstMcRedReconciliationFileDetail;
    log.info("IN");
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,';');
      csvReader.readNext();
      String[] record;
      lstMcRedReconciliationFileDetail = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        log.debug(Arrays.toString(record));
        McRedReconciliationFileDetail mcRedReconciliationFileDetail = new McRedReconciliationFileDetail();
        mcRedReconciliationFileDetail.setMcCode(record[0]);
        mcRedReconciliationFileDetail.setDateTrx(Timestamp.valueOf(record[1]));
        log.info("FECHA:::::: "+mcRedReconciliationFileDetail.getDateTrx());
        mcRedReconciliationFileDetail.setClientId(Long.valueOf(record[2]));
        mcRedReconciliationFileDetail.setAmount(getNumberUtils().toBigDecimal(record[3]));

        if(!fileName.contains("reversa")) {
          mcRedReconciliationFileDetail.setExternalId(Long.valueOf(record[4]));
        }
        lstMcRedReconciliationFileDetail.add(mcRedReconciliationFileDetail);
      }
      is.close();
    }catch (Exception e){
      is.close();
      log.error("Exception: "+e);
      e.printStackTrace();
      System.out.println("Exception: "+e);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
    log.info("OUT");
    return lstMcRedReconciliationFileDetail;
  }

  @Override
  public McRedReconciliationFileDetail addFileMovement(Map<String,Object> header, McRedReconciliationFileDetail newSwitchMovement) throws Exception {
    if(newSwitchMovement == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement"));
    }

    if(newSwitchMovement.getFileId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.fileId"));
    }

    if(newSwitchMovement.getMcCode() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.McCode"));
    }

    if(newSwitchMovement.getClientId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.clientId"));
    }

    if(newSwitchMovement.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.amount"));
    }

    if(newSwitchMovement.getDateTrx() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newSwitchMovement.dateTrx"));
    }

    Object[] params = {
      new InParam(newSwitchMovement.getFileId(), Types.BIGINT),
      new InParam(newSwitchMovement.getMcCode(), Types.VARCHAR),
      new InParam(newSwitchMovement.getClientId(), Types.BIGINT),
      newSwitchMovement.getExternalId() != null ? new InParam(newSwitchMovement.getExternalId(), Types.BIGINT) : new NullParam(Types.BIGINT),
      new InParam(newSwitchMovement.getAmount(), Types.NUMERIC),
      new InParam(newSwitchMovement.getDateTrx(), Types.TIMESTAMP),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_r_id_int", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".prp_crea_movimiento_switch_v10", params);

    if("0".equals(resp.get("_error_code"))) {
      newSwitchMovement.setId(getNumberUtils().toLong(resp.get("_r_id")));
      return newSwitchMovement;
    } else {
      log.error("addFileMovement resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  public List<McRedReconciliationFileDetail> getFileMovements(Map<String,Object> header, Long fileId, Long movementId, String mcId) throws Exception {
    return getFileMovements(header, "prp_movimiento_switch", fileId, movementId, mcId);
  }

  public List<McRedReconciliationFileDetail> getFileMovements(Map<String,Object> header, String tableName, Long fileId, Long movementId, String mcId) throws Exception {
    Object[] params = {
      new InParam(tableName, Types.VARCHAR),
      movementId != null ? new InParam(movementId, Types.BIGINT) : new NullParam(Types.BIGINT),
      fileId != null ? new InParam(fileId, Types.BIGINT) : new NullParam(Types.BIGINT),
      mcId != null ? new InParam(mcId, Types.VARCHAR) : new NullParam(Types.VARCHAR)
    };

    RowMapper rm = (Map<String, Object> row) -> {
      McRedReconciliationFileDetail reconciliationMcRed10 = new McRedReconciliationFileDetail();
      reconciliationMcRed10.setId(getNumberUtils().toLong(row.get("_id")));
      reconciliationMcRed10.setFileId(getNumberUtils().toLong(row.get("_id_archivo")));
      reconciliationMcRed10.setMcCode(row.get("_id_multicaja").toString());
      reconciliationMcRed10.setClientId(getNumberUtils().toLong(row.get("_id_cliente")));
      reconciliationMcRed10.setExternalId(getNumberUtils().toLong(row.get("_id_multicaja_ref")));
      reconciliationMcRed10.setAmount(getNumberUtils().toBigDecimal(row.get("_monto")));
      reconciliationMcRed10.setDateTrx((Timestamp) row.get("_fecha_trx"));


      return reconciliationMcRed10;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_movimientos_switch_v10", rm,params);
    List<McRedReconciliationFileDetail> lstReturn = (List<McRedReconciliationFileDetail>) resp.get("result");

    return lstReturn;
  }

  @Override
  public void deleteFileMovementsByFileId(Map<String,Object> header, Long fileId) throws Exception {
    if(fileId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileId"));
    }

    Object[] params = {
      new InParam(fileId, Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_borrar_movimientos_switch_v10", params);

    if(!"0".equals(resp.get("_error_code"))) {
      log.error("deleteFileMovementsByFileId resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }
}
