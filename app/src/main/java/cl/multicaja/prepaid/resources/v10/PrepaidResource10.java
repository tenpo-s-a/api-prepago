package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.core.resources.BaseResource;
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
@Path("/1.0/prepaid")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class PrepaidResource10 extends BaseResource {

  private static Log log = LogFactory.getLog(PrepaidResource10.class);

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  /*
    Prepaid topup
   */
  @POST
  @Path("/topup")
  public Response topupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup10 prepaidTopup = this.prepaidEJBBean10.topupUserBalance(headersToMap(headers), topupRequest);
    return Response.ok(prepaidTopup).build();
  }

  @POST
  @Path("/topup/reverse")
  public Response reverseTopupUserBalance(NewPrepaidTopup10 topupRequest) {
    return Response.ok().build();
  }

  @GET
  @Path("/{userId}/topup")
  public Response getUserTopups(@PathParam("userId") Long userId) {
    return Response.ok().build();
  }

  /*
    Prepaid withdraw
   */

  @POST
  @Path("/withdrawal")
  public Response withdrawUserBalance(NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalance(headersToMap(headers), withdrawRequest);
    return Response.ok(withdrawTopup).build();
  }

  @POST
  @Path("/withdrawal/reverse")
  public Response reverseWithdrawUserBalance(NewPrepaidWithdraw10 withdraw10Request) {
    return Response.ok().build();
  }

  /*
    Prepaid Signup
   */

  @POST
  @Path("/signup")
  public Response initSignup(NewPrepaidUserSignup10 signupRequest) {
    return Response.ok().build();
  }

  @GET
  @Path("/signup/{signupId}")
  public Response getSignupStatus(@PathParam("signupId") Long signupId) {
    return Response.ok().build();
  }

  /*
    Prepaid protected
   */

  @POST
  @Path("/{userId}/card")
  public Response issuePrepaidCard(@PathParam("userId") Long userId) {
    return Response.ok().build();
  }

  @GET
  @Path("/{userId}/card")
  public Response getPrepaidCard(@PathParam("userId") Long userId) {
    return Response.ok().build();
  }

  @GET
  @Path("/{userId}/balance")
  public Response getBalance(@PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    PrepaidBalance10 prepaidBalance10 = this.prepaidUserEJBBean10.getPrepaidUserBalance(headersToMap(headers), userId);
    return Response.ok(prepaidBalance10).build();
  }

  @POST
  @Path("/{userId}/simulation/topup")
  public Response topupCalculator(SimulationNew10 simulationNew, @PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    SimulationTopup10 simulationTopup10 = this.prepaidEJBBean10.topupSimulation(headersToMap(headers), userId, simulationNew);
    return Response.ok(simulationTopup10).build();
  }

  @POST
  @Path("/{userId}/simulation/withdrawal")
  public Response withdrawalCalculator(SimulationNew10 simulationNew, @PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    SimulationWithdrawal10 simulationWithdrawal10 = this.prepaidEJBBean10.withdrawalSimulation(headersToMap(headers), userId, simulationNew);
    return Response.ok(simulationWithdrawal10).build();
  }
}
