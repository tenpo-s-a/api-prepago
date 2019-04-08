package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSMessenger;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import cl.multicaja.prepaid.model.v10.NewPrepaidBaseTransaction10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.QueuesNameType;
import org.apache.activemq.ScheduledMessage;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Queue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase base para todos los processor del proceso asincrono
 *
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
   * Crea un queue endpoint para rutas apache camel
   *
   * @param queueName
   * @return
   */
  public Endpoint createJMSEndpoint(String queueName) {
    return camelFactory.createJMSEndpoint(queueName);
  }

  /**
   * Crea un queue para el envio o recepcion de mensajes usando JMSMessenger
   *
   * @param queueName
   * @return
   */
  public Queue createJMSQueue(String queueName) {
    return camelFactory.createJMSQueue(queueName);
  }

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ usando el pool de conexiones
   * por defecto
   *
   * @return
   */
  public JMSMessenger createJMSMessenger() {
    return camelFactory.createJMSMessenger();
  }

  /**
   * crea y retorna una instancia de JMSMessenger para envio y recepcion de mensajes MQ con configuraciones especificas
   * de timeout
   *
   * @param receiveTimeout tiempo de espera en recibir un mensaje
   * @param timeToLive tiempo de vida en una cola MQ de un mensaje que se envia
   * @return
   */
  public JMSMessenger createJMSMessenger(long receiveTimeout, long timeToLive) {
    return camelFactory.createJMSMessenger(receiveTimeout, timeToLive);
  }

  /**
   * envia el mensaje a otra ruta camel
   *
   * @param endpoint endpoint camel al cual se desea enviar el mensaje
   * @param exchange instancia del mensaje original camel
   * @param req instancia del mensaje propio del proceso asincrono
   */
  @SuppressWarnings({"unchecked"})
  protected Object redirectRequestObject(Endpoint endpoint, Exchange exchange, Object req) {
    log.info("redirectRequestObject - " + endpoint.getEndpointUri());
    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, exchange.getIn().getHeaders());
    return req;
  }

  /**
   * envia el mensaje a otra ruta camel con un tiempo de espera establecido
   *
   * @param endpoint endpoint camel al cual se desea enviar el mensaje
   * @param exchange instancia del mensaje original camel
   * @param req instancia del mensaje propio del proceso asincrono
   * @param delayTimeoutToRedirect tiempo en milisegundos de espera para enviar el mensaje
   */
  @SuppressWarnings({"unchecked"})
  protected Object redirectRequestObject(Endpoint endpoint, Exchange exchange, Object req, long delayTimeoutToRedirect) {

    log.info("redirectRequestObject - " + endpoint.getEndpointUri() + ", delayTimeoutToRedirect: " + delayTimeoutToRedirect);

    Map<String, Object> headers = exchange.getIn().getHeaders();

    if (headers == null) {
      headers = new HashMap<>();
    }

    //si tiene tiempo de espera establecido como parametro significa que se desea enviar un mensaje con tiempo de espera
    //para esto se usa una caracteristica especial de ActiveMQ, se debe establecer en las cabeceras del mensaje
    //que el mensaje sera con tiempo de espera
    if (delayTimeoutToRedirect > 0 && !ConfigUtils.isEnvTest()) {
      log.debug("Estableciendo delayTimeoutToRedirect: " + delayTimeoutToRedirect);
      headers.put(ScheduledMessage.AMQ_SCHEDULED_DELAY, delayTimeoutToRedirect); //TODO si se migra a azure se debe investigar como se envian mensajes programados
      headers.remove("scheduledJobId"); //es necesario remover el scheduledJobId si existe con anterioridad en el mensaje
    }

    exchange.getContext().createProducerTemplate().sendBodyAndHeaders(endpoint, req, headers);
    return req;
  }

  /**
   * Configuracion basica de timeouts para los mensajes con espera usado en los reintentos
   *
   * 2do intento en +10s
   * 3to intento en +50s
   */
  private long[] arrayDelayTimeoutToRedirect = {
    10000L, //representa el valor de tiempo de espera del 2do intento
    50000L, //representa el valor de tiempo de espera del 3er intento
    0L //el 4to intento significa que debe terminar el proceso, por eso es 0, no se debe esperar
  };

  /**
   * Retorna un array de configuraciones de tiempos de espera
   *
   * @return
   */
  protected long[] getArrayDelayTimeoutToRedirect() {
    return arrayDelayTimeoutToRedirect;
  }

  /**
   * retorn el valor de DelayTimeoutToRedirect para un intento especifico
   *
   * @param retryCount
   * @return
   */
  protected long getDelayTimeoutToRedirectForRetryCount(int retryCount) {
    long[] array = getArrayDelayTimeoutToRedirect();
    if (retryCount < 0 || array == null || array.length == 0 || retryCount > array.length) {
      return 0L;
    }
    if(retryCount == 0) {
      return array[retryCount];
    }
    return array[retryCount-1];
  }

  /**
   * retorna el total de intentos maximos calculado en base a la cantidad del array retornado por getArrayDelayTimeoutToRedirect
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
   * envia el mensaje a otra ruta camel, especificamente una instancia de: ExchangeData<PrepaidTopupData10>
   *
   * @param endpoint endpoint camel al cual se desea enviar el mensaje
   * @param exchange instancia del mensaje original camel
   * @param req instancia del mensaje propio del proceso asincrono
   * @param withDelay true: es un envio de mensaje con tiempo de espera, false: es un envio simple de mensaje sin tiempo de espera
   * @return
   */
  protected ExchangeData<PrepaidTopupData10> redirectRequest(Endpoint endpoint, Exchange exchange, ExchangeData<PrepaidTopupData10> req, boolean withDelay) {
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));

    if (withDelay) {
      redirectRequestObject(endpoint, exchange, req, getDelayTimeoutToRedirectForRetryCount(req.getRetryCount()));
    } else {
      req.setRetryCount(0);
      redirectRequestObject(endpoint, exchange, req);
    }
    return req;
  }
  /**
   * envia el mensaje a otra ruta camel, especificamente una instancia de: ExchangeData<PrepaidTopupData10>
   *
   * @param endpoint endpoint camel al cual se desea enviar el mensaje
   * @param exchange instancia del mensaje original camel
   * @param req instancia del mensaje propio del proceso asincrono
   * @param withDelay true: es un envio de mensaje con tiempo de espera, false: es un envio simple de mensaje sin tiempo de espera
   * @return
   */
  protected ExchangeData<PrepaidReverseData10> redirectRequestReverse(Endpoint endpoint, Exchange exchange, ExchangeData<PrepaidReverseData10> req, boolean withDelay) {
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));

    if (withDelay) {
      redirectRequestObject(endpoint, exchange, req, getDelayTimeoutToRedirectForRetryCount(req.getRetryCount()));
    } else {
      redirectRequestObject(endpoint, exchange, req);
    }
    return req;
  }

  /**
   * envia el mensaje a otra ruta camel, especificamente una instancia de: ExchangeData<PrepaidProductChangeData10>
   *
   * @param endpoint endpoint camel al cual se desea enviar el mensaje
   * @param exchange instancia del mensaje original camel
   * @param req instancia del mensaje propio del proceso asincrono
   * @param withDelay true: es un envio de mensaje con tiempo de espera, false: es un envio simple de mensaje sin tiempo de espera
   * @return
   */
  protected ExchangeData<PrepaidProductChangeData10> redirectRequestProductChange(Endpoint endpoint, Exchange exchange, ExchangeData<PrepaidProductChangeData10> req, boolean withDelay) {
    req.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));

    if (withDelay) {
      redirectRequestObject(endpoint, exchange, req, getDelayTimeoutToRedirectForRetryCount(req.getRetryCount()));
    } else {
      redirectRequestObject(endpoint, exchange, req);
    }
    return req;
  }

  protected CdtTransaction10 callCDT(NewPrepaidBaseTransaction10 prepaidTopup10, PrepaidUser10 user, Long txRef, CdtTransactionType txType) throws Exception {
    // Incluir datos en CDT.
    CdtTransaction10 cdtTx = new CdtTransaction10();
    cdtTx.setAmount(prepaidTopup10.getAmount().getValue());
    cdtTx.setAccountId(String.format("PREPAGO_%s",user.getDocumentNumber()));
    cdtTx.setTransactionReference(txRef);
    cdtTx.setIndSimulacion(false);
    cdtTx.setExternalTransactionId(prepaidTopup10.getTransactionId());
    cdtTx.setTransactionType(txType);
    cdtTx.setGloss(txType.getName());
    cdtTx = getRoute().getCdtEJBBean10().addCdtTransaction(null,cdtTx);
    return cdtTx;
  }
  protected NewTicket createTicket(String subject, String template,String uniqueExternalId,String messgeID,QueuesNameType queuesNameType,Integer intentos){
    NewTicket newTicket = new NewTicket();
    newTicket.setDescription(template);
    newTicket.setGroupId(GroupId.OPERACIONES);
    newTicket.setUniqueExternalId(uniqueExternalId);
    newTicket.setType(TicketType.COLAS_NEGATIVAS);
    newTicket.setStatus(StatusType.OPEN);
    newTicket.setPriority(PriorityType.URGENT);
    newTicket.setSubject(subject);
    newTicket.setProductId(43000001595L);
    // Ticket Custom Fields:
    newTicket.addCustomField(CustomFieldsName.ID_COLA, messgeID);
    newTicket.addCustomField(CustomFieldsName.NOMBRE_COLA, queuesNameType.getValue());
    newTicket.addCustomField(CustomFieldsName.REINTENTOS,intentos);
    return newTicket;
  }

}
