package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.Timestamps;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserStatus;
import org.apache.commons.lang3.RandomStringUtils;
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

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

  private final BigDecimal ONE_HUNDRED = new BigDecimal(100);

  // TODO: externalizar estos porcentajes?
  private final BigDecimal POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  private final BigDecimal IVA_PERCENTAGE = new BigDecimal(19);

  //TODO: Valor dolar debe ser obtenido desde algun servicio.
  private final Integer USD_VALUE = 645;

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

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    return map;
  }

  @Override
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {

    if(topupRequest == null || topupRequest.getAmount() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "amount"));
    }
    if(topupRequest.getAmount().getValue() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "amount.value"));
    }
    if(topupRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "amount.currency_code"));
    }
    if(topupRequest.getRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isBlank(topupRequest.getMerchantCode())){
      throw new ValidationException(101004).setData(new KeyValue("value", "merchant_code"));
    }
    if(StringUtils.isBlank(topupRequest.getTransactionId())){
      throw new ValidationException(101004).setData(new KeyValue("value", "transaction_id"));
    }

    // Obtener Usuario
    User user = this.getUsersEJB10().getUserByRut(headers, topupRequest.getRut());

    if(user == null){
      throw new NotFoundException(102001); // Usuario MC no existe
    }

    if(!UserStatus.ENABLED.equals(user.getGlobalStatus())){
      throw new ValidationException(102002); // Usuario MC bloqueado o borrado
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJBBean10().getPrepaidUserByRut(null, user.getRut().getValue());

    if(prepaidUser == null){
      throw new NotFoundException(102003); // Usuario no tiene prepago
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(102004); // Usuario prepago bloqueado o borrado
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
        throw new ValidationException(106000).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
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
    if(!cdtTransaction.getNumError().equals("0")){
      long lNumError = numberUtils.toLong(cdtTransaction.getNumError(),-1L);
      if(lNumError != -1 && lNumError > 10000) {
        throw new ValidationException(107000).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(101006).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10(topupRequest);
    prepaidTopup.setId(cdtTransaction.getTransactionReference());
    prepaidTopup.setUserId(user.getId());
    prepaidTopup.setStatus("exitoso");
    prepaidTopup.setTimestamps(new Timestamps());

    /*
      Calcular monto a cargar y comisiones
     */
    this.calculateTopupFeeAndTotal(prepaidTopup);

    /*
      Agrega la informacion par el voucher
     */
    this.addVoucherData(prepaidTopup);

    /*
      Registra el movimiento en estado pendiente
     */
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidTopup, prepaidUser, prepaidCard, cdtTransaction);
    prepaidMovement = getPrepaidMovementEJB10().addPrepaidMovement(null, prepaidMovement);

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
      throw new ValidationException(101004).setData(new KeyValue("value", "amount"));
    }
    if(withdrawRequest.getAmount().getValue() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "amount.value"));
    }
    if(withdrawRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "amount.currency_code"));
    }
    if(withdrawRequest.getRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "rut"));
    }
    if(StringUtils.isBlank(withdrawRequest.getMerchantCode())){
      throw new ValidationException(101004).setData(new KeyValue("value", "merchant_code"));
    }
    if(StringUtils.isBlank(withdrawRequest.getTransactionId())){
      throw new ValidationException(101004).setData(new KeyValue("value", "transaction_id"));
    }

    // Obtener Usuario MC
    User user = this.getUsersEJB10().getUserByRut(headers, withdrawRequest.getRut());

    if(user == null){
      throw new NotFoundException(102001); // Usuario MC no existe
    }

    if(!UserStatus.ENABLED.equals(user.getGlobalStatus())){
      throw new ValidationException(102002); // Usuario MC bloqueado o borrado
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserEJBBean10().getPrepaidUserByRut(null, user.getRut().getValue());

    if(prepaidUser == null){
      throw new NotFoundException(102003); // Usuario no tiene prepago
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(102004); // Usuario prepago bloqueado o borrado
    }

    // TODO: que hacer con el nivel de usuario en el retiro?
    PrepaidUserLevel userLevel = this.getPrepaidUserEJBBean10().getUserLevel(user,prepaidUser);

    PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    if (prepaidCard == null) {

      prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.LOCKED_HARD,
        PrepaidCardStatus.EXPIRED,
        PrepaidCardStatus.PENDING);

      if (prepaidCard != null) {
        throw new ValidationException(106000).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
      }

      throw new ValidationException(102003); // cliente no tiene prepago
    }

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdrawRequest.getAmount().getValue());
    cdtTransaction.setTransactionType(withdrawRequest.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME)+"_"+user.getRut().getValue());
    cdtTransaction.setGloss(withdrawRequest.getCdtTransactionType().getName()+" "+withdrawRequest.getAmount().getValue());
    cdtTransaction.setTransactionReference(Long.valueOf(0));
    cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);


    PrepaidWithdraw10 prepaidWithdraw = new PrepaidWithdraw10(withdrawRequest);
    prepaidWithdraw.setId(Long.valueOf(1));
    prepaidWithdraw.setUserId(user.getId());
    prepaidWithdraw.setStatus("exitoso");
    prepaidWithdraw.setTimestamps(new Timestamps());

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
    switch (topup.getTransactionOriginType()) {
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

  @Override
  public void addVoucherData(PrepaidTopup10 topup) throws Exception {

    if(topup == null || topup.getAmount() == null || topup.getAmount().getValue() == null) {
      throw new IllegalStateException();
    }

    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(new Locale("es_CL"));
    DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

    symbols.setGroupingSeparator('.');
    formatter.setDecimalFormatSymbols(symbols);

    topup.setMcVoucherType("A");

    Map<String, String> data = new HashMap<>();
    data.put("name", "amount_paid");
    data.put("value", formatter.format(topup.getAmount().getValue().longValue()));

    List<Map<String, String>> mcVoucherData = new ArrayList<>();
    mcVoucherData.add(data);

    topup.setMcVoucherData(mcVoucherData);
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

    String codent = null;
    try {
      codent = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      log.error("Error al cargar parametro cod_entidad");
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura = null;

    if (TransactionOriginType.WEB.equals(prepaidTopup.getTransactionOriginType())) {
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
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(prepaidTopup.getAmount().getValue());
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup.getMerchantCode());
    prepaidMovement.setCodact(prepaidTopup.getMerchantCategory());
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

  @Override
  public CalculatorTopupResponse10 topupCalculator(Map<String,Object> header, CalculatorRequest10 calculatorRequest) throws Exception {

    if(calculatorRequest == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest"));
    }

    if(calculatorRequest.getUserRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.userRut"));
    }

    if(calculatorRequest.getPaymentMethod() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.paymentMethod"));
    }

    if(calculatorRequest.getAmount() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.amount"));
    }

    if(calculatorRequest.getAmount().getValue() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.amount.value"));
    }

    if(calculatorRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.amount.currencyCode"));
    }

    //VALIDACIONES USUARIO USERMC
    User user = getUsersEJB10().getUserByRut(null, calculatorRequest.getUserRut());
    if(user == null){
      throw new NotFoundException(102001); // Usuario MC no existe
    }

    if(!UserStatus.ENABLED.equals(user.getGlobalStatus())){
      throw new ValidationException(102002); // Usuario MC bloqueado o borrado
    }

    // VALIDACIONES USUARIO PREPAGO
    PrepaidUser10 prepaidUser10 = getPrepaidUserEJBBean10().getPrepaidUserByRut(null,calculatorRequest.getUserRut());
    if(prepaidUser10 == null){
      throw new NotFoundException(102003); // Usuario no tiene prepago
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser10.getStatus())){
      throw new ValidationException(102004); // Usuario prepago bloqueado o borrado
    }

    // LLAMADA AL CDT
    CdtTransaction10 cdtTransaction10 = new CdtTransaction10();
    cdtTransaction10.setAmount(calculatorRequest.getAmount().getValue());
    cdtTransaction10.setExternalTransactionId(RandomStringUtils.random(20));
    cdtTransaction10.setTransactionReference(0L);
    cdtTransaction10.setAccountId(getConfigUtils().getProperty(APP_NAME)+"_"+calculatorRequest.getUserRut());
    cdtTransaction10.setIndSimulacion(true);//ES UNA SIMULACION.
    cdtTransaction10.setGloss("");

    if (TransactionOriginType.POS.equals(calculatorRequest.getPaymentMethod()))
      cdtTransaction10.setTransactionType(CdtTransactionType.CARGA_POS);
    else
      cdtTransaction10.setTransactionType(CdtTransactionType.CARGA_WEB);

    cdtTransaction10 = getCdtEJB10().addCdtTransaction(null,cdtTransaction10);

    // VALIDACIONES CDT
    if(!cdtTransaction10.getNumError().equals("0")){
      long lNumError = numberUtils.toLong(cdtTransaction10.getNumError(),-1L);
      if(lNumError != -1 && lNumError > 10000) {
        throw new ValidationException(107000).setData(new KeyValue("value", cdtTransaction10.getMsjError()));
      } else {
        throw new ValidationException(101006).setData(new KeyValue("value", cdtTransaction10.getMsjError()));
      }
    }

    // OBTENGO LA TARJETA DEL USUARIO Y VALIDO
    PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser10.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED);

    if (prepaidCard10 == null) {
      prepaidCard10 = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser10.getId(),
        PrepaidCardStatus.LOCKED_HARD,
        PrepaidCardStatus.EXPIRED);

      if (prepaidCard10 != null) {
        throw new ValidationException(106000).setData(new KeyValue("value", prepaidCard10.getStatus().toString())); //tarjeta invalida
      }
    }

    //TODO falta una valdacion si prepaidCard10 == null

    // CONSULTA SALDO TECNOCOM
    ConsultaSaldoDTO consultaSaldoDTO = getTecnocomService().consultaSaldo(prepaidCard10.getProcessorUserId(),""+user.getRut().getValue(),TipoDocumento.RUT);

    // SALDO TARJETA EN TECNOCOM
    double saldoTarjeta = consultaSaldoDTO.getSaldisconp().doubleValue()-consultaSaldoDTO.getSalautconp().doubleValue();

    // MONTO A CARGAR
    double montoCarga = calculatorRequest.getAmount().getValue().doubleValue();

    if(saldoTarjeta+montoCarga>500000) {
     throw new ValidationException(304101);
    }

    CalculatorTopupResponse10 calculatorResponse = new CalculatorTopupResponse10();

    calculatorResponse = calcCalculaCarga(calculatorResponse, calculatorRequest.getPaymentMethod(), calculatorRequest.getAmount().getValue().doubleValue());

    return calculatorResponse;
  }

  /**
   *
   * @param resp
   * @param paymentMethod
   * @param amount
   * @return
   */
  private CalculatorTopupResponse10 calcCalculaCarga(CalculatorTopupResponse10 resp, TransactionOriginType paymentMethod, double amount) {

    double pca = (amount-240)/1.022;
    double comission = 0;
    double eed = pca / USD_VALUE;

    if(TransactionOriginType.WEB.equals(paymentMethod)){
      comission = 0;
    } else {
      comission = Math.round(Math.max(100, (amount*0.5/100)))*1.19;
    }

    if(resp == null){
      resp = new CalculatorTopupResponse10();
    }

    resp.setToPay(amount+comission);
    resp.setComission(comission);
    resp.setPca(pca);
    resp.setEed(eed);
    return resp;
  }

  @Override
  public CalculatorWithdrawalResponse10 withdrawalCalculator(Map<String,Object> header, CalculatorRequest10 calculatorRequest) throws Exception {

    if(calculatorRequest == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest"));
    }

    if(calculatorRequest.getUserRut() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.userRut"));
    }

    if(calculatorRequest.getPaymentMethod() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.paymentMethod"));
    }

    if(calculatorRequest.getAmount() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.amount"));
    }

    if(calculatorRequest.getAmount().getValue() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.amount.value"));
    }

    if(calculatorRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(101004).setData(new KeyValue("value", "calculatorRequest.amount.currencyCode"));
    }

    return null;
  }
}
