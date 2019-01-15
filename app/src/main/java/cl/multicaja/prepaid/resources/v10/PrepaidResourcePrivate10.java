package cl.multicaja.prepaid.resources.v10;

import cl.multicaja.core.resources.BaseResource;
import cl.multicaja.prepaid.ejb.v10.MailPrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserFile;
import cl.multicaja.prepaid.model.v10.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;


/**
 * @author JOG
 */


@Path("/1.0/prepaid/private")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrepaidResourcePrivate10 extends BaseResource {

  private static Log log = LogFactory.getLog(PrepaidResource10.class);

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  @GET
  @Path("/me")
  public Response getPrepaidUser (@Context HttpHeaders headers) throws Exception {
    PrepaidUser10 prepaidUser = this.prepaidEJBBean10.getPrepaidUser(headersToMap(headers), null);
    return Response.ok(prepaidUser).build();
  }

  //TODO VERIFICAR ESTE METODO
  //@GET
  //@Path("/")
  public Response findPrepaidUser(@QueryParam("rut") Integer rut, @Context HttpHeaders headers) throws Exception {
    PrepaidUser10 prepaidUser = this.prepaidEJBBean10.findPrepaidUser(headersToMap(headers), rut);
    return Response.ok(prepaidUser).build();
  }

  @GET
  @Path("/me/card")
  public Response getPrepaidCard(@Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = prepaidEJBBean10.getPrepaidCard(headersToMap(headers), null);
    return Response.ok(prepaidCard10).build();
  }

  @GET
  @Path("/me/balance")
  public Response getPrepaidUserBalance(@Context HttpHeaders headers) throws Exception {
    PrepaidBalance10 prepaidBalance10 = this.prepaidUserEJBBean10.getPrepaidUserBalance(headersToMap(headers), null);
    return Response.ok(prepaidBalance10).build();
  }


  @POST
  @Path("/me/identity/files")
  public Response uploadIdentityVerificationFiles(Map<String, UserFile> identityVerificationFiles, @Context HttpHeaders headers) throws Exception {
    User user = this.prepaidEJBBean10.uploadIdentityVerificationFiles(headersToMap(headers),null, identityVerificationFiles);
    return Response.ok(user).build();
  }

  /*
     Prepaid Simulations
   */
  @POST
  @Path("/me/simulation/topup")
  public Response topupSimulation(SimulationNew10 simulationNew, @Context HttpHeaders headers) throws Exception {
    SimulationTopupGroup10 simulationTopupGroup10 = this.prepaidEJBBean10.topupSimulationGroup(headersToMap(headers), null, simulationNew);
    return Response.ok(simulationTopupGroup10).build();
  }

  @POST
  @Path("/me/simulation/withdrawal")
  public Response withdrawalSimulation(SimulationNew10 simulationNew, @Context HttpHeaders headers) throws Exception {
    SimulationWithdrawal10 simulationWithdrawal10 = this.prepaidEJBBean10.withdrawalSimulation(headersToMap(headers), null, simulationNew);
    return Response.ok(simulationWithdrawal10).build();
  }


  @GET
  @Path("/me/transactions")
  public Response getTransactions(@QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("count") Integer count, @Context HttpHeaders headers) throws Exception {
    PrepaidTransactionExtend10 prepaidTransactionExtend10 = this.prepaidEJBBean10.getTransactions(headersToMap(headers),null,from,to, count);
    return Response.ok(prepaidTransactionExtend10).build();
  }

  @PUT
  @Path("/me/card/lock")
  public Response lockPrepaidCard(@Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = this.prepaidEJBBean10.lockPrepaidCard(headersToMap(headers), null);
    return Response.ok(prepaidCard10).build();
  }

  @PUT
  @Path("/me/card/unlock")
  public Response unlockPrepaidCard(@Context HttpHeaders headers) throws Exception {
    PrepaidCard10 prepaidCard10 = this.prepaidEJBBean10.unlockPrepaidCard(headersToMap(headers), null);
    return Response.ok(prepaidCard10).build();
  }

}
