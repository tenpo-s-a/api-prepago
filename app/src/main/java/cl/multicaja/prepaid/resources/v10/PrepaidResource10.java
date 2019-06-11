package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.CLIENTE_NO_TIENE_PREPAGO;
import static cl.multicaja.core.model.Errors.SALDO_NO_DISPONIBLE_$VALUE;

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
  private AccountEJBBean10 accountEJBBean10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  @EJB
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  /**
   * CashIn Tempo
   *
   * @param extUserId
   * @param topupRequest
   * @param headers
   * @return
   * @throws Exception
   */
  @POST
  @Path("/{user_id}/cash_in")
  public Response topupUserBalance(@PathParam("user_id") String extUserId, NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup10 prepaidTopup = this.prepaidEJBBean10.topupUserBalance(headersToMap(headers), extUserId, topupRequest, true);
    return Response.ok(prepaidTopup).status(201).build();
  }

  @POST
  @Path("/{user_id}/cash_in/reverse")
  public Response reverseTopupUserBalance(@PathParam("user_id") String extUserId, NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseTopupUserBalance(headersToMap(headers), extUserId, topupRequest, true);
    return Response.status(201).build();
  }

  @POST
  @Path("/{user_id}/cash_out")
  public Response withdrawUserBalance(@PathParam("user_id") String extUserId, NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    if(withdrawRequest != null && withdrawRequest.WEB_MERCHANT_CODE.equals(withdrawRequest.getMerchantCode())){
      throw new ValidationException(Errors.INVALID_MERCHANT_CODE);
    }
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalance(headersToMap(headers), extUserId, withdrawRequest,true);
    return Response.ok(withdrawTopup).status(201).build();
  }

  @POST
  @Path("/{user_id}/defered_cash_out")
  public Response withdrawUserBalanceDefered(@PathParam("user_id") String extUserId, NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    if(withdrawRequest != null &&  !withdrawRequest.WEB_MERCHANT_CODE.equals(withdrawRequest.getMerchantCode())){
      throw new ValidationException(Errors.INVALID_MERCHANT_CODE_DEFERED);
    }
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalance(headersToMap(headers), extUserId, withdrawRequest,true);
    return Response.ok(withdrawTopup).status(201).build();
  }

  @POST
  @Path("/{user_id}/cash_out/reverse")
  public Response reverseWithdrawUserBalandoV2(@PathParam("user_id") String extUserId, NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseWithdrawUserBalance(headersToMap(headers), extUserId, withdrawRequest,true);
    return Response.status(201).build();
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
  @Path("/{userId}/balance")
  @Deprecated
  public Response getPrepaidUserBalance(@PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    PrepaidUser10 prepaidUser10 = this.prepaidUserEJBBean10.getPrepaidUserByUserIdMc(headersToMap(headers), userIdMc);
    if(prepaidUser10 == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }
    Account account = this.accountEJBBean10.findByUserId(prepaidUser10.getId());
    if(account == null) {
      throw new ValidationException(SALDO_NO_DISPONIBLE_$VALUE);
    }
    PrepaidBalance10 prepaidBalance10 =  this.accountEJBBean10.getBalance(headersToMap(headers), account.getId());
    return Response.ok(prepaidBalance10).build();
  }

  @GET
  @Path("/{user_id}/account/{account_id}/balance")
  public Response getAccountBalance(@PathParam("user_id") String userUuid, @PathParam("account_id") String accountUuid,@Context HttpHeaders headers) throws Exception {
    PrepaidBalance10 prepaidBalance10 =  this.prepaidEJBBean10.getAccountBalance(headersToMap(headers), userUuid, accountUuid);
    return Response.ok(prepaidBalance10).build();
  }

  /*
     Prepaid Simulations
   */
  @POST
  @Path("/{userId}/simulation/topup")
  @Deprecated
  public Response topupSimulation(SimulationNew10 simulationNew, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    SimulationTopupGroup10 simulationTopupGroup10 = this.prepaidEJBBean10.topupSimulationGroup(headersToMap(headers), userIdMc, simulationNew);
    return Response.ok(simulationTopupGroup10).build();
  }

  @POST
  @Path("/{userId}/simulation/withdrawal")
  @Deprecated
  public Response withdrawalSimulation(SimulationNew10 simulationNew, @PathParam("userId") Long userIdMc, @Context HttpHeaders headers) throws Exception {
    SimulationWithdrawal10 simulationWithdrawal10 = this.prepaidEJBBean10.withdrawalSimulation(headersToMap(headers), userIdMc, simulationNew);
    return Response.ok(simulationWithdrawal10).build();
  }

  @PUT
  @Path("/{userId}/account/{accountId}/upgrade_card")
  public Response upgradeCard(@PathParam("userId") String userUuid, @PathParam("accountId") String accountUuid, @Context HttpHeaders headers) throws Exception {
    PrepaidCardResponse10 prepaidCardResponse10 = prepaidCardEJBBean11.upgradePrepaidCard(headersToMap(headers), userUuid, accountUuid);
    return Response.ok(prepaidCardResponse10).build();
  }

  @POST
  @Path("/Queue")
  public Response reprocesQueue(ReprocesQueue reprocesQueue, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reprocessQueue(headersToMap(headers), reprocesQueue);
    return Response.ok().status(201).build();
  }

  @POST
  @Path("/{user_prepago_id}/transactions/{movement_id}/refund")
  public Response processRefundMovement(@PathParam("user_prepago_id") Long userPrepagoId, @PathParam("movement_id") Long movementId, @Context HttpHeaders headers) {
    try{
      this.prepaidEJBBean10.processRefundMovement(userPrepagoId,movementId);
    }catch (Exception ex) {
      log.error("Error processing refund for movement: "+movementId, ex);
    }
    return Response.accepted().build();
  }

  @POST
  @Path("/fake/datos")
  public Response datos1(Map<String, Object> body) {
    if(body == null){
      return Response.ok("body null").status(400).build();
    }
    if(StringUtils.isAllBlank(body.get("c").toString())){
      return Response.ok("contrato null or empty").status(400).build();
    }
    if(StringUtils.isAllBlank(body.get("r").toString())){
      return Response.ok("rut null or empty").status(400).build();
    }
    String r = this.prepaidEJBBean10.fakeDatosPersona(body.get("c").toString(), body.get("r").toString());
    return Response.ok(r).build();
  }

  @POST
  @Path("/fake/card")
  public Response datos2(Map<String, Object> body) {
    if(body == null){
      return Response.ok("body null").status(400).build();
    }
    if(StringUtils.isAllBlank(body.get("c").toString())){
      return Response.ok("contrato null or empty").status(400).build();
    }
    String r = this.prepaidEJBBean10.fakeDatosTarjeta(body.get("c").toString());
    return Response.ok(r).build();
  }

  @GET
  @Path("/fix/account_event")
  public Response accountsEvent() {
    try {
      List<Account> accountList = this.prepaidEJBBean10.generateAccountEvents();

      return Response.ok(accountList).build();
    } catch (Exception ex) {
      return Response.serverError().build();
    }
  }


}
