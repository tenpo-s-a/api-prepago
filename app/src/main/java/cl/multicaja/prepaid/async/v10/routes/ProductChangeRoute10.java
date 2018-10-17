package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.PendingProductChange10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author abarazarte
 **/
public class ProductChangeRoute10 extends BaseRoute10  {

  private static Log log = LogFactory.getLog(ProductChangeRoute10.class);

  public static final String PENDING_PRODUCT_CHANGE_REQ = "ProductChangeRoute10.pendingProductChange.req";
  public static final String PENDING_PRODUCT_CHANGE_RESP = "ProductChangeRoute10.pendingProductChange.resp";

  public static final String ERROR_PRODUCT_CHANGE_REQ = "ProductChangeRoute10.errorProductChange.req";
  public static final String ERROR_PRODUCT_CHANGE_RESP = "ProductChangeRoute10.errorProductChange.resp";

  @Override
  public void configure() throws Exception {
    int concurrentConsumers = 10;

    //los mensajes de las colas de respuesta se usan para verificaciones en los test, en la practica no se usan realmente
    //dado eso se establece un tiempo de vida de esos mensajes de solo 10 minutos
    String confResp = "?timeToLive=" + 600000;

    /**
     * Cambio de producto
     */
    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_PRODUCT_CHANGE_REQ, concurrentConsumers)))
      .process(new PendingProductChange10(this).processPendingProductChange())
      .to(createJMSEndpoint(PENDING_PRODUCT_CHANGE_RESP + confResp)).end();

    //Errores Reversa de carga
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", ERROR_PRODUCT_CHANGE_REQ, concurrentConsumers)))
      .process(new PendingProductChange10(this).processErrorProductChange())
      .to(createJMSEndpoint(ERROR_PRODUCT_CHANGE_RESP)).end();

  }
}
