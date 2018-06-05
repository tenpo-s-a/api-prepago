package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.*;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidUserEJBBean10 implements PrepaidUserEJB10 {

  private static Log log = LogFactory.getLog(PrepaidUserEJBBean10.class);

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
  public PrepaidUser10 createPrepaidUser(Map<String, Object> headers, PrepaidUser10 prepaidUser) throws Exception {

    if(prepaidUser == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "prepaidUser"));
    }

    if(prepaidUser.getIdUserMc() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "idUserMc"));
    }

    if(prepaidUser.getRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut"));
    }

    if(prepaidUser.getStatus() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      prepaidUser.getIdUserMc(),
      prepaidUser.getRut(),
      prepaidUser.getStatus().toString(),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crear_usuario_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidUser.setId(numberUtils.toLong(resp.get("_r_id")));
      return prepaidUser;
    } else {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }

  @Override
  public List<PrepaidUser10> getPrepaidUsers(Map<String, Object> headers, Long userId, Long userIdMc, Integer rut, PrepaidUserStatus status) throws Exception {
    //si viene algun parametro en null se establece NullParam
    Object[] params = {
      userId != null ? userId : new NullParam(Types.BIGINT),
      userIdMc != null ? userIdMc : new NullParam(Types.BIGINT),
      rut != null ? rut : new NullParam(Types.INTEGER),
      status != null ? status.toString() : new NullParam(Types.VARCHAR)
    };
    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      PrepaidUser10 u = new PrepaidUser10();
      u.setId(numberUtils.toLong(row.get("_id"), null));
      u.setIdUserMc(numberUtils.toLong(row.get("_id_usuario_mc"), null));
      u.setRut(numberUtils.toInteger(row.get("_rut"), null));
      u.setStatus(PrepaidUserStatus.valueOfEnum(row.get("_estado").toString().trim()));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_fecha_creacion"));
      timestamps.setUpdatedAt((Timestamp)row.get("_fecha_actualizacion"));
      u.setTimestamps(timestamps);
      return u;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_usuarios_v10", rm, params);
    return (List)resp.get("result");
  }

  @Override
  public PrepaidUser10 getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception {
    if(userId == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "userId"));
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, userId, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser10 getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception {
    if(userIdMc == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "userIdMc"));
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, null, userIdMc, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser10 getPrepaidUserByRut(Map<String, Object> headers, Integer rut) throws Exception {
    if(rut == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut"));
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, null, null, rut, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public void updatePrepaidUserStatus(Map<String, Object> headers, Long id, PrepaidUserStatus status) throws Exception {

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

    Map<String, Object> resp = dbUtils.execute(getSchema() + ".mc_prp_actualizar_estado_usuario_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }

  @Override
  public PrepaidUserLevel getUserLevel(User oUser, PrepaidUser10 prepaidUser10) throws Exception {
    if(oUser == null) {
      throw new NotFoundException(102001);
    }
    if(oUser.getRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut"));
    }
    if(oUser.getRut().getStatus() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut.status"));
    }
    if(prepaidUser10 == null) {
      throw new NotFoundException(102003);
    }

    if(RutStatus.VERIFIED.equals(oUser.getRut().getStatus()) && NameStatus.VERIFIED.equals(oUser.getNameStatus())) {
      return PrepaidUserLevel.LEVEL_2;
    }
    else {
      return PrepaidUserLevel.LEVEL_1;
    }
  }
}
