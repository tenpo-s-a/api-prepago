package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.JMSMessenger;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
public abstract class BaseProcessor10 {

  private BaseRoute10 route;

  protected static CamelFactory camelFactory = CamelFactory.getInstance();

  protected static NumberUtils numberUtils = NumberUtils.getInstance();

  public BaseProcessor10(BaseRoute10 route) {
    super();
    this.route = route;
  }

  /**
   *
   * @return
   */
  public BaseRoute10 getRoute() {
    return this.route;
  }

  /**
   * Crea un queue endpoint ser consumido por apache camel
   *
   * @param queueName
   * @return
   */
  public Endpoint createJMSEndpoint(String queueName) {
    return this.getRoute().createJMSEndpoint(queueName);
  }

  /**
   * Crea un queue para el envio o recepcion de mensajes usando JMSMessenger
   *
   * @param queueName
   * @return
   */
  public Queue createJMSQueue(String queueName) {
    return this.getRoute().createJMSQueue(queueName);
  }

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ usando el pool de conexiones
   * por defecto
   *
   * @return
   */
  public JMSMessenger createJMSMessenger() {
    return this.getRoute().createJMSMessenger();
  }

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ con configuraciones especificas
   * de timeout
   *
   * @param receiveTimeout
   * @param timeToLive
   * @return
   */
  public JMSMessenger createJMSMessenger(long receiveTimeout, long timeToLive) {
    return this.getRoute().createJMSMessenger(receiveTimeout, timeToLive);
  }

  /**
   * permite redirigir el mensaje a otra ruta camel
   *
   * @param endpoint
   * @param exchange
   * @param req
   */
  protected void redirectRequest(Endpoint endpoint, Exchange exchange, Object req) {
    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, exchange.getIn().getHeaders());
  }
}
