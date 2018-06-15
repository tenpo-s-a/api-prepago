package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.data.ejb.v10.DataEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.ParamValue;
import cl.multicaja.users.model.v10.Timestamps;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.io.IOException;
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

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private UsersEJBBean10 usersEJB10;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJB10;

  @EJB
  private DataEJBBean10 usersDataEJB10;

  public PrepaidTopupDelegate10 getDelegate() {
    return delegate;
  }

  public void setDelegate(PrepaidTopupDelegate10 delegate) {
    this.delegate = delegate;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
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

    // Obtener Usuario
    User user = this.getUsersEJB10().getUserByRut(headers, topupRequest.getRut());

    if(user == null){
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if(!UserStatus.ENABLED.equals(user.getGlobalStatus())){
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJBBean10().getPrepaidUserByRut(null, user.getRut().getValue());

    if(prepaidUser == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    if(PrepaidUserLevel.LEVEL_1 != this.getPrepaidUserEJBBean10().getUserLevel(user,prepaidUser)) {
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

    PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                                            PrepaidCardStatus.ACTIVE,
                                                                                                            PrepaidCardStatus.LOCKED);

    if (prepaidCard == null) {

      prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
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

    // Obtener Usuario MC
    User user = this.getUsersEJB10().getUserByRut(headers, withdrawRequest.getRut());

    if(user == null){
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if(!UserStatus.ENABLED.equals(user.getGlobalStatus())){
      throw new ValidationException(CLIENTE_BLOQUEADO_O_BORRADO);
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJBBean10().getPrepaidUserByRut(null, user.getRut().getValue());

    if(prepaidUser == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    // Se verifica la clave
    ParamValue passwordParam = new ParamValue();
    passwordParam.setValue(withdrawRequest.getPassword());
    this.getUsersDataEJB10().checkPassword(headers, prepaidUser.getIdUserMc(), passwordParam);

    PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    if (prepaidCard == null) {

      prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
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

    prepaidWithdraw.setId(cdtTransaction.getTransactionReference());

    CodigoMoneda clamon = prepaidMovement.getClamon();
    IndicadorNormalCorrector indnorcor = prepaidMovement.getIndnorcor();
    TipoFactura tipofac = prepaidMovement.getTipofac();
    BigDecimal impfac = prepaidMovement.getImpfac();
    String codcom = prepaidMovement.getCodcom();
    Integer codact = prepaidMovement.getCodact();
    CodigoPais codpais = prepaidMovement.getCodpais();
    String nomcomred = prepaidWithdraw.getMerchantName();
    String numreffac = prepaidMovement.getId().toString();
    String numaut = numreffac;

    //solamente los 6 primeros digitos de numreffac
    if (numaut.length() > 6) {
      numaut = numaut.substring(numaut.length()-6);
    }

    InclusionMovimientosDTO inclusionMovimientosDTO =  TecnocomServiceHelper.getInstance().getTecnocomService()
      .inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
                            numreffac, impfac, numaut, codcom,
                            nomcomred, codact, codpais);

    if (inclusionMovimientosDTO.isRetornoExitoso()) {

      getPrepaidMovementEJB10().updatePrepaidMovement(null,
        prepaidMovement.getId(),
        inclusionMovimientosDTO.getNumextcta(),
        inclusionMovimientosDTO.getNummovext(),
        inclusionMovimientosDTO.getClamone(),
        PrepaidMovementStatus.PROCESS_OK);

      // se confirma la transaccion
      cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + cdtTransaction.getExternalTransactionId());
      cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    } else {
      //Colocar el movimiento en error
      PrepaidMovementStatus status = TransactionOriginType.WEB.equals(prepaidWithdraw.getTransactionOriginType()) ? PrepaidMovementStatus.ERROR_WEB_WITHDRAW : PrepaidMovementStatus.ERROR_POS_WITHDRAW;
      getPrepaidMovementEJB10().updatePrepaidMovement(null, prepaidMovement.getId(), status);

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

      getPrepaidMovementEJB10().updatePrepaidMovement(null, prepaidMovement.getId(), PrepaidMovementStatus.REVERSED);

      throw new IOException();
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
   * @param userId
   * @param simulationNew
   * @throws BaseException
   */
  private void validateSimulationNew10(Long userId, SimulationNew10 simulationNew) throws BaseException {

    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    if(simulationNew == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "simulationNew"));
    }

    if(simulationNew.getPaymentMethod() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "paymentMethod"));
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
  public SimulationTopup10 topupSimulation(Map<String,Object> header, Long userId, SimulationNew10 simulationNew) throws Exception {

    this.validateSimulationNew10(userId, simulationNew);

    PrepaidUser10 prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, userId);
    if(prepaidUser10 == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    final BigDecimal amountValue = simulationNew.getAmount().getValue();

    // LLAMADA AL CDT
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(amountValue);
    cdtTransaction.setExternalTransactionId(String.valueOf(Utils.uniqueCurrentTimeNano()));
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + prepaidUser10.getRut());
    cdtTransaction.setIndSimulacion(true);
    cdtTransaction.setTransactionType(simulationNew.isTransactionWeb() ? CdtTransactionType.CARGA_WEB : CdtTransactionType.CARGA_POS);
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
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    //saldo del usuario
    PrepaidBalance10 balance = this.getPrepaidUserEJBBean10().getPrepaidUserBalance(header, prepaidUser10.getId());

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
    log.info("Monto a cargar + comision: " + calculatedAmount);

    SimulationTopup10 simulationTopup = new SimulationTopup10();
    simulationTopup.setFee(new NewAmountAndCurrency10(fee));
    simulationTopup.setPca(new NewAmountAndCurrency10(calculatePca(amountValue)));
    simulationTopup.setEed(new NewAmountAndCurrency10(calculateEed(amountValue), CodigoMoneda.USA_USN));
    simulationTopup.setAmountToPay(new NewAmountAndCurrency10(calculatedAmount));

    return simulationTopup;
  }

  @Override
  public SimulationWithdrawal10 withdrawalSimulation(Map<String,Object> header, Long userId, SimulationNew10 simulationNew) throws Exception {

    this.validateSimulationNew10(userId, simulationNew);

    PrepaidUser10 prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserById(null, userId);
    if(prepaidUser10 == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

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
    PrepaidBalance10 balance = this.getPrepaidUserEJBBean10().getPrepaidUserBalance(header, prepaidUser10.getId());

    log.info("Saldo del usuario: " + balance.getBalance().getValue());
    log.info("Monto a retirar: " + amountValue);
    log.info("Comision: " + fee);
    log.info("Monto a retirar + comision: " + calculatedAmount);

    if(balance.getBalance().getValue().doubleValue() < calculatedAmount.doubleValue()) {
      throw new ValidationException(SALDO_INSUFICIENTE_$VALUE).setData(new KeyValue("value", balance.getBalance().getValue()));
    }

    SimulationWithdrawal10 simulationWithdrawal = new SimulationWithdrawal10();
    simulationWithdrawal.setFee(new NewAmountAndCurrency10(fee));
    simulationWithdrawal.setAmountToDiscount(new NewAmountAndCurrency10(calculatedAmount));

    return simulationWithdrawal;
  }
}
