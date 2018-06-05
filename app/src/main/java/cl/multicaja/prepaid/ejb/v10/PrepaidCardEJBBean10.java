package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.users.model.v10.Timestamps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidCardEJBBean10 implements PrepaidCardEJB10 {

  private static Log log = LogFactory.getLog(PrepaidCardEJBBean10.class);

  protected NumberUtils numberUtils = NumberUtils.getInstance();

  private ConfigUtils configUtils;

  private DBUtils dbUtils;

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  /**
   *
   * @return
   */
  public DBUtils getDbUtils() {
    if (this.dbUtils == null) {
      this.dbUtils = new DBUtils(this.getConfigUtils());
    }
    return this.dbUtils;
  }

  /**
   *
   * @return
   */
  private String getSchema() {
    return this.getConfigUtils().getProperty("schema");
  }

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    return map;
  }

  @Override
  public PrepaidCard10 createPrepaidCard(Map<String, Object> headers, PrepaidCard10 prepaidCard) throws Exception {

    if(prepaidCard == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "prepaidCard"));
    }

    if(prepaidCard.getIdUser() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "idUser"));
    }

    if(prepaidCard.getStatus() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      prepaidCard.getIdUser(),
      prepaidCard.getPan()==null ?new NullParam(Types.VARCHAR):prepaidCard.getPan(),
      prepaidCard.getEncryptedPan()==null ?new NullParam(Types.VARCHAR):prepaidCard.getEncryptedPan(),
      prepaidCard.getProcessorUserId()==null ?new NullParam(Types.VARCHAR):prepaidCard.getProcessorUserId(),
      prepaidCard.getExpiration()==null ?new NullParam(Types.INTEGER):prepaidCard.getExpiration(),
      prepaidCard.getStatus().toString(),
      prepaidCard.getNameOnCard()==null ?new NullParam(Types.VARCHAR):prepaidCard.getNameOnCard(),
      prepaidCard.getProducto() == null ? new NullParam(Types.VARCHAR) : prepaidCard.getProducto(),
      prepaidCard.getNumeroUnico() == null ? new NullParam(Types.VARCHAR): prepaidCard.getNumeroUnico(),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crear_tarjeta_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidCard.setId(numberUtils.toLong(resp.get("_r_id")));
      return prepaidCard;
    } else {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }

  @Override
  public List<PrepaidCard10> getPrepaidCards(Map<String, Object> headers, Long id, Long userId, Integer expiration, PrepaidCardStatus status, String processorUserId) throws Exception {
    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      userId != null ? userId : new NullParam(Types.BIGINT),
      expiration != null ? expiration : new NullParam(Types.INTEGER),
      status != null ? status.toString() : new NullParam(Types.VARCHAR),
      processorUserId != null ? processorUserId : new NullParam(Types.VARCHAR)
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      PrepaidCard10 c = new PrepaidCard10();
      c.setId(numberUtils.toLong(row.get("_id"), null));
      c.setIdUser(numberUtils.toLong(row.get("_id_usuario"), null));
      c.setPan(String.valueOf(row.get("_pan")));
      c.setEncryptedPan(String.valueOf(row.get("_pan_encriptado")));
      c.setProcessorUserId(String.valueOf(row.get("_contrato")));
      c.setExpiration(numberUtils.toInteger(row.get("_expiracion"), null));
      c.setStatus(PrepaidCardStatus.valueOfEnum(row.get("_estado").toString().trim()));
      c.setNameOnCard(String.valueOf(row.get("_nombre_tarjeta")));
      c.setProducto(String.valueOf(row.get("_producto")));
      c.setNumeroUnico(String.valueOf(row.get("_numero_unico")));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_fecha_creacion"));
      timestamps.setUpdatedAt((Timestamp)row.get("_fecha_actualizacion"));
      c.setTimestamps(timestamps);
      return c;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_tarjetas_v10", rm, params);
    return (List)resp.get("result");
  }

  @Override
  public PrepaidCard10 getPrepaidCardById(Map<String, Object> headers, Long id) throws Exception {
    if(id == null){
      throw new ValidationException(2);
    }
    List<PrepaidCard10> lst = this.getPrepaidCards(headers, id, null, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidCard10 getPrepaidCardByUserId(Map<String, Object> headers, Long userId, PrepaidCardStatus status) throws Exception {
    if(userId == null){
      throw new ValidationException(2);
    }
    if(status == null){
      throw new ValidationException(2);
    }
    List<PrepaidCard10> lst = this.getPrepaidCards(headers, null, userId, null, status, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public void updatePrepaidCardStatus(Map<String, Object> headers, Long id, PrepaidCardStatus status) throws Exception {

    if(id == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "id"));
    }
    if(status == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      id, //id
      status.toString(), //estado
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(getSchema() + ".mc_prp_actualizar_estado_tarjeta_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }

  @Override
  public boolean updateCard(Map<String, Object> headers,Long cardId, Long userId, PrepaidCardStatus oldState, PrepaidCard10 prepaidCard) throws Exception {

    final String SP_NAME = getSchema() + ".mc_prp_actualiza_tarjeta_v10";

    Object[] params = {
      cardId == null ? new NullParam(Types.BIGINT): cardId,
      userId == null ? new NullParam(Types.BIGINT):userId , //_id_usuario
      oldState == null ? new NullParam(Types.VARCHAR):oldState.toString() ,
      prepaidCard.getPan(), //_pan
      prepaidCard.getEncryptedPan(), //_pan_encriptado
      prepaidCard.getProcessorUserId(), //_contrato
      prepaidCard.getExpiration(), //_expiracion
      prepaidCard.getStatus().toString(), //_estado
      prepaidCard.getNameOnCard(), //_nombre_tarjeta
      prepaidCard.getProducto(), //_producto
      prepaidCard.getNumeroUnico(), //_numero_unico
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    log.info("resp update card: " + resp);

    if(resp.get("_error_code").equals("0")){
      return true;
    } else {
      return false;
    }
  }
}
