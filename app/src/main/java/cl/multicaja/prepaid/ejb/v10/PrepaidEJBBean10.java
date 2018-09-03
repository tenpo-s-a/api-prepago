package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.*;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.*;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.BloqueoDesbloqueoDTO;
import cl.multicaja.tecnocom.dto.ConsultaMovimientosDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.tecnocom.dto.MovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.calculateEed;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.calculatePca;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);
  private static BigDecimal NEGATIVE = new BigDecimal(-1);
  private static String APP_NAME = "api-prepaid";
  private static String TERMS_AND_CONDITIONS = "TERMS_AND_CONDITIONS";
  /**
   * Foto frontal de la CI del usuario
   */
  private static final String USER_ID_FRONT = "USER_ID_FRONT";

  /**
   * Foto posterior de la CI del usuario
   */
  private static final String USER_ID_BACK = "USER_ID_BACK";

  /**
   * Selfie del usuario con CI
   */
  private static final String USER_SELFIE= "USER_SELFIE";

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJB10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJB10;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJB10;

  private TecnocomService tecnocomService;

  private UserClient userClient;

  public PrepaidTopupDelegate10 getDelegate() {
    return delegate;
  }

  public void setDelegate(PrepaidTopupDelegate10 delegate) {
    this.delegate = delegate;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJB10() {
    return prepaidUserEJB10;
  }

  public void setPrepaidUserEJB10(PrepaidUserEJBBean10 prepaidUserEJB10) {
    this.prepaidUserEJB10 = prepaidUserEJB10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJB10() {
    return prepaidCardEJB10;
  }

  public void setPrepaidCardEJB10(PrepaidCardEJBBean10 prepaidCardEJB10) {
    this.prepaidCardEJB10 = prepaidCardEJB10;
  }

  public CdtEJBBean10 getCdtEJB10() {
    return cdtEJB10;
  }

  public void setCdtEJB10(CdtEJBBean10 cdtEJB10) {
    this.cdtEJB10 = cdtEJB10;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJB10() {
    return prepaidMovementEJB10;
  }

  public void setPrepaidMovementEJB10(PrepaidMovementEJBBean10 prepaidMovementEJB10) {
    this.prepaidMovementEJB10 = prepaidMovementEJB10;
  }

  @Override
  public TecnocomService getTecnocomService() {
    if(tecnocomService == null) {
      tecnocomService = TecnocomServiceHelper.getInstance().getTecnocomService();
    }
    return tecnocomService;
  }

  @Override
  public UserClient getUserClient() {
    if(userClient == null) {
      userClient = UserClient.getInstance();
    }
    return userClient;
  }
  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    return map;
  }

  @Override
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {

    this.validateTopupRequest(topupRequest);

    // Obtener usuario Multicaja
    User user = this.getUserMcByRut(headers, topupRequest.getRut());

    // Verificar si el usuario esta en lista negra
    if(user.getIsBlacklisted()) {
      throw new ValidationException(CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_CARGAR);
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, user.getId());

    //verifica el nivel del usuario
    prepaidUser = this.getPrepaidUserEJB10().getUserLevel(user,prepaidUser);

    if(!PrepaidUserLevel.LEVEL_1.equals(prepaidUser.getUserLevel())) {
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

    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                                            PrepaidCardStatus.ACTIVE,
                                                                                                            PrepaidCardStatus.LOCKED);

    if (prepaidCard == null) {

      prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                                  PrepaidCardStatus.LOCKED_HARD,
                                                                                                  PrepaidCardStatus.EXPIRED);

      if (prepaidCard != null) {
        throw new ValidationException(TARJETA_INVALIDA_$VALUE).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
      }
    }

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(topupRequest.getAmount().getValue());
    cdtTransaction.setTransactionType(topupRequest.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME)+"_"+user.getRut().getValue());
    cdtTransaction.setGloss(topupRequest.getCdtTransactionType().getName()+" "+topupRequest.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(topupRequest.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    // Si no cumple con los limites
    if(!cdtTransaction.isNumErrorOk()){
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10(topupRequest);
    prepaidTopup.setUserId(user.getId());
    prepaidTopup.setStatus("exitoso");
    prepaidTopup.setTimestamps(new Timestamps());

    /*
      Calcular monto a cargar y comisiones
     */
    this.calculateFeeAndTotal(prepaidTopup);

    /*
      Agrega la informacion par el voucher
     */
    this.addVoucherData(prepaidTopup);

    /*
      Registra el movimiento en estado pendiente
     */
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidTopup, prepaidUser, prepaidCard, cdtTransaction);
    prepaidMovement = getPrepaidMovementEJB10().addPrepaidMovement(null, prepaidMovement);

    prepaidTopup.setId(prepaidMovement.getId());

    /*
      Enviar mensaje al proceso asincrono
     */
    String messageId = this.getDelegate().sendTopUp(prepaidTopup, user, cdtTransaction, prepaidMovement);
    prepaidTopup.setMessageId(messageId);

    return prepaidTopup;
  }

  @Override
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {
    this.validateTopupRequest(topupRequest);

    // Obtener usuario Multicaja
    User user = this.getUserMcByRut(headers, topupRequest.getRut());

    // Verificar si el usuario esta en lista negra
    if(user.getIsBlacklisted()) {
      throw new ValidationException(CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_CARGAR);
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, user.getId());

    // Obtiene la tarjeta
    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    TipoFactura tipoFacTopup = TransactionOriginType.WEB.equals(topupRequest.getTransactionOriginType()) ? TipoFactura.CARGA_TRANSFERENCIA : TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
    TipoFactura tipoFacReverse = TransactionOriginType.WEB.equals(topupRequest.getTransactionOriginType()) ? TipoFactura.ANULA_CARGA_TRANSFERENCIA : TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;

    // Se verifica si ya se tiene una reversa con los mismos datos
    PrepaidMovement10 previousReverse = this.getPrepaidMovementEJB10().getPrepaidMovementForReverse(prepaidUser.getId(),
      topupRequest.getTransactionId(), PrepaidMovementType.TOPUP,
      tipoFacReverse);

    if(previousReverse == null) {

      // Busca el movimiento de carga original
      PrepaidMovement10 originalTopup = this.getPrepaidMovementEJB10().getPrepaidMovementForReverse(prepaidUser.getId(),
        topupRequest.getTransactionId(), PrepaidMovementType.TOPUP, tipoFacTopup);

      // Verifica si existe la carga original topup
      if(originalTopup != null && originalTopup.getMonto().equals(topupRequest.getAmount().getValue())) {

        if(getDateUtils().inLastHours(Long.valueOf(24), originalTopup.getFechaCreacion(), headers.get(Constants.HEADER_USER_TIMEZONE).toString())) {
          // Agrego la reversa al cdt
          CdtTransaction10 cdtTransaction = new CdtTransaction10();
          cdtTransaction.setTransactionReference(0L);
          cdtTransaction.setExternalTransactionId(topupRequest.getTransactionId());

          PrepaidTopup10 reverse = new PrepaidTopup10(topupRequest);

          PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
          prepaidMovement.setTipofac(tipoFacReverse);
          prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
          prepaidMovement = getPrepaidMovementEJB10().addPrepaidMovement(headers, prepaidMovement);

         this.getDelegate().sendPendingTopupReverse(reverse,prepaidCard,prepaidUser,prepaidMovement);

        } else {
          log.info(String.format("El plazo de reversa ha expirado para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), originalTopup.getIdTxExterno(), originalTopup.getMonto()));
          BaseException bex = new BaseException();
          bex.setStatus(410);
          bex.setCode(TRANSACCION_ERROR_GENERICO_$VALUE.getValue());
          bex.setData(new KeyValue("value", "tiempo de reversaexpirado"));
          throw bex;
        }
      } else {
        log.info(String.format("No existe una carga con los datos -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), topupRequest.getTransactionId(), topupRequest.getAmount().getValue()));
        CdtTransaction10 cdtTransaction = new CdtTransaction10();
        cdtTransaction.setExternalTransactionId(topupRequest.getTransactionId());
        cdtTransaction.setTransactionReference(0L);

        PrepaidTopup10 reverse = new PrepaidTopup10(topupRequest);
        PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
        prepaidMovement.setTipofac(tipoFacReverse);
        prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
        prepaidMovement = this.getPrepaidMovementEJB10().addPrepaidMovement(headers, prepaidMovement);
        this.getPrepaidMovementEJB10().updatePrepaidMovementStatus(headers, prepaidMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
      }
    } else {
      log.info(String.format("Ya existe una reversa para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), topupRequest.getTransactionId(), topupRequest.getAmount().getValue()));
    }

  }

  private void validateTopupRequest(NewPrepaidTopup10 request) throws Exception {
    if(request == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "topupRequest"));
    }
    if(request.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(request.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(request.getAmount().getCurrencyCode() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.currency_code"));
    }
    if(request.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isBlank(request.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(StringUtils.isBlank(request.getMerchantName())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_name"));
    }
    if(request.getMerchantCategory() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_category"));
    }
    if(StringUtils.isBlank(request.getTransactionId())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "transaction_id"));
    }
  }

  @Override
  public PrepaidWithdraw10 withdrawUserBalance(Map<String, Object> headers, NewPrepaidWithdraw10 withdrawRequest) throws Exception {

    this.validateWithdrawRequest(withdrawRequest);

    // Obtener usuario Multicaja
    User user = this.getUserMcByRut(headers, withdrawRequest.getRut());
    if(user.getIsBlacklisted()){
      throw new ValidationException(CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_RETIRAR);
    }
    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, user.getId());

    // Se verifica la clave
    UserPasswordNew userPasswordNew = new UserPasswordNew();
    userPasswordNew.setValue(withdrawRequest.getPassword());
    getUserClient().checkPassword(headers, prepaidUser.getUserIdMc(), userPasswordNew);

    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    if (prepaidCard == null) {

      prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.LOCKED_HARD,
        PrepaidCardStatus.EXPIRED,
        PrepaidCardStatus.PENDING);

      if (prepaidCard != null) {
        throw new ValidationException(TARJETA_INVALIDA_$VALUE).setData(new KeyValue("value", prepaidCard.getStatus().toString()));
      }

      throw new ValidationException(CLIENTE_NO_TIENE_PREPAGO);
    }

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdrawRequest.getAmount().getValue());
    cdtTransaction.setTransactionType(withdrawRequest.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + user.getRut().getValue());
    cdtTransaction.setGloss(withdrawRequest.getCdtTransactionType().getName()+" "+withdrawRequest.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);
    cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    // Si no cumple con los limites
    if(!cdtTransaction.isNumErrorOk()){
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    PrepaidWithdraw10 prepaidWithdraw = new PrepaidWithdraw10(withdrawRequest);

    prepaidWithdraw.setUserId(user.getId());
    prepaidWithdraw.setStatus("exitoso");
    prepaidWithdraw.setTimestamps(new Timestamps());

    String contrato = prepaidCard.getProcessorUserId();
    String pan = EncryptUtil.getInstance().decrypt(prepaidCard.getEncryptedPan());

    /*
      Registra el movimiento en estado pendiente
     */
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidWithdraw, prepaidUser, prepaidCard, cdtTransaction);
    prepaidMovement = getPrepaidMovementEJB10().addPrepaidMovement(null, prepaidMovement);

    prepaidWithdraw.setId(prepaidMovement.getId());

    CodigoMoneda clamon = prepaidMovement.getClamon();
    IndicadorNormalCorrector indnorcor = prepaidMovement.getIndnorcor();
    TipoFactura tipofac = prepaidMovement.getTipofac();
    BigDecimal impfac = prepaidMovement.getImpfac();
    String codcom = prepaidMovement.getCodcom();
    Integer codact = prepaidMovement.getCodact();
    CodigoMoneda clamondiv = CodigoMoneda.NONE;
    String nomcomred = prepaidWithdraw.getMerchantName();
    String numreffac = prepaidMovement.getId().toString(); //Esto se realiza en tecnocom se reemplaza por 000000000
    String numaut = numreffac;

    //solamente los 6 ultimos digitos de numreffac
    if (numaut.length() > 6) {
      numaut = numaut.substring(numaut.length()-6);
    }

    InclusionMovimientosDTO inclusionMovimientosDTO =  TecnocomServiceHelper.getInstance().getTecnocomService()
      .inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac, numreffac, impfac, numaut, codcom, nomcomred, codact, clamondiv,impfac);

    if (inclusionMovimientosDTO.isRetornoExitoso()) {
      Integer numextcta = inclusionMovimientosDTO.getNumextcta();
      Integer nummovext = inclusionMovimientosDTO.getNummovext();
      Integer clamone = inclusionMovimientosDTO.getClamone();
      PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK;

      getPrepaidMovementEJB10().updatePrepaidMovement(null,
        prepaidMovement.getId(),
        numextcta,
        nummovext,
        clamone,
        status);

      // se confirma la transaccion
      cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + cdtTransaction.getExternalTransactionId());
      cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    } else if (CodigoRetorno._1010.equals(inclusionMovimientosDTO.getRetorno())) {
      /**
       * Esto es para marcar cuando no pude obtener el response de un Retiro.
       */
      //Colocar el movimiento en error
      PrepaidMovementStatus status = TransactionOriginType.WEB.equals(prepaidWithdraw.getTransactionOriginType()) ? PrepaidMovementStatus.ERROR_WEB_WITHDRAW : PrepaidMovementStatus.ERROR_POS_WITHDRAW;
      getPrepaidMovementEJB10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);

      //Confirmar el retiro en CDT
      cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + cdtTransaction.getExternalTransactionId());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      getPrepaidMovementEJB10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);

      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()));
    }
    //TODO: que hacer si el retiro devuelve un error de TIME_OUT_RESPONSE?
    //else if(CodigoRetorno._1020.equals(inclusionMovimientosDTO.getRetorno())) {}
    else {
      //Colocar el movimiento en error
      PrepaidMovementStatus status = TransactionOriginType.WEB.equals(prepaidWithdraw.getTransactionOriginType()) ? PrepaidMovementStatus.ERROR_WEB_WITHDRAW : PrepaidMovementStatus.ERROR_POS_WITHDRAW;
      getPrepaidMovementEJB10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);

      //Confirmar el retiro en CDT
      cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + cdtTransaction.getExternalTransactionId());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //Iniciar reversa en CDT
      cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_RETIRO);
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + cdtTransaction.getExternalTransactionId());
      cdtTransaction.setTransactionReference(0L);
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //Confirmar reversa en CDT
      cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_RETIRO_CONF);
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + cdtTransaction.getExternalTransactionId());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //TODO: actualizar el status de movimiento a REJECTED
      getPrepaidMovementEJB10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), PrepaidMovementStatus.REVERSED);

      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()));
    }

    /*
      Calcular comisiones
     */
    this.calculateFeeAndTotal(prepaidWithdraw);

    /*
      Agrega la informacion par el voucher
     */
    this.addVoucherData(prepaidWithdraw);

    /*
      Enviar mensaje al proceso asincrono
     */
    String messageId = this.getDelegate().sendWithdraw(prepaidWithdraw, user, cdtTransaction, prepaidMovement);
    prepaidWithdraw.setMessageId(messageId);

    return prepaidWithdraw;
  }

  @Override
  public void reverseWithdrawUserBalance(Map<String, Object> headers, NewPrepaidWithdraw10 withdrawRequest) throws Exception {
    this.validateWithdrawRequest(withdrawRequest);

    // Obtener usuario Multicaja
    User user = this.getUserMcByRut(headers, withdrawRequest.getRut());
    if(user.getIsBlacklisted()){
      throw new ValidationException(CLIENTE_EN_LISTA_NEGRA_NO_PUEDE_RETIRAR);
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, user.getId());

    // Se verifica la clave
    UserPasswordNew userPasswordNew = new UserPasswordNew();
    userPasswordNew.setValue(withdrawRequest.getPassword());
    getUserClient().checkPassword(headers, prepaidUser.getUserIdMc(), userPasswordNew);

    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(headers, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    TipoFactura tipoFacTopup = TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType()) ? TipoFactura.RETIRO_TRANSFERENCIA : TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
    TipoFactura tipoFacReverse = TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType()) ? TipoFactura.ANULA_RETIRO_TRANSFERENCIA : TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;

    // Se verifica si ya se tiene una reversa con los mismos datos
    PrepaidMovement10 previousReverse = this.getPrepaidMovementEJB10().getPrepaidMovementForReverse(prepaidUser.getId(),
      withdrawRequest.getTransactionId(), PrepaidMovementType.WITHDRAW, tipoFacReverse);

    if(previousReverse == null) {
      // Busca el movimiento de retiro original
      PrepaidMovement10 originalwithdraw = this.getPrepaidMovementEJB10().getPrepaidMovementForReverse(prepaidUser.getId(),
        withdrawRequest.getTransactionId(), PrepaidMovementType.WITHDRAW, tipoFacTopup);

      if(originalwithdraw != null && originalwithdraw.getMonto().equals(withdrawRequest.getAmount().getValue())) {

        String timezone;
        if(headers.get(Constants.HEADER_USER_TIMEZONE) != null ){
          timezone = headers.get(Constants.HEADER_USER_TIMEZONE).toString();
        } else {
          timezone = "America/Santiago";
        }

        if(getDateUtils().inLastHours(Long.valueOf(24), originalwithdraw.getFechaCreacion(), timezone)) {
          // Agrego la reversa al cdt
          CdtTransaction10 cdtTransaction = new CdtTransaction10();
          cdtTransaction.setTransactionReference(0L);
          cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());

          PrepaidWithdraw10 reverse = new PrepaidWithdraw10(withdrawRequest);

          PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
          prepaidMovement.setTipofac(tipoFacReverse);
          prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
          prepaidMovement = getPrepaidMovementEJB10().addPrepaidMovement(headers, prepaidMovement);
          this.getDelegate().sendPendingWithdrawReversal(reverse,prepaidUser,prepaidMovement);

        } else {
          log.info(String.format("El plazo de reversa ha expirado para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), originalwithdraw.getIdTxExterno(), originalwithdraw.getMonto()));
          BaseException bex = new BaseException();
          bex.setStatus(410);
          bex.setCode(TRANSACCION_ERROR_GENERICO_$VALUE.getValue());
          bex.setData(new KeyValue("value", "tiempo de reversaexpirado"));
          throw bex;
        }

      } else {
        log.info(String.format("No existe un retiro con los datos -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), withdrawRequest.getTransactionId(), withdrawRequest.getAmount().getValue()));
        CdtTransaction10 cdtTransaction = new CdtTransaction10();
        cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());
        cdtTransaction.setTransactionReference(0L);

        PrepaidWithdraw10 reverse = new PrepaidWithdraw10(withdrawRequest);
        PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
        prepaidMovement.setTipofac(tipoFacReverse);
        prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
        prepaidMovement = this.getPrepaidMovementEJB10().addPrepaidMovement(headers, prepaidMovement);
        this.getPrepaidMovementEJB10().updatePrepaidMovementStatus(headers, prepaidMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
      }
    } else {
    log.info(String.format("Ya existe una reversa para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), withdrawRequest.getTransactionId(), withdrawRequest.getAmount().getValue()));
    }
  }

  private void validateWithdrawRequest(NewPrepaidWithdraw10 request) throws Exception {
    if(request == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "withdrawRequest"));
    }
    if(request.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(request.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(request.getAmount().getCurrencyCode() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.currency_code"));
    }
    if(request.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isBlank(request.getPassword())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "password"));
    }
    if(StringUtils.isBlank(request.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(StringUtils.isBlank(request.getMerchantName())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_name"));
    }
    if(request.getMerchantCategory() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_category"));
    }
    if(StringUtils.isBlank(request.getTransactionId())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "transaction_id"));
    }
  }

  @Override
  public List<PrepaidTopup10> getUserTopups(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidUserSignup10 initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup10 newPrepaidUserSignup) throws Exception {

    if(newPrepaidUserSignup == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "newPrepaidUserSignup"));
    }
    if(newPrepaidUserSignup.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isAllBlank(newPrepaidUserSignup.getEmail())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "email"));
    }

    if(newPrepaidUserSignup.isLogin()) {

      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserByRut(headers,newPrepaidUserSignup.getRut());

      if(prepaidUser10 != null) {
        throw new ValidationException(CLIENTE_YA_TIENE_PREPAGO);
      }

    } else {

      User user = getUserClient().getUserByRut(headers, newPrepaidUserSignup.getRut());

      if (user == null) {

        user = getUserClient().getUserByEmail(headers, newPrepaidUserSignup.getEmail());

        if (user != null) {

          if (UserStatus.ENABLED.equals(user.getGlobalStatus())) {

            PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserByRut(headers, newPrepaidUserSignup.getRut());

            if (prepaidUser10 != null) {

              PrepaidCard10 prepaidCard10 = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser10.getId());

              if (prepaidCard10 != null) {
                throw new ValidationException(CORREO_YA_TIENE_TARJETA);
              } else {
                throw new ValidationException(CORREO_PERTENECE_A_CLIENTE);
              }

            } else {
              throw new ValidationException(CORREO_PERTENECE_A_CLIENTE);
            }

          } else if (UserStatus.PREREGISTERED.equals(user.getGlobalStatus())) {
            throw new ValidationException(CORREO_YA_UTILIZADO);
          } else {
            throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
          }
        }
      } else {

        if (user.getEmail().getValue().equalsIgnoreCase(newPrepaidUserSignup.getEmail())) {

          if (UserStatus.ENABLED.equals(user.getGlobalStatus())) {

            PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserByRut(headers, newPrepaidUserSignup.getRut());

            if (prepaidUser10 != null) {

              PrepaidCard10 prepaidCard10 = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser10.getId());

              if (prepaidCard10 != null) {
                throw new ValidationException(CLIENTE_YA_TIENE_TARJETA);
              } else {
                throw new ValidationException(CLIENTE_YA_TIENE_CLAVE);
              }
            }

          } else if (!UserStatus.PREREGISTERED.equals(user.getGlobalStatus())) {
            throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
          }

        } else {

          if (UserStatus.ENABLED.equals(user.getGlobalStatus())) {

            PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserByRut(headers, newPrepaidUserSignup.getRut());

            if (prepaidUser10 != null) {

              PrepaidCard10 prepaidCard10 = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser10.getId());

              if (prepaidCard10 != null) {
                throw new ValidationException(RUT_YA_TIENE_TARJETA);
              } else {
                throw new ValidationException(RUT_YA_TIENE_CLAVE);
              }
            }

          } else if (!UserStatus.PREREGISTERED.equals(user.getGlobalStatus())) {
            throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
          }
        }
      }
    }
    SignUPNew signUPNew = new SignUPNew(newPrepaidUserSignup.getEmail(),newPrepaidUserSignup.getRut());
    SignUp signUp = getUserClient().signUp(headers, signUPNew);

    PrepaidUserSignup10 prepaidUserSignup10 = new PrepaidUserSignup10();
    prepaidUserSignup10.setId(signUp.getId());
    prepaidUserSignup10.setUserId(signUp.getUserId());
    prepaidUserSignup10.setName(signUp.getName());
    prepaidUserSignup10.setLastname_1(signUp.getLastname_1());
    prepaidUserSignup10.setEmail(signUp.getEmail());
    prepaidUserSignup10.setRut(signUp.getRut());
    prepaidUserSignup10.setMustAcceptTermsAndConditions(Boolean.TRUE);
    prepaidUserSignup10.setMustChoosePassword(Boolean.TRUE);
    prepaidUserSignup10.setMustValidateCellphone(Boolean.TRUE);
    prepaidUserSignup10.setMustValidateEmail(Boolean.TRUE);

    AppFile tac = this.getLastTermsAndConditions(headers);
    if(tac != null) {
      List<String> tacs = new ArrayList<>();
      tacs.add(tac.getVersion());
      prepaidUserSignup10.setTermsAndConditionsList(tacs);
    }

    return prepaidUserSignup10;
  }

  @Override
  public PrepaidUser10 finishSignup(Map<String, Object> headers, Long userIdMc) throws Exception {

    if(userIdMc == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    User user = getUserClient().getUserById(headers,userIdMc);
    if(user == null){
      throw new ValidationException(CLIENTE_NO_EXISTE);
    }
    if(!user.getHasPassword()){
      throw new ValidationException(CLIENTE_NO_TIENE_CLAVE);
    }
    if(!EmailStatus.VERIFIED.equals(user.getEmail().getStatus())){
      throw new ValidationException(PROCESO_DE_REGISTRO_EMAIL_NO_VALIDADO);
    }
    if(!CellphoneStatus.VERIFIED.equals(user.getCellphone().getStatus())) {
      throw new ValidationException(PROCESO_DE_REGISTRO_CELULAR_NO_VALIDADO);
    }

    user = getUserClient().finishSignup(headers,userIdMc);

    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    prepaidUser10.setUserIdMc(user.getId());
    prepaidUser10.setRut(user.getRut().getValue());
    prepaidUser10.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser10.setBalanceExpiration(0L);
    prepaidUser10 = getPrepaidUserEJB10().createPrepaidUser(headers,prepaidUser10);

    return prepaidUser10;
  }

  @Override
  public PrepaidCard10 getPrepaidCard(Map<String, Object> headers, Long userIdMc) throws Exception {

    if(userIdMc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    // Obtener usuario Multicaja
    User user = this.getUserMcById(headers, userIdMc);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, userIdMc);

    // Obtener tarjeta
    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser.getId());

    //Obtener ultimo movimiento
    PrepaidMovement10 movement = getPrepaidMovementEJB10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.PENDING,
      PrepaidMovementStatus.IN_PROCESS);

    if(prepaidCard == null) {
      // Si el ultimo movimiento esta en estatus Pendiente o En Proceso
      if(movement != null){
        throw new ValidationException(TARJETA_PRIMERA_CARGA_EN_PROCESO);
      }
      throw new ValidationException(TARJETA_PRIMERA_CARGA_PENDIENTE);
    } else if(PrepaidCardStatus.PENDING.equals(prepaidCard.getStatus())) {
      throw new ValidationException(TARJETA_PRIMERA_CARGA_EN_PROCESO);
    }

    return prepaidCard;
  }

  @Override
  public void calculateFeeAndTotal(IPrepaidTransaction10 transaction) throws Exception {

    if(transaction == null || transaction.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(transaction.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(StringUtils.isBlank(transaction.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }

    CodigoMoneda currencyCodeClp = CodigoMoneda.CHILE_CLP;

    NewAmountAndCurrency10 total = new NewAmountAndCurrency10();
    total.setCurrencyCode(currencyCodeClp);

    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10();
    fee.setCurrencyCode(currencyCodeClp);

    switch (transaction.getMovementType()) {
      case TOPUP:
        // Calcula las comisiones segun el tipo de carga (WEB o POS)
        if (TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
          fee.setValue(getPercentage().getTOPUP_WEB_FEE_AMOUNT());
        } else {
          // MAX(100; 0,5% * prepaid_topup_new_amount_value) + IVA
          BigDecimal commission = getCalculationsHelper().calculateFee(transaction.getAmount().getValue(),getPercentage().getTOPUP_POS_FEE_PERCENTAGE());
          fee.setValue(commission);
        }
        // Calculo el total
        total.setValue(transaction.getAmount().getValue().subtract(fee.getValue()));
      break;
      case WITHDRAW:
        // Calcula las comisiones segun el tipo de carga (WEB o POS)
        if (TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
          fee.setValue(getPercentage().getWITHDRAW_WEB_FEE_AMOUNT());
        } else {
          // MAX ( 100; 0,5%*prepaid_topup_new_amount_value ) + IVA
          BigDecimal commission =getCalculationsHelper().calculateFee(transaction.getAmount().getValue(), getPercentage().getWITHDRAW_POS_FEE_PERCENTAGE());
          fee.setValue(commission);
        }
        // Calculo el total
        total.setValue(transaction.getAmount().getValue().add(fee.getValue()));
      break;
    }

    transaction.setFee(fee);
    transaction.setTotal(total);
  }

  @Override
  public void addVoucherData(IPrepaidTransaction10 transaction) throws Exception {

    if(transaction == null || transaction.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(transaction.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }

    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(new Locale("es_CL"));
    DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

    symbols.setGroupingSeparator('.');
    formatter.setDecimalFormatSymbols(symbols);

    transaction.setMcVoucherType("A");

    Map<String, String> data = new HashMap<>();
    data.put("name", "amount_paid");
    data.put("value", formatter.format(transaction.getAmount().getValue().longValue()));

    Map<String, String> dataRut = new HashMap<>();
    dataRut.put("name", "rut");
    dataRut.put("value", RutUtils.getInstance().format(transaction.getRut(), null));

    List<Map<String, String>> mcVoucherData = new ArrayList<>();
    mcVoucherData.add(data);
    mcVoucherData.add(dataRut);

    transaction.setMcVoucherData(mcVoucherData);
  }

  /**
   *
   * @param transaction
   * @param prepaidUser
   * @param prepaidCard
   * @param cdtTransaction
   * @return
   */
  private PrepaidMovement10 buildPrepaidMovement(IPrepaidTransaction10 transaction, PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) {

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      log.error("Error al cargar parametro cod_entidad");
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura = null;

    // Verifico el tipo de movimiento (TOPUP/WITHDRAW) y el origen (POS/WEB)
    switch (transaction.getMovementType()) {
      case TOPUP:
          if(TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
            tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
          } else {
            tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
          }
      break;
      case WITHDRAW:
        if(TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
          tipoFactura = TipoFactura.RETIRO_TRANSFERENCIA;
        } else {
          tipoFactura = TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
        }
      break;
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(cdtTransaction.getTransactionReference());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(cdtTransaction.getExternalTransactionId());
    prepaidMovement.setTipoMovimiento(transaction.getMovementType());
    prepaidMovement.setMonto(transaction.getAmount().getValue());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFactura.getCorrector())); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(transaction.getAmount().getValue());
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(transaction.getMerchantCode());
    prepaidMovement.setCodact(transaction.getMerchantCategory());
    prepaidMovement.setImpliq(0L); // se debe actualizar despues
    prepaidMovement.setClamonliq(0); // se debe actualizar despues
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob(""); // se debe actualizar despues
    prepaidMovement.setNumextcta(0); // se debe actualizar despues
    prepaidMovement.setNummovext(0); // se debe actualizar despues
    prepaidMovement.setClamone(0); // se debe actualizar despues
    prepaidMovement.setTipolin(""); // se debe actualizar despues
    prepaidMovement.setLinref(0); // se debe actualizar despues
    prepaidMovement.setNumbencta(1); // se debe actualizar despues
    prepaidMovement.setNumplastico(0L); // se debe actualizar despues

    return prepaidMovement;
  }

  /**
   *
   * @param userIdMc
   * @param simulationNew
   * @throws BaseException
   */
  private void validateSimulationNew10(Long userIdMc, SimulationNew10 simulationNew) throws BaseException {

    if(userIdMc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userIdMc"));
    }

    if(simulationNew == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "simulationNew"));
    }

    if(simulationNew.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }

    if(simulationNew.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }

    if(simulationNew.getAmount().getCurrencyCode() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.currencyCode"));
    }
  }

  @Override
  public SimulationTopupGroup10 topupSimulationGroup(Map<String,Object> headers, Long userIdMc, SimulationNew10 simulationNew) throws Exception {

    this.validateSimulationNew10(userIdMc, simulationNew);

    // Obtener usuario Multicaja
    User user = this.getUserMcById(headers, userIdMc);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser10 = this.getPrepaidUserByUserIdMc(headers, userIdMc);

    prepaidUser10 = getPrepaidUserEJB10().getUserLevel(user,prepaidUser10);

    SimulationTopupGroup10 simulationTopupGroup10 = new SimulationTopupGroup10();

    simulationNew.setPaymentMethod(TransactionOriginType.WEB);
    simulationTopupGroup10.setSimulationTopupWeb(topupSimulation(headers,prepaidUser10,simulationNew));

    simulationNew.setPaymentMethod(TransactionOriginType.POS);
    simulationTopupGroup10.setSimulationTopupPOS(topupSimulation(headers,prepaidUser10,simulationNew));

    return simulationTopupGroup10;
  }

  @Override
  public SimulationTopup10 topupSimulation(Map<String,Object> headers,PrepaidUser10 prepaidUser10, SimulationNew10 simulationNew) throws Exception {

    SimulationTopup10 simulationTopup = new SimulationTopup10();
    Boolean isFirstTopup = this.getPrepaidMovementEJB10().isFirstTopup(prepaidUser10.getId());
    simulationTopup.setFirstTopup(isFirstTopup);

    final BigDecimal amountValue = simulationNew.getAmount().getValue();

    // LLAMADA AL CDT
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(amountValue);
    cdtTransaction.setExternalTransactionId(String.valueOf(Utils.uniqueCurrentTimeNano()));
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser10.getRut());
    cdtTransaction.setIndSimulacion(true);

    if(PrepaidUserLevel.LEVEL_1.equals(prepaidUser10.getUserLevel())) {
      cdtTransaction.setTransactionType(CdtTransactionType.PRIMERA_CARGA);
    } else {
      cdtTransaction.setTransactionType(simulationNew.isTransactionWeb() ? CdtTransactionType.CARGA_WEB : CdtTransactionType.CARGA_POS);
    }

    cdtTransaction.setGloss(cdtTransaction.getTransactionType().toString());
    cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    if(!cdtTransaction.isNumErrorOk()){
      /* Posibles errores:
      La carga supera el monto máximo de carga web
      La carga supera el monto máximo de carga pos
      La carga es menor al mínimo de carga
      La carga supera el monto máximo de cargas mensuales.
      */
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
        if(lNumError == LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_CARGA_WEB.getValue() || lNumError == LA_CARGA_SUPERA_EL_MONTO_MAXIMO_DE_CARGA_POS.getValue()){
          simulationTopup.setCode(lNumError);
          simulationTopup.setMessage(cdtTransaction.getMsjError());
        } else {
          throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
        }
      } else {
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
      NewAmountAndCurrency10 zero = new NewAmountAndCurrency10(BigDecimal.valueOf(0));
      simulationTopup.setFee(zero);
      simulationTopup.setPca(zero);
      simulationTopup.setEed(new NewAmountAndCurrency10(BigDecimal.valueOf(0), CodigoMoneda.USA_USN));
      simulationTopup.setAmountToPay(zero);
      simulationTopup.setOpeningFee(zero);
      return simulationTopup;
    }

    //saldo del usuario
    PrepaidBalance10 balance;
    if(isFirstTopup){
      balance = new PrepaidBalance10();
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(0));
      balance.setPcaMain(amount);
      balance.setBalance(amount);
      balance.setPcaSecondary(amount);
      balance.setUsdValue(CalculationsHelper.getUsdValue());
      balance.setUpdated(Boolean.FALSE);
    } else {
      balance = this.getPrepaidUserEJB10().getPrepaidUserBalance(headers, prepaidUser10.getUserIdMc());
    }

    log.info("Saldo del usuario: " + balance.getBalance().getValue());
    log.info("Monto a cargar: " + amountValue);
    log.info("Monto maximo a cargar: " + getPercentage().getMAX_AMOUNT_BY_USER());

    if((balance.getBalance().getValue().doubleValue() + amountValue.doubleValue()) > getPercentage().getMAX_AMOUNT_BY_USER()) {
      throw new ValidationException(SALDO_SUPERARA_LOS_$$VALUE).setData(new KeyValue("value", getPercentage().getMAX_AMOUNT_BY_USER()));
    }

    BigDecimal fee;

    if(simulationNew.isTransactionWeb()){
      fee = getPercentage().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT();
    } else {
      fee = getCalculationsHelper().calculateFee(simulationNew.getAmount().getValue(), getPercentage().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE());
    }

    //monto a cargar + comision
    BigDecimal calculatedAmount = amountValue.add(fee);

    log.info("Comision: " + fee);

    if(isFirstTopup) {
      calculatedAmount = calculatedAmount.add(getPercentage().getOPENING_FEE());
      simulationTopup.setOpeningFee(new NewAmountAndCurrency10(getPercentage().getOPENING_FEE()));
      log.info("Comision de apertura: " + getPercentage().getOPENING_FEE());
    }

    log.info("Monto a cargar + comisiones: " + calculatedAmount);

    simulationTopup.setFee(new NewAmountAndCurrency10(fee));
    simulationTopup.setPca(new NewAmountAndCurrency10(calculatePca(amountValue)));
    simulationTopup.setEed(new NewAmountAndCurrency10(calculateEed(amountValue), CodigoMoneda.USA_USN));
    simulationTopup.setAmountToPay(new NewAmountAndCurrency10(calculatedAmount));

    return simulationTopup;
  }

  @Override
  public SimulationWithdrawal10 withdrawalSimulation(Map<String,Object> headers, Long userIdMc, SimulationNew10 simulationNew) throws Exception {

    this.validateSimulationNew10(userIdMc, simulationNew);

    if(simulationNew.getPaymentMethod() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "method"));
    }

    // Obtener usuario Multicaja
    User user = this.getUserMcById(headers, userIdMc);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser10 = this.getPrepaidUserByUserIdMc(headers, userIdMc);

    final BigDecimal amountValue = simulationNew.getAmount().getValue();

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(amountValue);
    cdtTransaction.setExternalTransactionId(String.valueOf(Utils.uniqueCurrentTimeNano()));
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser10.getRut());
    cdtTransaction.setIndSimulacion(true);
    cdtTransaction.setTransactionType(simulationNew.isTransactionWeb() ? CdtTransactionType.RETIRO_WEB : CdtTransactionType.RETIRO_POS);
    cdtTransaction.setGloss(cdtTransaction.getTransactionType().toString());

    cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    if(!cdtTransaction.isNumErrorOk()){
      /* Posibles errores:
      El retiro supera el monto máximo de un retiro web
      El retiro supera el monto máximo de un retiro pos
      El monto de retiro es menor al monto mínimo de retiros
      El retiro supera el monto máximo de retiros mensuales.
     */
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    BigDecimal fee;

    if (simulationNew.isTransactionWeb()) {
      fee = getPercentage().getCALCULATOR_WITHDRAW_WEB_FEE_AMOUNT();
    } else {
      fee = getCalculationsHelper().calculateFee(simulationNew.getAmount().getValue(), getPercentage().getCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE());
    }

    //monto a cargar + comision
    BigDecimal calculatedAmount = amountValue.add(fee);

    //saldo del usuario
    PrepaidBalance10 balance = this.getPrepaidUserEJB10().getPrepaidUserBalance(headers, userIdMc);

    log.info("Saldo del usuario: " + balance.getBalance().getValue());
    log.info("Monto a retirar: " + amountValue);
    log.info("Comision: " + fee);
    log.info("Monto a retirar + comision: " + calculatedAmount);

    if(balance.getBalance().getValue().doubleValue() < calculatedAmount.doubleValue()) {
      throw new ValidationException(SALDO_INSUFICIENTE_$VALUE).setData(new KeyValue("value", String.format("-%s", balance.getBalance().getValue().add(fee.multiply(BigDecimal.valueOf(-1))))));
    }

    SimulationWithdrawal10 simulationWithdrawal = new SimulationWithdrawal10();
    simulationWithdrawal.setFee(new NewAmountAndCurrency10(fee));
    simulationWithdrawal.setAmountToDiscount(new NewAmountAndCurrency10(calculatedAmount));

    return simulationWithdrawal;
  }

  @Override
  public PrepaidUser10 getPrepaidUser(Map<String, Object> headers, Long userIdMc) throws Exception {

    if(userIdMc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    // Busco el usuario MC
    User user = getUserClient().getUserById(headers, userIdMc);

    if(user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    // Busco el usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJB10().getPrepaidUserByUserIdMc(headers, userIdMc);

    if(prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    // Obtiene el nivel del usuario
    prepaidUser = this.getPrepaidUserEJB10().getUserLevel(user, prepaidUser);

    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED,
      PrepaidCardStatus.PENDING);

    prepaidUser.setHasPrepaidCard(prepaidCard != null);

    // verifica si el usuario ha realizado cargas anteriormente
    prepaidUser.setHasPendingFirstTopup(getPrepaidMovementEJB10().isFirstTopup(prepaidUser.getId()));

    return prepaidUser;
  }

  @Override
  public PrepaidUser10 findPrepaidUser(Map<String, Object> headers, Integer rut) throws Exception {

    if(rut == null || Integer.valueOf(0).equals(rut)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }

    // Busco el usuario MC
    User user = getUserClient().getUserByRut(headers, rut);

    if(user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    // Busco el usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJB10().getPrepaidUserByUserIdMc(headers, user.getId());

    if(prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    // Obtiene el nivel del usuario
    prepaidUser = this.getPrepaidUserEJB10().getUserLevel(user, prepaidUser);

    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED,
      PrepaidCardStatus.PENDING);

    prepaidUser.setHasPrepaidCard(prepaidCard != null);

    // verifica si el usuario ha realizado cargas anteriormente
    prepaidUser.setHasPendingFirstTopup(getPrepaidMovementEJB10().isFirstTopup(prepaidUser.getId()));

    return prepaidUser;
  }

  @Override
  public List<PrepaidTransaction10> getTransactions(Map<String,Object> headers, Long userIdMc, String startDate, String endDate, Integer count) throws Exception {

    if(userIdMc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    // Obtener usuario Multicaja
    User user = getUserMcById(headers, userIdMc);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = getPrepaidUserByUserIdMc(headers, userIdMc);

    // Obtener tarjeta
    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser.getId());

    //Obtener ultimo movimiento
    PrepaidMovement10 movement = getPrepaidMovementEJB10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
    PrepaidMovementStatus.PENDING,
    PrepaidMovementStatus.IN_PROCESS);

    if(prepaidCard == null) {
      // Si el ultimo movimiento esta en estatus Pendiente o En Proceso
      if(movement != null){
        throw new ValidationException(TARJETA_PRIMERA_CARGA_EN_PROCESO);
      }else {
        throw new ValidationException(TARJETA_PRIMERA_CARGA_PENDIENTE);
      }
    } else if(PrepaidCardStatus.PENDING.equals(prepaidCard.getStatus())) {
      throw new ValidationException(TARJETA_PRIMERA_CARGA_EN_PROCESO);
    }

    Date _startDate;
    Date _endDate;

    if(StringUtils.isAllBlank(startDate) || StringUtils.isAllBlank(endDate)) {
      String timeZone = "";
      if(headers != null && headers.containsKey(Constants.HEADER_USER_TIMEZONE)) {
        timeZone = headers.get(Constants.HEADER_USER_TIMEZONE).toString();
      }else {
        timeZone = "America/Santiago";
      }
      _startDate = getDateUtils().timeStampToLocaleDate( new Date(prepaidCard.getTimestamps().getCreatedAt().getTime()),timeZone);
      _endDate = new Date(System.currentTimeMillis());
    } else {
      _startDate = getDateUtils().dateStringToDate(startDate,"dd-MM-yyyy");
      _endDate = getDateUtils().dateStringToDate(endDate,"dd-MM-yyyy");
    }

    ConsultaMovimientosDTO consultaMovimientosDTO = getTecnocomService().consultaMovimientos(prepaidCard.getProcessorUserId(),user.getRut().getValue().toString(),TipoDocumento.RUT,_startDate,_endDate);

    List<PrepaidTransaction10> listTransaction10 = new ArrayList<>();

    for(MovimientosDTO movimientosDTO : consultaMovimientosDTO.getMovimientos()) {

      PrepaidTransaction10 transaction10 = new PrepaidTransaction10();
      // Get Date and parse
      String sDate = (String) movimientosDTO.getFecfac().get("valueDate");
      String sFormat = (String) movimientosDTO.getFecfac().get("format");
      transaction10.setDate(getDateUtils().dateStringToDate(sDate,sFormat));
      transaction10.setCommerceCode(movimientosDTO.getCodcom());
      transaction10.setInvoiceType(TipoFactura.valueOfEnumByCodeAndCorrector(movimientosDTO.getTipofac(),movimientosDTO.getIndnorcor()));
      transaction10.setCorrector(transaction10.getInvoiceType().getCorrector()==0?false:true);
      switch (transaction10.getInvoiceType()) {
        case COMISION_APERTURA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());
          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
                                                              movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          transaction10.setFee(new NewAmountAndCurrency10(sumImpbrueco.multiply(NEGATIVE),movimientosDTO.getClamon()));//Comisiones

          break;
        }
        case ANULA_COMISION_APERTURA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());

          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
                                                              movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          transaction10.setFee(new NewAmountAndCurrency10(sumImpbrueco,movimientosDTO.getClamon()));//Comisiones
          break;
        }
        case CARGA_TRANSFERENCIA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());
          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
                                                              movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte(), movimientosDTO.getClamon()));
          transaction10.setFinalAmount(new NewAmountAndCurrency10(movimientosDTO.getImporte().subtract(sumImpbrueco), movimientosDTO.getClamon()));
          break;
        }
        case ANULA_CARGA_TRANSFERENCIA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());
          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(), movimientosDTO.getImpbrueco2(),
                                                              movimientosDTO.getImpbrueco3(), movimientosDTO.getImpbrueco4());
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte().multiply(NEGATIVE), movimientosDTO.getClamon()));
          transaction10.setFinalAmount(new NewAmountAndCurrency10(movimientosDTO.getImporte().subtract(sumImpbrueco).multiply(NEGATIVE), movimientosDTO.getClamon()));
          break;
        }
        case CARGA_EFECTIVO_COMERCIO_MULTICAJA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());

          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(), movimientosDTO.getImpbrueco2(),
                                                              movimientosDTO.getImpbrueco3(), movimientosDTO.getImpbrueco4());

          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte(), movimientosDTO.getClamon()));//Monto Carga
          transaction10.setFee(new NewAmountAndCurrency10(sumImpbrueco,movimientosDTO.getClamon()));//Comisiones
          transaction10.setFinalAmount(new NewAmountAndCurrency10(movimientosDTO.getImporte().subtract(sumImpbrueco), movimientosDTO.getClamon()));
          break;
        }
        case ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());
          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(), movimientosDTO.getImpbrueco2(),
                                                              movimientosDTO.getImpbrueco3(), movimientosDTO.getImpbrueco4());

          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte().multiply(NEGATIVE), movimientosDTO.getClamon()));//Monto Carga
          transaction10.setFee(new NewAmountAndCurrency10(sumImpbrueco, movimientosDTO.getClamon()));//Comisiones
          transaction10.setFinalAmount(new NewAmountAndCurrency10(movimientosDTO.getImporte().subtract(sumImpbrueco).multiply(NEGATIVE), movimientosDTO.getClamon()));
          break;
        }
        case RETIRO_TRANSFERENCIA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());

          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte(),CodigoMoneda.CHILE_CLP));
          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoDescontar = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);

          transaction10.setFinalAmount(new NewAmountAndCurrency10(montoDescontar,CodigoMoneda.CHILE_CLP));
          transaction10.setFee(new NewAmountAndCurrency10(fee,CodigoMoneda.CHILE_CLP));
          break;
        }
        case ANULA_RETIRO_TRANSFERENCIA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());

          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte(),CodigoMoneda.CHILE_CLP));
          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoDescontar = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);

          transaction10.setFinalAmount(new NewAmountAndCurrency10(montoDescontar,CodigoMoneda.CHILE_CLP));
          transaction10.setFee(new NewAmountAndCurrency10(fee,CodigoMoneda.CHILE_CLP));
          break;
        }
        case RETIRO_EFECTIVO_COMERCIO_MULTICJA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());

          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte(),CodigoMoneda.CHILE_CLP));
          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
                                                      movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoDescontar = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);

          transaction10.setFinalAmount(new NewAmountAndCurrency10(montoDescontar,CodigoMoneda.CHILE_CLP));
          transaction10.setFee(new NewAmountAndCurrency10(fee,CodigoMoneda.CHILE_CLP));
          break;
        }
        case ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());

          transaction10.setAmountPrimary(new NewAmountAndCurrency10(movimientosDTO.getImporte(),CodigoMoneda.CHILE_CLP));
          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoDescontar = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);

          transaction10.setFinalAmount(new NewAmountAndCurrency10(montoDescontar,CodigoMoneda.CHILE_CLP));
          transaction10.setFee(new NewAmountAndCurrency10(fee,CodigoMoneda.CHILE_CLP));
          break;
        }
        case REEMISION_DE_TARJETA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());
          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          transaction10.setFee(new NewAmountAndCurrency10(sumImpbrueco.multiply(NEGATIVE),movimientosDTO.getClamon()));//Comisiones

          break;
        }
        case ANULA_REEMISION_DE_TARJETA:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription());
          transaction10.setType(transaction10.getInvoiceType().getType());
          // Suma de Comisiones
          BigDecimal sumImpbrueco = numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          transaction10.setFee(new NewAmountAndCurrency10(sumImpbrueco,movimientosDTO.getClamon()));//Comisiones

          break;
        }
        case SUSCRIPCION_INTERNACIONAL:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription()+" en "+movimientosDTO.getNomcomred());
          transaction10.setType(transaction10.getInvoiceType().getType());

          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
                                                      movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoPesos = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(montoPesos,CodigoMoneda.CHILE_CLP));

          transaction10.setAmountSecondary(new NewAmountAndCurrency10(movimientosDTO.getImpdiv(),movimientosDTO.getClamondiv()));
          transaction10.setUsdValue(new NewAmountAndCurrency10(montoPesos.divide(movimientosDTO.getImpdiv())));
          transaction10.setCountry(movimientosDTO.getNompais());
          break;
        }
        case ANULA_SUSCRIPCION_INTERNACIONAL:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription()+" en "+movimientosDTO.getNomcomred());
          transaction10.setType(transaction10.getInvoiceType().getType());

          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoPesos = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(montoPesos,CodigoMoneda.CHILE_CLP));

          transaction10.setAmountSecondary(new NewAmountAndCurrency10(movimientosDTO.getImpdiv(),movimientosDTO.getClamondiv()));
          transaction10.setUsdValue(new NewAmountAndCurrency10(montoPesos.divide(movimientosDTO.getImpdiv())));
          transaction10.setCountry(movimientosDTO.getNompais());
          break;
        }
        case COMPRA_INTERNACIONAL:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription()+" en "+movimientosDTO.getNomcomred());
          transaction10.setType(transaction10.getInvoiceType().getType());
          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoPesos = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(montoPesos,CodigoMoneda.CHILE_CLP));

          transaction10.setAmountSecondary(new NewAmountAndCurrency10(movimientosDTO.getImpdiv(),movimientosDTO.getClamondiv()));
          transaction10.setUsdValue(new NewAmountAndCurrency10(montoPesos.divide(movimientosDTO.getImpdiv())));
          transaction10.setCountry(movimientosDTO.getNompais());
          break;
        }
        case ANULA_COMPRA_INTERNACIONAL:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription()+" en "+movimientosDTO.getNomcomred());
          transaction10.setType(transaction10.getInvoiceType().getType());

          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoPesos = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(montoPesos,CodigoMoneda.CHILE_CLP));

          transaction10.setAmountSecondary(new NewAmountAndCurrency10(movimientosDTO.getImpdiv(),movimientosDTO.getClamondiv()));
          transaction10.setUsdValue(new NewAmountAndCurrency10(montoPesos.divide(movimientosDTO.getImpdiv())));
          transaction10.setCountry(movimientosDTO.getNompais());
          break;
        }
        case DEVOLUCION_COMPRA_INTERNACIONAL:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription()+" en "+movimientosDTO.getNomcomred());
          transaction10.setType(transaction10.getInvoiceType().getType());
          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoPesos = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(montoPesos,CodigoMoneda.CHILE_CLP));

          transaction10.setAmountSecondary(new NewAmountAndCurrency10(movimientosDTO.getImpdiv(),movimientosDTO.getClamondiv()));
          transaction10.setUsdValue(new NewAmountAndCurrency10(montoPesos.divide(movimientosDTO.getImpdiv())));
          transaction10.setCountry(movimientosDTO.getNompais());
          break;
        }
        case ANULA_DEVOLUCION_COMPRA_INTERNACIONAL:{
          transaction10.setGloss(transaction10.getInvoiceType().getDescription()+" en "+movimientosDTO.getNomcomred());
          transaction10.setType(transaction10.getInvoiceType().getType());

          BigDecimal fee =  numberUtils.sumBigDecimal(movimientosDTO.getImpbrueco1(),movimientosDTO.getImpbrueco2(),
            movimientosDTO.getImpbrueco3(),movimientosDTO.getImpbrueco4());
          BigDecimal montoPesos = numberUtils.sumBigDecimal(movimientosDTO.getImporte(),fee);
          transaction10.setAmountPrimary(new NewAmountAndCurrency10(montoPesos,CodigoMoneda.CHILE_CLP));

          transaction10.setAmountSecondary(new NewAmountAndCurrency10(movimientosDTO.getImpdiv(),movimientosDTO.getClamondiv()));
          transaction10.setUsdValue(new NewAmountAndCurrency10(montoPesos.divide(movimientosDTO.getImpdiv())));
          transaction10.setCountry(movimientosDTO.getNompais());
          break;
        }
      }
      listTransaction10.add(transaction10);
    }

    return listTransaction10;
  }

  @Override
  public PrepaidCard10 lockPrepaidCard(Map<String, Object> headers, Long userIdMc) throws Exception {
    if(userIdMc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    this.getUserMcById(headers, userIdMc);
    PrepaidUser10 prepaidUser10 = this.getPrepaidUserByUserIdMc(headers, userIdMc);
    PrepaidCard10 prepaidCard = this.getPrepaidCardToLock(headers, prepaidUser10.getId());

    if(prepaidCard.isActive()) {
      BloqueoDesbloqueoDTO bloqueoDesbloqueoDTO = TecnocomServiceHelper.getInstance().getTecnocomService().bloqueo(prepaidCard.getProcessorUserId(), getEncryptUtil().decrypt(prepaidCard.getEncryptedPan()));
      if(bloqueoDesbloqueoDTO.isRetornoExitoso()) {
        getPrepaidCardEJB10().updatePrepaidCardStatus(headers, prepaidCard.getId(), PrepaidCardStatus.LOCKED);
        prepaidCard.setStatus(PrepaidCardStatus.LOCKED);
      } else {
        throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", bloqueoDesbloqueoDTO.getDescRetorno()));
      }
    }
    return prepaidCard;
  }

  @Override
  public PrepaidCard10 unlockPrepaidCard(Map<String, Object> headers, Long userIdMc) throws Exception {
    if(userIdMc == null || Long.valueOf(0).equals(userIdMc)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    this.getUserMcById(headers, userIdMc);
    PrepaidUser10 prepaidUser10 = this.getPrepaidUserByUserIdMc(headers, userIdMc);
    PrepaidCard10 prepaidCard = this.getPrepaidCardToUnlock(headers, prepaidUser10.getId());

    if(prepaidCard.isLocked()) {
      BloqueoDesbloqueoDTO bloqueoDesbloqueoDTO = TecnocomServiceHelper.getInstance().getTecnocomService().desbloqueo(prepaidCard.getProcessorUserId(), getEncryptUtil().decrypt(prepaidCard.getEncryptedPan()));
      if(bloqueoDesbloqueoDTO.isRetornoExitoso()) {
        getPrepaidCardEJB10().updatePrepaidCardStatus(headers, prepaidCard.getId(), PrepaidCardStatus.ACTIVE);
        prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
      } else {
        throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", bloqueoDesbloqueoDTO.getDescRetorno()));
      }
    }
    return prepaidCard;
  }

  private PrepaidCard10 getPrepaidCardToLock(Map<String, Object> headers, Long userId)throws Exception {
    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, userId);
    if(prepaidCard == null || (prepaidCard != null && prepaidCard.getStatus() == null)) {
      throw new ValidationException(TARJETA_NO_EXISTE);
    } else if(!PrepaidCardStatus.ACTIVE.equals(prepaidCard.getStatus()) && !PrepaidCardStatus.LOCKED.equals(prepaidCard.getStatus())){
      throw new ValidationException(TARJETA_NO_ACTIVA);
    }
    return prepaidCard;
  }

  private PrepaidCard10 getPrepaidCardToUnlock(Map<String, Object> headers, Long userId)throws Exception {
    PrepaidCard10 prepaidCard = getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, userId);
    if(prepaidCard == null || (prepaidCard != null && prepaidCard.getStatus() == null)) {
      throw new ValidationException(TARJETA_NO_EXISTE);
    } else if(!PrepaidCardStatus.ACTIVE.equals(prepaidCard.getStatus()) && !PrepaidCardStatus.LOCKED.equals(prepaidCard.getStatus())){
      throw new ValidationException(TARJETA_NO_BLOQUEADA);
    }
    return prepaidCard;
  }

  public User getUserMcById(Map<String, Object> headers, Long userIdMc) throws Exception {

    User user = getUserClient().getUserById(headers, userIdMc);

    if (user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if (!UserStatus.ENABLED.equals(user.getGlobalStatus())) {
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }
    return user;
  }

  public User getUserMcByRut(Map<String, Object> headers, Integer rut) throws Exception {
    User user = getUserClient().getUserByRut(headers, rut);

    if (user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if (!UserStatus.ENABLED.equals(user.getGlobalStatus())) {
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }
    return user;
  }

  private PrepaidUser10 getPrepaidUserByUserIdMc(Map<String, Object> headers, Long userIdMc) throws Exception {
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJB10().getPrepaidUserByUserIdMc(headers, userIdMc);

    if (prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    if (!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())) {
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }
    return prepaidUser;
  }

  /**
   * Obtiene los ultimos terminos y condiciones activos para el producto Prepago
   * @param headers
   * @return
   * @throws Exception
   */
  private AppFile getLastTermsAndConditions(Map<String, Object> headers) throws Exception {
     //TODO: Verificar esto!!!!!!!
      Optional<AppFile> file = null; //= getFilesEJBBean10().getAppFiles(headers, null, APP_NAME, TERMS_AND_CONDITIONS, null, AppFileStatus.ENABLED)
     // .stream()
     // .findFirst();

    return (file.isPresent()) ? file.get() : null;
  }

  public PrepaidTac10 getTermsAndConditions(Map<String, Object> headers) throws Exception {
    AppFile tacFile = this.getLastTermsAndConditions(headers);

    if(tacFile == null){
      throw new NotFoundException(TERMINOS_Y_CONDICIONES_NO_EXISTE);
    }

    PrepaidTac10 tac = new PrepaidTac10();
    tac.setLocation(tacFile.getLocation());
    tac.setVersion(tacFile.getVersion());

    return tac;
  }


  /**
   *  Aceptar los terminos y condiciones
   * @param headers
   * @param userIdMc
   * @param termsAndConditions10
   * @throws Exception
   */
  @Override
  public void acceptTermsAndConditions(Map<String, Object> headers, Long userIdMc, NewTermsAndConditions10 termsAndConditions10) throws Exception {
    if(userIdMc == null || Long.valueOf(0).equals(userIdMc)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    if(termsAndConditions10 == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "termsAndConditions"));
    }
    if(StringUtils.isBlank(termsAndConditions10.getVersion())) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "termsAndConditions.version"));
    }

    // Se obtiene el usuario MC
    User user = getUserClient().getUserById(headers, userIdMc);

    if (user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if (!UserStatus.ENABLED.equals(user.getGlobalStatus()) && !UserStatus.PREREGISTERED.equals(user.getGlobalStatus())) {
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }

    // Se verifica que la version que se esta aceptando sea la actual
    AppFile prepaidTac = this.getLastTermsAndConditions(headers);

    if(!prepaidTac.getVersion().equals(termsAndConditions10.getVersion())) {
      throw new ValidationException(VERSION_TERMINOS_Y_CONDICIONES_NO_COINCIDEN);
    }

    //TODO: VERIFICAR ESTO CUANDO SE COMIENCE A MIGRAR ESTO A PREPAGO
    // Se verifica si el usuario ya acepto los tac
    List<UserFile> files = getUserClient().getUserFiles(headers, user.getId(), APP_NAME, TERMS_AND_CONDITIONS, termsAndConditions10.getVersion());

    // Si el usuario ya acepto la version de los tac no se hace nada
    if (files == null || files.size() == 0) {
      getUserClient().createUserFile(headers, user.getId(), new UserFile());
    }

    //TODO guardar si el usuario acepta recibir beneficios

  }

  @Override
  public User uploadIdentityVerificationFiles(Map<String, Object> headers, Long userIdMc, Map<String, UserFile> identityVerificationFiles) throws Exception {
    if(userIdMc == null || Long.valueOf(0).equals(userIdMc)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    if(identityVerificationFiles == null || identityVerificationFiles.isEmpty()){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "identityVerificationFiles"));
    }

    if(!identityVerificationFiles.containsKey(USER_ID_FRONT) || identityVerificationFiles.get(USER_ID_FRONT) == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "identityVerificationFiles." + USER_ID_FRONT));
    }

    if(!identityVerificationFiles.containsKey(USER_ID_BACK) || identityVerificationFiles.get(USER_ID_BACK) == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "identityVerificationFiles." + USER_ID_BACK));
    }

    if(!identityVerificationFiles.containsKey(USER_SELFIE) || identityVerificationFiles.get(USER_SELFIE) == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "identityVerificationFiles." + USER_SELFIE));
    }

    // Obtener usuario Multicaja
    User user = this.getUserMcById(headers, userIdMc);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, userIdMc);

    // CI frontal
    UserFile ciFront = identityVerificationFiles.get(USER_ID_FRONT);
    ciFront.setApp(APP_NAME);
    ciFront.setName(USER_ID_FRONT);
    ciFront.setVersion("v1.0");
    ciFront.setDescription("CI frontal");
    getUserClient().createUserFile(headers, user.getId(), ciFront);

    // CI posterior
    UserFile ciBack = identityVerificationFiles.get(USER_ID_BACK);
    ciFront.setApp(APP_NAME);
    ciFront.setName(USER_ID_BACK);
    ciFront.setVersion("v1.0");
    ciFront.setDescription("CI posterior");
    getUserClient().createUserFile(headers, user.getId(), ciBack);
    // Selfie
    UserFile selfie = identityVerificationFiles.get(USER_SELFIE);
    ciFront.setApp(APP_NAME);
    ciFront.setName(USER_SELFIE);
    ciFront.setVersion("v1.0");
    ciFront.setDescription("Selfie + CI");
    getUserClient().createUserFile(headers, user.getId(), selfie);

    // Actualizar el nameStatus a IN_REVIEW
    //TODO: Ver como quedara esto.
    return  getUserClient().updateNameStatus(headers, user.getId(), NameStatus.IN_REVIEW);
  }
}
