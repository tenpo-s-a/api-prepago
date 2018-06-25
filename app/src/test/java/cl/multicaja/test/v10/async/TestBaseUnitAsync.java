package cl.multicaja.test.v10.async;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.test.TestSuite;
import cl.multicaja.test.v10.unit.TestBaseUnit;
import cl.multicaja.users.async.v10.routes.UsersEmailRoute10;
import cl.multicaja.users.model.v10.User;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.jms.Queue;
import javax.naming.spi.NamingManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor vutreras
 */
public class TestBaseUnitAsync extends TestBaseUnit {

  private static Log log = LogFactory.getLog(TestBaseUnit.class);

  public static CamelFactory camelFactory = CamelFactory.getInstance();

  private static BrokerService brokerService;

  @BeforeClass
  public static void beforeClass() throws Exception {

    SimpleNamingContextBuilder simpleNamingContextBuilder = new SimpleNamingContextBuilder();

    //Por un extraño conflicto con payara cuando no se usa, se debe sobre-escribir el InitialContext por defecto
    //sino se lanza un NullPointerException en camel producto de la existencia de payara.
    if (!NamingManager.hasInitialContextFactoryBuilder() || !TestSuite.isServerRunning()) {
      simpleNamingContextBuilder.activate();
    }

    //independiente de la configuración obliga a que el activemq no sea persistente en disco
    getConfigUtils().setProperty("activemq.broker.embedded.persistent","false");

    //crea e inicia apache camel con las rutas creadas anteriormente
    if (!camelFactory.isCamelRunning()) {

      //crea e inicia el activemq
      brokerService = camelFactory.createBrokerService();
      brokerService.start();

      //Inicializa las rutas camel, se inicializa aun cuando no se incluya en camel, se crea dado que de
      // ella depende la instancia de tecnocomService
      PrepaidTopupRoute10 prepaidTopupRoute10 = new PrepaidTopupRoute10();
      prepaidTopupRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      prepaidTopupRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      prepaidTopupRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidTopupRoute10.setUsersEJBBean10(getUsersEJBBean10());
      prepaidTopupRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      prepaidTopupRoute10.setCdtEJBBean10(getCdtEJBBean10());
      prepaidTopupRoute10.setMailEJBBean10(getMailEJBBean10());

      /**
       * Agrega rutas de envio de emails de users pero al camel context de prepago necesario para los test
       */

      UsersEmailRoute10 usersEmailRoute10 = new UsersEmailRoute10();
      usersEmailRoute10.setUsersEJBBean10(getUsersEJBBean10());
      usersEmailRoute10.setMailEJBBean10(getMailEJBBean10());

      camelFactory.startCamelContextWithRoutes(true, prepaidTopupRoute10, usersEmailRoute10);
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
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   * @throws Exception
   */
  public String sendTopup(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) throws Exception {
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
  public String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user) throws Exception {
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
  public String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction) throws Exception {
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
  public String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) throws Exception {
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
   * @param retryCount
   * @return
   */
  public String sendPendingTopup(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);
    data.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));

    //se envia el mensaje a la cola
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

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
  public String sendPendingCardIssuanceFee(PrepaidTopup10 prepaidTopup, PrepaidMovement10 prepaidMovement, PrepaidCard10 prepaidCard, Integer retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ);

    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, null, null, prepaidMovement);
    data.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
    data.setPrepaidCard10(prepaidCard);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);

    if (retryCount != null){
      req.setRetryCount(retryCount);
    }

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /******
   * Envia un mensaje directo al proceso PENDING_EMISSION_REQ
   * @param user
   * @return
   */
  public String sendPendingEmissionCard(PrepaidTopup10 prepaidTopup, User user, PrepaidUser10 prepaidUser, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_EMISSION_REQ);

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);
    data.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getData().setPrepaidUser10(prepaidUser);

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param prepaidUser
   * @param prepaidCard
   * @param cdtTransaction
   * @param prepaidMovement
   * @param retryCount
   * @return
   */
  public String sendPendingCreateCard(PrepaidTopup10 prepaidTopup, User user, PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_CREATE_CARD_REQ);
    // Realiza alta en tecnocom para que el usuario exista

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);
    data.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getData().setPrepaidCard10(prepaidCard);
    req.getData().setPrepaidUser10(prepaidUser);

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /**
   *
   * @param user
   * @param prepaidUser10
   * @param prepaidCard10
   * @param retryCount
   * @return
   */
  public String sendPendingSendMail(User user,PrepaidUser10 prepaidUser10,PrepaidCard10 prepaidCard10, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }
    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_CARD_REQ);
    // Realiza alta en tecnocom para que el usuario exista

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(null, user, null, null);
    data.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getData().setPrepaidCard10(prepaidCard10);
    req.getData().setPrepaidUser10(prepaidUser10);

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /**
   *
   * @param user
   * @param prepaidWithdraw10
   * @param retryCount
   * @return
   */
  public String sendPendingWithdrawMail(User user, PrepaidWithdraw10 prepaidWithdraw10, Integer retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }
    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_WITHDRAW_REQ);
    // Realiza alta en tecnocom para que el usuario exista

    //se crea la el objeto con los datos del proceso
    prepaidWithdraw10.setMessageId(messageId);
    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setUser(user);
    data.setPrepaidWithdraw10(prepaidWithdraw10);

    data.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);

    req.setRetryCount(retryCount == null ? 0 : retryCount);

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public Map<String,Object> getDefaultHeaders(){
    Map<String,Object> header = new HashMap<>();
    header.put(Constants.HEADER_USER_LOCALE,Constants.DEFAULT_LOCALE.toString());
    header.put(Constants.HEADER_USER_TIMEZONE,"America/Santiago");
    return header;
  }

}
