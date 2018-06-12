package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.users.model.v10.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidUserEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidUserEJB10 {

  private static Log log = LogFactory.getLog(PrepaidUserEJBBean10.class);

  public static Integer BALANCE_CACHE_EXPIRATION_MILLISECONDS = 60000;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  public PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
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
      u.setBalanceExpiration(0L);
      try {
        String saldo = String.valueOf(row.get("_saldo"));
        if (StringUtils.isNotBlank(saldo)) {
          u.setBalance(JsonUtils.getJsonParser().fromJson(saldo, PrepaidBalanceInfo10.class));
          u.setBalanceExpiration(numberUtils.toLong(row.get("_saldo_expiracion")));
        }
      } catch(Exception ex) {
        log.error("Error al convertir el saldo del usuario", ex);
      }
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
  public void updatePrepaidUserStatus(Map<String, Object> headers, Long userId, PrepaidUserStatus status) throws Exception {

    if(userId == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "id"));
    }
    if(status == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      userId, //id
      status.toString(), //estado
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_actualizar_estado_usuario_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }

  @Override
  public PrepaidUserLevel getUserLevel(User user, PrepaidUser10 prepaidUser10) throws Exception {

    if(user == null) {
      throw new NotFoundException(102001);
    }
    if(user.getRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut"));
    }
    if(user.getRut().getStatus() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut.status"));
    }
    if(prepaidUser10 == null) {
      throw new NotFoundException(102003);
    }

    if(RutStatus.VERIFIED.equals(user.getRut().getStatus()) && NameStatus.VERIFIED.equals(user.getNameStatus())) {
      return PrepaidUserLevel.LEVEL_2;
    } else {
      return PrepaidUserLevel.LEVEL_1;
    }
  }

  @Override
  public PrepaidBalance10 getPrepaidUserBalance(Map<String, Object> headers, Long userId) throws Exception {

    if(userId == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "userId"));
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserById(null, userId);

    if(prepaidUser == null){
      throw new NotFoundException(102003); // Usuario no tiene prepago
    }

    Long balanceExpiration = prepaidUser.getBalanceExpiration();

    boolean updated = false;
    PrepaidBalanceInfo10 pBalance = prepaidUser.getBalance();

    //solamente si el usuario no tiene saldo registrado o se encuentra expirado, se busca en tecnocom
    if (pBalance == null || balanceExpiration <= 0 || System.currentTimeMillis() >= balanceExpiration) {

      //se busca la ultima tarjeta para obtener el contrado de ella
      PrepaidCard10 prepaidCard10 = this.getPrepaidCardEJBBean10().getLastPrepaidCardByUserId(headers, userId);

      if (prepaidCard10 != null) {

        ConsultaSaldoDTO consultaSaldoDTO = TecnocomServiceHelper.getInstance().getTecnocomService().consultaSaldo(prepaidCard10.getProcessorUserId(), prepaidUser.getRut().toString(), TipoDocumento.RUT);

        if (consultaSaldoDTO != null) {
          pBalance = new PrepaidBalanceInfo10(consultaSaldoDTO);
          this.updatePrepaidUserBalance(headers, prepaidUser.getId(), pBalance);
          updated = true;
        }
      }
    }

    BigDecimal balance = BigDecimal.valueOf(0L);
    CodigoMoneda clamonp = CodigoMoneda.CHILE_CLP;

    if (pBalance != null) {
      //El que le mostraremos al cliente será el saldo dispuesto principal menos el saldo autorizado principal
      balance = BigDecimal.valueOf(pBalance.getSaldisconp().longValue() - pBalance.getSalautconp().longValue());
    }

    //TODO falta calcular el pcaClp y pcaUsd
    BigDecimal pcaClp = BigDecimal.valueOf(0L); //entero
    BigDecimal pcaUsd = BigDecimal.valueOf(0d); //decimal con 2 decimales

    pcaUsd = pcaUsd.setScale(2, RoundingMode.CEILING); //solo 2 decimales

    return new PrepaidBalance10(new NewAmountAndCurrency10(balance, clamonp), pcaClp, pcaUsd, updated);
  }

  @Override
  public void updatePrepaidUserBalance(Map<String, Object> headers, Long userId, PrepaidBalanceInfo10 balance) throws Exception {

    if(userId == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "userId"));
    }
    if(balance == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "balance"));
    }

    //expira en 1 minuto (60
    Long balanceExpiration = System.currentTimeMillis() + BALANCE_CACHE_EXPIRATION_MILLISECONDS;

    Object[] params = {
      userId, //id
      JsonUtils.getJsonParser().toJson(balance), //saldo
      balanceExpiration, //saldo_expiracion
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_actualizar_saldo_usuario_v10", params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }
}
