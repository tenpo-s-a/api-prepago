package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_EXISTE;
import static cl.multicaja.core.model.Errors.ERROR_DATA_NOT_FOUND;

/**
 * @author vutreras
 */
@Path("/1.0/prepaid_testhelpers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class TestHelpersResource10 extends BaseResource {

  private static Log log = LogFactory.getLog(TestHelpersResource10.class);

  private NumberUtils numberUtils = NumberUtils.getInstance();


  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

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

    // Crear movimiento de compra
    String numreffac = "9872348974987";
    String numaut = numreffac;
    // Los 6 primeros digitos de numreffac
    numaut = numaut.substring(numaut.length()-6);

    // Agregar compra
    InclusionMovimientosDTO inclusionMovimientosDTO = tecnocomService.inclusionMovimientos(prepaidCard10.getProcessorUserId(), prepaidCard10.getPan(), CodigoMoneda.CHILE_CLP, IndicadorNormalCorrector.NORMAL, TipoFactura.COMPRA_INTERNACIONAL, numreffac, gastoAleatorio, numaut, "codcom", "nomcomred", 123, CodigoMoneda.CHILE_CLP, gastoAleatorio);
    if (!inclusionMovimientosDTO.isRetornoExitoso()) {
      log.error("* Compra rechazada por Tecnocom * Error: " + inclusionMovimientosDTO.getRetorno());
      log.error(inclusionMovimientosDTO.getDescRetorno());
    }

    return Response.ok(gastoAleatorio).status(201).build();
  }
}
