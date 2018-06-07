package cl.multicaja.prepaid.resources.v10;

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

  /**
   * Calculadora de Carga
   */
  @POST
  @Path("/prepaid/topup/calculator")
  public Response topupCalculator(CalculatorRequest10 topupCalculatorRequest, @Context HttpHeaders headers) throws Exception {
    CalculatorTopupResponse10 calculatorTopupResponse10 = this.prepaidEJBBean10.topupCalculator(headersToMap(headers), topupCalculatorRequest);
    return Response.ok(calculatorTopupResponse10).build();
  }

  /**
   * Calculadora de Retiro
   */
  @POST
  @Path("/prepaid/withdrawal/calculator")
  public Response withdrawalCalculator(CalculatorRequest10 topupCalculatorRequest, @Context HttpHeaders headers) throws Exception {
    CalculatorWithdrawalResponse10 calculatorWithdrawalResponse10 = this.prepaidEJBBean10.withdrawalCalculator(headersToMap(headers), topupCalculatorRequest);
    return Response.ok(calculatorWithdrawalResponse10).build();
  }
}
