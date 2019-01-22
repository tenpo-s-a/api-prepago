package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.Clearing10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidClearingEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidClearingEJB10 {

  private static Log log = LogFactory.getLog(PrepaidClearingEJBBean10.class);

  @Override
  public Clearing10 insertClearingData(Map<String, Object> header, Clearing10 clearing10) throws Exception {

    if(clearing10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "clearing10"));
    }
    //Error Id Accounting Null
    if(clearing10.getId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "getId()"));
    }

    Object[] params = {
      clearing10.getId() == null ? new NullParam(Types.BIGINT):new InParam(clearing10.getId(), Types.BIGINT),
      clearing10.getUserAccount() == null || clearing10.getUserAccount().getId() == null ? new NullParam(Types.BIGINT) : new InParam(clearing10.getUserAccount().getId(), Types.BIGINT),
      clearing10.getClearingFileId() == null ? new NullParam(Types.BIGINT) : new InParam(clearing10.getClearingFileId(), Types.BIGINT),
      clearing10.getClearingStatus() == null ? new NullParam(Types.VARCHAR) : new InParam(clearing10.getClearingStatus().getValue(), Types.VARCHAR),
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_acc_create_clearing_data_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_create_accounting_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }

    return searchClearingDataById(header,getNumberUtils().toLong(resp.get("_id")));
  }

  @Override
  public Clearing10 updateClearingData(Map<String, Object> header,Long id, Long fileId, AccountingStatusType status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    if(fileId == null && status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "allNull"));
    }

    Object[] params = {
      id == null ? new NullParam(Types.BIGINT) : new InParam(id, Types.BIGINT),
      fileId == null ? new NullParam(Types.BIGINT) : new InParam(fileId, Types.BIGINT),
      status == null ? new NullParam(Types.VARCHAR) : new InParam(status, Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_clearing_data_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_create_accounting_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    return searchClearingDataById(header,getNumberUtils().toLong(id));
  }

  //TODO: este metodo no tiene test usando el parametro "status"
  @Override
  public List<Clearing10> searchClearingData(Map<String, Object> header, Long id, AccountingStatusType status) throws Exception {

    if(id == null && status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value","allNull"));
    }
    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      status != null ? status.getValue() : new NullParam(Types.VARCHAR),
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      Clearing10 clearing10 = new Clearing10();
      clearing10.setClearingId(getNumberUtils().toLong(row.get("_id")));
      clearing10.setId(getNumberUtils().toLong(row.get("_accounting_id"))); //IdAccounting
      clearing10.setUserAccountId(getNumberUtils().toLong(row.get("_user_account_id")));
      System.out.println("User accounti id: " + clearing10.getUserAccount().getId());
      clearing10.setClearingStatus(AccountingStatusType.fromValue(String.valueOf(row.get("_status"))));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_created"));
      timestamps.setUpdatedAt((Timestamp)row.get("_updated"));
      clearing10.setTimestamps(timestamps);
      return clearing10;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_clearing_data_v10",  rm, params);
    List<Clearing10> res = (List<Clearing10>)resp.get("result");
    log.info(res);
    return res != null ? res : Collections.EMPTY_LIST;
  }

  @Override
  public Clearing10 searchClearingDataById(Map<String, Object> header, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    List<Clearing10> clearing10s = searchClearingData(null,id,null);
    return clearing10s != null && !clearing10s.isEmpty() ? clearing10s.get(0) : null;
  }

  //Todo: Se debe crear un metodo que retorne Accounting + Clearing &&  Accounting + Clearing + UserAccount para Archivo Liquidacion
  public List<Clearing10>  seachClearingDataToFile(){
    return  null;
  }

}
