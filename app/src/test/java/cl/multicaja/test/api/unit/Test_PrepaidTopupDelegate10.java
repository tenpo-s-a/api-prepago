package cl.multicaja.test.api.unit;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.users.model.v10.SingUP;
import cl.multicaja.users.model.v10.User;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.jms.Queue;
import javax.naming.spi.NamingManager;

/**
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_PrepaidTopupDelegate10 extends TestBaseUnit {

  private static CamelFactory camelFactory = CamelFactory.getInstance();

  private static BrokerService brokerService;

  @BeforeClass
  public static void beforeClass() throws Exception {

    SimpleNamingContextBuilder simpleNamingContextBuilder = new SimpleNamingContextBuilder();

    if (!NamingManager.hasInitialContextFactoryBuilder()) {
      simpleNamingContextBuilder.activate();
    }

    //independiente de la configuración obliga a que el activemq no sea persistente en disco
    ConfigUtils.getInstance().setProperty("activemq.broker.embedded.persistent","false");

    //crea e inicia apache camel con las rutas creadas anteriormente
    if (!camelFactory.isCamelRunning()) {

      //crea e inicia el activemq
      brokerService = camelFactory.createBrokerService();
      brokerService.start();

      //Inicializa las rutas camel
      Test_PrepaidTopupDelegate10 t = new Test_PrepaidTopupDelegate10();
      PrepaidTopupRoute10 prepaidTopupRoute10 = new PrepaidTopupRoute10();
      prepaidTopupRoute10.setPrepaidEJBBean10(t.getPrepaidEJBBean10());
      prepaidTopupRoute10.setUsersEJBBean10(t.getUsersEJBBean10());

      camelFactory.startCamelContextWithRoutes(true, prepaidTopupRoute10);
    }

    simpleNamingContextBuilder.deactivate();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (brokerService != null) {
      camelFactory.releaseCamelContext();
      brokerService.stop();
    }
  }

  private User registerRandomUser() throws Exception {
    Integer rut = getUniqueRutNumber();
    String email = String.format("%s@mail.com", RandomStringUtils.randomAlphabetic(20));
    SingUP singUP = getUsersEJBBean10().singUpUser(null, rut, email);
    return getUsersEJBBean10().getUserById(null, singUP.getUserId());
  }

  @Test
  public void pendingTopup_RutIsNull() throws Exception {

    User user = registerRandomUser();

    user.setRut(null);

    PrepaidTopup10 topup = new PrepaidTopup10();
    topup.setId(getUniqueLong());
    topup.setUserId(user.getId());
    topup.setMerchantCode(RandomStringUtils.randomAlphabetic(10));
    topup.setTransactionId(getUniqueInteger().toString());

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_PrepaidUserIsNull() throws Exception {

    User user = registerRandomUser();

    PrepaidTopup10 topup = new PrepaidTopup10();
    topup.setId(getUniqueLong());
    topup.setUserId(user.getId());
    topup.setMerchantCode(RandomStringUtils.randomAlphabetic(10));
    topup.setTransactionId(getUniqueInteger().toString());

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_PendingEmission() throws Exception {

    User user = registerRandomUser();

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setRut(user.getRut().getValue());
    prepaidUser.setIdUserMc(user.getId());
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    prepaidUser = getPrepaidEJBBean10().createPrepaidUser(null, prepaidUser);

    System.out.println("PrepaidUser10: " + prepaidUser);

    PrepaidTopup10 topup = new PrepaidTopup10();

    topup.setId(getUniqueLong());
    topup.setUserId(user.getId());
    topup.setMerchantCode(RandomStringUtils.randomAlphabetic(10));
    topup.setTransactionId(getUniqueInteger().toString());

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", topup.getId(), remoteTopup.getData().getPrepaidTopup().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNull("No Deberia contener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
  }
}
