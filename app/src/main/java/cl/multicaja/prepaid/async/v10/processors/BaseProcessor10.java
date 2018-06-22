package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.*;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.activemq.ScheduledMessage;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor vutreras
 */
public abstract class BaseProcessor10 {

  private static Log log = LogFactory.getLog(BaseProcessor10.class);

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
  @SuppressWarnings({"unchecked"})
  protected Object redirectRequestObject(Endpoint endpoint, Exchange exchange, Object req) {
    log.info("redirectRequestObject - " + endpoint.getEndpointUri());
    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, exchange.getIn().getHeaders());
    return req;
  }

  /**
   * permite redirigir el mensaje a otra ruta camel con un tiempo de espera establecido
   *
   * @param endpoint
   * @param exchange
   * @param req
   * @param delayTimeoutToRedirect tiempo en milisegundos de espera para enviar el mensaje
   */
  @SuppressWarnings({"unchecked"})
  protected Object redirectRequestObject(Endpoint endpoint, Exchange exchange, Object req, long delayTimeoutToRedirect) {

    log.info("redirectRequestObject - " + endpoint.getEndpointUri() + ", delayTimeoutToRedirect: " + delayTimeoutToRedirect);

    Map<String, Object> headers = exchange.getIn().getHeaders();

    if (headers == null) {
      headers = new HashMap<>();
    }

    if (delayTimeoutToRedirect > 0) {
      log.debug("Estableciendo delayTimeoutToRedirect: " + delayTimeoutToRedirect);
      headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, delayTimeoutToRedirect);
      headers.remove("scheduledJobId");
    }

    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);
    return req;
  }

  //2do intento en +10s, 3to en +50s
  private long[] arrayDelayTimeoutToRedirect = {
    10000L, //representa el valor de tiempo de espera del 2do intento
    50000L, //representa el valor de tiempo de espera del 3er intento
    0L //el 4to intento significa que debe terminar el proceso, por eso es 0, no se debe esperar
  };

  /**
   * Retorna un array de DelayTimeoutToRedirect
   *
   * @return
   */
  protected long[] getArrayDelayTimeoutToRedirect() {
    return arrayDelayTimeoutToRedirect;
  }

  /**
   * retorn el valor de DelayTimeoutToRedirect para el reintento especifico
   *
   * @param retryCount
   * @return
   */
  protected long getDelayTimeoutToRedirectForRetryCount(int retryCount) {
    long[] array = getArrayDelayTimeoutToRedirect();
    if (retryCount < 0 || array == null || array.length == 0 || retryCount > array.length) {
      return 0L;
    }
    return array[retryCount-1];
  }

  /**
   * retorna el total de reintentos de acuerdo a los timeouts establecidos
   *
   * @return
   */
  protected int getMaxRetryCount() {
    long[] array = getArrayDelayTimeoutToRedirect();
    if (array == null || array.length == 0) {
      return 1;
    } else {
      return array.length;
    }
  }

  /**
   * reenvia un mensaje de  ExchangeData<PrepaidTopupData10>
   *
   * @param endpoint endpoint al cual se desea reenviar el mensaje
   * @param exchange instancia del mensaje de apache camel
   * @param req instancia del mensaje de prepago
   * @param isRetry true: es un mensaje de reintento, usara el tiempo de delay, false: es un mensaje de reenvio simplemente sin reintento
   * @return
   */
  protected ExchangeData<PrepaidTopupData10> redirectRequest(Endpoint endpoint, Exchange exchange, ExchangeData<PrepaidTopupData10> req, boolean isRetry) {

    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));

    if (isRetry) {
      redirectRequestObject(endpoint, exchange, req, getDelayTimeoutToRedirectForRetryCount(req.getRetryCount()));
    } else {
      req.setRetryCount(0);
      redirectRequestObject(endpoint, exchange, req);
    }

    return req;
  }
}
