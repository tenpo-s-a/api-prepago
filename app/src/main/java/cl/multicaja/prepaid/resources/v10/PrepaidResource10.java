package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.domain.NewPrepaidTopup;
import cl.multicaja.prepaid.domain.NewPrepaidUserSignup;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.prepaid.domain.PrepaidTopup;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJB10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author vutreras
 */
@Path("/1.0")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class PrepaidResource10 extends BaseResource {

  private static Log log = LogFactory.getLog(PrepaidResource10.class);

  @EJB
  private PrepaidEJBBean10 ejb;

  /*
    Prepaid
   */

  @POST
  @Path("/prepaid/topup")
  public Response topupUserBalance(NewPrepaidTopup topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup prepaidTopup = this.ejb.topupUserBalance(headersToMap(headers), topupRequest);
    return Response.ok(prepaidTopup).build();
  }

  @POST
  @Path("/prepaid/topup/reverse")
  public Response reverseTopupUserBalance(NewPrepaidTopup topupRequest) {
    return Response.ok().build();
  }

  @GET
  @Path("/prepaid/{userId}/topup")
  public Response getUserTopups(@PathParam("userId") Long userId) {
    return Response.ok().build();
  }


  /*
    Prepaid Signup
   */

  @POST
  @Path("/prepaid/signup")
  public Response initSignup(NewPrepaidUserSignup signupRequest) {
    return Response.ok().build();
  }

  @GET
  @Path("/prepaid/signup/{signupId}")
  public Response getSignupStatus(@PathParam("signupId") Long signupId) {
    return Response.ok().build();
  }

  /*
    Prepaid protected
   */

  @POST
  @Path("/prepaid/{userId}/card")
  public Response issuePrepaidCard(@PathParam("userId") Long userId) {
    return Response.ok().build();
  }

  @GET
  @Path("/prepaid/{userId}/card")
  public Response getPrepaidCard(@PathParam("userId") Long userId) {
    return Response.ok().build();
  }

}
