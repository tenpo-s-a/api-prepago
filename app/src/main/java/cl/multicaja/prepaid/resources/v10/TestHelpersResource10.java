package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.users.data.ejb.v10.DataEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import cl.multicaja.users.model.v10.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.CLIENTE_BLOQUEADO_O_BORRADO;
import static cl.multicaja.core.model.Errors.CLIENTE_NO_EXISTE;

/**
 * @author vutreras
 */
@Path("/1.0/testhelpers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class TestHelpersResource10 extends BaseResource {

  private static Log log = LogFactory.getLog(TestHelpersResource10.class);

  private NumberUtils numberUtils = NumberUtils.getInstance();

  @EJB
  private UsersEJBBean10 usersEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private CdtEJBBean10 cdtEJBBean10;

	@EJB
  private MailEJBBean10 mailEJBBean10;

	@EJB
  private DataEJBBean10 dataEJBBean10;

	private void validate() {
    if (ConfigUtils.isEnvProduction()) {
      throw new SecurityException("Este metodo no puede ser ejecutado en un ambiente de produccion");
    }
  }

  //http://localhost:8080/api-prepaid-1.0/1.0/testhelpers/send/test?address=pepito@mail.com&withAttachment=false
  @GET
  @Path("/send/test")
  public Response sendEmailAsynTest(@QueryParam("address") String address, @QueryParam("withAttachment") String withAttachment) throws Exception {
    validate();
    return Response.status(200).entity(this.mailEJBBean10.sendEmailAsynTest(address, numberUtils.toBoolean(withAttachment, false))).build();
  }

  @POST
  @Path("/users/reset")
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

    //se borran usuarios en api-users
    {
      String schema = usersEJBBean10.getSchema();
      JdbcTemplate jdbcTemplate = usersEJBBean10.getDbUtils().getJdbcTemplate();

      //se buscan los rut dentro del rango y cada rut se transforma a PREPAGO_rut
      lstAccountsCdt = jdbcTemplate.queryForList(String.format("select rut from %s.users where rut >= %s AND rut <= %s", schema, min, max), String.class);
      lstAccountsCdt = lstAccountsCdt.stream().map((x) -> "'PREPAGO_" + x + "'").collect(Collectors.toList());

      String subQuery = String.format("select id from %s.users where rut >= %s AND rut <= %s", schema, min, max);

      jdbcExecute(jdbcTemplate, String.format("delete from %s.users_address where users_id in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.users_bank_account where users_id in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.users_cellphone where users_id in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.users_email where users_id in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.users_rut where users_id in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.users_signup where users_id in (%s);", schema, subQuery));
      jdbcExecute(jdbcTemplate, String.format("delete from %s.users where id in (%s);", schema, subQuery));
    }

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

  private void jdbcExecute(JdbcTemplate jdbcTemplate, String sql) {
	  log.info("Ejecutando sql: " + sql);
    jdbcTemplate.execute(sql);
  }

  @POST
  @Path("/user")
  public Response createUser(User user, @Context HttpHeaders headers) throws Exception {

    validate();

	  Map<String, Object> mapHeaders = headersToMap(headers);

    SignUp signUp = usersEJBBean10.signUpUser(mapHeaders, user.getRut().getValue(), user.getEmail().getValue());

    return this.updateUser(user, signUp.getUserId(), headers);
  }

  @PUT
  @Path("/user/{userId}")
  public Response updateUser(User user, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {

    validate();

    user.setId(userIdMc);

	  log.info("Before user: " + user);

    Map<String, Object> mapHeaders = headersToMap(headers);

    if (StringUtils.isNotBlank(user.getName()) && StringUtils.isNotBlank(user.getLastname_1())) {

      if (StringUtils.isBlank(user.getLastname_2())) {
        user.setLastname_2(".");
      }

      PersonalData personalData = new PersonalData();
      personalData.setName(user.getName());
      personalData.setLastname_1(user.getLastname_1());
      personalData.setLastname_2(user.getLastname_2());

      dataEJBBean10.updatePersonalData(mapHeaders, userIdMc, personalData);

      user.setNameStatus(NameStatus.VERIFIED);
    }

    if (user.getRut() != null && user.getRut().getStatus() == null) {
      user.getRut().setStatus(RutStatus.UNVERIFIED);
    }

    if (user.getEmail() != null && user.getEmail().getStatus() == null) {
      user.getEmail().setStatus(EmailStatus.UNVERIFIED);
    }

    usersEJBBean10.updateUser(user, user.getRut(), user.getEmail(), user.getCellphone(), user.getNameStatus(),
                            user.getGlobalStatus(), user.getBirthday(), user.getPassword(), user.getCompanyData());

    user = usersEJBBean10.getUserById(mapHeaders, userIdMc);

    log.info("After user: " + user);

    return Response.ok(user).status(200).build();
  }

  @POST
  @Path("/prepaiduser")
  public Response createPrepaidUser(User user, @Context HttpHeaders headers) throws Exception {

    validate();

    Map<String, Object> mapHeaders = headersToMap(headers);

    if (user.getId() != null) {

      user = usersEJBBean10.getUserById(mapHeaders, user.getId());

    } else {

      SignUp signUp = usersEJBBean10.signUpUser(mapHeaders, user.getRut().getValue(), user.getEmail().getValue());

      user = usersEJBBean10.getUserById(mapHeaders, signUp.getUserId());
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

    user.setGlobalStatus(UserStatus.ENABLED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user.getEmail().setStatus(EmailStatus.VERIFIED);
    user.setNameStatus(NameStatus.VERIFIED);
    user.setPassword(String.valueOf(1357));

    user = usersEJBBean10.fillUser(user);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setUserIdMc(user.getId());
    prepaidUser.setRut(user.getRut().getValue());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser.setBalanceExpiration(0L);

    prepaidUserEJBBean10.createPrepaidUser(mapHeaders, prepaidUser);

    return Response.ok(user).status(200).build();
  }

  @GET
  @Path("/user/{userId}/email_code")
  public Response getEmailCode(@PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {

    validate();

    Map<String, Object> mapHeaders = headersToMap(headers);

    User user = usersEJBBean10.getUserById(mapHeaders, userIdMc);

    if(user == null){
      throw new NotFoundException(CLIENTE_NO_EXISTE);
    }

    String sql = String.format("select code from %s.users_email where users_id = %s", UsersEJBBean10.getSchema(), userIdMc);

    String code = UsersEJBBean10.getDbUtils().getJdbcTemplate().queryForObject(sql, String.class);

    Map<String, Object> resp = new HashMap<>();
    resp.put("code", code);

    return Response.ok(resp).status(200).build();
  }
}
