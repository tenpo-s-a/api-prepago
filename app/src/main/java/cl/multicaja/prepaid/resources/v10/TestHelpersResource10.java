package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.*;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_EXISTE;

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



}
