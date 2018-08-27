package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.test.integration.v10.helper.TestContextHelper;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
public class TestBaseUnitAsync extends TestContextHelper {
  private static Log log = LogFactory.getLog(TestBaseUnit.class);

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCamelContext();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    destroyCamelContext();
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

    //se envia el mensaje a la cola
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));

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
  public String sendPendingCardIssuanceFee(User user, PrepaidTopup10 prepaidTopup, PrepaidMovement10 prepaidMovement, PrepaidCard10 prepaidCard, Integer retryCount) {

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
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, null, prepaidMovement);
    data.setPrepaidCard10(prepaidCard);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
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

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));
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

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));
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

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));
    req.getData().setPrepaidCard10(prepaidCard10);
    req.getData().setPrepaidUser10(prepaidUser10);
    req.getData().setUser(user);

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /**
   *
   * @param user
   * @param retryCount
   * @return
   */
  public String sendPendingSendMailError(User user, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }
    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_SEND_MAIL_CARD_REQ);
    // Realiza alta en tecnocom para que el usuario exista

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(null, user, null, null);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));
    req.getData().setUser(user);
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

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount == null ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(retryCount, qReq.toString()));

    //se envia el mensaje a la cola
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  /**
   * Envia un mensaje directo al proceso PENDING_REVERSAL_WITHDRAW_REQ
   *
   * @param prepaidWithdraw
   * @param user
   * @param reverse
   * @param retryCount
   * @return
   */
  public String sendPendingWithdrawReversal(PrepaidWithdraw10 prepaidWithdraw, PrepaidUser10 user, PrepaidMovement10 reverse, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_REQ);

    //se crea la el objeto con los datos del proceso
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidWithdraw, user, reverse);

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public String sendPendingTopupReverse(PrepaidTopup10 prepaidTopup,PrepaidCard10 prepaidCard10, User user, PrepaidUser10 prepaidUser10, PrepaidMovement10 prepaidMovement, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_REQ);

    //se crea la el objeto con los datos del proceso PrepaidTopup10 , User , PrepaidMovement10 prepaidMovementReverse
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidTopup,prepaidCard10, user,prepaidUser10, prepaidMovement);

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

}