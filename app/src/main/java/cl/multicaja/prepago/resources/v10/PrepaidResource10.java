package cl.multicaja.prepago.resources.v10;

import cl.multicaja.prepago.ejb.v10.PrepaidEJB10;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vutreras
 */
@Path("/1.0")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrepaidResource10 {

  @EJB
  private PrepaidEJB10 prepaidEJB10;

	@GET
	@Path("/ping")
	public Response ping() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("service", this.getClass().getSimpleName());
		map.put("ejb", this.prepaidEJB10.info());
		return Response.status(200).entity(map).build();
	}

}
