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
public final class TopUpAmountRoute10 extends CamelRouteBuilder {

  private static Log log = LogFactory.getLog(TopUpAmountRoute10.class);

  public TopUpAmountRoute10() {
    super();
  }

  @Override
  public void configure() {

    int concurrentConsumers = 10;
    int sedaSize = 1000;

    //consume un mensaje desde un componente seda de alta velocidad y lo envia a una cola de requerimientos
    from(String.format("seda:TopUpAmountRoute10.topUp?concurrentConsumers=%s&size=%s", concurrentConsumers, sedaSize))
      .to(createJMSEndpoint("TopUpAmountRoute10.topUp.req"));

    //consume un mensaje desde una cola de requerimientos y lo envia a una cola de respuestas
    from(createJMSEndpoint(String.format("TopUpAmountRoute10.topUp.req?concurrentConsumers=%s", concurrentConsumers)))
      .process(this.processTopUp()).end();
  }

  private ProcessorRoute processTopUp() {
    return new ProcessorRoute<RequestRoute, ResponseRoute>() {
      @Override
      public ResponseRoute processExchange(long idTrx, RequestRoute req, Exchange exchange) throws Exception {
        //TODO implementar logica
        return new ResponseRoute(req.getData());
      }
    };
  }
}
