package cl.multicaja.camel;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;

import javax.jms.Queue;

/**
 * Implementaciṕn basica de RouteBuilder de camel, todas las clases RouteBuilder deben heredar de esta
 *
 * @autor vutreras
 */
public abstract class CamelRouteBuilder extends RouteBuilder {

  protected CamelFactory camelFactory = CamelFactory.getInstance();

  /**
   *
   */
  public CamelRouteBuilder() {
    super();
  }

  /**
   * Crea un queue endpoint ser consumido por apache camel
   *
   * @param queueName
   * @return
   */
  public Endpoint createJMSEndpoint(String queueName) {
    return this.camelFactory.createJMSEndpoint(queueName);
  }

  /**
   * Crea un queue para el envio o recepcion de mensajes usando JMSMessenger
   *
   * @param queueName
   * @return
   */
  public Queue createJMSQueue(String queueName) {
    return this.camelFactory.createJMSQueue(queueName);
  }

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ usando el pool de conexiones
   * por defecto
   *
   * @return
   */
  public JMSMessenger createJMSMessenger() {
    return this.camelFactory.createJMSMessenger();
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
    return this.camelFactory.createJMSMessenger(receiveTimeout, timeToLive);
  }
}
