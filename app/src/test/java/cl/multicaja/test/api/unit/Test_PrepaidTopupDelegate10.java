package cl.multicaja.test.api.unit;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.TipoFactura;
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

  @Test
  public void pendingTopup_RutIsNull() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    topup.setRut(null);
    user.setRut(null);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_PrepaidUserIsNull() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }


  @Test
  public void pendingTopup_Get_CodEntity() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard = createPrepaidCard(prepaidCard);

    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", topup.getId(), remoteTopup.getData().getPrepaidTopup().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    String codEntity = parametersUtil.getString("api-prepaid", "cod_entidad", "v10");

    Assert.assertEquals("Deberia contener una codEntity", codEntity, remoteTopup.getData().getTecnocomCodEntity());

    if (TopupType.WEB.equals(remoteTopup.getData().getPrepaidTopup().getType())) {
      Assert.assertEquals("debe ser tipo factura CARGA_TRANSFERENCIA", TipoFactura.CARGA_TRANSFERENCIA, remoteTopup.getData().getTecnocomInvoiceType());
    } else {
      Assert.assertEquals("debe ser tipo factura CARGA_EFECTIVO_COMERCIO_MULTICAJA", TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, remoteTopup.getData().getTecnocomInvoiceType());
    }
  }

  @Test
  public void pendingTopup_WithCardLockedhard() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard(prepaidCard);

    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingTopup_WithCardExpired() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard(prepaidCard);

    System.out.println("prepaidCard: " + prepaidCard);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Test
  public void pendingEmissionCard() throws Exception {

    User user = registerUser();
    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", topup.getId(), remoteTopup.getData().getPrepaidTopup().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    System.out.println("Contrato: "+remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
  }

  @Test
  public void pendingEmissionCardPrepaidUserIsNull() throws Exception {
    User user = registerUser();
    PrepaidTopup10 topup = buildPrepaidTopup(user);
    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);
    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }


  @Test
  public void pendingCreateCard() throws Exception {

    User user = registerUser();
    user.setName(RandomStringUtils.randomAlphabetic(5,10));
    user.setLastname_1(RandomStringUtils.randomAlphabetic(5,10));

    PrepaidUser10 prepaidUser = buildPrepaidUser(user);

    prepaidUser = createPrepaidUser(prepaidUser);

    System.out.println("prepaidUser: " + prepaidUser);

    PrepaidTopup10 topup = buildPrepaidTopup(user);

    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATECARD_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", topup.getId(), remoteTopup.getData().getPrepaidTopup().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    /******************************
     * Validacion de datos Tarjeta
     ******************************/
    Assert.assertNotNull("Deberia tener una PrepaidCard ProcessorUserId", remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
    Assert.assertNotNull("Deberia tener PAN", remoteTopup.getData().getPrepaidCard10().getPan());
    Assert.assertNotNull("Deberia tener PAN Encriptado", remoteTopup.getData().getPrepaidCard10().getEncryptedPan());
    Assert.assertNotNull("Deberia tener Expire Date", remoteTopup.getData().getPrepaidCard10().getExpiration());
    Assert.assertEquals("Status Igual a",PrepaidCardStatus.ACTIVE, remoteTopup.getData().getPrepaidCard10().getStatus());
    Assert.assertNotNull("Deberia Tener Nombre",remoteTopup.getData().getPrepaidCard10().getNameOnCard());

    System.out.println("Contrato: "+remoteTopup.getData().getPrepaidCard10().getProcessorUserId());
  }

  @Test
  public void pendingCreateCardPrepaidUserIsNull() throws Exception {
    User user = registerUser();
    PrepaidTopup10 topup = buildPrepaidTopup(user);
    String messageId = getPrepaidTopupDelegate10().sendTopUp(topup, user);
    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remoteTopup = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

}
