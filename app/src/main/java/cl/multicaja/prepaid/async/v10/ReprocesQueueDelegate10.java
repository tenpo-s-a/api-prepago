package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.ERROR_CAMELFACTORY;

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

  public ExchangeData<PrepaidReverseData10> searchInErroReverse(String messageId, String route){
    Queue qResp = camelFactory.createJMSQueue(route);
    ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteReverse;
  }

  public ExchangeData<PrepaidTopupData10> searchInErrorTopup(String messageId, String route){
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_EMISSION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    return remoteTopup;
  }


  public void redirectRequest(String endpoint, String messageId, ExchangeData<PrepaidTopupData10> req) throws ValidationException {
    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      throw new ValidationException(ERROR_CAMELFACTORY);
    }
    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    if(req.getData().getPrepaidTopup10() != null) {
      req.getData().getPrepaidTopup10().setMessageId(messageId);
    }
    if(req.getData().getPrepaidWithdraw10() != null) {
      req.getData().getPrepaidWithdraw10().setMessageId(messageId);
    }
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));
    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);

  }
  public void redirectRequestReverse(String endpoint, String messageId, ExchangeData<PrepaidReverseData10> req) throws ValidationException {
    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      throw new ValidationException(ERROR_CAMELFACTORY);
    }
    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    if(req.getData().getPrepaidTopup10() != null) {
      req.getData().getPrepaidTopup10().setMessageId(messageId);
    }
    if(req.getData().getPrepaidWithdraw10() != null) {
      req.getData().getPrepaidWithdraw10().setMessageId(messageId);
    }
    req.getProcessorMetadata().add(new ProcessorMetadata(0, endpoint));
    this.getProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);
  }

}
