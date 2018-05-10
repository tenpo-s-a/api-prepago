package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.prepaid.domain.NewPrepaidTopup;
import cl.multicaja.prepaid.domain.PrepaidTopup;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;

import javax.jms.Queue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @autor vutreras
 */
public final class PrepaidTopupDelegate10 {

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
  public String sendTopUp(PrepaidTopup prepaidTopup, User user) {
    String messageId = String.valueOf(prepaidTopup.getId());
    System.out.println("Enviando mensaje por messageId: " + messageId);
    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    this.getProducerTemplate().sendBodyAndHeaders("seda:PrepaidTopupRoute10.topUp", new PrepaidTopupRequestRoute10(prepaidTopup, user), headers);
    return messageId;
  }
}