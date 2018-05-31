package cl.multicaja.test.api.unit;

import cl.multicaja.camel.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.jms.Queue;
import javax.naming.spi.NamingManager;

/**
 * @autor vutreras
 */
public class TestBaseRouteUnit extends TestBaseUnit {

  private static Log log = LogFactory.getLog(TestBaseUnit.class);

  protected static CamelFactory camelFactory = CamelFactory.getInstance();

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

      //Inicializa las rutas camel, se inicializa aun cuando no se incluya en camel, se crea dado que de
      // ella depende la instancia de tecnocomService
      PrepaidTopupRoute10 prepaidTopupRoute10 = new PrepaidTopupRoute10();
      prepaidTopupRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidTopupRoute10.setUsersEJBBean10(getUsersEJBBean10());
      prepaidTopupRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      prepaidTopupRoute10.setCdtEJBBean10(getCdtEJBBean10());

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

  @After
  public void after() {
    //Todos los test que involucren procesos asincronos esperaran 1 segundo despues que terminen para
    //asegurarse que los mensajes de su test fueron procesado
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
    }
    System.out.println("----------------------------------------------------------------");
    System.out.println("After Test - class: " + this.getClass().getSimpleName() + ", method: " + testName.getMethodName());
    System.out.println("----------------------------------------------------------------");
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @return
   */
  protected String sendTopup(PrepaidTopup10 prepaidTopup, User user) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUp(prepaidTopup, user, null, null);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @return
   */
  protected String sendTopup(PrepaidTopup10 prepaidTopup, User user, PrepaidMovement10 prepaidMovement) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUp(prepaidTopup, user, null, prepaidMovement);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   * @throws Exception
   */
  protected String sendTopup(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUp(prepaidTopup, user, cdtTransaction, prepaidMovement);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @return
   * @throws Exception
   */
  protected String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUpReverseConfirmation(prepaidTopup, user, null, null);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @return
   * @throws Exception
   */
  protected String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUpReverseConfirmation(prepaidTopup, user, cdtTransaction, null);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   * @throws Exception
   */
  protected String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUpReverseConfirmation(prepaidTopup, user, cdtTransaction, prepaidMovement);
    return messageId;
  }

  /**
   * Envia un mensaje directo al proceso PENDING_TOPUP_REQ
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   */
  protected String sendPendingTopup(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = RandomStringUtils.randomAlphabetic(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);

    //se crea la el objeto con los datos del proceso
    PrepaidTopupDataRoute10 data = new PrepaidTopupDataRoute10(prepaidTopup, user, cdtTransaction, prepaidMovement);
    data.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, new RequestRoute<>(data), new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }


  /**
   * Envia un mensaje directo al proceso PENDING_CARD_ISSUANCE_FEE_REQ
   *
   * @param prepaidTopup
   * @param prepaidMovement
   * @param prepaidCard
   * @return
   */
  protected String sendPendingCardIssuanceFee(PrepaidTopup10 prepaidTopup, PrepaidMovement10 prepaidMovement, PrepaidCard10 prepaidCard, Integer retryCount) {
    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = RandomStringUtils.randomAlphabetic(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ);

    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }

    //se crea la el objeto con los datos del proceso
    PrepaidTopupDataRoute10 data = new PrepaidTopupDataRoute10(prepaidTopup, null, null, prepaidMovement);
    data.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
    data.setPrepaidCard10(prepaidCard);

    RequestRoute<PrepaidTopupDataRoute10> requestRoute = new RequestRoute<>(data);

    if (retryCount != null){
      requestRoute.setRetryCount(retryCount);
    }

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, requestRoute, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

}
