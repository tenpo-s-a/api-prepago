package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.prepaid.dto.NewRawTransactionDTO;
import cl.multicaja.prepaid.dto.NewPrepaidTransactionDTO;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJB10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
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
  private PrepaidEJB10 ejb;

  @GET
  @Path("/ping")
  public Response ping(@Context HttpHeaders headers) throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("service", this.getClass().getSimpleName());
    map.put("implementation", this.ejb.info());
    return Response.status(200).entity(map).build();
  }

  @POST
  @Path("/prepaid/processor/notification")
  public Response processorNotification(NewRawTransactionDTO trx){
    return Response.ok().build();
  }

  @GET
  @Path("/prepaid/{userId}")
  public Response getPrepaid(@PathParam("userId") String userId) {
    return Response.ok().build();
  }

  @GET
  @Path("/prepaid/{userId}/balance")
  public Response getBalance(@PathParam("userId") String userId){
    return Response.ok().build();
  }

  @POST
  @Path("/prepaid/{userId}/balance/topup")
  public Response topupBalance(@PathParam("userId") String userId, NewPrepaidTransactionDTO trx){
    return Response.ok().build();
  }

  @POST
  @Path("/prepaid/{userId}/balance/widthdraw")
  public Response widthdrawBalance(@PathParam("userId") String userId, NewPrepaidTransactionDTO trx){
    return Response.ok().build();
  }

  @POST
  @Path("/prepaid/{userId}/status/lock")
  public Response lockCard(@PathParam("userId") String userId){
    return Response.ok().build();
  }

  @POST
  @Path("/prepaid/{userId}/status/unlock")
  public Response unlockCard(@PathParam("userId") String userId){
    return Response.ok().build();
  }

  @GET
  @Path("/prepaid/{userId}/transactions")
  public Response getTransactions(@PathParam("userId") String userId){
    return Response.ok().build();
  }

  @POST
  @Path("/prepaid/{userId}/mail")
  public Response sendCardEmail(@PathParam("userId") String userId){
    return Response.ok().build();
  }


}
