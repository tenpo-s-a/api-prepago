package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.ejb.v10.MailPrepaidEJBBean10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.User;
import cl.multicaja.users.model.v10.UserFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import cl.multicaja.core.exceptions.BadRequestException;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

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

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

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
  public Response initSignup(NewPrepaidUserSignup10 signupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidUserSignup10 prepaidUserSignup10 = this.prepaidEJBBean10.initUserSignup(headersToMap(headers),signupRequest);
    return Response.ok(prepaidUserSignup10).status(201).build();
  }

  @GET
  @Path("/signup/tac")
  public Response getTermsAndConditions(@Context HttpHeaders headers) throws Exception {
    PrepaidTac10 tac = this.prepaidEJBBean10.getTermsAndConditions(headersToMap(headers));
    return Response.ok(tac).build();
  }

  @POST
  @Path("/{userId}/finish_signup")
  public Response finishSignup(@PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    PrepaidUser10 prepaidUser10 = this.prepaidEJBBean10.finishSignup(headersToMap(headers),userId);
    return Response.ok(prepaidUser10).build();
  }

  @POST
  @Path("/{userId}/signup/tac")
  public Response acceptTermsAndConditions(NewTermsAndConditions10 newTermsAndConditions, @PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.acceptTermsAndConditions(headersToMap(headers),userId, newTermsAndConditions);
    return Response.ok().build();
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
  @Path("/")
  public Response findPrepaidUser(@QueryParam("rut") Integer rut, @Context HttpHeaders headers) throws Exception {
    PrepaidUser10 prepaidUser = this.prepaidEJBBean10.findPrepaidUser(headersToMap(headers), rut);
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

  @POST
  @Path("/{userId}/identity/files")
  public Response uploadIdentityVerificationFiles(Map<String, UserFile> identityVerificationFiles, @PathParam("userId") Long userId, @Context HttpHeaders headers) throws Exception {
    User user = this.prepaidEJBBean10.uploadIdentityVerificationFiles(headersToMap(headers),userId, identityVerificationFiles);
    return Response.ok(user).build();
  }

  /*
     Prepaid Simulations
   */
  @POST
  @Path("/{userId}/simulation/topup")
  public Response topupSimulation(SimulationNew10 simulationNew, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    SimulationTopupGroup10 simulationTopupGroup10 = this.prepaidEJBBean10.topupSimulationGroup(headersToMap(headers), userIdMc, simulationNew);
    return Response.ok(simulationTopupGroup10).build();
  }

  @POST
  @Path("/{userId}/simulation/withdrawal")
  public Response withdrawalSimulation(SimulationNew10 simulationNew, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    SimulationWithdrawal10 simulationWithdrawal10 = this.prepaidEJBBean10.withdrawalSimulation(headersToMap(headers), userIdMc, simulationNew);
    return Response.ok(simulationWithdrawal10).build();
  }


  @GET
  @Path("/{userId}/transactions")
  public Response getTransactions(@PathParam("userId") Long userIdMc, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("count") Integer count, @Context HttpHeaders headers) throws Exception {
    List<PrepaidTransaction10> transaction10List = this.prepaidEJBBean10.getTransactions(headersToMap(headers),userIdMc,from,to, count);
    return Response.ok(transaction10List).build();
  }

  @PUT
  @Path("/{userId}/card/lock")
  public Response lockPrepaidCard(@PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = this.prepaidEJBBean10.lockPrepaidCard(headersToMap(headers), userIdMc);
    return Response.ok(prepaidCard10).build();
  }

  @PUT
  @Path("/{userId}/card/unlock")
  public Response unlockPrepaidCard(@PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = this.prepaidEJBBean10.unlockPrepaidCard(headersToMap(headers), userIdMc);
    return Response.ok(prepaidCard10).build();
  }


  @POST
  @Path("/{user_id}/mail")
  public Response sendMail(EmailBody emailBody, @PathParam("user_id") Long userId, @Context HttpHeaders headers) throws Exception {
    this.mailPrepaidEJBBean10.sendMailAsync(headersToMap(headers), userId, emailBody);
    return Response.ok().status(201).build();
  }

}
