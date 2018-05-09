package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.RequestRoute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @autor vutreras
 */
public final class TopUpAmountDelegate10 {

  private CamelFactory camelFactory = CamelFactory.getInstance();

  public TopUpAmountDelegate10() {
    super();
  }

  /**
   * Envia un registro de topup al proceso asincrono
   * @param obj objeto que se desea enviar
   * @return id del mensaje enviado
   */
  public String sendTopUp(Serializable obj) {
    String messageId = camelFactory.createUniqueMessageID();
    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    camelFactory.createProducerTemplate().sendBodyAndHeaders("seda:TopUpAmountRoute10.topUp", new RequestRoute(obj), headers);
    return messageId;
  }
}
