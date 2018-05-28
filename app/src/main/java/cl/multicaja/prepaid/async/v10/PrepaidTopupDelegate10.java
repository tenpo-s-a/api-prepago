package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @autor vutreras
 */
public final class PrepaidTopupDelegate10 {

  private static Log log = LogFactory.getLog(PrepaidTopupDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private ProducerTemplate producerTemplate;

  public PrepaidTopupDelegate10() {
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
   * @param prepaidTopup
   * @param user
   * @return
   */
  public String sendTopUp(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%s#%s", prepaidTopup.getMerchantCode(), prepaidTopup.getTransactionId(), prepaidTopup.getId(), Utils.uniqueCurrentTimeNano());
    System.out.println("Enviando mensaje por messageId: " + messageId);
    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    prepaidTopup.setMessageId(messageId);
    this.getProducerTemplate().sendBodyAndHeaders("seda:PrepaidTopupRoute10.pendingTopup", new RequestRoute<>(new PrepaidTopupDataRoute10(prepaidTopup, user, cdtTransaction, prepaidMovement)), headers);
    return messageId;
  }
}
