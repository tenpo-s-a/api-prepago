package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
public final class PrepaidResource10 {

  private static Log log = LogFactory.getLog(PrepaidResource10.class);

  @EJB
  private PrepaidEJBBean10 ejb;

  @GET
  @Path("/ping")
  public Response ping(@Context HttpHeaders headers) throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("service", this.getClass().getSimpleName());
    map.put("implementation", this.ejb.info());
    return Response.status(200).entity(map).build();
  }

}
