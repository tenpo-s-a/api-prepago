package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
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

import static cl.multicaja.core.model.Errors.*;

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

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  /*
    Prepaid topup
   */
  @POST
  @Path("/topup")
  public Response topupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup10 prepaidTopup = this.prepaidEJBBean10.topupUserBalance(headersToMap(headers), topupRequest);
    return Response.ok(prepaidTopup).status(201).build();
  }

  @POST
  @Path("/topup/reverse")
  public Response reverseTopupUserBalance(NewPrepaidTopup10 topupRequest) {
    //TODO falta implementar
    return Response.ok().status(201).build();
  }

  @GET
  @Path("/{userId}/topup")
  public Response getUserTopups(@PathParam("userId") Long userIdMc) {
    //TODO falta implementar
    return Response.ok().build();
  }

  /*
    Prepaid withdraw
   */

  @POST
  @Path("/withdrawal")
  public Response withdrawUserBalance(NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalance(headersToMap(headers), withdrawRequest);
    return Response.ok(withdrawTopup).status(201).build();
  }

  @POST
  @Path("/withdrawal/reverse")
  public Response reverseWithdrawUserBalance(NewPrepaidWithdraw10 withdraw10Request, @Context HttpHeaders headers) {
    //TODO falta implementar
    return Response.ok().status(201).build();
  }

  /*
    Prepaid Signup
   */

  @POST
  @Path("/signup")
  public Response initSignup(NewPrepaidUserSignup10 signupRequest, @Context HttpHeaders headers) {
    //TODO falta implementar
    return Response.ok().status(201).build();
  }

  @GET
  @Path("/signup/{signupId}")
  public Response getSignup(@PathParam("signupId") Long signupId, @Context HttpHeaders headers) {
    //TODO falta implementar
    return Response.ok().status(201).build();
  }

  /*
    Prepaid User
   */
  @GET
  @Path("/{userId}")
  public Response getPrepaidUser(@PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    PrepaidUser10 prepaidUser = this.prepaidEJBBean10.getPrepaidUser(headersToMap(headers), userId);
    return Response.ok(prepaidUser).build();
  }

  @GET
  @Path("/{userId}/card")
  public Response getPrepaidCard(@PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = prepaidEJBBean10.getPrepaidCard(headersToMap(headers), userIdMc);
    return Response.ok(prepaidCard10).build();
  }

  @GET
  @Path("/{userId}/balance")
  public Response getPrepaidUserBalance(@PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    PrepaidBalance10 prepaidBalance10 = this.prepaidUserEJBBean10.getPrepaidUserBalance(headersToMap(headers), userIdMc);
    return Response.ok(prepaidBalance10).build();
  }

  /*
     Prepaid Simulations
   */
  @POST
  @Path("/{userId}/simulation/topup")
  public Response topupSimulation(SimulationNew10 simulationNew, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    SimulationTopup10 simulationTopup10 = this.prepaidEJBBean10.topupSimulation(headersToMap(headers), userIdMc, simulationNew);
    return Response.ok(simulationTopup10).build();
  }

  @POST
  @Path("/{userId}/simulation/withdrawal")
  public Response withdrawalSimulation(SimulationNew10 simulationNew, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    SimulationWithdrawal10 simulationWithdrawal10 = this.prepaidEJBBean10.withdrawalSimulation(headersToMap(headers), userIdMc, simulationNew);
    return Response.ok(simulationWithdrawal10).build();
  }
}
