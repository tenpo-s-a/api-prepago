package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.InvoiceRoute10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.test.integration.v10.helper.TestContextHelper;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.jms.Queue;
import java.math.BigDecimal;

import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.PENDING_SEND_MAIL_WITHDRAW_REQ;

/**
 * @autor vutreras
 */
public class TestBaseUnitAsync extends TestContextHelper {
  public static Log log = LogFactory.getLog(TestBaseUnit.class);

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

    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }

    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);


    //se envia el mensaje a la cola
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

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
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
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
    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }
    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(100), CodigoMoneda.CHILE_CLP));

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
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
    if(prepaidTopup != null) {
      prepaidTopup.setMessageId(messageId);
    }
    //se crea la el objeto con los datos del proceso
    PrepaidTopupData10 data = new PrepaidTopupData10(prepaidTopup, user, cdtTransaction, prepaidMovement);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(100), CodigoMoneda.CHILE_CLP));

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
    req.getData().setPrepaidCard10(prepaidCard);
    req.getData().setPrepaidUser10(prepaidUser);

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
  public String sendPendingWithdrawMail(User user, PrepaidWithdraw10 prepaidWithdraw10, PrepaidMovement10 prepaidWithdrawMovement,  Integer retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }
    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(PENDING_SEND_MAIL_WITHDRAW_REQ);
    // Realiza alta en tecnocom para que el usuario exista

    //se crea la el objeto con los datos del proceso
    prepaidWithdraw10.setMessageId(messageId);
    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setUser(user);
    data.setPrepaidWithdraw10(prepaidWithdraw10);
    data.setPrepaidMovement10(prepaidWithdrawMovement);

    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount == null ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

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
    return sendPendingWithdrawReversal(prepaidWithdraw, null, user, reverse, retryCount);
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
  public String sendPendingWithdrawReversal(PrepaidWithdraw10 prepaidWithdraw, User user, PrepaidUser10 prepaidUser, PrepaidMovement10 reverse, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_REQ);
    prepaidWithdraw.setMessageId(messageId);
    //se crea la el objeto con los datos del proceso
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidWithdraw, prepaidUser, reverse);
    if(user != null) {
      data.setUser(user);
    }

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

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
    prepaidTopup.setMessageId(messageId);
    //se crea la el objeto con los datos del proceso PrepaidTopup10 , User , PrepaidMovement10 prepaidMovementReverse
    PrepaidReverseData10 data = new PrepaidReverseData10(prepaidTopup,prepaidCard10, user,prepaidUser10, prepaidMovement);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(100), CodigoMoneda.CHILE_CLP));

    //se envia el mensaje a la cola
    ExchangeData<PrepaidReverseData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public String sendPendingProductChange(User user, PrepaidCard10 prepaidCard, TipoAlta tipoAlta, int retryCount) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = getRandomString(20);

    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(ProductChangeRoute10.PENDING_PRODUCT_CHANGE_REQ);

    //se crea la el objeto con los datos del proceso
    PrepaidProductChangeData10 data = new PrepaidProductChangeData10(user, prepaidCard, tipoAlta);

    //se envia el mensaje a la cola
    ExchangeData<PrepaidProductChangeData10> req = new ExchangeData<>(data);
    req.setRetryCount(retryCount < 0 ? 0 : retryCount);
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

  public String sendWithdrawToAccounting(PrepaidMovement10 prepaidMovement10, UserAccount userAccount) throws Exception {
    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    // Si prepaid movement viene null (util solo para tests fallidos) agregar un id cualquier
    Long id = prepaidMovement10 == null ? RandomUtils.nextInt(1, 100) : prepaidMovement10.getId();
    // Se crea un messageId unico
    String messageId = String.format("%s#%s", id, Utils.uniqueCurrentTimeNano());

    Queue qReq = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MOVEMENT_TO_ACCOUNTING_REQ);
    PrepaidTopupData10 data = new PrepaidTopupData10();
    data.setPrepaidMovement10(prepaidMovement10);
    data.setUserAccount(userAccount);
    ExchangeData<PrepaidTopupData10> req = new ExchangeData<>(data);
    req.setRetryCount(0);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }


  public String sendPendingInvoice(InvoiceData10 invoiceData10, Integer retryCount) throws JsonProcessingException {
    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    //se crea un messageId unico
    String messageId = String.format("%s#%s#%d#", invoiceData10.getType(),invoiceData10.getMovementId(),invoiceData10.getRut());


    //se crea la cola de requerimiento
    Queue qReq = camelFactory.createJMSQueue(InvoiceRoute10.INVOICE_ENDPOINT);
    ObjectMapper Obj = new ObjectMapper();
    ExchangeData<String> req = new ExchangeData<>(Obj.writeValueAsString(invoiceData10));
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), qReq.toString()));
    if (retryCount != null){
      req.setRetryCount(retryCount);
    }

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }

}
