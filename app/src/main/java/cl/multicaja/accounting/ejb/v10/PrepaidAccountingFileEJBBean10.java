package cl.multicaja.accounting.ejb.v10;

import cl.multicaja.accounting.model.v10.AccountingFileFormatType;
import cl.multicaja.accounting.model.v10.AccountingFileType;
import cl.multicaja.accounting.model.v10.AccountingFiles10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
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
public class PrepaidAccountingFileEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidAccountingFileEJB10{

  private static Log log = LogFactory.getLog(PrepaidAccountingFileEJBBean10.class);

  @Override
  public AccountingFiles10 insertAccountingFile(Map<String, Object> header, AccountingFiles10 accountingFiles10) throws Exception {

    if(accountingFiles10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingFiles10"));
    }
    if(accountingFiles10.getName() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingFiles10.getName()"));
    }
    if(accountingFiles10.getFileId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingFiles10.getFileId()"));
    }
    if(accountingFiles10.getFileType() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingFiles10.getFileType()"));
    }
    if(accountingFiles10.getStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingFiles10.getStatus()"));
    }

    Object[] params = {
      accountingFiles10.getName() == null ? new NullParam(Types.VARCHAR):new InParam(accountingFiles10.getName(), Types.VARCHAR),
      accountingFiles10.getFileId() == null ? new NullParam(Types.VARCHAR) : new InParam(accountingFiles10.getFileId(), Types.VARCHAR),
      accountingFiles10.getFileType() == null ? new NullParam(Types.VARCHAR) : new InParam(accountingFiles10.getFileType().getValue(), Types.VARCHAR),
      accountingFiles10.getFileFormatType() == null ? new NullParam(Types.VARCHAR) : new InParam(accountingFiles10.getFileFormatType().getValue(), Types.VARCHAR),
      accountingFiles10.getUrl() == null ? new NullParam(Types.VARCHAR) : new InParam(accountingFiles10.getUrl(), Types.VARCHAR),
      accountingFiles10.getStatus() == null ? new NullParam(Types.VARCHAR) : new InParam(accountingFiles10.getStatus().getValue(), Types.VARCHAR),
      new OutParam("_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchemaAccounting() + ".mc_acc_create_accounting_file_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_create_accounting_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    accountingFiles10 =  searchAccountingFileById(header,getNumberUtils().toLong(resp.get("_id")));
    return accountingFiles10;
  }

  @Override
  public List<AccountingFiles10>  searchAccountingFile(Map<String, Object>header, Long id, String fileName, String fileId, AccountingStatusType status) throws Exception {

    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      fileName != null ? fileName : new NullParam(Types.VARCHAR),
      fileId != null ? fileId : new NullParam(Types.VARCHAR),
      status != null ? status.getValue() : new NullParam(Types.VARCHAR)
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      AccountingFiles10 c = new AccountingFiles10();
      c.setId(numberUtil.toLong(row.get("_id")));
      c.setName(String.valueOf(row.get("_name")));
      c.setFileId(String.valueOf(row.get("_file_id")));
      c.setFileFormatType(AccountingFileFormatType.fromValue(String.valueOf(row.get("_format"))));
      c.setFileType(AccountingFileType.fromValue(String.valueOf(row.get("_type"))));
      c.setUrl(String.valueOf(row.get("_url")));
      c.setStatus(AccountingStatusType.valueOfEnum(row.get("_status").toString().trim()));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_created"));
      timestamps.setUpdatedAt((Timestamp)row.get("_updated"));
      c.setTimestamps(timestamps);
      return c;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_search_accounting_file_v10",  rm, params);
    List<AccountingFiles10> res = (List<AccountingFiles10>)resp.get("result");
    log.info(res);
    return res != null ? res : Collections.EMPTY_LIST;
  }

  @Override
  public AccountingFiles10 searchAccountingFileById(Map<String, Object> header, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    List<AccountingFiles10> searchAccountingFiles10s = searchAccountingFile(null,id,null,null,null);
    return searchAccountingFiles10s != null && !searchAccountingFiles10s.isEmpty() ? searchAccountingFiles10s.get(0) : null;
  }

  @Override
  public AccountingFiles10 updateAccountingFile(Map<String, Object> header, Long id,String fileId,String url, AccountingStatusType status) throws Exception {
    if(id == null && status == null ){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountingFiles10"));
    }
    if(fileId == null && status == null && url == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "allNull"));
    }
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      fileId != null ? fileId : new NullParam(Types.VARCHAR),
      url != null ? url : new NullParam(Types.VARCHAR),
      status != null ? status.getValue() : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp = getDbUtils().execute(getSchemaAccounting() + ".mc_acc_update_accounting_file_v10", params);
    log.info(resp);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_acc_update_accounting_file_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
    return searchAccountingFileById(header,id);
  }

}
