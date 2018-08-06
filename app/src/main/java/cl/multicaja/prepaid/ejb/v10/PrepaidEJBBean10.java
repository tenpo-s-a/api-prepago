package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.*;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.*;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.BloqueoDesbloqueoDTO;
import cl.multicaja.tecnocom.dto.ConsultaMovimientosDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.tecnocom.dto.MovimientosDTO;
import cl.multicaja.users.ejb.v10.DataEJBBean10;
import cl.multicaja.users.ejb.v10.FilesEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.*;
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
import static cl.multicaja.prepaid.helpers.CalculationsHelper.*;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

  private static String APP_NAME = "api-prepaid";
  private static String TERMS_AND_CONDITIONS = "TERMS_AND_CONDITIONS";

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJB10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJB10;

  @EJB
  private UsersEJBBean10 usersEJB10;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJB10;

  @EJB
  private DataEJBBean10 usersDataEJB10;

  @EJB
  private FilesEJBBean10 filesEJBBean10;

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

  public PrepaidMovementEJBBean10 getPrepaidMovementEJB10() {
    return prepaidMovementEJB10;
  }

  public void setPrepaidMovementEJB10(PrepaidMovementEJBBean10 prepaidMovementEJB10) {
    this.prepaidMovementEJB10 = prepaidMovementEJB10;
  }

  public DataEJBBean10 getUsersDataEJB10() {
    return usersDataEJB10;
  }

  public void setUsersDataEJB10(DataEJBBean10 usersDataEJB10) {
    this.usersDataEJB10 = usersDataEJB10;
  }

  public FilesEJBBean10 getFilesEJBBean10() {
    return filesEJBBean10;
  }

  public void setFilesEJBBean10(FilesEJBBean10 filesEJBBean10) {
    this.filesEJBBean10 = filesEJBBean10;
  }

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    return map;
  }

  @Override
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {

    if(topupRequest == null || topupRequest.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(topupRequest.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(topupRequest.getAmount().getCurrencyCode() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.currency_code"));
    }
    if(topupRequest.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isBlank(topupRequest.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(StringUtils.isBlank(topupRequest.getMerchantName())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_name"));
    }
    if(topupRequest.getMerchantCategory() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_category"));
    }
    if(StringUtils.isBlank(topupRequest.getTransactionId())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "transaction_id"));
    }

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
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) {

  }

  @Override
  public PrepaidWithdraw10 withdrawUserBalance(Map<String, Object> headers, NewPrepaidWithdraw10 withdrawRequest) throws Exception {

    if(withdrawRequest == null || withdrawRequest.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(withdrawRequest.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(withdrawRequest.getAmount().getCurrencyCode() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.currency_code"));
    }
    if(withdrawRequest.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isBlank(withdrawRequest.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(StringUtils.isBlank(withdrawRequest.getTransactionId())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "transaction_id"));
    }
    if(StringUtils.isBlank(withdrawRequest.getPassword())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "password"));
    }
    if(StringUtils.isBlank(withdrawRequest.getMerchantName())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_name"));
    }
    if(withdrawRequest.getMerchantCategory() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_category"));
    }

    // Obtener usuario Multicaja
    User user = this.getUserMcByRut(headers, withdrawRequest.getRut());

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, user.getId());

    // Se verifica la clave
    UserPasswordNew userPasswordNew = new UserPasswordNew();
    userPasswordNew.setValue(withdrawRequest.getPassword());
    this.getUsersDataEJB10().checkPassword(headers, prepaidUser.getUserIdMc(), userPasswordNew);

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
    String numreffac = prepaidMovement.getId().toString(); //TODO esto debe ser enviado en varios 0
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

    } else {
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

      User user = getUsersEJB10().getUserByRut(headers, newPrepaidUserSignup.getRut());

      if (user == null) {

        user = getUsersEJB10().getUserByEmail(headers, newPrepaidUserSignup.getEmail());

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

    SignUp signUp = getUsersEJB10().signUpUser(headers, newPrepaidUserSignup.getRut(), newPrepaidUserSignup.getEmail());
    //TODO: Revisar proceso.
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

    User user = getUsersEJB10().getUserById(headers,userIdMc);
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

    user = getUsersEJB10().finishSignupUser(headers,userIdMc);

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
          fee.setValue(TOPUP_WEB_FEE_AMOUNT);
        } else {
          // MAX(100; 0,5% * prepaid_topup_new_amount_value) + IVA
          BigDecimal commission = calculateFee(transaction.getAmount().getValue(), TOPUP_POS_FEE_PERCENTAGE);
          fee.setValue(commission);
        }
        // Calculo el total
        total.setValue(transaction.getAmount().getValue().subtract(fee.getValue()));
      break;
      case WITHDRAW:
        // Calcula las comisiones segun el tipo de carga (WEB o POS)
        if (TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
          fee.setValue(WITHDRAW_WEB_FEE_AMOUNT);
        } else {
          // MAX ( 100; 0,5%*prepaid_topup_new_amount_value ) + IVA
          BigDecimal commission = calculateFee(transaction.getAmount().getValue(), WITHDRAW_POS_FEE_PERCENTAGE);
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
    log.info("Monto maximo a cargar: " + MAX_AMOUNT_BY_USER);

    if((balance.getBalance().getValue().doubleValue() + amountValue.doubleValue()) > MAX_AMOUNT_BY_USER) {
      throw new ValidationException(SALDO_SUPERARA_LOS_$$VALUE).setData(new KeyValue("value", MAX_AMOUNT_BY_USER));
    }

    BigDecimal fee;

    if(simulationNew.isTransactionWeb()){
      fee = CALCULATOR_TOPUP_WEB_FEE_AMOUNT;
    } else {
      fee = calculateFee(simulationNew.getAmount().getValue(), CALCULATOR_TOPUP_POS_FEE_PERCENTAGE);
    }

    //monto a cargar + comision
    BigDecimal calculatedAmount = amountValue.add(fee);

    log.info("Comision: " + fee);

    if(isFirstTopup) {
      calculatedAmount = calculatedAmount.add(OPENING_FEE);
      simulationTopup.setOpeningFee(new NewAmountAndCurrency10(OPENING_FEE));
      log.info("Comision de apertura: " + OPENING_FEE);
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
      fee = CALCULATOR_WITHDRAW_WEB_FEE_AMOUNT;
    } else {
      fee = calculateFee(simulationNew.getAmount().getValue(), CALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE);
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
    User user = this.getUsersEJB10().getUserById(headers, userIdMc);

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
    User user = this.getUsersEJB10().getUserByRut(headers, rut);

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
    User user = this.getUserMcById(headers, userIdMc);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, userIdMc);

    // Obtener tarjeta
    PrepaidCard10 prepaidCard = this.getPrepaidCardEJB10().getLastPrepaidCardByUserId(headers, prepaidUser.getId());

    //Obtener ultimo movimiento
    PrepaidMovement10 movement = this.getPrepaidMovementEJB10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
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
      _startDate = getDateUtils().timeStampToLocaleDate( new Date(prepaidCard.getTimestamps().getCreatedAt().getTime()),headers.get(Constants.HEADER_USER_TIMEZONE).toString());
      _endDate = new Date(System.currentTimeMillis());
    } else {
      _startDate = getDateUtils().dateStringToDate(startDate,"dd-MM-yyyy");
      _endDate = getDateUtils().dateStringToDate(endDate,"dd-MM-yyyy");
    }

    ConsultaMovimientosDTO consultaMovimientosDTO = this.getTecnocomService().consultaMovimientos(prepaidCard.getProcessorUserId(),user.getRut().getValue().toString(),TipoDocumento.RUT,_startDate,_endDate);

    List<PrepaidTransaction10> listTransaction10 = new ArrayList<>();

    count = count != null ? count : -1;
    int index = 0;

    for(MovimientosDTO movimientosDTO : consultaMovimientosDTO.getMovimientos()) {

      PrepaidTransaction10 transaction10 = new PrepaidTransaction10();
      // Get Date and parse
      String sDate = (String) movimientosDTO.getFecfac().get("valueDate");
      String sFormat = (String) movimientosDTO.getFecfac().get("format");
      transaction10.setDate(getDateUtils().dateStringToDate(sDate,sFormat));
      transaction10.setInvoiceDescription(movimientosDTO.getDestipfac());
      transaction10.setExchangeRate(movimientosDTO.getCmbapli());
      transaction10.setCommerceCode(movimientosDTO.getCodcom());
      transaction10.setEconomicConcept1(movimientosDTO.getCodconeco1());
      transaction10.setEconomicConcept2(movimientosDTO.getCodconeco2());
      transaction10.setDescEconomicConcept1(movimientosDTO.getDesconeco1());
      transaction10.setDescEconomicConcept2(movimientosDTO.getDesconeco2());
      transaction10.setAmountDescriptionType1(movimientosDTO.getDesimp1());
      transaction10.setAmountDescriptionType2(movimientosDTO.getDesimp2());
      transaction10.setApplicationAmount1(movimientosDTO.getImpapleco1());
      transaction10.setApplicationAmount2(movimientosDTO.getImpapleco2());
      transaction10.setGrossValue1(movimientosDTO.getImpbrueco1());
      transaction10.setGrossValue2(movimientosDTO.getImpbrueco2());
      transaction10.setInvoiceDescription(movimientosDTO.getDestipfac());
      transaction10.setExtractAccount(movimientosDTO.getNumextcta());
      transaction10.setExtractTransaction(movimientosDTO.getNummovext());
      transaction10.setInvoiceType(movimientosDTO.getTipofac());

      // New Ammount And Curreycy Principal
      NewAmountAndCurrency10 amountPrimary = new NewAmountAndCurrency10();
      amountPrimary.setCurrencyCode(movimientosDTO.getClamon());
      amountPrimary.setValue(movimientosDTO.getImporte());
      transaction10.setAmountPrimary(amountPrimary);

      if (movimientosDTO.getClamondiv() != null && movimientosDTO.getImpdiv() != null) {
        // New Ammount And Curreycy Secondary
        NewAmountAndCurrency10 amountSecondary = new NewAmountAndCurrency10();
        amountSecondary.setCurrencyCode(movimientosDTO.getClamondiv());
        amountSecondary.setValue(movimientosDTO.getImpdiv());
        transaction10.setAmountSecondary(amountSecondary);
      }

      //TODO esta implementacion es provisoria, se debe definir con Felipe cuando es compra, retiro o carga

      transaction10.setDescription(transaction10.getInvoiceDescription());

      Integer invoiceType = transaction10.getInvoiceType();
      TipoFactura tf = TipoFactura.valueOfEnumByCodeAndCorrector(invoiceType, 0);
      transaction10.setDescription(tf != null ? tf.name() : null);

      if (invoiceType == TipoFactura.COMPRA_COMERCIO_RELACIONADO.getCode() ||
        invoiceType == TipoFactura.COMPRA_INTERNACIONAL.getCode() ||
        invoiceType == TipoFactura.COMPRA_NACIONAL.getCode()) {

        transaction10.setOperation("Compra");

      } else if (invoiceType == TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.getCode() ||
                invoiceType == TipoFactura.CARGA_TRANSFERENCIA.getCode()) {

        transaction10.setOperation("Carga");

      } else if (invoiceType == TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.getCode() ||
                  invoiceType == TipoFactura.RETIRO_TRANSFERENCIA.getCode()) {

        transaction10.setOperation("Retiro");

      } else if (invoiceType == TipoFactura.COMISION_APERTURA.getCode()) {

        transaction10.setOperation("Comisión de apertura");

      } else {
        transaction10.setOperation("Operación indefinida");
      }

      if (StringUtils.isBlank(transaction10.getDescription())) {
        transaction10.setDescription("Descripción indefinida");
      } else {
        transaction10.setDescription(StringUtils.capitalize(transaction10.getDescription().replaceAll("_", " ").toLowerCase()));
      }

      listTransaction10.add(transaction10);

      index++;

      if (count > 0) {
        if (index == count) {
          break;
        }
      }
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

  private User getUserMcById(Map<String, Object> headers, Long userIdMc) throws Exception {
    User user = this.getUsersEJB10().getUserById(headers, userIdMc);

    if (user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if (!UserStatus.ENABLED.equals(user.getGlobalStatus()) && !UserStatus.PREREGISTERED.equals(user.getGlobalStatus())) {
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }
    return user;
  }

  private User getUserMcByRut(Map<String, Object> headers, Integer rut) throws Exception {
    User user = this.getUsersEJB10().getUserByRut(headers, rut);

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
    Optional<AppFile> file = getFilesEJBBean10().getAppFiles(headers, null, APP_NAME, TERMS_AND_CONDITIONS, null, AppFileStatus.ENABLED)
      .stream()
      .findFirst();

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
    User user = this.getUserMcById(headers, userIdMc);

    // Se verifica que la version que se esta aceptando sea la actual
    AppFile prepaidTac = this.getLastTermsAndConditions(headers);

    if(!prepaidTac.getVersion().equals(termsAndConditions10.getVersion())) {
      throw new ValidationException(VERSION_TERMINOS_Y_CONDICIONES_NO_COINCIDEN);
    }

    // Se verifica si el usuario ya acepto los tac
    List<UserFile> files = getFilesEJBBean10().getUsersFile(headers, null, user.getId(), APP_NAME, TERMS_AND_CONDITIONS, termsAndConditions10.getVersion(), null);

    // Si el usuario ya acepto la version de los tac no se hace nada
    if (files == null || files.size() == 0) {
      getFilesEJBBean10().createUserFile(headers, user.getId(), prepaidTac.getId(), null, null, null, null, null, null);
    }

    //TODO guardar si el usuario acepta recibir beneficios

  }

}
