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
import cl.multicaja.prepaid.async.v10.BackofficeDelegate10;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.external.freshdesk.model.NewTicket;
import cl.multicaja.prepaid.external.freshdesk.model.Ticket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.prepaid.utils.ParametersUtil;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Base64Utils;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
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
import static cl.multicaja.prepaid.model.v10.PrepaidMovementStatus.REJECTED;
import static cl.multicaja.prepaid.model.v10.PrepaidMovementType.TOPUP;

/**
 * @author vutreras
 */
//TODO: se seguira utilizando esta clase?. Esta clase se utilizaba cuando se tenian los tests en la WEB.
// De ser necesario, revisar que metodos se deben dejar.

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
  private BackofficeEJBBean10 backofficeEJBBEan10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @EJB
  private AccountEJBBean10 accountEJBBean10;

  @Inject
  private ClearingFileDelegate10 clearingFileDelegate;

  @Inject
  private BackofficeDelegate10 backofficeDelegate10;

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

 	private void validate() {
    if (ConfigUtils.isEnvProduction()) {
      throw new SecurityException("Este metodo no puede ser ejecutado en un ambiente de produccion");
    }
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

  @GET
  @Path("/generate_e06_report")
  public Response generateE06ReportFile(@Context HttpHeaders headers) throws Exception {
    Map<String, Object> response = new HashMap<>();
    try{
      validate();

      File file = backofficeEJBBEan10.generateE06Report(ZonedDateTime.now());

      response.put("file_name", file.getName());
      response.put("file_exists", Files.exists(Paths.get("report_e06/" + file.getName())));

      FileInputStream attachmentFile = new FileInputStream(file);
      String fileToSend = Base64Utils.encodeToString(IOUtils.toByteArray(attachmentFile));
      attachmentFile.close();

      Map<String, Object> templateData = new HashMap<>();

      templateData.put("description", "Se adjunta archivo para reporte E06");

      // Enviamos el archivo al mail de reportes diarios
      /*EmailBody emailBodyToSend = new EmailBody();
      emailBodyToSend.addAttached(fileToSend, MimeType.CSV.getValue(), file.getName());
      emailBodyToSend.setTemplateData(templateData);
      emailBodyToSend.setTemplate(MailTemplates.TEMPLATE_MAIL_E06_REPORT);
      emailBodyToSend.setAddress("e06_report@multicaja.cl");
      mailPrepaidEJBBean10.sendMailAsync(null, emailBodyToSend);
       */

      //backofficeDelegate10.uploadE06ReportFile(file.getName());

      file.delete();

    } catch (Exception e) {
      e.printStackTrace();
      response.put("error", e.getMessage());
    }

    return Response.ok(response).status(202).build();
  }

  @Deprecated
  public PrepaidUser10 buildPrepaidUser10() {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(getUniqueLong());
    prepaidUser.setRut(getUniqueRutNumber());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    prepaidUser.setDocumentNumber(getUniqueRutNumber().toString());
    prepaidUser.setDocumentType(DocumentType.DNI_CL);
    prepaidUser.setName(getRandomString(10));
    prepaidUser.setLastName(getRandomString(10));
    prepaidUser.setUserLevel(PrepaidUserLevel.LEVEL_1);
    return prepaidUser;
  }

  public PrepaidUser10 buildPrepaidUserV2() {
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(getUniqueLong());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);
    prepaidUser.setDocumentNumber(getUniqueRutNumber().toString());
    prepaidUser.setDocumentType(DocumentType.DNI_CL);
    prepaidUser.setName(getRandomString(10));
    prepaidUser.setLastName(getRandomString(10));
    prepaidUser.setUserLevel(PrepaidUserLevel.LEVEL_1);
    prepaidUser.setDocumentNumber(getUniqueRutNumber().toString());
    prepaidUser.setDocumentType(DocumentType.DNI_CL);
    prepaidUser.setName(getRandomString(10));
    prepaidUser.setLastName(getRandomString(10));
    prepaidUser.setUuid(UUID.randomUUID().toString());

    return prepaidUser;
  }


  public Account buildPrepaidAccountFromTecnocom(PrepaidUser10 prepaidUser) throws Exception {
    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = TecnocomServiceHelper.getInstance().getTecnocomService().altaClientes(prepaidUser.getName(), prepaidUser.getLastName(), "",prepaidUser.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);
    Account account = new Account();
    account.setUserId(prepaidUser.getId());
    account.setProcessor(altaClienteDTO.getContrato());
    return account;
  }

  public PrepaidCard10 buildPrepaidCard10FromTecnocom(PrepaidUser10 prepaidUser,String contrato) throws Exception {

    DatosTarjetaDTO datosTarjetaDTO = TecnocomServiceHelper.getInstance().getTecnocomService().datosTarjeta(contrato);

    PrepaidCard10 prepaidCard = new PrepaidCard10();
    prepaidCard.setIdUser(prepaidUser.getId());
    prepaidCard.setProcessorUserId(contrato);
    prepaidCard.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
    prepaidCard.setEncryptedPan(encryptUtil.encrypt(datosTarjetaDTO.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setNameOnCard(prepaidUser.getName() + " " + prepaidUser.getLastName());
    prepaidCard.setProducto(datosTarjetaDTO.getProducto());
    prepaidCard.setNumeroUnico(datosTarjetaDTO.getIdentclitar());

    return prepaidCard;
  }
  public PrepaidTopup10 buildPrepaidTopup10() {

    String merchantCode = numberUtils.random(0,2) == 0 ? NewPrepaidTopup10.WEB_MERCHANT_CODE : getUniqueLong().toString();

    PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
    prepaidTopup.setRut(getUniqueRutNumber());
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


  public CdtTransaction10 buildCdtTransaction10(PrepaidUser10 user, PrepaidTopup10 prepaidTopup) throws BaseException {
    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(prepaidTopup.getAmount().getValue());
    cdtTransaction.setTransactionType(prepaidTopup.getCdtTransactionType());
    cdtTransaction.setAccountId(getConfigUtils().getProperty(APP_NAME) + "_" + user.getDocumentNumber());
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
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(prepaidTopup != null ? prepaidTopup.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(""); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(prepaidTopup != null ? prepaidTopup.getMerchantCode() : null);
    prepaidMovement.setCodact(prepaidTopup != null ? prepaidTopup.getMerchantCategory() : null);
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
  public String sendPendingTopup(PrepaidTopup10 prepaidTopup, PrepaidUser10 user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

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
  public String sendPendingEmissionCard(PrepaidTopup10 prepaidTopup, PrepaidUser10 user, PrepaidUser10 prepaidUser, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

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

  public String sendPendingCreateCard(PrepaidTopup10 prepaidTopup, PrepaidUser10 user, PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

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
    prepaidMovement.setImpdiv(BigDecimal.ZERO);
    prepaidMovement.setImpfac(reverseRequest != null ? reverseRequest.getAmount().getValue() : null);
    prepaidMovement.setCmbapli(0); // se debe actualizar despues
    prepaidMovement.setNumaut(getRandomNumericString(6)); // se debe actualizar despues con los 6 ultimos digitos de NumFacturaRef
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA); // A-Ajena
    prepaidMovement.setCodcom(reverseRequest != null ? reverseRequest.getMerchantCode() : null);
    prepaidMovement.setCodact(reverseRequest != null ? reverseRequest.getMerchantCategory() : null);
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
    prepaidMovement.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement.setOriginType(MovementOriginType.API);

    return prepaidMovement;
  }

  public String sendPendingTopupReverse(PrepaidTopup10 prepaidTopup,PrepaidCard10 prepaidCard10,PrepaidUser10 prepaidUser10, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!CamelFactory.getInstance().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = CamelFactory.getInstance().createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_REQ);
    prepaidTopup.setMessageId(messageId);
    //se crea la el objeto con los datos del proceso PrepaidTopup10 , PrepaidMovement10 prepaidMovementReverse
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidTopup,prepaidCard10,prepaidUser10, prepaidMovement);

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

   /*
   * Envia un mensaje directo al proceso PENDING_REVERSAL_WITHDRAW_REQ
   *
   * @param prepaidWithdraw
   * @param user
   * @param reverse
   * @param retryCount
   * @return
   */
  public String sendPendingWithdrawReversal(PrepaidWithdraw10 prepaidWithdraw, PrepaidUser10 prepaidUser, PrepaidMovement10 reverse, int retryCount) {

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

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    CamelFactory.getInstance().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public Account createRandomAccount(PrepaidUser10 prepaidUser) throws Exception {
    return accountEJBBean10.insertAccount(prepaidUser.getId(),getRandomNumericString(20));
  }

  public PrepaidCard10 buildPrepaidCard10(PrepaidUser10 prepaidUser,Account account) throws Exception {
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
    prepaidCard.setAccountId(account.getId());

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
  public String sendPendingCardIssuanceFee(PrepaidUser10 user, PrepaidTopup10 prepaidTopup, PrepaidMovement10 prepaidMovement, PrepaidCard10 prepaidCard, Integer retryCount) {

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


    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    // Crea cuenta
    Account account = buildPrepaidAccountFromTecnocom(prepaidUser);
    account = accountEJBBean10.insertAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(prepaidUser,account.getAccountNumber());

    prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    //Se setea para que de error de conexion!
    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    sendPendingTopup(prepaidTopup, prepaidUser, cdtTransaction, prepaidMovement, 2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectAltaCliente() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingEmissionCard(prepaidTopup, prepaidUser, prepaidUser, cdtTransaction, prepaidMovement,2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectCreateCard() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    PrepaidUser10 prepaidUser = buildPrepaidUserV2();
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement = prepaidMovementEJBBean10.addPrepaidMovement(null, prepaidMovement);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = tc.getTecnocomService().altaClientes(prepaidUser.getName(), prepaidUser.getLastName(), "", prepaidUser.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);
    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setProcessorUserId(altaClienteDTO.getContrato());
    prepaidCard10.setIdUser(prepaidUser.getId());
    prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard10 = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard10);

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1010);

    String messageId = sendPendingCreateCard(prepaidTopup, prepaidUser, prepaidUser, prepaidCard10, cdtTransaction, prepaidMovement, 2);
    System.out.println("TICKET CREADO");
  }

  public void testReinjectTopupReverse() throws Exception{
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    // Crea cuenta
    Account account = buildPrepaidAccountFromTecnocom(prepaidUser);
    account = accountEJBBean10.insertAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(prepaidUser,account.getAccountNumber());

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);

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

    //TODO: REVISAR ESTO
    //String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,2);
    System.out.println("TICKET CREADO");
  }

  //TODO: Revisar esto.
  public void testReinjectWithdrawReversal() throws Exception {
    /*TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

    // Crea cuenta
    Account account = buildPrepaidAccountFromTecnocom(prepaidUser);
    account = accountEJBBean10.insertAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(prepaidUser,account.getAccountNumber());

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
    //TODO: Revisar
    //String messageId = sendPendingWithdrawReversal(withdraw10, user,prepaidUser, reverse, 2);
    System.out.println("TICKET CREADO");
     */
  }

  public void testReinjectIssuanceFee() throws Exception {
    TecnocomServiceHelper tc = TecnocomServiceHelper.getInstance();

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);


    PrepaidUser10 prepaidUser = buildPrepaidUser10();
    prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);
    log.info("prepaidUser: " + prepaidUser);

    Account account = createRandomAccount(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser,account);

    TipoAlta tipoAlta = prepaidUser.getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
    AltaClienteDTO altaClienteDTO = tc.getTecnocomService().altaClientes(prepaidUser.getName(), prepaidUser.getLastName(), "", prepaidUser.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);
    prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());

    DatosTarjetaDTO datosTarjetaDTO = tc.getTecnocomService().datosTarjeta(prepaidCard.getProcessorUserId());
    prepaidCard.setPan(datosTarjetaDTO.getPan());
    prepaidCard.setExpiration(datosTarjetaDTO.getFeccadtar());
    prepaidCard.setEncryptedPan(EncryptUtil.getInstance().encrypt(prepaidCard.getPan()));
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);

    prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);
    log.info("prepaidCard: " + prepaidCard);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

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
    String messageId = sendPendingCardIssuanceFee(prepaidUser, prepaidTopup, prepaidMovement, prepaidCard, 2);
    log.info("TICKET CREADO");
  }


  @POST
  @Path("/{user_prepago_id}/transactions/{movement_id}/refund")
  public Response testRefundMovementWithMovementId(@PathParam("user_prepago_id") Long userPrepagoId, @PathParam("movement_id") Long movementId,
                                                   @Context HttpHeaders headers) throws Exception {
    try{
      this.prepaidEJBBean10.processRefundMovement(userPrepagoId,movementId);
       return Response.accepted().build();
    }catch (Exception ex) {
      log.error("Error processing refund for movement: "+movementId, ex);
      return Response.ok(ex).status(410).build();
    }
  }

  @POST
  @Path("/transactions/prepare_to_refund")
  public Response prepareToRefund(@Context HttpHeaders headers) throws Exception {

    Response returnResponse = null;


    try{

      PrepaidUser10 prepaidUser = buildPrepaidUser10();
      prepaidUser = prepaidUserEJBBean10.createPrepaidUser(null, prepaidUser);

      Account account = createRandomAccount(prepaidUser);

      PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser,account);

      prepaidCard = prepaidCardEJBBean10.createPrepaidCard(null, prepaidCard);

      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
      prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
      prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

      CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidTopup);
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
      template = TemplateUtils.freshDeskTemplateDevolucion(template, String.format("%s %s", prepaidUser.getName(), prepaidUser.getLastName()), prepaidUser.getDocumentNumber(), prepaidUser.getId(), "8888", 200000L, getUniqueEmail(), getUniqueLong());


      NewTicket newTicket = new NewTicket();
      //newTicket.setRequesterId(prepaidUser.getId().longValue());
      newTicket.setGroupId(GroupId.OPERACIONES);
      //newTicket.setUniqueExternalId(prepaidUser.getDocumentNumber());
      newTicket.setUniqueExternalId(prepaidUser.getUuid());
      newTicket.setType(TicketType.DEVOLUCION.getValue());
      newTicket.setSubject(String.format("%s - %s %s",
        TicketType.DEVOLUCION.getValue(), prepaidUser.getName(), prepaidUser.getLastName()));
      newTicket.setDescription(template);
      newTicket.setStatus(Long.valueOf(StatusType.OPEN.getValue()));
      newTicket.setPriority(Long.valueOf(PriorityType.URGENT.getValue()));
      newTicket.setProductId(43000001595L);
      newTicket.addCustomField("cf_id_movimiento", prepaidMovement.getId().toString());

      Ticket ticket = FreshdeskServiceHelper.getInstance().getFreshdeskService().createTicket(newTicket);
      if (ticket != null && ticket.getId() != null) {
        log.info("[prepareToRefund][Ticket_Success][ticketId]:"+ticket.getId());
      }else{
        log.info("[prepareToRefund][Ticket_Fail][ticketData]:"+newTicket.toString());
      }


      returnResponse = Response.ok(newTicket).status(201).build();

    }catch (Exception ex) {
      log.error("Error processing prepare_to_refund");
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }

    return returnResponse;

  }


  @POST
  @Path("/kafka_event")
  public Response sendKafkaEvent(Map<String, Object> body, @Context HttpHeaders headers) throws Exception {

    try {
      kafkaEventDelegate10.publishTestEvent(body.get("message").toString());
      return Response.noContent().build();
    } catch (Exception ex) {
      return Response.ok(ex).status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

  }

}
