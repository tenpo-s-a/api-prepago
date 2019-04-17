package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
  @Path("/topup")
  public Response topupUserBalanceV1(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup10 prepaidTopup = this.prepaidEJBBean10.topupUserBalanceV1(headersToMap(headers), topupRequest);
    return Response.ok(prepaidTopup).status(201).build();
  }


  @POST
  @Path("/topup/reverse")
  public Response reverseTopupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseTopupUserBalanceTmp(headersToMap(headers), topupRequest,true);
    return Response.status(201).build();
  }


  /*
    Prepaid withdraw
   */

  @POST
  @Path("/withdrawal")
  public Response withdrawUserBalance( NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalanceDeprecated(headersToMap(headers), withdrawRequest,true);
    return Response.ok(withdrawTopup).status(201).build();
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
  @Path("/withdrawal/reverse")
  public Response reverseWithdrawUserBalance(NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseWithdrawUserBalanceOld(headersToMap(headers), withdrawRequest,true);
    return Response.status(201).build();
  }
  @POST
  @Path("/{user_id}/cash_out/reverse")
  public Response reverseWithdrawUserBalandoV2(@PathParam("user_id") String extUserId, NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseWithdrawUserBalance(headersToMap(headers), extUserId, withdrawRequest,true);
    return Response.status(201).build();
  }

  @GET
  @Path("/{user_id}/account/{account_id}/balance")
  public Response getAccountBalance(@PathParam("user_id") String userUuid, @PathParam("account_id") String accountUuid,@Context HttpHeaders headers) throws Exception {
    PrepaidBalance10 prepaidBalance10 =  this.prepaidEJBBean10.getAccountBalance(headersToMap(headers), userUuid, accountUuid);
    return Response.ok(prepaidBalance10).build();
  }

  @GET
  @Path("/{userId}/transactions")
  public Response getTransactions(@PathParam("userId") Long userIdMc, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("count") Integer count, @Context HttpHeaders headers) throws Exception {
    PrepaidTransactionExtend10 prepaidTransactionExtend10 = this.prepaidEJBBean10.getTransactions(headersToMap(headers),userIdMc,from,to, count);
    return Response.ok(prepaidTransactionExtend10).build();
  }

  @PUT
  @Path("/{userId}/account/{accountId}/upgrade_card")
  public Response upgradeCard(@PathParam("userId") String userUuid, @PathParam("accountId") String accountUuid, @Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = prepaidCardEJBBean11.upgradePrepaidCard(headersToMap(headers), userUuid, accountUuid);
    return Response.ok(prepaidCard10).build();
  }

  @POST
  @Path("/{user_id}/mail")
  public Response sendMail(EmailBody emailBody, @PathParam("user_id") Long userId, @Context HttpHeaders headers) throws Exception {
    this.mailPrepaidEJBBean10.sendMailAsync(headersToMap(headers), userId, emailBody);
    return Response.ok().status(201).build();
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
  @Path("/processor/notification")
  public Response callNotificationTecnocom(NotificationTecnocom notificationTecnocom,@Context HttpHeaders headers) throws Exception {
    Response returnResponse = null;

    String textLogBase = "TestHelperResource-callNotification: ";
    NotificationTecnocom notificationTecnocomResponse;
    try{

      notificationTecnocomResponse = this.prepaidEJBBean10.setNotificationCallback(null,notificationTecnocom);
      returnResponse = Response.ok(notificationTecnocomResponse).status(202).build();
      log.info(textLogBase+notificationTecnocomResponse.toString());

    }catch(Exception ex){
      log.error(textLogBase+ex.toString());
      ex.printStackTrace();
      returnResponse = Response.ok(ex).build();
    }

    return returnResponse;

  }

}
