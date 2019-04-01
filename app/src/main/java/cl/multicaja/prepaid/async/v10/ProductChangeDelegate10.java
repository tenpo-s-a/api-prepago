package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.TipoAlta;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @author abarazarte
 **/
public class ProductChangeDelegate10 {

  private static Log log = LogFactory.getLog(ProductChangeDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private ProducerTemplate producerTemplate;

  public ProductChangeDelegate10() {
    super();
  }

  /**
   *
   * @return
   */
  public ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  /**
   * Envia un registro de topup al proceso asincrono
   *
   * @param prepaidUser
   * @param prepaidCard10
   * @return
   */
  public String sendProductChange(PrepaidUser10 prepaidUser, PrepaidCard10 prepaidCard10, TipoAlta tipoAlta) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s", prepaidUser.getId(), prepaidUser.getDocument());
    Queue qReq = camelFactory.createJMSQueue(ProductChangeRoute10.PENDING_PRODUCT_CHANGE_REQ);
    PrepaidProductChangeData10 data = new PrepaidProductChangeData10(prepaidUser, prepaidCard10, tipoAlta);

    ExchangeData<PrepaidProductChangeData10> req = new ExchangeData<>(data);
    req.setRetryCount(0);
    req.getProcessorMetadata().add(new ProcessorMetadata(0, qReq.toString()));
    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req, new JMSHeader("JMSCorrelationID", messageId));

    return messageId;
  }
}
