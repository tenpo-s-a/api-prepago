package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	private void validate() {
    if (ConfigUtils.isEnvProduction()) {
      throw new SecurityException("Este metodo no puede ser ejecutado en un ambiente de produccion");
    }
  }

  //http://localhost:8080/api-prepaid-1.0/1.0/testhelpers/send/test?address=pepito@mail.com&withAttachment=false
  @GET
  @Path("/send/test")
  public Response sendEmailAsynTest(@QueryParam("address") String address, @QueryParam("withAttachment") String withAttachment) throws Exception {
    return Response.status(200).entity(this.mailEJBBean10.sendEmailAsynTest(address, numberUtils.toBoolean(withAttachment, false))).build();
  }

  @POST
  @Path("/users/reset")
  public Response usersReset(Map<String, Object> body) throws Exception {

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
}
