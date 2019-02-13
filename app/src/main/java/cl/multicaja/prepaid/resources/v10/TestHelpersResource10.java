package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.accounting.async.v10.ClearingFileDelegate10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.accounting.model.v10.AccountingFiles10;
import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.*;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.test.TestBase.*;
import static cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10.APP_NAME;
import static cl.multicaja.prepaid.ejb.v10.PrepaidBaseEJBBean10.getConfigUtils;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.getParametersUtil;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_NOTIFICATION_CALLBACK_TECNOCOM;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementStatus.REJECTED;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementType.TOPUP;

/**
 * @author vutreras
 */
@Path("/1.0/prepaid_testhelpers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class TestHelpersResource10 extends BaseResource {

  public Log log = LogFactory.getLog(TestHelpersResource10.class);

  private NumberUtils numberUtils = NumberUtils.getInstance();

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @Inject
  private ClearingFileDelegate10 clearingFileDelegate;

  private UserClient userClient;

	private void validate() {
    if (ConfigUtils.isEnvProduction()) {
      throw new SecurityException("Este metodo no puede ser ejecutado en un ambiente de produccion");
    }
  }

  public UserClient getUserClient() {
	  if(userClient == null) {
      userClient = UserClient.getInstance();
    }
    return userClient;
  }

  private void jdbcExecute(JdbcTemplate jdbcTemplate, String sql) {
	  log.info("Ejecutando sql: " + sql);
    jdbcTemplate.execute(sql);
  }

  @POST
  @Path("/prepaiduser/reset")
  public Response usersReset(Map<String, Object> body, @Context HttpHeaders headers) throws Exception {

    validate();

    String min = String.valueOf(body.get("min"));
    String max = String.valueOf(body.get("max"));

    if (StringUtils.isBlank(min)) {
      min = "0";
    }

    if (StringUtils.isBlank(max)) {
      max = String.valueOf(Long.MAX_VALUE);
    }

    log.info(String.format("Borrando todos los datos de usuarios con rut entre %s y %s", min, max));

    List<String> lstAccountsCdt = null;

    //se borran usuarios en api-prepaid
    {
      String schema = prepaidEJBBean10.getSchema();
      JdbcTemplate jdbcTemplate = prepaidEJBBean10.getDbUtils().getJdbcTemplate();

      String subQuery = String.format("select id from %s.prp_usuario where rut >= %s AND rut <= %s", schema, min, max);

      jdbcExecute(jdbcTemplate, String.format("delete from %s.prp_tarjeta where id_usuario in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.prp_movimiento where id_usuario in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.prp_usuario where id in (%s);", schema, subQuery));
    }

    //se borran datos en cdt
    if (lstAccountsCdt != null && !lstAccountsCdt.isEmpty()) {

      String schema = cdtEJBBean10.getSchema();
      JdbcTemplate jdbcTemplate = cdtEJBBean10.getDbUtils().getJdbcTemplate();

      String subQuery = String.format("select id from %s.cdt_cuenta where id_externo in (%s)", schema, StringUtils.join(lstAccountsCdt, ","));

      jdbcExecute(jdbcTemplate, String.format("delete from %s.cdt_cuenta_acumulador where id_cuenta in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.cdt_movimiento_cuenta where id_cuenta in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.cdt_cuenta where id in (%s);", schema, subQuery));
    }

    log.info("Borrado exitoso de datos de usuarios");

    return Response.status(200).build();
  }

  @POST
  @Path("/prepaiduser")
  public Response createPrepaidUser(User user, @Context HttpHeaders headers) throws Exception {

    validate();

    NameStatus initialNameStatus = user.getNameStatus();
    UserIdentityStatus initialIdentityStatus = user.getIdentityStatus();
    RutStatus initialRutStatus = user.getRut() != null ? user.getRut().getStatus() : null;

    Map<String, Object> mapHeaders = headersToMap(headers);

    if (user.getId() != null) {
      user = getUserClient().getUserById(mapHeaders, user.getId());
    } else {
      SignUp signUp = getUserClient().signUp(mapHeaders, new SignUPNew(user.getEmail().getValue(),user.getRut().getValue()));
      user = getUserClient().getUserById(mapHeaders, signUp.getUserId());
    }

    if (user == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    if (StringUtils.isBlank(user.getName())) {
      user.setName(null);
    }
    if (StringUtils.isBlank(user.getLastname_1())) {
      user.setLastname_1(null);
    }
    if (StringUtils.isBlank(user.getLastname_2())) {
      user.setLastname_2(null);
    }

    user.setNameStatus(initialNameStatus == null ? NameStatus.VERIFIED : initialNameStatus);
    user.setIdentityStatus(initialIdentityStatus ==  null ? UserIdentityStatus.NORMAL : initialIdentityStatus);
    user.getRut().setStatus(initialRutStatus == null ? RutStatus.VERIFIED : initialRutStatus);

    user.setGlobalStatus(UserStatus.ENABLED);
    user.getEmail().setStatus(EmailStatus.VERIFIED);
    user.getCellphone().setStatus(CellphoneStatus.VERIFIED);
    user.setPassword(String.valueOf(1357));

    user = getUserClient().fillUser(null,user);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(user.getId());
    prepaidUser.setRut(user.getRut().getValue());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);

    prepaidUserEJBBean10.createPrepaidUser(mapHeaders, prepaidUser);

    return Response.ok(user).status(200).build();
  }

  @POST
  @Path("/{userId}/randomPurchase")
	public Response simulatePurchaseForUser(@PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {

	  validate();

    Map<String, Object> mapHeaders = headersToMap(headers);
    PrepaidUser10 prepaidUser = prepaidUserEJBBean10.getPrepaidUserByUserIdMc(mapHeaders, userId);
    if (prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    PrepaidCard10 prepaidCard10 = prepaidCardEJBBean10.getLastPrepaidCardByUserId(mapHeaders, prepaidUser.getId());
    if (prepaidCard10 == null) {
      throw new NotFoundException(ERROR_DATA_NOT_FOUND);
    }

    TecnocomService tecnocomService = TecnocomServiceHelper.getInstance().getTecnocomService();
    ConsultaSaldoDTO consultaSaldoDTO = tecnocomService.consultaSaldo(prepaidCard10.getProcessorUserId(), prepaidUser.getRut().toString(), TipoDocumento.RUT);

    // Hacer un gasto aleatorio del saldo disponible
    BigDecimal saldoDisponible = consultaSaldoDTO.getSaldisconp();
    BigDecimal gastoAleatorio = new BigDecimal(Math.random()).multiply(saldoDisponible).setScale(0, BigDecimal.ROUND_HALF_UP);
    gastoAleatorio = gastoAleatorio.divide(new BigDecimal(1.025), 0, RoundingMode.HALF_UP); // Dejar espacio para el fee internacional

    // Crear movimiento de compra
    String numreffac = getRandomNumericString(10);
    String numaut = TecnocomServiceHelper.getNumautFromIdMov(numreffac);

    String[] nomcomreds = {"Uber", "Spotify", "Netflix", "Twitch", "AmazonTV"};
    int selectedNomcomRed = RandomUtils.nextInt(0, nomcomreds.length - 1);

    // Agregar compra
    InclusionMovimientosDTO inclusionMovimientosDTO = tecnocomService.inclusionMovimientos(prepaidCard10.getProcessorUserId(), prepaidCard10.getPan(), CodigoMoneda.CHILE_CLP, IndicadorNormalCorrector.NORMAL, TipoFactura.COMPRA_INTERNACIONAL, numreffac, gastoAleatorio, numaut, nomcomreds[selectedNomcomRed], nomcomreds[selectedNomcomRed], 123, CodigoMoneda.CHILE_CLP, gastoAleatorio);
    if (!inclusionMovimientosDTO.isRetornoExitoso()) {
      log.error("* Compra rechazada por Tecnocom * Error: " + inclusionMovimientosDTO.getRetorno());
      log.error(inclusionMovimientosDTO.getDescRetorno());
      throw new RunTimeValidationException(TARJETA_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", inclusionMovimientosDTO.getDescRetorno()));
    }

    return Response.ok(gastoAleatorio).status(201).build();
  }

  @POST
  @Path("/{userId}/randomAuthorization")
  public Response simulateAuthorizationForUser(@PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {

    validate();

    String codcom = "codcom";
    Integer codact = 123;
    String nomcomred = "nomcomred";
    BigDecimal impfac = BigDecimal.valueOf(3000);
    BigDecimal impliq = BigDecimal.valueOf(3000);

    Map<String, Object> mapHeaders = headersToMap(headers);

    PrepaidUser10 prepaidUser = prepaidUserEJBBean10.getPrepaidUserByUserIdMc(mapHeaders, userId);
    if (prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    PrepaidCard10 prepaidCard10 = prepaidCardEJBBean10.getLastPrepaidCardByUserId(mapHeaders, prepaidUser.getId());
    if (prepaidCard10 == null) {
      throw new NotFoundException(ERROR_DATA_NOT_FOUND);
    }

    TecnocomService tecnocomService = TecnocomServiceHelper.getInstance().getTecnocomService();

    String numreffac = getRandomNumericString(10);
    String numaut = TecnocomServiceHelper.getNumautFromIdMov(numreffac);

    //add authorization
    InclusionMovimientosDTO inclusionAutorizacionesDTO = tecnocomService.inclusionAutorizaciones(
      prepaidCard10.getProcessorUserId(),
      prepaidCard10.getPan(),
      CodigoMoneda.CHILE_CLP,
      IndicadorNormalCorrector.NORMAL,
      TipoFactura.COMPRA_INTERNACIONAL,
      numreffac,
      impfac,
      numaut,
      codcom,
      nomcomred,
      codact,
      CodigoMoneda.CHILE_CLP,
      impliq
    );

    if (!inclusionAutorizacionesDTO.isRetornoExitoso()) {
      log.error("* Compra autorizada rechazada por Tecnocom * Error: " + inclusionAutorizacionesDTO.getRetorno());
      log.error(inclusionAutorizacionesDTO.getDescRetorno());
      return Response.ok(impliq).status(400).build();
    }else{
      System.out.println("Compra autorizada aprobada por Tecnocom");
      return Response.ok(impliq).status(200).build();
    }

  }
    
  @GET
  @Path("/{userId}/authorizations")
  public Response getAuthorizationByPrepaidUserId(@PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {

    Map<String, Object> mapHeaders = headersToMap(headers);

    PrepaidUser10 prepaidUser = prepaidUserEJBBean10.getPrepaidUserByUserIdMc(mapHeaders, userId);
    if (prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    PrepaidCard10 prepaidCard10 = prepaidCardEJBBean10.getLastPrepaidCardByUserId(mapHeaders, prepaidUser.getId());
    if (prepaidCard10 == null) {
      throw new NotFoundException(ERROR_DATA_NOT_FOUND);
    }

    String contrato = prepaidCard10.getProcessorUserId();

    TecnocomService tecnocomService = TecnocomServiceHelper.getInstance().getTecnocomService();

    ConsultaAutorizacionesDTO autorizacionesDTO = tecnocomService.consultaAutorizaciones(
      contrato,null,new Date(),new Date());

    Iterator<AutorizacionesDTO> iterator = autorizacionesDTO.getListAutorizacionesDTOS().iterator();
    BigDecimal impTrn = null;
    while (iterator.hasNext()) {
      if(iterator.hasNext()){
        impTrn = iterator.next().getImptrn();
      }
    }

    return Response.ok(impTrn).status(200).build();
  }


  @POST
  @Path("/simulateTecnocomError")
  public Response simulateTecnocomError(ReprocesQueue reprocesQueue, @Context HttpHeaders headers) throws Exception {
    log.info(String.format("** LLamada a simulate a TecnocomErrorTopup**"));

    validate();

    // Por parametro el nombre de la cola
    String queueName = reprocesQueue != null ? reprocesQueue.getLastQueue().getValue() : "[Error: No Queue In Call]";
    String repeatFail = reprocesQueue != null && reprocesQueue.getIdQueue() != null ? reprocesQueue.getIdQueue() : "[no repeat fail]";
    log.info(String.format("** En la cola: %s/%s **", queueName, repeatFail));

    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    QueuesNameType queuesNameType = reprocesQueue.getLastQueue();
    switch (queuesNameType) {
      case TOPUP:
        testReinjectTopup();
        break;
      case PENDING_EMISSION:
        testReinjectAltaCliente();
        break;
      case CREATE_CARD:
        testReinjectCreateCard();
        break;
      case SEND_MAIL:
        testReinjectSendMailCard();
        break;
      case REVERSE_TOPUP:
        testReinjectTopupReverse();
        break;
      case REVERSE_WITHDRAWAL:
        testReinjectWithdrawReversal();
        break;
      case ISSUANCE_FEE:
        testReinjectIssuanceFee();
        break;
    }

    Thread.sleep(3000);
    log.info("** Ticket Creado **");

    if (!repeatFail.equals("fail")) {
      // Se setea para que no de error de conexion!
      tc.getTecnocomService().setAutomaticError(false);
      tc.getTecnocomService().setRetorno(null);
    }

    return Response.ok("OK").status(201).build();
  }


  @POST
  @Path("/generate_clearing_file")
  public Response generateClearingFile(@Context HttpHeaders headers) throws Exception {
    Map<String, Object> response = new HashMap<>();
	  try{
      validate();

      AccountingFiles10 file = prepaidClearingEJBBean10.generateClearingFile(headersToMap(headers), ZonedDateTime.now());

      //clearingFileDelegate.uploadFile(file.getName());


      response.put("file_name", file.getName());
      response.put("file_exists", Files.exists(Paths.get("clearing_files/" + file.getName())));
    } catch (Exception e) {
	    e.printStackTrace();
	    response.put("error", e.getMessage());
    }


    return Response.ok(response).status(202).build();
  }

  public User registerUser(String password, UserStatus status, UserIdentityStatus identityStatus) throws Exception {
    Integer rut = getUniqueRutNumber();
    String email = getUniqueEmail();
    SignUp signUp = getUserClient().signUp(null, new SignUPNew(email, rut));
    User user = getUserClient().getUserById(null, signUp.getUserId());
    user.setName(null);
    user.setLastname_1(null);
    user.setLastname_2(null);
    user = getUserClient().fillUser(null, user);
    user.setGlobalStatus(status);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.getEmail().setStatus(EmailStatus.VERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);
    user.setIdentityStatus(identityStatus);
    user.setPassword(password);
    user = getUserClient().updateUser(null, user.getId(), user);
    return user;
  }

  public PrepaidUser10 buildPrepaidUser10(User user) {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(user != null ? user.getId() : null);
    prepaidUser.setRut(user != null ? user.getRut().getValue() : null);
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    return prepaidUser;
  }

  public PrepaidCard10 buildPrepaidCard10FromTecnocom(User user, PrepaidUser10 prepaidUser) throws Exception {

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = TecnocomServiceHelper.getInstance().getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    DatosTarjetaDTO datosTarjetaDTO = TecnocomServiceHelper.getInstance().getTecnocomService().datosTarjeta(altaClienteDTO.getContrato());

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser.getId());
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
    prepaidCard.setEncryptedPan(encryptUtil.encrypt(datosTarjetaDTO.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setNameOnCard(user.getName() + " " + user.getLastname_1());
    prepaidCard.setProducto(datosTarjetaDTO.getProducto());
    prepaidCard.setNumeroUnico(datosTarjetaDTO.getIdentclitar());

    return prepaidCard;
  }

  public PrepaidTopup10 buildPrepaidTopup10(User user) {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setRut(user != null ? user.getRut().getValue() : null);
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(3000));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidTopup.setAmount(newAmountAndCurrency);
    prepaidTopup.setTotal(newAmountAndCurrency);
    prepaidTopup.setMerchantCategory(1);
    prepaidTopup.setMerchantName(getRandomString(6));

    return prepaidTopup;
  }

  public CdtTransaction10 buildCdtTransaction10(User user, PrepaidTopup10 prepaidTopup) throws BaseException {
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + user.getRut().getValue());
    cdtTransaction.setGloss(prepaidTopup.getCdtTransactionType().getName()+" "+prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(prepaidTopup.getTransactionId());
    cdtTransaction.setIndSimulacion(false);
    return cdtTransaction;
  }

  public CdtTransaction10 createCdtTransaction10(CdtTransaction10 cdtTransaction) throws Exception {

    cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

    // Si no cumple con los limites
    if(!cdtTransaction.isNumErrorOk()){
      int lNumError = cdtTransaction.getNumErrorInt();
      if(lNumError != -1 && lNumError > 10000) {
        throw new ValidationException(LIMITES_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      } else {
        throw new ValidationException(LIMITES_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
      }
    }

    return cdtTransaction;
  }

  public PrepaidMovement10 buildPrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 prepaidTopup, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovementType type) {

    String codent = null;
    try {
      codent = ParametersUtil.getInstance().getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura;
    if(PrepaidMovementType.TOPUP.equals(type)) {
      tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
    } else {
      tipoFactura = TipoFactura.RETIRO_TRANSFERENCIA;
    }

    if (prepaidTopup != null) {
      if (TransactionOriginType.POS.equals(prepaidTopup.getTransactionOriginType())) {
        if (PrepaidMovementType.TOPUP.equals(type)) {
          tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
        } else {
          tipoFactura = TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
        }
      }
    }

    String centalta = "";
    String cuenta = "";
    if(prepaidCard != null && !StringUtils.isBlank(prepaidCard.getProcessorUserId())) {
      centalta = prepaidCard.getProcessorUserId().substring(4, 8);
      cuenta = prepaidCard.getProcessorUserId().substring(12);
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(cdtTransaction != null ? cdtTransaction.getTransactionReference() : getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(cdtTransaction != null ? cdtTransaction.getExternalTransactionId() : getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(centalta); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(cuenta); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
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
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);

    return prepaidMovement;
  }

  /**
   * Envia un mensaje directo al proceso PENDING_TOPUP_REQ
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @param retryCount
   * @return
   */
  public String sendPendingTopup(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);

    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);


    //se envia el mensaje a la cola
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /******
   * Envia un mensaje directo al proceso PENDING_EMISSION_REQ
   * @param user
   * @return
   */
  public String sendPendingEmissionCard(PrepaidTopup10 prepaidTopup, User user, PrepaidUser10 prepaidUser, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_REQ);
    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }
    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);


    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
    req.getData().setPrepaidUser10(prepaidUser);

    //se envia el mensaje a la cola
    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public String sendPendingCreateCard(PrepaidTopup10 prepaidTopup, User user, PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_REQ);
    // Realiza alta en tecnocom para que el usuario exista
    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }
    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
    req.getData().setPrepaidCard10(prepaidCard);
    req.getData().setPrepaidUser10(prepaidUser);

    //se envia el mensaje a la cola
    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public String sendPendingSendMail(User user,PrepaidUser10 prepaidUser10,PrepaidCard10 prepaidCard10, PrepaidTopup10 topup, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }
    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_CARD_REQ);
    // Realiza alta en tecnocom para que el usuario exista
    if(topup != null) {
      topup.setMessageId(messageId);
    }
    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(null, user, null, null);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
    req.getData().setPrepaidCard10(prepaidCard10);
    req.getData().setPrepaidUser10(prepaidUser10);
    req.getData().setUser(user);
    req.getData().setPrepaidTopup10(topup);

    //se envia el mensaje a la cola
    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public PrepaidMovement10 buildReversePrepaidMovement10(PrepaidUser10 prepaidUser, NewPrepaidBaseTransaction10 reverseRequest, PrepaidCard10 prepaidCard, PrepaidMovementType type) {

    String codent = null;
    try {
      codent = ParametersUtil.getInstance().getString("api-prepaid", "cod_entidad", "v10");
    } catch (SQLException e) {
      codent = getConfigUtils().getProperty("tecnocom.codEntity");
    }

    TipoFactura tipoFactura;
    if(PrepaidMovementType.TOPUP.equals(type)){
      tipoFactura = TipoFactura.ANULA_CARGA_TRANSFERENCIA;
    } else {
      tipoFactura = TipoFactura.ANULA_RETIRO_TRANSFERENCIA;
    }

    if (reverseRequest != null) {
      if (TransactionOriginType.POS.equals(reverseRequest.getTransactionOriginType())) {
        if(PrepaidMovementType.TOPUP.equals(type)){
          tipoFactura = TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;
        } else {
          tipoFactura = TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;
        }
      }
    }

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(reverseRequest.getTransactionId());
    prepaidMovement.setTipoMovimiento(type);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(codent);
    prepaidMovement.setCentalta(""); //contrato (Numeros del 5 al 8) - se debe actualizar despues
    prepaidMovement.setCuenta(""); ////contrato (Numeros del 9 al 20) - se debe actualizar despues
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.CORRECTORA); //0-Normal
    prepaidMovement.setTipofac(tipoFactura);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(prepaidCard != null ? prepaidCard.getPan() : ""); // se debe actualizar despues
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(reverseRequest != null ? reverseRequest.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(getRandomNumericString(6)); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(reverseRequest != null ? reverseRequest.getMerchantCode() : null);
    prepaidMovement.setCodact(reverseRequest != null ? reverseRequest.getMerchantCategory() : null);
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
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);

    return prepaidMovement;
  }

  public String sendPendingTopupReverse(PrepaidTopup10 prepaidTopup,PrepaidCard10 prepaidCard10, User user, PrepaidUser10 prepaidUser10, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_REQ);
    prepaidTopup.setMessageId(messageId);
    //se crea la el objeto con los datos del proceso PrepaidTopup10 , User , PrepaidMovement10 prepaidMovementReverse
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidTopup,prepaidCard10, user,prepaidUser10, prepaidMovement);

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public NewPrepaidWithdraw10 buildNewPrepaidWithdraw10(User user, String password) {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(user != null ? user.getRut().getValue() : null);
    prepaidWithdraw.setMerchantCode(merchantCode);
    prepaidWithdraw.setTransactionId(getUniqueInteger().toString());

    NewAmountAndCurrency10 newAmountAndCurrency = new NewAmountAndCurrency10();
    newAmountAndCurrency.setValue(new BigDecimal(RandomUtils.nextDouble(2000,9000)));
    newAmountAndCurrency.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(newAmountAndCurrency);

    prepaidWithdraw.setMerchantCategory(1);
    prepaidWithdraw.setMerchantName(getRandomString(6));

    prepaidWithdraw.setPassword(password);

    return prepaidWithdraw;
  }

  /**
   * Envia un mensaje directo al proceso PENDING_REVERSAL_WITHDRAW_REQ
   *
   * @param prepaidWithdraw
   * @param user
   * @param reverse
   * @param retryCount
   * @return
   */
  public String sendPendingWithdrawReversal(PrepaidWithdraw10 prepaidWithdraw, User user, PrepaidUser10 prepaidUser, PrepaidMovement10 reverse, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_REQ);
    prepaidWithdraw.setMessageId(messageId);
    //se crea la el objeto con los datos del proceso
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidWithdraw, prepaidUser, reverse);
    if(user != null) {
      data.setUser(user);
    }

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public PrepaidCard10 buildPrepaidCard10(PrepaidUser10 prepaidUser) throws Exception {
    int expiryYear = numberUtils.random(1000, 9999);
    int expiryMonth = numberUtils.random(1, 99);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"));
    String pan = getRandomNumericString(16);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser != null ? prepaidUser.getId() : null);
    prepaidCard.setPan(Utils.replacePan(pan));
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(pan));
    prepaidCard.setExpiration(expiryDate);
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setProcessorUserId(getRandomNumericString(20));
    prepaidCard.setNameOnCard("Tarjeta de: " + getRandomString(5));
    prepaidCard.setProducto(getRandomNumericString(2));
    prepaidCard.setNumeroUnico(getRandomNumericString(8));
    return prepaidCard;
  }

  /**
   * Envia un mensaje directo al proceso PENDING_CARD_ISSUANCE_FEE_REQ
   *
   * @param prepaidTopup
   * @param prepaidMovement
   * @param prepaidCard
   * @return
   */
  public String sendPendingCardIssuanceFee(User user, PrepaidTopup10 prepaidTopup, PrepaidMovement10 prepaidMovement, PrepaidCard10 prepaidCard, Integer retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ);

    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, null, prepaidMovement);
    data.setPrepaidCard10(prepaidCard);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
    if (retryCount != null){
      req.setRetryCount(retryCount);
    }

    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public void testReinjectTopup() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    sendPendingTopup(prepaidTopup, user, cdtTransaction, prepaidMovement, 2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectAltaCliente() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingEmissionCard(prepaidTopup, user, prepaidUser, cdtTransaction, prepaidMovement,2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectCreateCard() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = tc.getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard10);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCreateCard(prepaidTopup, user, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectSendMailCard() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    System.out.println("User Rut: "+prepaidUser.getRut());
    System.out.println("User Mail: "+user.getEmail());

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = tc.getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);

    DatosTarjetaDTO datosTarjetaDTO = tc.getTecnocomService().datosTarjeta(prepaidCard10.getProcessorUserId());
    prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
    prepaidCard10.setEncryptedPan(encryptUtil.encrypt(datosTarjetaDTO.getPan()));
    prepaidCard10 = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard10);

    PrepaidTopup10 topup = buildPrepaidTopup10(user);
    topup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingSendMail(user,prepaidUser ,prepaidCard10, topup,2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectTopupReverse() throws Exception{
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);
    System.out.println(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser, prepaidTopup, null, PrepaidMovementType.TOPUP);
    prepaidReverseMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidReverseMovement);

    //Error TimeOut
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectWithdrawReversal() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, String.valueOf(numberUtils.random(1111,9999)));
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10, null, null, PrepaidMovementType.WITHDRAW);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw = prepaidMovementEJBBean10.addPrepaidMovement(null, originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw, null, PrepaidMovementType.WITHDRAW);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse = prepaidMovementEJBBean10.addPrepaidMovement(null, reverse);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingWithdrawReversal(withdraw10, user,prepaidUser, reverse, 2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectIssuanceFee() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);
    log.info("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = tc.getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());

    DatosTarjetaDTO datosTarjetaDTO = tc.getTecnocomService().datosTarjeta(prepaidCard.getProcessorUserId());
    prepaidCard.setPan(datosTarjetaDTO.getPan());
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);
    log.info("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.WITHDRAW);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    prepaidMovementEJBBean10.updatePrepaidMovement(null,
      prepaidMovement.getId(),
      prepaidCard.getPan(),
      prepaidCard.getProcessorUserId().substring(4, 8),
      prepaidCard.getProcessorUserId().substring(12),
      123,
      123,
      152,
      null,
      PrepaidMovementStatus.PROCESS_OK);

    log.info("prepaidMovement: " + prepaidMovement);
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
    String messageId = sendPendingCardIssuanceFee(user, prepaidTopup, prepaidMovement, prepaidCard, 2);
    log.info("TICKET CREADO");
  }


  @POST
  @Path("/{user_prepago_id}/transactions/{movement_id}/refund")
  public Response testRefundMovementWithMovementId(@PathParam("user_prepago_id") Long userPrepagoId, @PathParam("movement_id") Long movementId,
                                                   @Context HttpHeaders headers) throws Exception {
    Response returnResponse = null;
    try{
      CdtTransaction10 cdtTransaction = this.prepaidMovementEJBBean10.processRefundMovement(userPrepagoId,movementId);
      if(cdtTransaction == null){
        System.out.println("CDT_TRANSACTION_IS_NULL");
        log.error("testRefundMovementWithMovementId:CDT_TRANSACTION_IS_NULL by using userPrepagoId:"+userPrepagoId+" & movementId:"+movementId);
      }
      returnResponse = Response.ok(cdtTransaction).status(201).build();
    }catch (Exception ex) {
      log.error("Error processing refund for movement: "+movementId+" with status rejected");
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }
    return returnResponse;
  }

  @POST
  @Path("/transactions/prepare_to_refund")
  public Response prepareToRefund(@Context HttpHeaders headers) throws Exception {

    Response returnResponse = null;


    try{

      User user = registerUser(String.valueOf(numberUtils.random(1111,9999)), UserStatus.ENABLED, UserIdentityStatus.NORMAL);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

      PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);
      prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
      prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

      CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
      cdtTransaction = createCdtTransaction10(cdtTransaction);
      cdtTransaction.setIndSimulacion(Boolean.FALSE);


      PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
      prepaidMovement.setEstado(REJECTED);
      prepaidMovement.setTipoMovimiento(TOPUP);
      prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);


      // Enviar movimiento a REFUND
      prepaidMovementEJBBean10.updatePrepaidBusinessStatus(null, prepaidMovement.getId(), BusinessStatusType.TO_REFUND);

      CdtEJBBean10 cdtEJBBean10 = new CdtEJBBean10();

      // Confirmar el topup en el CDT
      cdtTransaction = cdtEJBBean10.buscaMovimientoByIdExterno(null, prepaidMovement.getIdTxExterno());

      CdtTransactionType reverseTransactionType = cdtTransaction.getCdtTransactionTypeReverse();
      cdtTransaction.setTransactionType(cdtTransaction.getCdtTransactionTypeConfirm());
      cdtTransaction.setIndSimulacion(Boolean.FALSE);
      cdtTransaction.setTransactionReference(cdtTransaction.getId());
      cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

      // Iniciar reversa en CDT
      cdtTransaction.setTransactionType(reverseTransactionType);
      cdtTransaction.setTransactionReference(0L);
      cdtTransaction = cdtEJBBean10.addCdtTransaction(null, cdtTransaction);

      String template = getParametersUtil().getString("api-prepaid", "template_ticket_devolucion", "v1.0");
      template = TemplateUtils.freshDeskTemplateDevolucion(template, String.format("%s %s", user.getName(), user.getLastname_1()), String.format("%s-%s", user.getRut().getValue(), user.getRut().getDv()), user.getId(), "8888", 200000L, user.getEmail().getValue(), user.getCellphone().getValue());


      user = getUserClient().finishSignup(headersToMap(headers),user.getId(),"Prepago");

      NewTicket newTicket = new NewTicket();
      //newTicket.setRequesterId(prepaidUser.getId().longValue());
      newTicket.setGroupId(GroupId.OPERACIONES);
      newTicket.setUniqueExternalId(user.getRut().getValue().toString());
      newTicket.setType(TicketType.DEVOLUCION);
      newTicket.setSubject(String.format("%s - %s %s",
        TicketType.DEVOLUCION.getValue(), user.getName(), user.getLastname_1()));
      newTicket.setDescription(template);
      newTicket.setStatus(StatusType.OPEN);
      newTicket.setPriority(PriorityType.URGENT);
      newTicket.setProductId(43000001595L);
      newTicket.addCustomField("cf_id_movimiento", prepaidMovement.getId().toString());

      Ticket ticket = getUserClient().createFreshdeskTicket(headersToMap(headers), user.getId(), newTicket);

      returnResponse = Response.ok(newTicket).status(201).build();

    }catch (Exception ex) {
      log.error("Error processing prepare_to_refund");
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }

    return returnResponse;

  }


  @POST
  @Path("/processor/notificationA")
  public Response callNotificationTecnocomA(NotificationTecnocom notificationTecnocom,@Context HttpHeaders headers) throws Exception {
    Response returnResponse = null;

    String textLogBase = "TestHelperResource-callNotification: ";
    try{

      String errorCode;
      String errorMessage;

      String errorCodeOnHeader = "";
      String errorMessageOnHeader = "";

      String errorCodeOnBody;
      String errorMessageOnBody;

      //Test Headers
      Map<String, Object> mapHeaders = null;
      if (headers != null) {
        mapHeaders = new HashMap<>();
        MultivaluedMap<String, String> mapHeadersTmp = headers.getRequestHeaders();
        Set<String> keys = mapHeadersTmp.keySet();
        for (String k : keys) {
          mapHeaders.put(k, mapHeadersTmp.getFirst(k));
        }
      }

      if(mapHeaders.keySet().size() == 0 || mapHeaders == null){
        errorCodeOnHeader = PARAMETRO_FALTANTE_$VALUE.getValue().toString();//"101004";
        errorMessageOnHeader = "Empty Header, must to add header params";
      }

      //Test Body
      if(notificationTecnocom != null){
        notificationTecnocom = this.prepaidEJBBean10.setNotificationCallback(mapHeaders,notificationTecnocom);

        errorCodeOnBody = notificationTecnocom.getCode() == null ?
          "001": notificationTecnocom.getCode();
        errorMessageOnBody = notificationTecnocom.getMessage() == null ?
          "Not Error, but not Accepted": notificationTecnocom.getMessage();

      }else{
        errorCodeOnBody = PARAMETRO_FALTANTE_$VALUE.getValue().toString();//"101004";
        errorMessageOnBody = "Empty Body, must to add body params";
      }
      errorCode = errorCodeOnBody;
      errorMessage = errorMessageOnBody;

      if(errorCodeOnHeader == errorCodeOnBody){
        errorCode = errorCodeOnBody;
        errorMessage = "Error Description, "+errorMessageOnHeader+" , "+errorMessageOnBody;
      }

      //Final Response
      JsonObject notifResponse = Json.createObjectBuilder().
        add("code", errorCode).
        add("message",errorMessage).build();

      if(errorCode == PARAMETRO_FALTANTE_$VALUE.getValue().toString()/*"101004"*/){
        System.out.println("ERROR_A");
        returnResponse = Response.ok(notifResponse).status(400).build();
        log.error(textLogBase+notifResponse.toString());
      }

      if(errorCode == PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue().toString()/*"101007"*/){
        System.out.println("ERROR_B");
        returnResponse = Response.ok(notifResponse).status(422).build();
        log.error(textLogBase+notifResponse.toString());
      }

      if(errorCode == "001"){
        System.out.println("ERROR_C");
        returnResponse = Response.ok(notifResponse).status(201).build();
        log.info(textLogBase+notifResponse.toString());
      }

      if(errorCode == "002"){
        System.out.println("ERROR_D");
        //Ok Service Response
        returnResponse = Response.ok(notifResponse).status(202).build();
        log.info(textLogBase+notifResponse.toString());

        //Send Async Mail
        Map<String, Object> templateData = new HashMap<String, Object>();
        templateData.put("notification_data",new ObjectMapper().writeValueAsString(notificationTecnocom));
        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(MailTemplates.TEMPLATE_MAIL_NOTIFICATION_CALLBACK_TECNOCOM);
        emailBody.setAddress("notification_tecnocom@multicaja.cl");
        //mailPrepaidEJBBean10.sendMailAsync(null,emailBody);

      }


    }catch(Exception ex){
      log.error(textLogBase+ex.toString());
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }

    return returnResponse;

  }

  @POST
  @Path("/processor/notification")
  //public Response callNotificationTecnocom(JsonObject body,@Context HttpHeaders headers) throws Exception {
  public Response callNotificationTecnocom(NotificationTecnocom notificationTecnocom,@Context HttpHeaders headers) throws Exception {
    Response returnResponse = null;

    String textLogBase = "TestHelperResource-callNotification: ";
    try{

      //Set Headers
      Map<String, Object> mapHeaders = null;
      if (headers != null) {
        mapHeaders = new HashMap<>();
        MultivaluedMap<String, String> mapHeadersTmp = headers.getRequestHeaders();
        Set<String> keys = mapHeadersTmp.keySet();
        for (String k : keys) {
          mapHeaders.put(k, mapHeadersTmp.getFirst(k));
        }
      }

      //Set Response
      JsonObject notifResponse = Json.createObjectBuilder().
        add("code", notificationTecnocom.getCode()).
        add("message",notificationTecnocom.getMessage()).build();

      System.out.println("NotificationTecnocom: "+notificationTecnocom);
      if(notificationTecnocom.getCode() == "202"){
        returnResponse = Response.ok(notifResponse).status(202).build();
      }

    }catch(Exception ex){
      log.error(textLogBase+ex.toString());
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }

    return returnResponse;

  }

}
