package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

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
  private PrepaidCardEJBBean10 prepaidCardEJB10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJB10;

  private TecnocomService tecnocomService;

  private UserClient userClient;

  public PrepaidCardEJBBean10 getPrepaidCardEJB10() {
    return prepaidCardEJB10;
  }

  public void setPrepaidCardEJB10(PrepaidCardEJBBean10 prepaidCardEJB10) {
    this.prepaidCardEJB10 = prepaidCardEJB10;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJB10() {
    return prepaidMovementEJB10;
  }

  public void setPrepaidMovementEJB10(PrepaidMovementEJBBean10 prepaidMovementEJB10) {
    this.prepaidMovementEJB10 = prepaidMovementEJB10;
  }

  @Override
  public UserClient getUserClient() {
    if(userClient == null) {
      userClient = UserClient.getInstance();
    }
    return userClient;
  }

  @Override
  public TecnocomService getTecnocomService() {
    if(tecnocomService == null) {
      tecnocomService = TecnocomServiceHelper.getInstance().getTecnocomService();
    }
    return tecnocomService;
  }

  @Override
  public PrepaidUser10 createPrepaidUser(Map<String, Object> headers, PrepaidUser10 prepaidUser) throws Exception {

    if(prepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidUser"));
    }

    if(prepaidUser.getUserIdMc() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idUserMc"));
    }

    if(prepaidUser.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }

    if(prepaidUser.getStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      prepaidUser.getUserIdMc(),
      prepaidUser.getRut(),
      prepaidUser.getStatus().toString(),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crear_usuario_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidUser.setId(getNumberUtils().toLong(resp.get("_r_id")));
      return prepaidUser;
    } else {
      log.error("createPrepaidUser resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
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
      u.setId(getNumberUtils().toLong(row.get("_id"), null));
      u.setUserIdMc(getNumberUtils().toLong(row.get("_id_usuario_mc"), null));
      u.setRut(getNumberUtils().toInteger(row.get("_rut"), null));
      u.setStatus(PrepaidUserStatus.valueOfEnum(row.get("_estado").toString().trim()));
      u.setBalanceExpiration(0L);
      try {
        String saldo = String.valueOf(row.get("_saldo_info"));
        if (StringUtils.isNotBlank(saldo)) {
          u.setBalance(JsonUtils.getJsonParser().fromJson(saldo, PrepaidBalanceInfo10.class));
          u.setBalanceExpiration(getNumberUtils().toLong(row.get("_saldo_expiracion")));
        }
      } catch(Exception ex) {
        log.error("Error al convertir el saldo del usuario", ex);
      }
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt((Timestamp)row.get("_fecha_creacion"));
      timestamps.setUpdatedAt((Timestamp)row.get("_fecha_actualizacion"));
      u.setTimestamps(timestamps);
      u.setIdentityVerificationAttempts(getNumberUtils().toInteger(row.get("_intentos_validacion")));
      return u;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_usuarios_v10", rm, params);
    return (List)resp.get("result");
  }

  @Override
  public PrepaidUser10 getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception {
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, userId, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser10 getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception {
    if(userIdMc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userIdMc"));
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, null, userIdMc, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser10 getPrepaidUserByRut(Map<String, Object> headers, Integer rut) throws Exception {
    if(rut == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, null, null, rut, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public void updatePrepaidUserStatus(Map<String, Object> headers, Long userId, PrepaidUserStatus status) throws Exception {

    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      userId, //id
      status.toString(), //estado
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_actualizar_estado_usuario_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updatePrepaidUserStatus resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public PrepaidUser10 getUserLevel(User user, PrepaidUser10 prepaidUser10) throws Exception {

    if(user == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user"));
    }
    if(user.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.rut"));
    }
    if(user.getRut().getStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.rut.status"));
    }
    if(user.getNameStatus() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "user.nameStatus"));
    }
    if(prepaidUser10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidUser"));
    }

    if(RutStatus.VERIFIED.equals(user.getRut().getStatus()) && NameStatus.VERIFIED.equals(user.getNameStatus())) {
      prepaidUser10.setUserLevel(PrepaidUserLevel.LEVEL_2);
    } else {
      prepaidUser10.setUserLevel(PrepaidUserLevel.LEVEL_1);
    }
    return prepaidUser10;
  }

  @Override
  public PrepaidBalance10 getPrepaidUserBalance(Map<String, Object> headers, Long userIdMc) throws Exception {

    if(userIdMc == null){
      userIdMc = this.verifiUserAutentication(headers);
    }

    // Obtener Usuario MC
    User user = getUserClient().getUserById(headers, userIdMc);

    if(user == null){
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if(!UserStatus.ENABLED.equals(user.getGlobalStatus())){
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByRut(headers, user.getRut().getValue());

    if(prepaidUser == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    //permite refrescar el saldo del usuario de forma obligada, usado principalmente en test o podria usarse desde la web
    boolean forceRefreshBalance = headers != null ? getNumberUtils().toBoolean(headers.get("forceRefreshBalance"), false) : false;

    Long balanceExpiration = prepaidUser.getBalanceExpiration();

    boolean updated = false;
    PrepaidBalanceInfo10 pBalance = prepaidUser.getBalance();

    //solamente si el usuario no tiene saldo registrado o se encuentra expirado, se busca en tecnocom
    if (pBalance == null || balanceExpiration <= 0 || System.currentTimeMillis() >= balanceExpiration || forceRefreshBalance) {

      //se busca la ultima tarjeta para obtener el contrado de ella, aqui se puede lanzar una excepcion con codigos (TARJETA_PRIMERA_CARGA_PENDIENTE o TARJETA_PRIMERA_CARGA_EN_PROCESO)
      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser.getId());

      if(prepaidCard10 == null) {

        //Obtener ultimo movimiento
        PrepaidMovement10 movement = getPrepaidMovementEJB10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
          PrepaidMovementStatus.PENDING,
          PrepaidMovementStatus.IN_PROCESS);

        // Si el ultimo movimiento esta en estatus Pendiente o En Proceso
        if(movement != null){
          throw new ValidationException(TARJETA_PRIMERA_CARGA_EN_PROCESO);
        } else {
          throw new ValidationException(TARJETA_PRIMERA_CARGA_PENDIENTE);
        }

      } else if(PrepaidCardStatus.PENDING.equals(prepaidCard10.getStatus())) {
        throw new ValidationException(TARJETA_PRIMERA_CARGA_EN_PROCESO);
      }

      ConsultaSaldoDTO consultaSaldoDTO = getTecnocomService().consultaSaldo(prepaidCard10.getProcessorUserId(), prepaidUser.getRut().toString(), TipoDocumento.RUT);

      if (consultaSaldoDTO != null && consultaSaldoDTO.isRetornoExitoso()) {
        pBalance = new PrepaidBalanceInfo10(consultaSaldoDTO);
        try {
          this.updatePrepaidUserBalance(headers, prepaidUser.getId(), pBalance);
          updated = true;
        } catch(Exception ex) {
          log.error("Error al actualizar el saldo del usuario: " + userIdMc, ex);
        }
      } else {
        String codErrorTecnocom = consultaSaldoDTO != null ? consultaSaldoDTO.getRetorno() : null;
        throw new ValidationException(SALDO_NO_DISPONIBLE_$VALUE).setData(new KeyValue("value", codErrorTecnocom));
      }
    }

    //https://www.pivotaltracker.com/story/show/158367667
    //por defecto debe ser 0
    BigDecimal balanceValue = BigDecimal.valueOf(0L);

    if (pBalance != null) {
      //El que le mostraremos al cliente será el saldo dispuesto principal menos el saldo autorizado principal
      balanceValue = BigDecimal.valueOf(pBalance.getSaldisconp().longValue() - pBalance.getSalautconp().longValue());
    }

    if(balanceValue.compareTo(BigDecimal.ZERO) < 0) {
      balanceValue = balanceValue.multiply(BigDecimal.valueOf(-1));
    }

    NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(balanceValue);
    NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
    NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

    //TODO: debe ser el valor de venta o el valor del día?.
    return new PrepaidBalance10(balance, pcaMain, pcaSecondary, getCalculationsHelper().getUsdValue().intValue(), updated);
  }

  @Override
  public void updatePrepaidUserBalance(Map<String, Object> headers, Long userId, PrepaidBalanceInfo10 balance) throws Exception {

    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    if(balance == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "balance"));
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
      log.error("updatePrepaidUserBalance resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  public void updatePrepaidUserLevel(Long userId, PrepaidUserLevel level) throws BaseException {
    if(userId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    if(level == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "level"));
    }

    StringBuilder sb = new StringBuilder();
    sb.append("nivel = '")
      .append(level.toString())
      .append("', ");

    sb.append("fecha_actualizacion = timezone('utc', now())");

    int resp = getDbUtils().getJdbcTemplate().update(String.format("UPDATE %s.prp_usuario SET %s WHERE id = ?", getSchema(), sb.toString()), userId);

    if(resp == 0) {
      throw new ValidationException(TARJETA_NO_EXISTE);
    }
  }

  @Override
  public PrepaidUser10 incrementIdentityVerificationAttempt(Map<String, Object> headers, PrepaidUser10 prepaidUser) throws Exception {
    if(prepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidUser"));
    }

    if(prepaidUser.getId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    Object[] params = {
      prepaidUser.getId(),
      new OutParam("_intentos_validacion", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_incrementa_intento_validacion_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidUser.setIdentityVerificationAttempts(getNumberUtils().toInteger(resp.get("_intentos_validacion")));
      return prepaidUser;
    } else {
      log.error("incrementIdentityVerificationAttempt resp: " + resp);
      throw new BaseException(ERROR_INTERNO_BBDD);
    }
  }
}
