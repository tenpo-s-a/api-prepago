package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.domain.v10.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.Timestamps;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.math.BigDecimal;
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

  private final BigDecimal ONE_HUNDRED = new BigDecimal(100);

  // TODO: externalizar estos porcentajes?
  private final BigDecimal POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  private final BigDecimal IVA_PERCENTAGE = new BigDecimal(19);

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
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {
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

    PrepaidTopup10 topup = new PrepaidTopup10(topupRequest);
    // Id Solicitud de carga devuelto por CDT
    topup.setId(numberUtils.random(1, Integer.MAX_VALUE));
    // UserId
    // topup.setUserId(user.getId());
    topup.setUserId(1);
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps());

    /*
      Calcular monto a cargar y comisiones
     */
    this.calculateTopupFeeAndTotal(topup);

    /*
      Enviar mensaje a cosa de carga
     */
    // TODO: Enviar mensaje a cola de carga

    delegate.sendTopUp(topup, user);

    return topup;
  }

  @Override
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) {

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
  public PrepaidUser10 createPrepaidUser(Map<String, Object> headers, PrepaidUser10 prepaidUser) throws Exception {

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

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crear_usuario_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidUser.setId(numberUtils.toLong(resp.get("_r_id"), 0));
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
      status != null ? status.toString() : new NullParam(Types.VARCHAR),
      //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
      new OutParam("_result", Types.OTHER, (Map<String, Object> row) -> {
        PrepaidUser10 u = new PrepaidUser10();
        u.setId(numberUtils.toLong(row.get("id")));
        u.setIdUser(numberUtils.toLong(row.get("id_usuario_mc")));
        u.setRut(numberUtils.toInt(row.get("rut")));
        u.setStatus(PrepaidUserStatus.valueOfEnum(row.get("estado").toString().trim()));
        Timestamps timestamps = new Timestamps();
        timestamps.setCreatedAt((Timestamp)row.get("fecha_creacion"));
        timestamps.setUpdatedAt((Timestamp)row.get("fecha_actualizacion"));
        return u;
      }),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_usuarios_v10", params);
    return (List)resp.get("_result");
  }

  @Override
  public PrepaidUser10 getPrepaidUserById(Map<String, Object> headers, Long userId) throws Exception {
    if(userId == null){
      throw new ValidationException(2);
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, userId, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser10 getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception {
    if(userIdMc == null){
      throw new ValidationException(2);
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, null, userIdMc, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidUser10 getPrepaidUserByRut(Map<String, Object> headers, Integer rut) throws Exception {
    if(rut == null){
      throw new ValidationException(2);
    }
    List<PrepaidUser10> lst = this.getPrepaidUsers(headers, null, null, rut, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidCard10 createPrepaidCard(Map<String, Object> headers, PrepaidCard10 prepaidCard) throws Exception {

    if(prepaidCard == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getUserId() == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getPan() == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getEncryptedPan() == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getProcessorUserId() == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getExpiration() == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getStatus() == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getNameOnCard() == null){
      throw new ValidationException(2);
    }

    Object[] params = {
      prepaidCard.getUserId(),
      prepaidCard.getPan(),
      prepaidCard.getEncryptedPan(),
      prepaidCard.getProcessorUserId(),
      prepaidCard.getExpiration(),
      prepaidCard.getStatus().toString(),
      prepaidCard.getNameOnCard(),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crear_tarjeta_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      prepaidCard.setId(numberUtils.toLong(resp.get("_r_id"), 0));
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
      processorUserId != null ? processorUserId : new NullParam(Types.VARCHAR),
      //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
      new OutParam("_result", Types.OTHER, (Map<String, Object> row) -> {
        PrepaidCard10 c = new PrepaidCard10();
        c.setId(numberUtils.toLong(row.get("id")));
        c.setUserId(numberUtils.toLong(row.get("id_usuario")));
        c.setPan(String.valueOf(row.get("pan")));
        c.setEncryptedPan(String.valueOf(row.get("pan_encriptado")));
        c.setProcessorUserId(String.valueOf(row.get("contrato")));
        c.setExpiration(numberUtils.toInt(row.get("expiracion")));
        c.setStatus(PrepaidCardStatus.valueOfEnum(row.get("estado").toString().trim()));
        c.setNameOnCard(String.valueOf(row.get("nombre_tarjeta")));
        Timestamps timestamps = new Timestamps();
        timestamps.setCreatedAt((Timestamp)row.get("fecha_creacion"));
        timestamps.setUpdatedAt((Timestamp)row.get("fecha_actualizacion"));
        return c;
      }),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_usuarios_v10", params);
    return (List)resp.get("_result");
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
  public PrepaidCard10 getPrepaidCardByUserId(Map<String, Object> headers, Long userId) throws Exception {
    if(userId == null){
      throw new ValidationException(2);
    }
    List<PrepaidCard10> lst = this.getPrepaidCards(headers, null, userId, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  /**
   *  Calcula la comision y total a cargar segun el el tipo de carga (POS/WEB)
   *
   * @param topup al que se le calculara la comision y total
   * @throws IllegalStateException si el topup es null
   * @throws IllegalStateException si el topup.amount es null
   * @throws IllegalStateException si el topup.amount.value es null
   * @throws IllegalStateException si el topup.merchantCode es null o vacio
   */
  @Override
  public void calculateTopupFeeAndTotal(PrepaidTopup10 topup) throws Exception {

    if(topup == null || topup.getAmount() == null || topup.getAmount().getValue() == null || StringUtils.isBlank(topup.getMerchantCode())){
      throw  new IllegalStateException();
    }

    NewAmountAndCurrency10 total = new NewAmountAndCurrency10();
    total.setCurrencyCode(152);
    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10();
    fee.setCurrencyCode(152);

    // Calcula las comisiones segun el tipo de carga (WEB o POS)
    switch (topup.getType()) {
      case WEB:
        fee.setValue(new BigDecimal(0));
        break;
      case POS:
        // MAX(100; 0,5% * prepaid_topup_new_amount_value) + IVA

        BigDecimal com = topup.getAmount().getValue().multiply(POS_COMMISSION_PERCENTAGE).divide(ONE_HUNDRED);
        // Calcula el max
        BigDecimal max = com.max(new BigDecimal(100));
        // Suma IVA
        fee.setValue(max.add(max.multiply(IVA_PERCENTAGE).divide(ONE_HUNDRED)));
        break;
    }
    // Calculo el total
    total.setValue(topup.getAmount().getValue().subtract(fee.getValue()));

    topup.setFee(fee);
    topup.setTotal(total);
  }
}
