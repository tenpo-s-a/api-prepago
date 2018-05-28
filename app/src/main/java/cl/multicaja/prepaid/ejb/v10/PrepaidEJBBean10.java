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
import java.util.Date;
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

  protected ParametersUtil parametersUtil = ParametersUtil.getInstance();

  private ConfigUtils configUtils;

  private DBUtils dbUtils;

  private final BigDecimal ONE_HUNDRED = new BigDecimal(100);

  // TODO: externalizar estos porcentajes?
  private final BigDecimal POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  private final BigDecimal IVA_PERCENTAGE = new BigDecimal(19);

  private final static String APP_NAME = "prepaid.appname";


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

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @EJB
  private UsersEJBBean10 usersEJB10;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  public PrepaidTopupDelegate10 getDelegate() {
    return delegate;
  }

  public void setDelegate(PrepaidTopupDelegate10 delegate) {
    this.delegate = delegate;
  }

  public UsersEJBBean10 getUsersEJB10() {
    return usersEJB10;
  }

  public void setUsersEJB10(UsersEJBBean10 usersEJB10) {
    this.usersEJB10 = usersEJB10;
  }

  public CdtEJBBean10 getCdtEJB10() {
    return cdtEJB10;
  }

  public void setCdtEJB10(CdtEJBBean10 cdtEJB10) {
    this.cdtEJB10 = cdtEJB10;
  }

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
    User user = this.getUsersEJB10().getUserByRut(headers, topupRequest.getRut());

    if(user == null){
      throw new NotFoundException(102001); // Usuario MC no existe
    }
    if(!UserStatus.ENABLED.toString().equals(user.getGlobalStatus())){
      throw new ValidationException(102002); // Usuario MC bloqueado o borrado
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByRut(null, user.getRut().getValue());

    if(prepaidUser == null){
      throw new NotFoundException(302003); // Usuario no tiene prepago
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(302002); // Usuario prepago bloqueado o borrado
    }

    if(PrepaidUserLevel.LEVEL_1 != this.getUserLevel(user,prepaidUser)) {
      // Si el usuario tiene validacion > N1, no aplica restriccion de primera carga
      topupRequest.setFirstTopup(Boolean.FALSE);
    }

    /*
      Identificar ID Tipo de Movimiento
        - N = 1 -> Primera Carga
        - CodCom = WEB -> Carga WEB
        - CodCom != WEB -> Carga POS
     */

    /*
    1- La API deberá ir a consultar el estado de la tarjeta a cargar. Para esto se deberá ir a la BBDD de prepago a la tabla tarjetas y consultar el estado.
    2- Si el estado es fecha expirada o bloqueada dura se deberá responder un mensaje de error al switch o POS Tarjeta inválida
    3- Para cualquier otro estado de la tarjeta, se deberá seguir el proceso
     */

    PrepaidCard10 card = this.getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

    if (card == null) {

      card = this.getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED);

      if (card == null) {

        card = this.getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED_HARD);

        if (card == null) {
          card = this.getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.EXPIRED);
        }

        if (card != null) {
          throw new ValidationException(106000); //tarjeta invalida
        }
      }
    }

    /*
      Validar movimiento en CDT, en caso de error lanzar exception
     */

    CdtTransaction10 oCdtTransaction10 = new CdtTransaction10();
    oCdtTransaction10.setAmount(topupRequest.getAmount().getValue());
    oCdtTransaction10.setTransactionType(topupRequest.getCdtTransactionType());
    oCdtTransaction10.setAccountId(getConfigUtils().getProperty(APP_NAME)+"_"+user.getRut().getValue());
    oCdtTransaction10.setGloss(topupRequest.getCdtTransactionType().getName()+" "+topupRequest.getAmount().getValue());
    oCdtTransaction10.setTransactionReference(0L);
    oCdtTransaction10.setExternalTransactionId(topupRequest.getTransactionId());

    oCdtTransaction10 = this.getCdtEJB10().addCdtTransaction(null,oCdtTransaction10);

    // Si no cumple con los limites
    if(!oCdtTransaction10.getNumError().equals("0")){
      long lNumError = numberUtils.toLong(oCdtTransaction10.getNumError(),-1L);
      if(lNumError != -1 && lNumError > 10000)
        throw new ValidationException(4).setData(new KeyValue("value",oCdtTransaction10.getMsjError()));
      else
        throw new ValidationException(2);
    }


    PrepaidTopup10 topup = new PrepaidTopup10(topupRequest);
    topup.setId(oCdtTransaction10.getTransactionReference());
    topup.setUserId(user.getId());
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps());

    /*
      Calcular monto a cargar y comisiones
     */
    this.calculateTopupFeeAndTotal(topup);

    /*
      Enviar mensaje al proceso asincrono
     */
    String messageId = this.getDelegate().sendTopUp(topup, user);
    topup.setMessageId(messageId);

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

    if(prepaidUser.getIdUserMc() == null){
      throw new ValidationException(2);
    }

    if(prepaidUser.getRut() == null){
      throw new ValidationException(2);
    }

    if(prepaidUser.getStatus() == null){
      throw new ValidationException(2);
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
  public void updatePrepaidUserStatus(Map<String, Object> headers, Long id, PrepaidUserStatus status) throws Exception {
    if(id == null){
      throw new ValidationException(2);
    }
    if(status == null){
      throw new ValidationException(2);
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
  public PrepaidCard10 createPrepaidCard(Map<String, Object> headers, PrepaidCard10 prepaidCard) throws Exception {

    if(prepaidCard == null){
      throw new ValidationException(2);
    }

    if(prepaidCard.getIdUser() == null){
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
      prepaidCard.getIdUser(),
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
      throw new ValidationException(2);
    }
    if(status == null){
      throw new ValidationException(2);
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
      throw new IllegalStateException();
    }

    CodigoMoneda currencyCodeClp = CodigoMoneda.CHILE_CLP;

    NewAmountAndCurrency10 total = new NewAmountAndCurrency10();
    total.setCurrencyCode(currencyCodeClp);
    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10();
    fee.setCurrencyCode(currencyCodeClp);

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

  /**
   *  Verifica el nivel del usuario
   * @param oUser usuario multicaja
   * @param prepaidUser10 usuario prepago
   * @return el nivel del usuario
   */
  public PrepaidUserLevel getUserLevel(User oUser, PrepaidUser10 prepaidUser10) throws Exception {
    if(oUser == null) {
      throw new NotFoundException(102001);
    }
    if(oUser.getRut() == null || oUser.getRut().getStatus() == null){
      throw new ValidationException(101000);
    }
    if(prepaidUser10 == null) {
      throw new NotFoundException(302003);
    }

    if(RutStatus.VERIFIED.equals(oUser.getRut().getStatus()) && UserNameStatus.VERIFIED.equals(oUser.getNameStatus())) {
      return PrepaidUserLevel.LEVEL_2;
    }
    else {
      return PrepaidUserLevel.LEVEL_1;
    }
  }

  /**
   *
   * @param prepaidTopup
   * @param prepaidUser
   * @param prepaidCard
   * @param cdtTransaction
   * @return
   */
  private PrepaidMovement10 buildPrepaidMovement(PrepaidTopup10 prepaidTopup, PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) {

    String codEntity = null;
    try {
      codEntity = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      log.error("Error al cargar parametro cod_entidad");
      codEntity = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura = null;

    if (TopupType.WEB.equals(prepaidTopup.getType())) {
      tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
    } else {
      tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(cdtTransaction.getExternalTransactionId());
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement.setMonto(prepaidTopup.getAmount().getValue());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setCodent(codEntity);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(prepaidTopup.getAmount().getValue());
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(prepaidTopup.getMerchantCode());
    prepaidMovement.setCodact(String.valueOf(prepaidTopup.getMerchantCategory()));
    prepaidMovement.setImpliq(0L); // se debe actualizar despues
    prepaidMovement.setClamonliq(0); // se debe actualizar despues
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob(""); // se debe actualizar despues
    prepaidMovement.setNumextcta(0); // se debe actualizar despues
    prepaidMovement.setNummovext(0); // se debe actualizar despues
    prepaidMovement.setClamone(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setTipolin(""); // se debe actualizar despues
    prepaidMovement.setLinref(1); // se debe actualizar despues
    prepaidMovement.setNumbencta(1); // se debe actualizar despues
    prepaidMovement.setNumplastico(123L); // se debe actualizar despues

    return prepaidMovement;
  }
}
