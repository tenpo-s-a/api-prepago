package cl.multicaja.camel;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.Utils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Clase Processor camel, se restringe a uso de Serializable como datos de entrada y salida
 *
 * @autor vutreras
 */
public abstract class ProcessorRoute<REQ extends Serializable, RESP extends Serializable> implements Processor {

  private static final Log log = LogFactory.getLog(ProcessorRoute.class);

  /**
   *p
   */
  public ProcessorRoute() {
    super();
  }

  /**
   *
   * @param idTrx
   * @param req
   * @param exchange
   * @return
   * @throws Exception
   */
  public abstract RESP processExchange(long idTrx, REQ req, Exchange exchange) throws Exception;

  @Override
  public void process(Exchange exchange) {

    long idTrx = Utils.uniqueCurrentTimeNano();

    ExchangeContext exchangeContext = new ExchangeContext();
    exchangeContext.setIdTrx(idTrx);
    exchangeContext.setTimestampStart(System.currentTimeMillis());

    RESP resp = null;

    try {
      REQ req = (REQ)exchange.getIn().getBody();
      resp = this.processExchange(idTrx, req, exchange);
    } catch (Exception ex) {
      log.error(idTrx + " - Error al procesar mensaje: ", ex);
      exchangeContext.setException(new BaseException(ex, 1000, "Error procesando mensaje camel: " + ex.getMessage()));
      try {
        Type superclass = this.getClass().getGenericSuperclass();
        //Type typeReq = ((ParameterizedType)superclass).getActualTypeArguments()[0];
        Type typeResp = ((ParameterizedType)superclass).getActualTypeArguments()[1];
        Class clsResp = Class.forName(typeResp.getTypeName());
        resp = (RESP)clsResp.newInstance();
      } catch(Exception ex2) {
      }
    }

    exchangeContext.setTimestampEnd(System.currentTimeMillis());
    exchangeContext.calcTimeProcess();

    if (resp != null && resp instanceof ResponseRoute) {
      ((ResponseRoute) resp).setExchangeContext(exchangeContext);
    }

    exchange.getOut().setHeaders(exchange.getIn().getHeaders());
    exchange.getOut().setBody(resp);
  }
}
