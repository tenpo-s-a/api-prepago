package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.JMSMessenger;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.utils.ParametersUtil;
import org.apache.camel.Endpoint;

import javax.jms.Queue;

/**
 * @autor vutreras
 */
public abstract class BaseProcessor10 {

  private PrepaidTopupRoute10 route;

  public BaseProcessor10(PrepaidTopupRoute10 route) {
    this.route = route;
  }

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    return this.getRoute().getConfigUtils();
  }

  public EncryptUtil getEncryptUtil(){
    return this.getRoute().getEncryptUtil();
  }

  public ParametersUtil getParametersUtil() {
    return this.getRoute().getParametersUtil();
  }

  public PrepaidTopupRoute10 getRoute() {
    return route;
  }

  public void setRoute(PrepaidTopupRoute10 route) {
    this.route = route;
  }

  public PrepaidMovementEJBBean10 getPrepaidMovementEJBBean10() {
    return this.getRoute().getPrepaidMovementEJBBean10();
  }

  public PrepaidEJBBean10 getPrepaidEJBBean10() {
    return this.getRoute().getPrepaidEJBBean10();
  }


  public UsersEJBBean10 getUsersEJBBean10() {
    return this.getRoute().getUsersEJBBean10();
  }

  public TecnocomService getTecnocomService() {
    return this.getRoute().getTecnocomService();
  }

  public CdtEJBBean10 getCdtEJBBean10() {
    return this.getRoute().getCdtEJBBean10();
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
}
