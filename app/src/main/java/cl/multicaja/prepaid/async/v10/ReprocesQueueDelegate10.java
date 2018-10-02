package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import org.apache.activemq.ScheduledMessage;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.ERROR_SEND_MAIL_CARD_REQ;

public final class ReprocesQueueDelegate10 {

  private static Log log = LogFactory.getLog(PrepaidTopupDelegate10.class);
  private CamelFactory camelFactory = CamelFactory.getInstance();
  private ProducerTemplate producerTemplate;

  public ReprocesQueueDelegate10() {
    super();
  }

  public ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  public ExchangeData<PrepaidTopupData10> searchInTopupErrorQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidTopupData10> searchInErrorEmissionQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidTopupData10> searchInErrorCreateCardQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CREATE_CARD_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidTopupData10> searchInErrorIssuanceFeeQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidReverseData10> searchInErrorTopupReverseQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidReverseData10> searchInErrorWithdrawReversalQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidTopupData10> searchInErrorPendingSendMailCardQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(ERROR_SEND_MAIL_CARD_REQ);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }

  public ExchangeData<PrepaidTopupData10> searchInErrorPendingWithdrawMailQueue(String messageId){
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_SEND_MAIL_WITHDRAW_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }
  public void reInjectTopup( ExchangeData<PrepaidTopupData10> data){

  }
  protected Object redirectRequestObject(Endpoint endpoint, Exchange exchange, Object req) {
    log.info("redirectRequestObject - " + endpoint.getEndpointUri());
    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, exchange.getIn().getHeaders());
    return req;
  }

  protected Object redirectRequestObject(Endpoint endpoint, Exchange exchange, Object req, long delayTimeoutToRedirect) {

    log.info("redirectRequestObject - " + endpoint.getEndpointUri() + ", delayTimeoutToRedirect: " + delayTimeoutToRedirect);
    Map<String, Object> headers = exchange.getIn().getHeaders();
    if (headers == null) {
      headers = new HashMap<>();
    }
    //si tiene tiempo de espera establecido como parametro significa que se desea enviar un mensaje con tiempo de espera
    //para esto se usa una caracteristica especial de ActiveMQ, se debe establecer en las cabeceras del mensaje
    //que el mensaje sera con tiempo de espera
    if (delayTimeoutToRedirect > 0) {
      log.debug("Estableciendo delayTimeoutToRedirect: " + delayTimeoutToRedirect);
      headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, delayTimeoutToRedirect); //TODO si se migra a azure se debe investigar como se envian mensajes programados
      headers.remove("scheduledJobId"); //es necesario remover el scheduledJobId si existe con anterioridad en el mensaje
    }
    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);
    return req;
  }
  protected ExchangeData<PrepaidTopupData10> redirectRequest(Endpoint endpoint, Exchange exchange, ExchangeData<PrepaidTopupData10> req) {
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
    req.setRetryCount(0);
    redirectRequestObject(endpoint, exchange, req);
    return req;
  }

  protected ExchangeData<PrepaidReverseData10> redirectRequestReverse(Endpoint endpoint, Exchange exchange, ExchangeData<PrepaidReverseData10> req) {
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
    redirectRequestObject(endpoint, exchange, req);
    return req;
  }
}
