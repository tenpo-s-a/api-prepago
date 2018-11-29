package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.prepaid.ejb.v10.MailPrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @author JOG
 */

@Path("/1.0/prepaid/public")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrepaidResourcePublic10 extends BaseResource {

  private static Log log = LogFactory.getLog(PrepaidResource10.class);

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;


  @POST
  @Path("/topup")
  public Response topupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup10 prepaidTopup = this.prepaidEJBBean10.topupUserBalance(headersToMap(headers), topupRequest);
    return Response.ok(prepaidTopup).status(201).build();
  }

  @POST
  @Path("/topup/reverse")
  public Response reverseTopupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseTopupUserBalance(headersToMap(headers), topupRequest);
    return Response.ok().status(201).build();
  }

  @POST
  @Path("/withdrawal")
  public Response withdrawUserBalance(NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalance(headersToMap(headers), withdrawRequest);
    return Response.ok(withdrawTopup).status(201).build();
  }

  @POST
  @Path("/withdrawal/reverse")
  public Response reverseWithdrawUserBalance(NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseWithdrawUserBalance(headersToMap(headers), withdrawRequest);
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
  @Path("/finish_signup")
  public Response finishSignup(@Context HttpHeaders headers) throws Exception {
    //TODO: revisar
    //PrepaidUser10 prepaidUser10 = this.prepaidEJBBean10.finishSignup(headersToMap(headers), userId);
    //return Response.ok(prepaidUser10).build();
    return Response.ok().build();
  }

  @POST
  @Path("/signup/tac")
  public Response acceptTermsAndConditions(NewTermsAndConditions10 newTermsAndConditions, @Context HttpHeaders headers) throws Exception {
    //TODO; revisar
    //this.prepaidEJBBean10.acceptTermsAndConditions(headersToMap(headers),userId, newTermsAndConditions);
    return Response.ok().build();
  }

  @POST
  @Path("/mail")
  public Response sendMail(EmailBody emailBody, @Context HttpHeaders headers) throws Exception {
    this.mailPrepaidEJBBean10.sendMailAsync(headersToMap(headers), null, emailBody);
    return Response.ok().status(201).build();
  }

  @POST
  @Path("/Queue")
  public Response reprocesQueue(ReprocesQueue reprocesQueue, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reprocessQueue(headersToMap(headers), reprocesQueue);
    return Response.ok().status(201).build();
  }

  @POST
  @Path("/identity_validation")
  public Response processIdentityValidation(IdentityValidation10 identityValidation10, @Context HttpHeaders headers) {
    //TODO: revisar
    /*
    try {
      User user = this.prepaidEJBBean10.processIdentityVerification(headersToMap(headers), userId, identityValidation10);
      return Response.ok(user).status(201).build();
    } catch (Exception ex) {
      log.error("Error processing identity validation for userId: " + userId);
      ex.printStackTrace();
    }
    */
    return Response.ok().status(201).build();
  }

}
