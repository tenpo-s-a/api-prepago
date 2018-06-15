package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.users.mail.ejb.v10.MailEJBBean10;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author vutreras
 */
@Path("/1.0/emails")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class EmailsResource10 extends BaseResource {

	@EJB
  private MailEJBBean10 mailEjb;

	@GET
	@Path("/send/test")
	public Response getSingUpFull() throws Exception {
		//return Response.status(200).entity(this.mailEjb.sendEmailAsynTest()).build();
    return null;
	}
}
