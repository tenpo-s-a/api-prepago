package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.*;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.*;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.ejb.v11.PrepaidMovementEJBBean11;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.EncryptHelper;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.tenpo.TenpoApiCall;
import cl.multicaja.prepaid.helpers.tenpo.model.State;
import cl.multicaja.prepaid.helpers.tenpo.model.TenpoUser;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.prepaid.model.v11.IvaType;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;


/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);
  private static final BigDecimal NEGATIVE = new BigDecimal(-1);
  private static final BigDecimal ONEHUNDRED = new BigDecimal(100);

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

  public KafkaEventDelegate10 getKafkaEventDelegate10() {
    return kafkaEventDelegate10;
  }

  public void setKafkaEventDelegate10(KafkaEventDelegate10 kafkaEventDelegate10) {
    this.kafkaEventDelegate10 = kafkaEventDelegate10;
  }

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @Inject
  private ReprocesQueueDelegate10 delegateReprocesQueue;

  @Inject
  private ProductChangeDelegate10 productChangeDelegate;

  @Inject
  private MailDelegate10 mailDelegate;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJB10;

  @EJB
  private PrepaidCardEJBBean11 prepaidCardEJB11;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  @EJB
  private PrepaidMovementEJBBean11 prepaidMovementEJB11;

  @EJB
  private FilesEJBBean10 filesEJBBean10;

  @EJB
  private AccountEJBBean10 accountEJBBean10;

  private TenpoApiCall tenpoApiCall;

  private TecnocomService tecnocomService;

  private TecnocomServiceHelper tecnocomServiceHelper;

  private ParametersUtil parametersUtil;

  private CalculationsHelper calculationsHelper;

  private  CalculatorParameter10 calculatorParameter10;

  private NotificationTecnocom notificationTecnocom;

  private EncryptHelper encryptHelper;

  public PrepaidTopupDelegate10 getDelegate() {
    return delegate;
  }

  public void setDelegate(PrepaidTopupDelegate10 delegate) {
    this.delegate = delegate;
  }

  public ReprocesQueueDelegate10 getDelegateReprocesQueue() {
    return delegateReprocesQueue;
  }

  public void setDelegateReprocesQueue(ReprocesQueueDelegate10 delegateReprocesQueue) {
    this.delegateReprocesQueue = delegateReprocesQueue;
  }

  public ProductChangeDelegate10 getProductChangeDelegate() {
    return productChangeDelegate;
  }

  public void setProductChangeDelegate(ProductChangeDelegate10 productChangeDelegate) {
    this.productChangeDelegate = productChangeDelegate;
  }

  public MailDelegate10 getMailDelegate() {
    return mailDelegate;
  }

  public void setMailDelegate(MailDelegate10 mailDelegate) {
    this.mailDelegate = mailDelegate;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJB10() {
    return prepaidUserEJB10;
  }

  public void setPrepaidUserEJB10(PrepaidUserEJBBean10 prepaidUserEJB10) {
    this.prepaidUserEJB10 = prepaidUserEJB10;
  }

  public PrepaidCardEJBBean11 getPrepaidCardEJB11() {
    return prepaidCardEJB11;
  }

  public void setPrepaidCardEJB11(PrepaidCardEJBBean11 prepaidCardEJB11) {
    this.prepaidCardEJB11 = prepaidCardEJB11;
  }

  public CdtEJBBean10 getCdtEJB10() {
    return cdtEJB10;
  }

  public void setCdtEJB10(CdtEJBBean10 cdtEJB10) {
    this.cdtEJB10 = cdtEJB10;
  }


  public PrepaidMovementEJBBean11 getPrepaidMovementEJB11() {
    return prepaidMovementEJB11;
  }

  public void setPrepaidMovementEJB11(PrepaidMovementEJBBean11 prepaidMovementEJB11) {
    this.prepaidMovementEJB11 = prepaidMovementEJB11;
  }

  public AccountEJBBean10 getAccountEJBBean10() {
    return accountEJBBean10;
  }

  public void setAccountEJBBean10(AccountEJBBean10 accountEJBBean10) {
    this.accountEJBBean10 = accountEJBBean10;
  }

  public FilesEJBBean10 getFilesEJBBean10() {
    return filesEJBBean10;
  }

  public void setFilesEJBBean10(FilesEJBBean10 filesEJBBean10) {
    this.filesEJBBean10 = filesEJBBean10;
  }

  public EncryptHelper getEncryptHelper() {
    if(encryptHelper == null){
      encryptHelper = EncryptHelper.getInstance();
    }
    return encryptHelper;
  }

  public CalculationsHelper getCalculationsHelper(){
    if(calculationsHelper == null){
      calculationsHelper = CalculationsHelper.getInstance();
    }
    return calculationsHelper;
  }

  @Override
  public TecnocomService getTecnocomService() {
    if(tecnocomService == null) {
      tecnocomService = TecnocomServiceHelper.getInstance().getTecnocomService();
    }
    return tecnocomService;
  }

  @Override
  public TecnocomServiceHelper getTecnocomServiceHelper() {
    if(tecnocomServiceHelper == null) {
      tecnocomServiceHelper = TecnocomServiceHelper.getInstance();
    }
    return tecnocomServiceHelper;
  }

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    return map;
  }

  public ParametersUtil getParametersUtil() {
    if(parametersUtil == null) {
      parametersUtil = ParametersUtil.getInstance();
    }
    return parametersUtil;
  }

  @Override
  public CalculatorParameter10 getPercentage(){
    if (calculatorParameter10 == null) {
      calculatorParameter10 = super.getPercentage();
    }
    return calculatorParameter10;
  }

  public TenpoApiCall getTenpoApiCall(){
    if(tenpoApiCall == null) {
      tenpoApiCall = TenpoApiCall.getInstance();
    }
    return tenpoApiCall;
  }

  public PrepaidTopup10 topupUserBalanceV1(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {

    this.validateTopupRequest(topupRequest);

    if(topupRequest.getRut() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "rut"));
    }

    // Obtener usuario prepago (V2)
    PrepaidUser10 user = getPrepaidUserEJB10().getPrepaidUserByRut(headers, topupRequest.getRut());
    if(user == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    return this.topupUserBalance(headers, user.getUuid(), topupRequest, Boolean.FALSE);
  }

  /**
   * V2 Con id de usuario Tempo
   * @param headers
   * @param userId
   * @param topupRequest
   * @param fromEndPoint
   * @return
   * @throws Exception
   */
  @Override
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers,String userId, NewPrepaidTopup10 topupRequest, Boolean fromEndPoint) throws Exception {
    if(fromEndPoint == null){
      fromEndPoint = Boolean.FALSE;
    }
    this.validateTopupRequest(topupRequest);

    // Obtener usuario prepago (V2)
    PrepaidUser10 user = getPrepaidUserEJB10().findByExtId(headers,userId);
    if(user == null) {
      // Busca si el usuario existe en Tenpo.
      user = validateTempoUser(userId);
      if(user == null){
        throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
      }
    }

    if (!PrepaidUserStatus.ACTIVE.equals(user.getStatus())) {
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    // 1- La API deberá ir a consultar el estado de la tarjeta a cargar. Para esto se deberá ir a la BBDD de prepago a la tabla tarjetas y consultar el estado.
    // 2- Si el estado es fecha expirada o bloqueada dura se deberá responder un mensaje de error al switch o POS Tarjeta inválida
    // 3- Para cualquier otro estado de la tarjeta, se deberá seguir el proceso
    PrepaidCard10 prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null, user.getId(), PrepaidCardStatus.PENDING, PrepaidCardStatus.ACTIVE, PrepaidCardStatus.LOCKED);
    if (prepaidCard == null) {
      prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null, user.getId(),PrepaidCardStatus.LOCKED_HARD,PrepaidCardStatus.EXPIRED);
      if(prepaidCard != null){
        throw new ValidationException(TARJETA_INVALIDA_$VALUE).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
      }
    }

    // Se verifica si es primera carga
    topupRequest.setFirstTopup(getPrepaidMovementEJB11().isFirstTopup(user.getId()));

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10(topupRequest);
    prepaidTopup.setUserId(user.getId());
    prepaidTopup.setStatus("exitoso");

    //TODO: Se debe corregir cuando se deje de utilizar el campo RUT
    if(prepaidTopup.getRut() == null) {
      prepaidTopup.setRut(user.getRut());
    }

    // Calcular monto a cargar y comisiones
    List<PrepaidMovementFee10> feeList = this.calculateFeeList(prepaidTopup);
    prepaidTopup = (PrepaidTopup10) this.calculateFeeAndTotal(prepaidTopup, feeList);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    log.info(String.format("Monto a cargar $ %d [$ %d]-[$ %d]",topupRequest.getAmount().getValue().subtract(prepaidTopup.getFee().getValue()).longValue(),topupRequest.getAmount().getValue().longValue(),prepaidTopup.getFee().getValue().longValue()));
    cdtTransaction.setAmount(topupRequest.getAmount().getValue().subtract(prepaidTopup.getFee().getValue()));
    cdtTransaction.setTransactionType(topupRequest.getCdtTransactionType());
    cdtTransaction.setAccountId(String.format("PREPAGO_%s",user.getDocumentNumber()));
    cdtTransaction.setGloss(topupRequest.getCdtTransactionType().getName() + " " + topupRequest.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(topupRequest.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);

    TipoFactura tipoFacReverse = TransactionOriginType.WEB.equals(topupRequest.getTransactionOriginType()) ? TipoFactura.ANULA_CARGA_TRANSFERENCIA : TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;

    //TODO: Revisar si esta decision esta correcta...
    // Si viene desde un endpoint, se debe verificar si ya se tiene una reversa con los mismos datos
    // Si viene internamente no se verifica, puesto que podria ser el movimiento contrario de una reversa
    // Verificar con Negocio o Desarrollo
    if(fromEndPoint) {
      PrepaidMovement10 previousReverse = this.getPrepaidMovementEJB11().getPrepaidMovementForReverse(prepaidCard == null ? 0:prepaidCard.getId(),
        topupRequest.getTransactionId(), PrepaidMovementType.TOPUP,
        tipoFacReverse);

      if(previousReverse != null &&
        previousReverse.getImpfac().stripTrailingZeros().equals(topupRequest.getAmount().getValue().stripTrailingZeros())) {
        cdtTransaction.setTransactionReference(0L);
        PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidTopup, user, prepaidCard, cdtTransaction);
        prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
        prepaidMovement.setEstadoNegocio(BusinessStatusType.REVERSED);
        prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
        prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
        prepaidMovement = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);

        throw new RunTimeValidationException(REVERSA_MOVIMIENTO_REVERSADO);
      }
    }

    cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

    // Si no cumple con los limites
    if(!cdtTransaction.isNumErrorOk()){
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        String msg = cdtTransaction.getMsjError();
        if(StringUtils.containsIgnoreCase(msg, "duplicate key value violates unique constraint")) {
          msg = "Transaccion duplicada";
        }
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", msg));
      }
    }

    // Agrega la informacion par el voucher
    this.addVoucherData(prepaidTopup);

    // Registra el movimiento en estado pendiente
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidTopup, user, prepaidCard, cdtTransaction);
    if(!fromEndPoint){
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
    }
    prepaidMovement = getPrepaidMovementEJB11().addPrepaidMovement(null, prepaidMovement);
    prepaidTopup.setTimestamps(new Timestamps(prepaidMovement.getFechaCreacion().toLocalDateTime(),
      prepaidMovement.getFechaActualizacion().toLocalDateTime()));

    /*
      Registra las comisiones asociadas a este movimiento
     */
    for(PrepaidMovementFee10 fee : feeList) {
      fee.setMovementId(prepaidMovement.getId()); // Asigna el idMovement a cada fee
    }
    getPrepaidMovementEJB11().addPrepaidMovementFeeList(feeList); // Se insertan en la BD

    prepaidTopup.setId(prepaidMovement.getId());

    //Obtiene Cuenta Usuario
    Account account = getAccountEJBBean10().findByUserId(user.getId());

    //TODO: si no hay cuenta, se realiza el alta cliente
    if(account == null) {
      try {
        log.info(String.format("[topupUserBalance] Realizando alta de cliente %s", user.getDocumentNumber()));

        AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastName(), "", user.getDocumentNumber(), TipoDocumento.RUT, TipoAlta.NIVEL2);

        if (altaClienteDTO.isRetornoExitoso()) {
          log.info("[topupUserBalance] alta cliente realizado exitosamente");

          // guarda la informacion de la cuenta
          account = getAccountEJBBean10().insertAccount(user.getId(), altaClienteDTO.getContrato());

          prepaidCard = new PrepaidCard10();
          prepaidCard.setAccountId(account.getId());
          prepaidCard.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard.setUuid(UUID.randomUUID().toString());
          prepaidCard = getPrepaidCardEJB11().createPrepaidCard(null, prepaidCard);
          //Solo para la primera carga se actualiza el id de la tarjeta al creado.

          prepaidMovement = getPrepaidMovementEJB11().updateMovementCardId(prepaidMovement.getId(),prepaidCard.getId());
          if(prepaidMovement != null){
            log.info("[topupUserBalance]  Actualizado Id Tarjeta en el movimiento");
          }else {
            log.info("[topupUserBalance]  Error actualizado Id Tarjeta en el movimiento");
          }
        } else {
          log.error(String.format("[topupUserBalance] Error realizando alta de cliente [%s] [%s]", altaClienteDTO.getRetorno(), altaClienteDTO.getDescRetorno()));
          throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", "Error realizando alta de cliente"));
        }
      } catch(Exception ex) {
        log.error("[topupUserBalance] Exception realizando alta de cliente", ex);
        log.error("[topupUserBalance] Stacktrace init");
        ex.printStackTrace();
        log.error("[topupUserBalance] Stacktrace finish");
        getPrepaidMovementEJB11().updatePrepaidMovementStatus(null, prepaidMovement.getId(), PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD);
        //Confirmar la carga en CDT
        cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
        cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        //Iniciar reversa en CDT
        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
        cdtTransaction.setTransactionReference(0L);
        cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        //Confirmar reversa en CDT
        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA_CONF);
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
        cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", "Error realizando alta de cliente"));
      }
    }

    if(StringUtils.isAllBlank(prepaidCard.getHashedPan())) {
      try {
        log.info(String.format("[topupUserBalance] Obeteniendo datos de tarjeta %s", prepaidCard.getUuid()));

        DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(account.getAccountNumber());

        if (datosTarjetaDTO.isRetornoExitoso()) {
          log.info("[topupUserBalance] datos de tarjeta obtenidos exitosamente");

          prepaidCard.setNameOnCard(user.getName() + " " + user.getLastName());
          prepaidCard.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
          prepaidCard.setProducto(datosTarjetaDTO.getProducto());
          prepaidCard.setNumeroUnico(datosTarjetaDTO.getIdentclitar());

          // se trunca el pan
          prepaidCard.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));

          // se encripta el Pan
          prepaidCard.setEncryptedPan(getEncryptHelper().encryptPan(datosTarjetaDTO.getPan()));

          // se guarda un hash del pan utilizando como secret el accountNumber (contrato)
          prepaidCard.setHashedPan(getPrepaidCardEJB11().hashPan(account.getAccountNumber(), datosTarjetaDTO.getPan()));

          try {
            // Actualiza la tarjeta
            getPrepaidCardEJB11().updatePrepaidCard(null, prepaidCard.getId(),
              account.getId(),
              prepaidCard);
          } catch(Exception ex) {

            log.error("[topupUserBalance] Error al actualizar tarjeta en BD", ex);

            throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", "Error al actualizar tarjeta en BD"));
          }

        } else {
          log.error(String.format("[topupUserBalance] Error realizando consulta datos tarjeta [%s] [%s]", datosTarjetaDTO.getRetorno(), datosTarjetaDTO.getDescRetorno()));
          throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", "Error consultando datos de tarjeta"));
        }
      } catch (Exception ex) {
        log.error("[topupUserBalance] Exception consultando datos de tarjeta", ex);
        log.error("[topupUserBalance] Stacktrace init");
        ex.printStackTrace();
        log.error("[topupUserBalance] Stacktrace finish");
        getPrepaidMovementEJB11().updatePrepaidMovementStatus(null, prepaidMovement.getId(), PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD);
        //Confirmar la carga en CDT
        cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
        cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        //Iniciar reversa en CDT
        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
        cdtTransaction.setTransactionReference(0L);
        cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        //Confirmar reversa en CDT
        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA_CONF);
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
        cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", "Error consultando datos de tarjeta"));
      }
    }

    String pan = getEncryptHelper().decryptPan(prepaidCard.getEncryptedPan());

    log.info(String.format("[topupUserBalance] Realizando inclusion de movimientos [id_tx_ext: %s]", prepaidMovement.getIdTxExterno()));

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomServiceHelper().topup(account.getAccountNumber(), pan, prepaidTopup.getMerchantName(), prepaidMovement);

    // Responde OK
    if (inclusionMovimientosDTO.isRetornoExitoso()) {
      log.error("[topupUserBalance] Inclusion de movimientos OK");

      getPrepaidMovementEJB11().updatePrepaidMovement(null,
        prepaidMovement.getId(),
        prepaidCard.getPan(),
        inclusionMovimientosDTO.getCenalta(),
        inclusionMovimientosDTO.getCuenta(),
        inclusionMovimientosDTO.getNumextcta(),
        inclusionMovimientosDTO.getNummovext(),
        inclusionMovimientosDTO.getClamone(),
        BusinessStatusType.CONFIRMED,
        PrepaidMovementStatus.PROCESS_OK);

      cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      if (!cdtTransaction.isNumErrorOk()) {
        log.error(String.format("Error en CDT %s", cdtTransaction.getMsjError()));
      }

      // Se envia informacion a accounting/clearing
      this.getDelegate().sendMovementToAccounting(prepaidMovement, null);

      // Evento de transaccion autorizada
      getPrepaidMovementEJB11().publishTransactionAuthorizedEvent(user.getUuid(), account.getUuid(), prepaidCard.getUuid(), prepaidMovement, prepaidTopup.getFeeList(), TransactionType.CASH_IN_MULTICAJA);

      // Expira cache del saldo de la cuenta
      getAccountEJBBean10().expireBalanceCache(account.getId());
    }
    else if(CodigoRetorno._1020.equals(inclusionMovimientosDTO.getRetorno())) {
      log.error("[topupUserBalance] Inclusion de movimientos Error Timeout Response");
      getPrepaidMovementEJB11().updatePrepaidMovementStatus(headers, prepaidMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
      //Inicia la reversa del movimiento

      // Agrego la reversa al cdt
      CdtTransaction10 cdtTransactionReverse = new CdtTransaction10();
      cdtTransactionReverse.setTransactionReference(0L);
      cdtTransactionReverse.setExternalTransactionId(topupRequest.getTransactionId());

      PrepaidTopup10 reverse = new PrepaidTopup10(topupRequest);

      PrepaidMovement10 prepaidMovementReverse = buildPrepaidMovement(reverse, user, prepaidCard, cdtTransactionReverse);
      prepaidMovementReverse.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovementReverse.setPan(prepaidMovement.getPan());
      prepaidMovementReverse.setCentalta(prepaidMovement.getCentalta());
      prepaidMovementReverse.setCuenta(prepaidMovement.getCuenta());
      prepaidMovementReverse.setTipofac(tipoFacReverse);
      prepaidMovementReverse.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
      prepaidMovementReverse = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovementReverse);

      String messageId = this.getDelegate().sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidMovementReverse);

      // Evento de transaccion rechazada
      getPrepaidMovementEJB11().publishTransactionRejectedEvent(user.getUuid(), account.getUuid(), prepaidCard.getUuid(), prepaidMovement, prepaidTopup.getFeeList(), TransactionType.CASH_IN_MULTICAJA);

      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()), new KeyValue("messageId", messageId));
    }
    else {
      log.error(String.format("[topupUserBalance] Error al realizar el cash-in [%s] [%s]", inclusionMovimientosDTO.getRetorno(), inclusionMovimientosDTO.getDescRetorno()));
      // Evento de transaccion rechazada
      getPrepaidMovementEJB11().publishTransactionRejectedEvent(user.getUuid(), account.getUuid(), prepaidCard.getUuid(), prepaidMovement, prepaidTopup.getFeeList(), TransactionType.CASH_IN_MULTICAJA);

      //Colocar el movimiento en error
      getPrepaidMovementEJB11().updatePrepaidMovementStatus(null, prepaidMovement.getId(), PrepaidMovementStatus.REJECTED);
      getPrepaidMovementEJB11().updatePrepaidBusinessStatus(headers, prepaidMovement.getId(), BusinessStatusType.REJECTED);

      //Confirmar la carga en CDT
      cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //Iniciar reversa en CDT
      cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
      cdtTransaction.setTransactionReference(0L);
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //Confirmar reversa en CDT
      cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA_CONF);
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + topupRequest.getAmount().getValue());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()));
    }

    if(prepaidTopup.isFirstTopup()) {
      // Activa la tarjeta luego de realizado el cobro de emision
      prepaidCard = getPrepaidCardEJB11().updatePrepaidCardStatus(prepaidCard.getId(),PrepaidCardStatus.ACTIVE);

      //TODO: se comenta la publicacion de evento de tarjeta creada ya que se estan generando en Nivel 2.
      // el evento se publica al realizar upgrade de tarjeta.

      // publica evento de tarjeta creada
      //getPrepaidCardEJB11().publishCardEvent(
      //  user.getUuid(),
      //  account.getUuid(),
      //  prepaidCard.getId(),
      //  KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT
      //);

      // publica evento de contrato/cuenta creada
      getAccountEJBBean10().publishAccountCreatedEvent(user.getUuid(), account);
    }
    return prepaidTopup;
  }

  /**
   * Permite buscar un usaurio en tenpo por ID. Y Actualiza el usuario en Prepago.
   * @param userId
   * @return
   * @throws Exception
   */
  public PrepaidUser10 validateTempoUser(String userId) throws Exception {

    PrepaidUser10 prepaidUser10 = null;
    try {

      //FIXME: Implementar integracion con servicio de usuarios.
      TenpoUser tenpoUser = getTenpoApiCall().getUserById(UUID.fromString(userId));
      if(tenpoUser != null){

        prepaidUser10 = new PrepaidUser10();

        prepaidUser10.setDocumentNumber(tenpoUser.getTributaryIdentifier());
        prepaidUser10.setDocumentType(DocumentType.DNI_CL); // REVISAR QUE VALORES VENDRAN ACA Y AGREGAR AL ENUM
        prepaidUser10.setName(tenpoUser.getFirstName());
        prepaidUser10.setLastName(tenpoUser.getLastName());
        prepaidUser10.setUuid(tenpoUser.getId().toString());
        prepaidUser10.setTimestamps(new Timestamps(LocalDateTime.now(ZoneOffset.UTC),LocalDateTime.now(ZoneOffset.UTC)));
        prepaidUser10.setUserLevel(PrepaidUserLevel.valueOfEnum(tenpoUser.getLevel().name()));
        prepaidUser10.setStatus(getUserStatusFromTenpoStatus(tenpoUser.getState()));
        prepaidUser10.setRut(Integer.parseInt(tenpoUser.getTributaryIdentifier().toLowerCase().replace("-","").replace("k","")));//TODO: Eliminar  cuando se deje de depender.
        prepaidUser10.setBalanceExpiration(0L);
        prepaidUser10.setUserIdMc(prepaidUser10.getRut().longValue());
        prepaidUser10.setUserPlan(UserPlanType.valueOfEnum(tenpoUser.getPlan().name()));
        prepaidUser10 = getPrepaidUserEJB10().createUser(null,prepaidUser10);
      }
    } catch (NotFoundException e){
      log.error(e);
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }catch (Exception e) {//TODO: Por mientras si no funcioa se enviara Usuario no encontrado.
      e.printStackTrace();
      log.error(e);
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }
    return prepaidUser10;
  }
  public PrepaidUserStatus getUserStatusFromTenpoStatus(State state){
    switch (state){
      case ACTIVE: return PrepaidUserStatus.ACTIVE;
      case BLOCKED: return PrepaidUserStatus.BLOCKED;
      default: return PrepaidUserStatus.DISABLED;
    }
  }

  public void reverseTopupUserBalance(Map<String, Object> headers,String userId,  NewPrepaidTopup10 topupRequest,Boolean fromEndPoint) throws Exception {

    this.validateTopupRequest(topupRequest);

    if(fromEndPoint == null){
      fromEndPoint = Boolean.FALSE;
    }

    // Obtener usuario prepago

    PrepaidUser10 prepaidUser = getPrepaidUserEJB10().findByExtId(headers, userId);

    if(prepaidUser == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    if(prepaidUser.getStatus().equals(PrepaidUserStatus.DISABLED)){
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    PrepaidCard10 prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null,prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);
    if (prepaidCard == null) {
      prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null,prepaidUser.getId(),PrepaidCardStatus.LOCKED_HARD,PrepaidCardStatus.EXPIRED);
      if(prepaidCard != null){
        throw new ValidationException(TARJETA_INVALIDA_$VALUE).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
      }
    }
    TipoFactura tipoFacTopup = TransactionOriginType.WEB.equals(topupRequest.getTransactionOriginType()) ? TipoFactura.CARGA_TRANSFERENCIA : TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
    TipoFactura tipoFacReverse = TransactionOriginType.WEB.equals(topupRequest.getTransactionOriginType()) ? TipoFactura.ANULA_CARGA_TRANSFERENCIA : TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;

    // Se verifica si ya se tiene una reversa con los mismos datos
    PrepaidMovement10 previousReverse = this.getPrepaidMovementEJB11().getPrepaidMovementForReverse(prepaidCard.getId(), topupRequest.getTransactionId(),
      PrepaidMovementType.TOPUP, tipoFacReverse);

    if(previousReverse == null) {

      // Busca el movimiento de carga original
      PrepaidMovement10 originalTopup = this.getPrepaidMovementEJB11().getPrepaidMovementForReverse(prepaidCard.getId(), topupRequest.getTransactionId(), PrepaidMovementType.TOPUP, tipoFacTopup);

      // Verifica si existe la carga original topup
      if(originalTopup != null) {
        if(originalTopup.getMonto().stripTrailingZeros().equals(topupRequest.getAmount().getValue().stripTrailingZeros())) {
          String timezone;
          if(headers == null || !headers.containsKey(Constants.HEADER_USER_TIMEZONE)){
            timezone="America/Santiago";
          }
          else {
            timezone= headers.get(Constants.HEADER_USER_TIMEZONE).toString();
          }
          if(getDateUtils().inLastHours(24L, originalTopup.getFechaCreacion(), timezone) || !fromEndPoint) {
            // Agrego la reversa al cdt
            CdtTransaction10 cdtTransaction = new CdtTransaction10();
            cdtTransaction.setTransactionReference(0L);
            cdtTransaction.setExternalTransactionId(topupRequest.getTransactionId());

            PrepaidTopup10 reverse = new PrepaidTopup10(topupRequest);


            PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
            if(!fromEndPoint){
              prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
            }
            prepaidMovement.setCentalta(originalTopup.getCentalta());
            prepaidMovement.setCuenta(originalTopup.getCuenta());
            prepaidMovement.setPan(originalTopup.getPan());
            prepaidMovement.setTipofac(tipoFacReverse);
            prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
            prepaidMovement = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);
            prepaidMovement.setNumaut(TecnocomServiceHelper.getNumautFromIdMov(prepaidMovement.getId().toString()));
            this.getDelegate().sendPendingTopupReverse(reverse,prepaidCard,prepaidUser,prepaidMovement);

            Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());

            // Se publica evento de transaccion reversada
            getPrepaidMovementEJB11().publishTransactionReversedEvent(prepaidUser.getUuid(), account.getUuid(), prepaidCard.getUuid(), originalTopup, null, TransactionType.CASH_IN_MULTICAJA);

          } else {
            log.info(String.format("El plazo de reversa ha expirado para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), originalTopup.getIdTxExterno(), originalTopup.getMonto()));
            throw new ReverseTimeExpiredException();
          }
        } else {
          log.error(String.format("Monto de la transaccion no concuerda. Original -> [%s], Reversa -> [%s].", originalTopup.getMonto(), topupRequest.getAmount().getValue()));
          throw new ValidationException(REVERSA_INFORMACION_NO_CONCUERDA);
        }
      } else {
        log.info(String.format("No existe una carga con los datos -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), topupRequest.getTransactionId(), topupRequest.getAmount().getValue()));
        CdtTransaction10 cdtTransaction = new CdtTransaction10();
        cdtTransaction.setExternalTransactionId(topupRequest.getTransactionId());
        cdtTransaction.setTransactionReference(0L);

        PrepaidTopup10 reverse = new PrepaidTopup10(topupRequest);
        PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
        if(!fromEndPoint){
          prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
        }
        prepaidMovement.setTipofac(tipoFacReverse);
        prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
        prepaidMovement.setCardId(prepaidCard.getId());
        // Se coloca conciliada contra tecnocom, ya que nunca se hace la reversa y por lo tanto no vendra en el archivo de operaciones diarias
        prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
        prepaidMovement = this.getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);
        this.getPrepaidMovementEJB11().updatePrepaidMovementStatus(headers, prepaidMovement.getId(), PrepaidMovementStatus.PROCESS_OK);

        throw new ReverseOriginalMovementNotFoundException();
      }
    } else {
      log.info(String.format("Ya existe una reversa para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), topupRequest.getTransactionId(), topupRequest.getAmount().getValue()));
      throw new ReverseAlreadyReceivedException();
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

    if(StringUtils.isBlank(request.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(!StringUtils.isNumeric(request.getMerchantCode())) {
      throw new BadRequestException(PARAMETRO_NO_CUMPLE_FORMATO_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(request.getMerchantCode().length() > 15) {
      request.setMerchantCode(request.getMerchantCode().substring(request.getMerchantCode().length() - 15));
    } else {
      request.setMerchantCode(StringUtils.leftPad(request.getMerchantCode(), 15, '0'));
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
  public PrepaidWithdraw10 withdrawUserBalance(Map<String, Object> headers, String externalUserId, NewPrepaidWithdraw10 withdrawRequest , Boolean fromEndPoint) throws Exception {

    if(fromEndPoint == null){
      fromEndPoint = Boolean.FALSE;
    }
    this.validateWithdrawRequest(withdrawRequest, false, fromEndPoint);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = getPrepaidUserEJB10().findByExtId(headers,externalUserId);
    if(prepaidUser == null ){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }
    else if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }


    // Obtiene la cuenta del usuario prepago
    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    if(account == null){
      throw new ValidationException(CLIENTE_NO_TIENE_PREPAGO);
    }
    // Obtiene la tarjeta de un usaurio rpeapgo
    PrepaidCard10 prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null,prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);
    if (prepaidCard == null) {
      prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null,prepaidUser.getId(),PrepaidCardStatus.LOCKED_HARD,PrepaidCardStatus.EXPIRED,PrepaidCardStatus.PENDING);
      if(prepaidCard != null){
        throw new ValidationException(TARJETA_INVALIDA_$VALUE).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
      }
      throw new ValidationException(CLIENTE_NO_TIENE_PREPAGO);
    }

    //Se cambia para calcular la comision previo al envio al CDT
    PrepaidWithdraw10 prepaidWithdraw = new PrepaidWithdraw10(withdrawRequest);
    prepaidWithdraw.setUserId(prepaidUser.getId());
    prepaidWithdraw.setStatus("exitoso");

    //TODO: Se debe corregir cuando se deje de utilizar el campo RUT
    if(prepaidWithdraw.getRut() == null) {
      prepaidWithdraw.setRut(prepaidUser.getRut());
    }

    /*
      Calcular monto a cargar y comisiones
    */
    List<PrepaidMovementFee10> feeList = this.calculateFeeList(prepaidWithdraw);
    prepaidWithdraw = (PrepaidWithdraw10) this.calculateFeeAndTotal(prepaidWithdraw, feeList);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(withdrawRequest.getAmount().getValue().subtract(prepaidWithdraw.getFee().getValue()));
    cdtTransaction.setTransactionType(withdrawRequest.getCdtTransactionType());
    cdtTransaction.setAccountId(String.format("PREPAGO_%s", prepaidUser.getDocumentNumber()));
    cdtTransaction.setGloss(withdrawRequest.getCdtTransactionType().getName() + " " + withdrawRequest.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());
    cdtTransaction.setIndSimulacion(Boolean.FALSE);

    TipoFactura tipoFacReverse = TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType()) ? TipoFactura.ANULA_RETIRO_TRANSFERENCIA : TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;

    // Se verifica si ya se tiene una reversa con los mismos datos
    PrepaidMovement10 previousReverse = this.getPrepaidMovementEJB11().getPrepaidMovementForReverse(prepaidCard.getId(),
      withdrawRequest.getTransactionId(), PrepaidMovementType.WITHDRAW,
      tipoFacReverse);

    if(previousReverse != null && previousReverse.getImpfac().stripTrailingZeros().equals(withdrawRequest.getAmount().getValue().stripTrailingZeros())) {
      cdtTransaction.setTransactionReference(0L);
      PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidWithdraw, prepaidUser, prepaidCard, cdtTransaction);
      prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
      prepaidMovement.setEstadoNegocio(BusinessStatusType.REVERSED);
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      //TODO: deberia tambien ser conciliada con swtich asi se responda error?
      prepaidMovement = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);

      throw new RunTimeValidationException(REVERSA_MOVIMIENTO_REVERSADO);
    }


    cdtTransaction = this.getCdtEJB10().addCdtTransaction(headers, cdtTransaction);

    // Si no cumple con los limites
    if (!cdtTransaction.isNumErrorOk()) {
      int lNumError = cdtTransaction.getNumErrorInt();
      if (lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        String msg = cdtTransaction.getMsjError();
        if(StringUtils.containsIgnoreCase(msg, "duplicate key value violates unique constraint")) {
          msg = "Transaccion duplicada";
        }
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", msg));
      }
    }

    /*
      Agrega la informacion para el voucher
    */
    this.addVoucherData(prepaidWithdraw);

    /*
      Registra el movimiento en estado pendiente
     */
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement(prepaidWithdraw, prepaidUser, prepaidCard, cdtTransaction);

    // Estos dos tipos de retiros no viene del SWITCH, por lo que no requieren esa conciliacion
    if(!fromEndPoint || TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType())) {
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
    }

    prepaidMovement = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);
    prepaidWithdraw.setTimestamps(new Timestamps(prepaidMovement.getFechaCreacion().toLocalDateTime(),
      prepaidMovement.getFechaActualizacion().toLocalDateTime()));

    prepaidWithdraw.setId(prepaidMovement.getId());

    /*
      Registra las comisiones asociadas a este movimiento
     */
    for(PrepaidMovementFee10 fee : feeList) {
      fee.setMovementId(prepaidMovement.getId()); // Asigna el idMovement a cada fee
    }
    getPrepaidMovementEJB11().addPrepaidMovementFeeList(feeList); // Se insertan en la BD

    String contrato = account.getAccountNumber();
    String pan = getEncryptHelper().decryptPan(prepaidCard.getEncryptedPan());

    InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomServiceHelper().withdraw(contrato, pan, prepaidWithdraw.getMerchantName(), prepaidMovement);
    log.info(String.format("Respuesta inclusion [%s] [%s]",inclusionMovimientosDTO.getRetorno(), inclusionMovimientosDTO.getDescRetorno()));

    if (inclusionMovimientosDTO.isRetornoExitoso()) {
      String centalta = inclusionMovimientosDTO.getCenalta();
      String cuenta = inclusionMovimientosDTO.getCuenta();
      Integer numextcta = inclusionMovimientosDTO.getNumextcta();
      Integer nummovext = inclusionMovimientosDTO.getNummovext();
      Integer clamone = inclusionMovimientosDTO.getClamone();
      PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK;

      getPrepaidMovementEJB11().updatePrepaidMovement(null,
        prepaidMovement.getId(),
        prepaidCard.getPan(),
        centalta,
        cuenta,
        numextcta,
        nummovext,
        clamone,
        null,
        status);

      // Expira cache del saldo de la cuenta
      getAccountEJBBean10().expireBalanceCache(account.getId());

      UserAccount userAccount = null;
      if(TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType())) {
        // Si el retiro es diferido, rescato los datos de la cuenta para la transferenca
        userAccount = new UserAccount();
        userAccount.setAccountNumber(withdrawRequest.getAccountNumber());
        userAccount.setBankId(withdrawRequest.getBankId());
        userAccount.setRut(withdrawRequest.getAccountRut());
        userAccount.setAccountType(withdrawRequest.getAccountType());
      }
      else {
        // se confirma la transaccion para los retiros no web
        cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionTypeConfirm());
        cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + withdrawRequest.getAmount().getValue());
        cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        getPrepaidMovementEJB11().updatePrepaidBusinessStatus(headers, prepaidMovement.getId(), BusinessStatusType.CONFIRMED);
      }
      // Se envia informacion a accounting/clearing
      this.getDelegate().sendMovementToAccounting(prepaidMovement, userAccount);

      //EVENTO DE RETIRO autorizado
      getPrepaidMovementEJB11().publishTransactionAuthorizedEvent(prepaidUser.getUuid(), account.getUuid(), prepaidCard.getUuid(), prepaidMovement, prepaidWithdraw.getFeeList(), TransactionType.CASH_OUT_MULTICAJA);
      log.info("Published event CASH_OUT_MULTICAJA");
    }
    else if(CodigoRetorno._1020.equals(inclusionMovimientosDTO.getRetorno())) {
      log.info("Error Timeout Response");
      getPrepaidMovementEJB11().updatePrepaidMovementStatus(headers, prepaidMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
      //Inicia la reversa del movimiento

      // Agrego la reversa al cdt
      CdtTransaction10 cdtTransactionReverse = new CdtTransaction10();
      cdtTransactionReverse.setTransactionReference(0L);
      cdtTransactionReverse.setExternalTransactionId(withdrawRequest.getTransactionId());

      PrepaidWithdraw10 reverse = new PrepaidWithdraw10(withdrawRequest);

      PrepaidMovement10 prepaidMovementReverse = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransactionReverse);
      if(!fromEndPoint || TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType())) {
        prepaidMovementReverse.setConSwitch(ReconciliationStatusType.RECONCILED);
      }
      prepaidMovementReverse.setPan(prepaidMovement.getPan());
      prepaidMovementReverse.setCentalta(prepaidMovement.getCentalta());
      prepaidMovementReverse.setCuenta(prepaidMovement.getCuenta());
      prepaidMovementReverse.setTipofac(tipoFacReverse);
      prepaidMovementReverse.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
      prepaidMovementReverse = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovementReverse);

      String messageId = this.getDelegate().sendPendingWithdrawReversal(reverse,prepaidUser,prepaidMovementReverse);

      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()),
        new KeyValue("messageId", messageId));
    }
    else {
      log.info("Error no reintentable");
      //Colocar el movimiento en error
      getPrepaidMovementEJB11().updatePrepaidMovementStatus(null, prepaidMovement.getId(), PrepaidMovementStatus.REJECTED);
      getPrepaidMovementEJB11().updatePrepaidBusinessStatus(headers, prepaidMovement.getId(), BusinessStatusType.REJECTED);

      //Confirmar el retiro en CDT
      cdtTransaction.setTransactionType(prepaidWithdraw.getCdtTransactionTypeConfirm());
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + withdrawRequest.getAmount().getValue());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //Iniciar reversa en CDT
      cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_RETIRO);
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + withdrawRequest.getAmount().getValue());
      cdtTransaction.setTransactionReference(0L);
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      //Confirmar reversa en CDT
      cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_RETIRO_CONF);
      cdtTransaction.setGloss(cdtTransaction.getTransactionType().getName() + " " + withdrawRequest.getAmount().getValue());
      cdtTransaction = this.getCdtEJB10().addCdtTransaction(null, cdtTransaction);

      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()));
    }

    return prepaidWithdraw;
  }

  @Override
  public void reverseWithdrawUserBalance(Map<String, Object> headers,String extUserId, NewPrepaidWithdraw10 withdrawRequest, Boolean fromEndPoint) throws Exception {
    if(fromEndPoint == null){
      fromEndPoint = Boolean.FALSE;
    }
    this.validateWithdrawRequest(withdrawRequest, true, fromEndPoint);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser = getPrepaidUserEJB10().findByExtId(headers, extUserId);

    if(prepaidUser == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    if(!PrepaidUserStatus.ACTIVE.equals(prepaidUser.getStatus())){
      throw new ValidationException(CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO);
    }

    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    if(account == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    PrepaidCard10 prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null,prepaidUser.getId(),PrepaidCardStatus.ACTIVE,PrepaidCardStatus.LOCKED);
    if (prepaidCard == null) {
      prepaidCard = getPrepaidCardEJB11().getByUserIdAndStatus(null,prepaidUser.getId(),PrepaidCardStatus.LOCKED_HARD,PrepaidCardStatus.EXPIRED);
      if(prepaidCard != null){
        throw new ValidationException(TARJETA_INVALIDA_$VALUE).setData(new KeyValue("value", prepaidCard.getStatus().toString())); //tarjeta invalida
      }
      throw new ValidationException(CLIENTE_NO_TIENE_PREPAGO);
    }

    TipoFactura tipoFacTopup = TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType()) ? TipoFactura.RETIRO_TRANSFERENCIA : TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
    TipoFactura tipoFacReverse = TransactionOriginType.WEB.equals(withdrawRequest.getTransactionOriginType()) ? TipoFactura.ANULA_RETIRO_TRANSFERENCIA : TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;

    // Se verifica si ya se tiene una reversa con los mismos datos
    PrepaidMovement10 previousReverse = this.getPrepaidMovementEJB11().getPrepaidMovementForReverse(prepaidCard.getId(), withdrawRequest.getTransactionId(),
      PrepaidMovementType.WITHDRAW, tipoFacReverse);

    if(previousReverse == null) {
      // Busca el movimiento de retiro original
      PrepaidMovement10 originalwithdraw = this.getPrepaidMovementEJB11().getPrepaidMovementForReverse(prepaidCard.getId(), withdrawRequest.getTransactionId(),
        PrepaidMovementType.WITHDRAW, tipoFacTopup);

      if(originalwithdraw != null) {
        if(originalwithdraw.getMonto().stripTrailingZeros().equals(withdrawRequest.getAmount().getValue().stripTrailingZeros())) {
          String timezone;
          if(headers == null || !headers.containsKey(Constants.HEADER_USER_TIMEZONE)){
            timezone ="America/Santiago";
          } else{
            timezone = headers.get(Constants.HEADER_USER_TIMEZONE).toString();
          }

          if(getDateUtils().inLastHours(Long.valueOf(24), originalwithdraw.getFechaCreacion(), timezone) || !fromEndPoint) {
            // Agrego la reversa al cdt
            CdtTransaction10 cdtTransaction = new CdtTransaction10();
            cdtTransaction.setTransactionReference(0L);
            cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());

            PrepaidWithdraw10 reverse = new PrepaidWithdraw10(withdrawRequest);

            PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
            if(!fromEndPoint){
              prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
            }

            prepaidMovement.setPan(originalwithdraw.getPan());
            prepaidMovement.setCentalta(originalwithdraw.getCentalta());
            prepaidMovement.setCuenta(originalwithdraw.getCuenta());
            prepaidMovement.setTipofac(tipoFacReverse);
            prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
            prepaidMovement = getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);
            prepaidMovement = getPrepaidMovementEJB11().getPrepaidMovementById(prepaidMovement.getId());
            // Publica evento de Trx reversada.
            if(PrepaidWithdraw10.WEB_MERCHANT_CODE.equals(withdrawRequest.getMerchantCode())){
              // Se publica evento de transaccion reversada
              getPrepaidMovementEJB11().publishTransactionReversedEvent(prepaidUser.getUuid(), account.getUuid(), prepaidCard.getUuid(), originalwithdraw, null, TransactionType.CASH_OUT_WEB);

              log.info("Published Event CASH_OUT_WEB");
            } else{
              getPrepaidMovementEJB11().publishTransactionReversedEvent(prepaidUser.getUuid(), account.getUuid(), prepaidCard.getUuid(), originalwithdraw, null, TransactionType.CASH_OUT_MULTICAJA);
              log.info("Published Event CASH_OUT_MULTICAJA");
            }

            this.getDelegate().sendPendingWithdrawReversal(reverse,prepaidUser,prepaidMovement);

          } else {
            log.info(String.format("El plazo de reversa ha expirado para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), originalwithdraw.getIdTxExterno(), originalwithdraw.getMonto()));
            throw new ReverseTimeExpiredException();
          }
        } else {
          log.error(String.format("Monto de la transaccion no concuerda. Original -> [%s], Reversa -> [%s].", originalwithdraw.getMonto(), withdrawRequest.getAmount().getValue()));
          throw new ValidationException(REVERSA_INFORMACION_NO_CONCUERDA);
        }
      } else {
        log.info(String.format("No existe un retiro con los datos -> idPrepaidUser: %s, idTxExterna: %s", prepaidUser.getId(), withdrawRequest.getTransactionId()));
        CdtTransaction10 cdtTransaction = new CdtTransaction10();
        cdtTransaction.setExternalTransactionId(withdrawRequest.getTransactionId());
        cdtTransaction.setTransactionReference(0L);

        PrepaidWithdraw10 reverse = new PrepaidWithdraw10(withdrawRequest);
        PrepaidMovement10 prepaidMovement = buildPrepaidMovement(reverse, prepaidUser, prepaidCard, cdtTransaction);
        if(!fromEndPoint){
          prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
        }
        prepaidMovement.setTipofac(tipoFacReverse);
        prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFacReverse.getCorrector()));
        // Se coloca conciliada contra tecnocom, ya que nunca se hace la reversa y por lo tanto no vendra en el archivo de operaciones diarias
        prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);
        prepaidMovement = this.getPrepaidMovementEJB11().addPrepaidMovement(headers, prepaidMovement);
        this.getPrepaidMovementEJB11().updatePrepaidMovementStatus(headers, prepaidMovement.getId(), PrepaidMovementStatus.PROCESS_OK);

        throw new ReverseOriginalMovementNotFoundException();
      }
    } else {
      log.info(String.format("Ya existe una reversa para -> idPrepaidUser: %s, idTxExterna: %s, monto: %s", prepaidUser.getId(), withdrawRequest.getTransactionId(), withdrawRequest.getAmount().getValue()));
      throw new ReverseAlreadyReceivedException();
    }
  }

  private void validateWithdrawRequest(NewPrepaidWithdraw10 request, Boolean isReverse, Boolean fromEndPoint) throws Exception {

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

    if(StringUtils.isBlank(request.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(!StringUtils.isNumeric(request.getMerchantCode())) {
      throw new BadRequestException(PARAMETRO_NO_CUMPLE_FORMATO_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }
    if(request.getMerchantCode().length() > 15) {
      request.setMerchantCode(request.getMerchantCode().substring(request.getMerchantCode().length() - 15));
    } else {
      request.setMerchantCode(StringUtils.leftPad(request.getMerchantCode(), 15, '0'));
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
    // Solo los retiros web deberian venir con el id de la cuenta donde hacer el retiro
    if(!isReverse &&  TransactionOriginType.WEB.equals(request.getTransactionOriginType())) {
      if (request.getBankId() == null) {
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "bank_id"));
      }
      if (request.getAccountNumber() == null) {
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account_number"));
      }
      if (request.getAccountType() == null) {
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account_type"));
      }
      if (request.getAccountRut() == null) {
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account_rut"));
      }
    }
  }


  public List<PrepaidMovementFee10> calculateFeeList(IPrepaidTransaction10 transaction) throws BaseException {
    if(transaction == null || transaction.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(transaction.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(StringUtils.isBlank(transaction.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }

    // Calcular las comisiones segun el tipo de carga (WEB o POS)
    List<PrepaidMovementFee10> feeList = null;
    switch (transaction.getMovementType()) {
      case TOPUP:
        if (TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
          feeList = calculateFeeList(transaction.getAmount().getValue(),
                                     getPercentage().getTOPUP_WEB_FEE_AMOUNT(),
                                     getPercentage().getTOPUP_WEB_FEE_PERCENTAGE(),
                                     getPercentage().getTOPUP_WEB_FEE_IVA_TYPE(),
                                     PrepaidMovementFeeType.TOPUP_WEB_FEE);
        } else {
          feeList = calculateFeeList(transaction.getAmount().getValue(),
                                     getPercentage().getTOPUP_POS_FEE_AMOUNT(),
                                     getPercentage().getTOPUP_POS_FEE_PERCENTAGE(),
                                     getPercentage().getTOPUP_POS_FEE_IVA_TYPE(),
                                     PrepaidMovementFeeType.TOPUP_POS_FEE);
        }
        break;
      case WITHDRAW:
        if (TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {
          feeList = calculateFeeList(transaction.getAmount().getValue(),
                                     getPercentage().getWITHDRAW_WEB_FEE_AMOUNT(),
                                     getPercentage().getWITHDRAW_WEB_FEE_PERCENTAGE(),
                                     getPercentage().getWITHDRAW_WEB_FEE_IVA_TYPE(),
                                     PrepaidMovementFeeType.WITHDRAW_WEB_FEE);
        } else {
          feeList = calculateFeeList(transaction.getAmount().getValue(),
                                     getPercentage().getWITHDRAW_POS_FEE_AMOUNT(),
                                     getPercentage().getWITHDRAW_POS_FEE_PERCENTAGE(),
                                     getPercentage().getWITHDRAW_POS_FEE_IVA_TYPE(),
                                     PrepaidMovementFeeType.WITHDRAW_POS_FEE);
        }
        break;
      default:
        feeList = new ArrayList<>();
        break;
    }
    return feeList;
  }


  /**
   * Construye una lista de fees (monto e iva)
   *
   * @param transactionAmount
   * @param baseFee
   * @param percentFee
   * @param ivaType
   * @param feeType
   * @return
   */
  public List<PrepaidMovementFee10> calculateFeeList(BigDecimal transactionAmount, BigDecimal baseFee, BigDecimal percentFee, IvaType ivaType, PrepaidMovementFeeType feeType) {

    ArrayList<PrepaidMovementFee10> feeList = new ArrayList<>();

    // Cobro base
    BigDecimal baseFeeAmount = baseFee.setScale(0, BigDecimal.ROUND_HALF_UP);

    // Cobro porcentual
    BigDecimal percentFeeAmount = transactionAmount.multiply(percentFee).divide(ONEHUNDRED).setScale(0, RoundingMode.HALF_UP);

    // Total Fee
    BigDecimal totalFee = baseFeeAmount.add(percentFeeAmount);

    // Dado el monto total y el tipo de iva ("incluido" o "mas iva") separa los dos valores
    Map<String, BigDecimal> feeAndIva = getCalculationsHelper().calculateFeeAndIva(totalFee, ivaType);

    // Crear la fee de prepago
    PrepaidMovementFee10 prepaidFee = new PrepaidMovementFee10();
    prepaidFee.setAmount(feeAndIva.get("fee"));
    prepaidFee.setFeeType(feeType);
    feeList.add(prepaidFee);

    // Crear el iva de la fee de prepago
    PrepaidMovementFee10 ivaFee = new PrepaidMovementFee10();
    ivaFee.setAmount(feeAndIva.get("iva"));
    ivaFee.setFeeType(PrepaidMovementFeeType.IVA);
    feeList.add(ivaFee);

    return feeList;
  }

  /**
   * Calcula la suma de fees y se las resta/suma al monto total de la transaccion
   * @param transaction
   * @param feeList
   * @return
   * @throws Exception
   */
  @Override
  public IPrepaidTransaction10 calculateFeeAndTotal(IPrepaidTransaction10 transaction, List<PrepaidMovementFee10> feeList) throws Exception {
    if(transaction == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "transaction"));
    }
    if(feeList == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "feeList"));
    }

    CodigoMoneda currencyCodeClp = CodigoMoneda.CLP;

    NewAmountAndCurrency10 total = new NewAmountAndCurrency10();
    total.setCurrencyCode(currencyCodeClp);

    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10();
    fee.setCurrencyCode(currencyCodeClp);

    // Suma la lista de fees
    BigDecimal totalFee = BigDecimal.ZERO;
    for(PrepaidMovementFee10 feeDetail : feeList) {
      totalFee = totalFee.add(feeDetail.getAmount());
    }
    fee.setValue(totalFee);

    // Calcula el total a restar de la cuenta del usuario
    switch (transaction.getMovementType()) {
      case TOPUP:
        //TODO: se debe agregar al calculo el cobro de emision
        total.setValue(transaction.getAmount().getValue().subtract(totalFee));
      break;
      case WITHDRAW:
        total.setValue(transaction.getAmount().getValue().add(totalFee));
      break;
    }

    transaction.setFeeList(feeList);
    transaction.setFee(fee);
    transaction.setTotal(total);
    return transaction;
  }

  @Deprecated
  @Override
  public IPrepaidTransaction10 calculateFeeAndTotal(IPrepaidTransaction10 transaction) throws Exception {

    if(transaction == null || transaction.getAmount() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount"));
    }
    if(transaction.getAmount().getValue() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "amount.value"));
    }
    if(StringUtils.isBlank(transaction.getMerchantCode())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "merchant_code"));
    }

    CodigoMoneda currencyCodeClp = CodigoMoneda.CLP;

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

        //TODO: se debe agregar al calculo el cobro de emision

        // Calculo el total
        total.setValue(transaction.getAmount().getValue().subtract(fee.getValue()));
        break;
      case WITHDRAW:
        // Calcula las comisiones segun el tipo de carga (WEB o POS)
        if (TransactionOriginType.WEB.equals(transaction.getTransactionOriginType())) {

          fee.setValue(BigDecimal.valueOf(getCalculationsHelper().addIva(getPercentage().getWITHDRAW_WEB_FEE_AMOUNT()).intValue()));
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

    return transaction;
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
  public PrepaidMovement10 buildPrepaidMovement(IPrepaidTransaction10 transaction, PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction) {

    String codent = null;
    try {
      codent = getParametersUtil().getString("api-prepaid", "cod_entidad", "v10");
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
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(tipoFactura.getCorrector())); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(transaction.getAmount().getValue());
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(transaction.getMerchantCode());
    prepaidMovement.setCodact(transaction.getMerchantCategory());
    prepaidMovement.setImpliq(BigDecimal.ZERO); // se debe actualizar despues
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
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setNomcomred(transaction.getMerchantName());
    prepaidMovement.setCardId(prepaidCard != null ? prepaidCard.getId(): 0);
    return prepaidMovement;
  }

  /**
   *
   * @param simulationNew
   */
  private void validateSimulationNew10(SimulationNew10 simulationNew) throws BaseException {


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
  @Deprecated
  public SimulationTopupGroup10 topupSimulationGroup(Map<String,Object> headers, Long userId, SimulationNew10 simulationNew) throws Exception {

    if(userId == null){
      userId = this.verifiUserAutentication(headers);
    }

    this.validateSimulationNew10(simulationNew);

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser10 =null;
    try{
      prepaidUser10 = getPrepaidUserEJB10().findById(headers, userId);
    }catch (Exception e){
        log.info("Error: "+e);
    }
    if(prepaidUser10 == null){
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    SimulationTopupGroup10 simulationTopupGroup10 = new SimulationTopupGroup10();

    simulationNew.setPaymentMethod(TransactionOriginType.WEB);
    simulationTopupGroup10.setSimulationTopupWeb(topupSimulation(headers,prepaidUser10,simulationNew));

    simulationNew.setPaymentMethod(TransactionOriginType.POS);
    simulationTopupGroup10.setSimulationTopupPOS(topupSimulation(headers,prepaidUser10,simulationNew));

    return simulationTopupGroup10;
  }

  @Override
  @Deprecated
  public SimulationTopup10 topupSimulation(Map<String,Object> headers,PrepaidUser10 prepaidUser10, SimulationNew10 simulationNew) throws Exception {

    SimulationTopup10 simulationTopup = new SimulationTopup10();
    Boolean isFirstTopup = this.getPrepaidMovementEJB11().isFirstTopup(prepaidUser10.getId());
    simulationTopup.setFirstTopup(isFirstTopup);

    BigDecimal amountValue = simulationNew.getAmount().getValue();

    // Si el codigo de moneda es dolar estadounidense se calcula el el monto inicial en pesos
    if(CodigoMoneda.USD.equals(simulationNew.getAmount().getCurrencyCode())) {
      simulationTopup.setEed(new NewAmountAndCurrency10(amountValue, CodigoMoneda.USD));
      amountValue = getCalculationsHelper().calculateAmountFromEed(amountValue);
      simulationTopup.setInitialAmount(new NewAmountAndCurrency10(amountValue));
    } else {
      simulationTopup.setInitialAmount(simulationNew.getAmount());
    }

    // LLAMADA AL CDT
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(amountValue);
    cdtTransaction.setExternalTransactionId(String.valueOf(Utils.uniqueCurrentTimeNano()));
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setAccountId(String.format("PREPAGO_%d", prepaidUser10.getRut()));
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
      simulationTopup.setEed(new NewAmountAndCurrency10(BigDecimal.valueOf(0), CodigoMoneda.USD));
      simulationTopup.setAmountToPay(zero);
      simulationTopup.setOpeningFee(zero);
      simulationTopup.setInitialAmount(zero);
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
      balance.setUsdValue(getCalculationsHelper().getUsdValue().intValue());
      balance.setUpdated(Boolean.FALSE);
    } else {
      Account acc = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

      balance = getAccountEJBBean10().getBalance(headers, acc.getId());
    }

    log.info("Saldo del usuario: " + balance.getBalance().getValue());
    log.info("Monto a cargar: " + amountValue);
    log.info("Monto maximo a cargar: " + getPercentage().getMAX_AMOUNT_BY_USER());

    if((balance.getBalance().getValue().doubleValue() + amountValue.doubleValue()) > getPercentage().getMAX_AMOUNT_BY_USER()) {
      // Responde mensaje de error, con el saldo total maximo y el monto maximo posible a cargar para no superarlo
      KeyValue maxAmount = new KeyValue("value", getPercentage().getMAX_AMOUNT_BY_USER());
      KeyValue topupAmount = new KeyValue("topup_amount", new BigDecimal(getPercentage().getMAX_AMOUNT_BY_USER()).subtract(balance.getBalance().getValue()));
      throw new ValidationException(SALDO_SUPERARA_LOS_$$VALUE).setData(maxAmount, topupAmount);
    }

    BigDecimal fee;

    if(simulationNew.isTransactionWeb()){
      fee = getPercentage().getCALCULATOR_TOPUP_WEB_FEE_AMOUNT();
    } else {
      fee = getCalculationsHelper().calculateFee(amountValue, getPercentage().getCALCULATOR_TOPUP_POS_FEE_PERCENTAGE());
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
    simulationTopup.setPca(new NewAmountAndCurrency10(getCalculationsHelper().calculatePca(amountValue)));
    if(simulationTopup.getEed() == null) {
      simulationTopup.setEed(new NewAmountAndCurrency10(getCalculationsHelper().calculateEed(amountValue), CodigoMoneda.USD));
    }
    simulationTopup.setAmountToPay(new NewAmountAndCurrency10(calculatedAmount));

    return simulationTopup;
  }

  @Override
  @Deprecated
  public SimulationWithdrawal10 withdrawalSimulation(Map<String,Object> headers, Long userIdMc, SimulationNew10 simulationNew) throws Exception {

    if(userIdMc == null){
      userIdMc = this.verifiUserAutentication(headers);
    }

    this.validateSimulationNew10(simulationNew);

    if(simulationNew.getPaymentMethod() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "method"));
    }

    // Obtener usuario prepago
    PrepaidUser10 prepaidUser10 = this.getPrepaidUserByUserIdMc(headers, userIdMc);

    final BigDecimal amountValue = simulationNew.getAmount().getValue();

    BigDecimal fee;

    if (simulationNew.isTransactionWeb()) {
      fee = BigDecimal.valueOf(getCalculationsHelper().addIva(getPercentage().getWITHDRAW_WEB_FEE_AMOUNT()).intValue());
    } else {
      fee = getCalculationsHelper().calculateFee(simulationNew.getAmount().getValue(), getPercentage().getCALCULATOR_WITHDRAW_POS_FEE_PERCENTAGE());
    }

    //monto a cargar + comision
    BigDecimal calculatedAmount = amountValue.add(fee);

    //saldo del usuario
    Account acc = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    PrepaidBalance10 balance = getAccountEJBBean10().getBalance(headers, acc.getId());

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(amountValue);
    cdtTransaction.setExternalTransactionId(String.valueOf(Utils.uniqueCurrentTimeNano()));
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setAccountId(String.format("PREPAGO_%d", prepaidUser10.getRut()));
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
        if(lNumError == EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS.getValue()) {
          this.withdrawSimulationAmountValidation(EL_MONTO_DE_RETIRO_ES_MENOR_AL_MONTO_MINIMO_DE_RETIROS, balance, fee);
        }
        throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    log.info("Saldo del usuario: " + balance.getBalance().getValue());
    log.info("Monto a retirar: " + amountValue);
    log.info("Comision: " + fee);
    log.info("Monto a retirar + comision: " + calculatedAmount);

    if(balance.getBalance().getValue().doubleValue() < calculatedAmount.doubleValue()) {
      this.withdrawSimulationAmountValidation(SALDO_INSUFICIENTE_$VALUE, balance, fee);
    }

    SimulationWithdrawal10 simulationWithdrawal = new SimulationWithdrawal10();
    simulationWithdrawal.setFee(new NewAmountAndCurrency10(fee));
    simulationWithdrawal.setAmountToDiscount(new NewAmountAndCurrency10(calculatedAmount));

    return simulationWithdrawal;
  }

  private void withdrawSimulationAmountValidation(Errors error, PrepaidBalance10 balance, BigDecimal fee) throws Exception {
    throw new ValidationException(error).setData(new KeyValue("value", String.format("-%s", balance.getBalance().getValue().add(fee.multiply(BigDecimal.valueOf(-1))))));
  }

  @Override
  public PrepaidUser10 getPrepaidUser(Map<String, Object> headers, Long userIdMc) throws Exception {

    if(userIdMc == null){
      userIdMc = this.verifiUserAutentication(headers);
    }

    // Busco el usuario MC
    //User user = getUserClient().getUserById(headers, userIdMc);

    /*if(user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }
     */

    // Busco el usuario prepago
    PrepaidUser10 prepaidUser = this.getPrepaidUserByUserIdMc(headers, userIdMc);
    if(prepaidUser == null){
      throw new ValidationException(CLIENTE_NO_EXISTE);
    }
    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());
    if(account == null){
      throw new ValidationException(CUENTA_NO_EXISTE);
    }

    PrepaidCard10 prepaidCard = getPrepaidCardEJB11().getLastPrepaidCardByAccountIdAndOneOfStatus(null, account.getId(),
      PrepaidCardStatus.ACTIVE,
      PrepaidCardStatus.LOCKED,
      PrepaidCardStatus.PENDING);

    prepaidUser.setHasPrepaidCard(prepaidCard != null);

    // verifica si el usuario ha realizado cargas anteriormente
    prepaidUser.setHasPendingFirstTopup(getPrepaidMovementEJB11().isFirstTopup(prepaidUser.getId()));

    return prepaidUser;
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

  @Override
  public String reprocessQueue(Map<String, Object> headers, ReprocesQueue reprocesQueue) throws Exception {

    String messageId = null;
    if (reprocesQueue == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reprocesQueue"));
    }
    if (reprocesQueue.getIdQueue() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reprocesQueue.getIdQueue()"));
    }
    if (reprocesQueue.getLastQueue() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "reprocesQueue.getLastQueue()"));
    }

    switch (reprocesQueue.getLastQueue()) {
      case TOPUP: {
        log.info(String.format("Reinject %s ",reprocesQueue.getIdQueue()));
        log.error(String.format("Reinject %s ",reprocesQueue.getIdQueue()));
        Queue qResp = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.ERROR_TOPUP_RESP);
        ExchangeData<PrepaidTopupData10> data = (ExchangeData<PrepaidTopupData10>) CamelFactory.getInstance().createJMSMessenger().getMessage(qResp, reprocesQueue.getIdQueue());
        if (data == null) {
          throw new ValidationException(ERROR_DATA_NOT_FOUND);
        }
        PrepaidMovement10  prepaidMovement10 = getPrepaidMovementEJB11().getPrepaidMovementById(data.getData().getPrepaidMovement10().getId());
        if (!ReconciliationStatusType.PENDING.equals(prepaidMovement10.getConTecnocom())&&!ReconciliationStatusType.PENDING.equals(prepaidMovement10.getConSwitch())) {
          messageId = "";
          break;
        }
        data.setRetryCount(0);
        data.reprocesQueueNext();
        messageId = this.getDelegateReprocesQueue().redirectRequest(PrepaidTopupRoute10.PENDING_TOPUP_REQ, data);
        break;
      }

      case CREATE_CARD: {
        log.info(String.format("Reinject %s ",reprocesQueue.getIdQueue()));
        Queue qResp = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.ERROR_CREATE_CARD_RESP);
        ExchangeData<PrepaidTopupData10> data = (ExchangeData<PrepaidTopupData10>)  CamelFactory.getInstance().createJMSMessenger().getMessage(qResp, reprocesQueue.getIdQueue());
        if(data == null) {
          throw new ValidationException(ERROR_DATA_NOT_FOUND);
        }
        data.setRetryCount(0);
        data.reprocesQueueNext();
        messageId = this.getDelegateReprocesQueue().redirectRequest(PrepaidTopupRoute10.PENDING_CREATE_CARD_REQ, data);
        break;
      }
      case PENDING_EMISSION: {
        log.info(String.format("Reinject %s ",reprocesQueue.getIdQueue()));
        Queue qResp = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.ERROR_EMISSION_RESP);
        ExchangeData<PrepaidTopupData10> data = (ExchangeData<PrepaidTopupData10>)  CamelFactory.getInstance().createJMSMessenger().getMessage(qResp, reprocesQueue.getIdQueue());
        if(data == null) {
          throw new ValidationException(ERROR_DATA_NOT_FOUND);
        }
        data.setRetryCount(0);
        data.reprocesQueueNext();
        messageId = this.getDelegateReprocesQueue().redirectRequest(PrepaidTopupRoute10.PENDING_EMISSION_REQ,data);
        break;
      }
      case REVERSE_TOPUP: {
        log.info(String.format("Reinject %s ",reprocesQueue.getIdQueue()));
        Queue qResp = CamelFactory.getInstance().createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_RESP);
        ExchangeData<PrepaidReverseData10> data = (ExchangeData<PrepaidReverseData10>)  CamelFactory.getInstance().createJMSMessenger().getMessage(qResp, reprocesQueue.getIdQueue());
        log.debug("Data not null "+data);
        if(data == null) {
          throw new ValidationException(ERROR_DATA_NOT_FOUND);
        }
        data.setRetryCount(0);
        data.reprocesQueueNext();
        messageId = this.getDelegateReprocesQueue().redirectRequestReverse(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_REQ, data);
        break;
      }
      case REVERSE_WITHDRAWAL: {
        Queue qResp = CamelFactory.getInstance().createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_RESP);
        ExchangeData<PrepaidReverseData10> data = (ExchangeData<PrepaidReverseData10>)  CamelFactory.getInstance().createJMSMessenger().getMessage(qResp, reprocesQueue.getIdQueue());
        if(data == null) {
          throw new ValidationException(ERROR_DATA_NOT_FOUND);
        }
        PrepaidMovement10  prepaidMovement10 =getPrepaidMovementEJB11().getPrepaidMovementById(data.getData().getPrepaidMovementReverse().getId());
        if(!ReconciliationStatusType.PENDING.equals(prepaidMovement10.getConTecnocom())&&!ReconciliationStatusType.PENDING.equals(prepaidMovement10.getConSwitch())){
          messageId = "";
          break;
        }
        data.setRetryCount(0);
        data.reprocesQueueNext();
        messageId = this.getDelegateReprocesQueue().redirectRequestReverse(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_REQ, data);
        break;
      }
      case ISSUANCE_FEE:{
        log.info(String.format("Reinject %s ",reprocesQueue.getIdQueue()));
        Queue qResp = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
        ExchangeData<PrepaidTopupData10> data = (ExchangeData<PrepaidTopupData10>)  CamelFactory.getInstance().createJMSMessenger().getMessage(qResp, reprocesQueue.getIdQueue());
        if(data == null) {
          throw new ValidationException(ERROR_DATA_NOT_FOUND);
        }
        PrepaidMovement10  prepaidMovement10 =getPrepaidMovementEJB11().getPrepaidMovementById(data.getData().getPrepaidMovement10().getId());
        if(!ReconciliationStatusType.PENDING.equals(prepaidMovement10.getConTecnocom())&&!ReconciliationStatusType.PENDING.equals(prepaidMovement10.getConSwitch())){
          messageId = "";
          break;
        }
        data.setRetryCount(0);
        data.reprocesQueueNext();
        messageId = this.getDelegateReprocesQueue().redirectRequest(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ, data);
        break;
      }
    }
    return messageId;
  }

  public void processRefundMovement(Long userPrepagoId, Long movementId) throws Exception {

    PrepaidUserEJBBean10 prepaidUserEJBBean10 = getPrepaidUserEJB10();
    PrepaidUser10 prepaidUserTest = prepaidUserEJBBean10.getPrepaidUserById(null, userPrepagoId);
    if (prepaidUserTest == null) {
      log.error("Error on processRefundMovement: prepaid user not found by using userPrepagoId: " + userPrepagoId);
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    PrepaidMovement10 prepaidMovement = getPrepaidMovementEJB11().getPrepaidMovementByIdPrepaidUserAndIdMovement(userPrepagoId, movementId);
    if (prepaidMovement == null) {
      log.error("Error on processRefundMovement: prepaid movement not found by using userPrepagoId:" + userPrepagoId + " & movementId:" + movementId);
      throw new NotFoundException(TRANSACCION_ERROR_GENERICO_$VALUE);
    }

    if (!BusinessStatusType.TO_REFUND.equals(prepaidMovement.getEstadoNegocio())) {
      log.error("Error on processRefundMovement: prepaid movement is not set to refund");
      throw new NotFoundException(TRANSACCION_ERROR_GENERICO_$VALUE);
    }

    getPrepaidMovementEJB11().updatePrepaidBusinessStatus(null, prepaidMovement.getId(), BusinessStatusType.REFUND_OK);

    List<CdtTransaction10> transaction10s = getCdtEJB10().buscaListaMovimientoByIdExterno(null, prepaidMovement.getIdTxExterno());

    if (!transaction10s.isEmpty()) {

      CdtTransaction10 cdtTransaction10 = transaction10s.stream().filter(t ->
        CdtTransactionType.REVERSA_CARGA.equals(t.getTransactionType()) ||
          CdtTransactionType.REVERSA_PRIMERA_CARGA.equals(t.getTransactionType())
      ).findFirst().orElse(null);

      if (cdtTransaction10 != null) {
        cdtTransaction10.setTransactionType(cdtTransaction10.getCdtTransactionTypeConfirm());
        cdtTransaction10.setIndSimulacion(Boolean.FALSE);
        cdtTransaction10.setTransactionReference(cdtTransaction10.getId());
        cdtTransaction10 = getCdtEJB10().addCdtTransaction(null, cdtTransaction10);

        //TODO: Se comenta por que ya no se enviara mails. Quese hace? se publica un evento? Verificar con negocio
        //this.getMailDelegate().sendTopupRefundCompleteMail(getUserClient().getUserById(null, prepaidUserTest.getUserIdMc()), prepaidMovement);
      }
    }
  }

  private Boolean validateBase64(String base64String){

    Base64.Decoder decoder = Base64.getDecoder();
    try {
      decoder.decode(base64String);
      return Boolean.TRUE;
    } catch(IllegalArgumentException iae) {
      return Boolean.FALSE;
    }
  }


  public PrepaidBalance10 getAccountBalance(Map<String, Object> headers, String userUuid, String accountUuid) throws Exception {
    if(StringUtils.isAllBlank(userUuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userUuid"));
    }
    if(StringUtils.isAllBlank(accountUuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountUuid"));
    }

    PrepaidUser10 prepaidUser = getPrepaidUserEJB10().findByExtId(headers, userUuid);
    if(prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    Account account = getAccountEJBBean10().findByUuid(accountUuid);
    if(account == null || !account.getUserId().equals(prepaidUser.getId())) {
      throw new ValidationException(SALDO_NO_DISPONIBLE_$VALUE);
    }

    return this.accountEJBBean10.getBalance(headers, prepaidUser, account);
  }



  public String fakeDatosTarjeta(String contrato) {

    log.info("[fakeDatosTarjeta] Obeteniendo datos de tarjeta [Camila]");
    String response = "";
    try {
      DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(contrato);

      if(datosTarjetaDTO.isRetornoExitoso()) {
        response = String.format("datos de tarjeta ok [%s] [%s]", Utils.replacePan(datosTarjetaDTO.getPan()), datosTarjetaDTO.getProducto());
        log.info(String.format("[fakeDatosTarjeta] %s", response));
      } else {
        response = String.format("Error al obtener datos de tarjeta [%s] [%s]", datosTarjetaDTO.getRetorno(), datosTarjetaDTO.getDescRetorno());
        log.error(String.format("[fakeDatosTarjeta] %s", response));
      }
      return response;
    } catch(Exception ex) {
      log.error("[fakeDatosTarjeta] Exception datos tarjeta", ex);
      log.info("[fakeDatosTarjeta] stackTrace init");
      ex.printStackTrace();
      log.info("[fakeDatosTarjeta] stackTrace finish");
      return ex.getMessage();
    }
  }

  public String fakeDatosPersona(String contrato, String rut) {

    log.info("[fakeDatosPersona] Obeteniendo datos de persona");
    String response = "";
    try {
      ConsultaDatosPersonaDTO datospersonaDTO = getTecnocomService().consultaDatosPersona(contrato, rut, TipoDocumento.RUT);

      if(datospersonaDTO.isRetornoExitoso()) {
        response = String.format("datos de persona ok [%s] [%s]", datospersonaDTO.getIdCliente(), datospersonaDTO.getTipper());
        log.info(String.format("[fakeDatosPersona] %s", response));
      } else {
        response = String.format("Error al obtener datos de persona [%s] [%s]", datospersonaDTO.getRetorno(), datospersonaDTO.getDescRetorno());
        log.error(String.format("[fakeDatosPersona] %s", response));
      }
      return response;
    } catch(Exception ex) {
      log.error("[fakeDatosPersona] Exception datos persona", ex);
      log.info("[fakeDatosPersona] stackTrace init");
      ex.printStackTrace();
      log.info("[fakeDatosPersona] stackTrace finish");
      return ex.getMessage();
    }
  }

}
