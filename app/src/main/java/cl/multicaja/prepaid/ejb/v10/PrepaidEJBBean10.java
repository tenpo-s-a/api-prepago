package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.helpers.ejb.v10.HelpersEJBBean10;
import cl.multicaja.prepaid.domain.v10.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
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
public class PrepaidEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

  protected NumberUtils numberUtils = NumberUtils.getInstance();

  private ConfigUtils configUtils;

  private DBUtils dbUtils;

  private static final String SCHEMA = "prepago";

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

  @EJB
  private UsersEJBBean10 usersEJB10;

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    map.put("ejb_users", this.usersEJB10.info());
    return map;
  }

  @Override
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) throws Exception {
    //TODO: lanzar las excepciones solo con el codigo del error especifico

    if(topupRequest == null || topupRequest.getAmount() == null){
      throw new ValidationException(2);
    }
    if(topupRequest.getRut() == null){
      throw new ValidationException(2);
    }
    if(StringUtils.isBlank(topupRequest.getMerchantCode())){
      throw new ValidationException(2);
    }
    if(StringUtils.isBlank(topupRequest.getTransactionId())){
      throw new ValidationException(2);
    }
    if(topupRequest.getAmount().getValue() == null){
      throw new ValidationException(2);
    }
    if(topupRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(2);
    }

    // Obtener Usuario
    //User user = this.usersEJB10.getUserByRut(headers, topupRequest.getRut());
    User user = new User();
    if(user == null){
      throw new NotFoundException(1);
    }

    /*
      Validar nivel del usuario
        - N = 0 Usuario MC null, Prepaid user null o usuario bloqueado
        - N = 1 Primera carga
        - N > 1 Carga
     */
    // Buscar usuario local de prepago
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    /*
      if(user.getGlobalStatus().equals("BLOQUEADO") || prepaidUser == null || prepaidUser.getStatus() == PrepaidUserStatus.DISABLED){
        // Si el usuario MC esta bloqueado o si no existe usuario local o el usuario local esta bloqueado, es N = 0
        throw new ValidationException(1024, "El cliente no pasó la validación");
      }
    */
    //if(user.getRut().getStatus().equals("VALIDADO_FOTO")){
    if(true){
      // Si el usuario tiene validacion de foto, es N = 2
      topupRequest.setFirstTopup(Boolean.FALSE);
    }

    /*
      Identificar ID Tipo de Movimiento
        - N = 1 -> Primera Carga
        - CodCom = WEB -> Carga WEB
        - CodCom != WEB -> Carga POS
     */
    CdtTransactionType cdtTpe = topupRequest.getCdtTransactionType();

    /*
      Validar movimiento en CDT, en caso de error lanzar exception
     */
    // TODO: Validar movimiento en CDT

    // Si no cumple con los limites
    if(false){
     /*
      En caso de ser TEF, iniciar proceso de devolucion
     */
      // TODO: Iniciar proceo de devolucion

      throw new ValidationException(2);
    }

    /*
      Calcular monto a cargar y comisiones
     */
    //TODO: Calcular monto y comisiones

    PrepaidTopup10 topup = new PrepaidTopup10(topupRequest);
    // Id Solicitud de carga devuelto por CDT
    topup.setId(numberUtils.random(1, Integer.MAX_VALUE));
    // UserId
    // topup.setUserId(user.getId());
    topup.setUserId(1);
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps10());

    /*
      Enviar mensaje a cosa de carga
     */
    // TODO: Enviar mensaje a cola de carga

    delegate.sendTopUp(topup, user);

    return topup;
  }

  @Override
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) {

  }

  @Override
  public List<PrepaidTopup10> getUserTopups(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidUserSignup10 initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup10 signupRequest) {
    return null;
  }

  @Override
  public PrepaidUserSignup10 getUserSignup(Map<String, Object> headers, Long signupId) {
    return null;
  }

  @Override
  public PrepaidCard10 issuePrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidCard10 getPrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidUser createPrepaidUser(Map<String, Object> headers, PrepaidUser prepaidUser) throws Exception {

    if(prepaidUser == null){
      throw new ValidationException(2);
    }

    if(prepaidUser.getIdUser() == null){
      throw new ValidationException(2);
    }

    if(prepaidUser.getRut() == null){
      throw new ValidationException(2);
    }

    if(prepaidUser.getStatus() == null){
      throw new ValidationException(2);
    }

    Object[] params = {
      prepaidUser.getIdUser(),
      prepaidUser.getRut(),
      prepaidUser.getStatus().toString(),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(SCHEMA + ".mc_prp_crear_usuario_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidUser.setId(numberUtils.toLong(resp.get("_r_id"), 0));
      return prepaidUser;
    } else {
      log.error("Error en invocacion a SP: " + resp);
      throw new BaseException(1);
    }
  }

  @Override
  public List<PrepaidUser> getPrepaidUsers(Map<String, Object> headers, Long userId, Long userIdMc, Integer rut, PrepaidUserStatus status) throws Exception {

    Object[] params = {
      userId != null ? userId : new NullParam(Types.BIGINT), //si biene parametro se envia, si no se envia NullParam
      userIdMc != null ? userIdMc : new NullParam(Types.BIGINT), //si biene parametro se envia, si no se envia NullParam
      rut != null ? rut : new NullParam(Types.INTEGER), //si biene parametro se envia, si no se envia NullParam
      status != null ? status.toString() : new NullParam(Types.VARCHAR), //si biene parametro se envia, si no se envia NullParam

      new OutParam("_result", Types.OTHER, (Map<String, Object> row) -> { //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row en ell objeto necesario
        PrepaidUser u = new PrepaidUser();
        u.setId(numberUtils.toLong(row.get("id"), 0));
        u.setIdUser(numberUtils.toLong(row.get("id_usuario_mc"), 0));
        u.setRut(numberUtils.toInt(row.get("rut"), 0));
        Timestamps timestamps = new Timestamps();
        timestamps.setCreatedAt((Timestamp)row.get("fecha_creacion"));
        timestamps.setUpdatedAt((Timestamp)row.get("fecha_actualizacion"));
        u.setStatus(PrepaidUserStatus.valueOfEnum(row.get("estado").toString().trim()));
        return u;
      }),

      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(SCHEMA + ".mc_prp_buscar_usuarios_v10", params);
    return (List)resp.get("_result");
  }

  @Override
  public PrepaidUser getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception {
    if(userId == null){
      throw new ValidationException(2);
    }
    List<PrepaidUser> lst = this.getPrepaidUsers(headers, userId, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception {
    if(userIdMc == null){
      throw new ValidationException(2);
    }
    List<PrepaidUser> lst = this.getPrepaidUsers(headers, null, userIdMc, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser getPrepaidUserByRut(Map<String, Object> headers, Integer rut) throws Exception {
    if(rut == null){
      throw new ValidationException(2);
    }
    List<PrepaidUser> lst = this.getPrepaidUsers(headers, null, null, rut, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }
}
