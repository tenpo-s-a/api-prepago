package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author vutreras
 */
@Path("/1.0/emails")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class EmailsResource10 extends BaseResource {

  private NumberUtils numberUtils = NumberUtils.getInstance();

	@EJB
  private MailEJBBean10 mailEjb;

  //http://localhost:8080/api-prepaid-1.0/1.0/emails/send/test?address=pepito@mail.com&withAttachment=false
  @GET
  @Path("/send/test")
  public Response sendEmailAsynTest(@QueryParam("address") String address, @QueryParam("withAttachment") String withAttachment) throws Exception {
    return Response.status(200).entity(this.mailEjb.sendEmailAsynTest(address, numberUtils.toBoolean(withAttachment, false))).build();
  }
}
