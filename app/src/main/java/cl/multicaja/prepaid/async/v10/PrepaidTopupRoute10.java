package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelRouteBuilder;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementacion personalizada de rutas camel
 *
 * @autor vutreras
 */
public final class PrepaidTopupRoute10 extends CamelRouteBuilder {

  private static Log log = LogFactory.getLog(PrepaidTopupRoute10.class);

  public PrepaidTopupRoute10() {
    super();
  }

  @Override
  public void configure() {

    int concurrentConsumers = 10;
    int sedaSize = 1000;

    //consume un mensaje desde un componente seda de alta velocidad y lo envia a una cola de requerimientos
    from(String.format("seda:PrepaidTopupRoute10.topUp?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint("PrepaidTopupRoute10.topUp.req"));

    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("PrepaidTopupRoute10.topUp.req?concurrentConsumers=%s", concurrentConsumers)))
      .process(this.processPrepaidTopupRequestRoute()).end();
  }

  private ProcessorRoute processPrepaidTopupRequestRoute() {
    return new ProcessorRoute<PrepaidTopupRequestRoute10, ResponseRoute>() {
      @Override
      public ResponseRoute processExchange(long idTrx, PrepaidTopupRequestRoute10 req, Exchange exchange) throws Exception {
        //TODO implementar logica

        System.out.println("REQ::::" + req);

        return new ResponseRoute(req.getNewPrepaidTopup());
      }
    };
  }
}
