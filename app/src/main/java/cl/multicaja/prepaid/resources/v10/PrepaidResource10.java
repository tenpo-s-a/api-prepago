package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.model.v10.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.PostUpdate;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

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
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @EJB
  private PrepaidMovementEJBBean10 prepaidMovementEJBBean10;

  /*
    Prepaid topup
   */
  @POST
  @Path("/topup")
  public Response topupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    PrepaidTopup10 prepaidTopup = this.prepaidEJBBean10.topupUserBalance(headersToMap(headers), topupRequest,true);
    return Response.ok(prepaidTopup).status(201).build();
  }

  @POST
  @Path("/topup/reverse")
  public Response reverseTopupUserBalance(NewPrepaidTopup10 topupRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseTopupUserBalance(headersToMap(headers), topupRequest,true);
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
    PrepaidWithdraw10 withdrawTopup = this.prepaidEJBBean10.withdrawUserBalance(headersToMap(headers), withdrawRequest,true);
    return Response.ok(withdrawTopup).status(201).build();
  }

  @POST
  @Path("/withdrawal/reverse")
  public Response reverseWithdrawUserBalance(NewPrepaidWithdraw10 withdrawRequest, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reverseWithdrawUserBalance(headersToMap(headers), withdrawRequest,true);
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
    PrepaidUser10 prepaidUser10 = this.prepaidEJBBean10.finishSignup(headersToMap(headers), userId);
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
    PrepaidTransactionExtend10 prepaidTransactionExtend10 = this.prepaidEJBBean10.getTransactions(headersToMap(headers),userIdMc,from,to, count);
    return Response.ok(prepaidTransactionExtend10).build();
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

  @POST
  @Path("/Queue")
  public Response reprocesQueue(ReprocesQueue reprocesQueue, @Context HttpHeaders headers) throws Exception {
    this.prepaidEJBBean10.reprocessQueue(headersToMap(headers), reprocesQueue);
    return Response.ok().status(201).build();
  }

  /*
   *  idnetity verification
   */

  @POST
  @Path("/{user_id}/identity_validation")
  public Response processIdentityValidation(IdentityValidation10 identityValidation10, @PathParam("user_id") Long userId, @Context HttpHeaders headers) {
    try {
      User user = this.prepaidEJBBean10.processIdentityVerification(headersToMap(headers), userId, identityValidation10);
      return Response.ok(user).status(201).build();
    } catch (Exception ex) {
      log.error("Error processing identity validation for userId: " + userId);
      ex.printStackTrace();
      //TODO: informar error?
    }
    return Response.ok().status(201).build();
  }


  @POST
  @Path("/{user_prepago_id}/transactions/{movement_id}/refund")
  public Response processRefundMovement(@PathParam("user_prepago_id") Long userPrepagoId, @PathParam("movement_id") Long movementId, @Context HttpHeaders headers) {

    Response returnResponse = null;
    try{
      CdtTransaction10 cdtTransaction = this.prepaidMovementEJBBean10.processRefundMovement(userPrepagoId,movementId);
      if(cdtTransaction == null){
        System.out.println("CDT_TRANSACTION_IS_NULL");
        log.error("processRefundMovement:CDT_TRANSACTION_IS_NULL");
      }
      returnResponse = Response.ok(cdtTransaction).status(201).build();
    }catch (Exception ex) {
      log.error("Error processing refund for movement: "+movementId+" with status rejected");
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }
    return returnResponse;
  }


  @POST
  @Path("/processor/notification")
  public Response callNotificationTecnocom(Map<String, Object> body,@Context HttpHeaders headers) throws Exception {

    Response returnResponse = null;
    NotificationTecnocom notificationTecnocom = null;
    String textLogBase = "TestHelperResource-callNotification: ";
    try{

      String errorCode;
      String errorMessage;

      String errorCodeOnHeader = "";
      String errorMessageOnHeader = "";

      String errorCodeOnBody;
      String errorMessageOnBody;

      //Test Headers
      Map<String, Object> mapHeaders = null;
      if (headers != null) {
        mapHeaders = new HashMap<>();
        MultivaluedMap<String, String> mapHeadersTmp = headers.getRequestHeaders();
        Set<String> keys = mapHeadersTmp.keySet();
        for (String k : keys) {
          mapHeaders.put(k, mapHeadersTmp.getFirst(k));
        }
      }

      if(mapHeaders.keySet().size() == 0 || mapHeaders == null){
        errorCodeOnHeader = "101004";
        errorMessageOnHeader = "Empty Header, must to add header params";
      }

      //Test Body
      ObjectMapper mapper = new ObjectMapper();

      if(body != null){
        String json = new ObjectMapper().writeValueAsString(body);
        notificationTecnocom = this.prepaidEJBBean10.setNotificationCallback(
          mapHeaders,mapper.readValue(json, NotificationTecnocom.class));

        errorCodeOnBody = notificationTecnocom.getResponseCode() == null ?
          "001": notificationTecnocom.getResponseCode();
        errorMessageOnBody = notificationTecnocom.getResponseMessage() == null ?
          "Not Error, but not Accepted": notificationTecnocom.getResponseMessage();
      }else{
        errorCodeOnBody = "101004";
        errorMessageOnBody = "Empty Body, must to add body params";
      }
      errorCode = errorCodeOnBody;
      errorMessage = errorMessageOnBody;

      if(errorCodeOnHeader == errorCodeOnBody){
        errorCode = errorCodeOnBody;
        errorMessage = "Error Description, "+errorMessageOnHeader+" , "+errorMessageOnBody;
      }

      //Final Response
      JsonObject notifResponse = Json.createObjectBuilder().
        add("code", errorCode).
        add("message",errorMessage).build();

      if(errorCode == "101004"){
        returnResponse = Response.ok(notifResponse).status(400).build();
        log.error(textLogBase+notifResponse.toString());
      }

      if(errorCode == "101007"){
        returnResponse = Response.ok(notifResponse).status(422).build();
        log.error(textLogBase+notifResponse.toString());
      }

      if(errorCode == "001"){
        returnResponse = Response.ok(notifResponse).status(201).build();
        log.info(textLogBase+notifResponse.toString());
      }

      if(errorCode == "002"){

        //Ok Service Response
        returnResponse = Response.ok(notifResponse).status(202).build();
        log.info(textLogBase+notifResponse.toString());

        //Send Async Mail
        Map<String, Object> templateData = new HashMap<String, Object>();
        templateData.put("notification_data",new ObjectMapper().writeValueAsString(notificationTecnocom));
        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(MailTemplates.TEMPLATE_MAIL_NOTIFICATION_CALLBACK_TECNOCOM);
        emailBody.setAddress("notification_tecnocom@multicaja.cl");
        mailPrepaidEJBBean10.sendMailAsync(null,emailBody);

      }


    }catch(Exception ex){
      log.error(textLogBase+ex.toString());
      ex.printStackTrace();
      returnResponse = Response.ok(ex).status(410).build();
    }

    return returnResponse;

  }


}
