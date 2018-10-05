package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;

import static cl.multicaja.core.model.Errors.ERROR_CAMELFACTORY;

public final class ReprocesQueueDelegate10 {

  private static Log log = LogFactory.getLog(PrepaidTopupDelegate10.class);
  private CamelFactory camelFactory;

  public ReprocesQueueDelegate10() {
    super();
  }

  public CamelFactory getCamelFactory() {
    if(camelFactory == null)
      camelFactory = CamelFactory.getInstance();
    return camelFactory;
  }

  public String redirectRequest(String endpoint, ExchangeData<PrepaidTopupData10> req) throws ValidationException {
    if (!getCamelFactory().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      throw new ValidationException(ERROR_CAMELFACTORY);
    }
    String messageId = Utils.createUniqueID();
    Queue qReq = getCamelFactory().createJMSQueue(endpoint);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
    getCamelFactory().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));
    return messageId;
  }
  public String redirectRequestReverse(String endpoint, ExchangeData<PrepaidReverseData10> req) throws ValidationException {
    if (!getCamelFactory().isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      throw new ValidationException(ERROR_CAMELFACTORY);
    }
    String messageId = Utils.createUniqueID();
    Queue qReq = getCamelFactory().createJMSQueue(endpoint);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
    getCamelFactory().createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));
    return messageId;
  }

}
