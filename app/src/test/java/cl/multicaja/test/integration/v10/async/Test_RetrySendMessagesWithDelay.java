package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.BaseModel;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.Queue;
import java.io.Serializable;

/**
 * Test para probar los mensajes con tiempo de retraso
 *
 * @autor vutreras
 */
@SuppressWarnings("unchecked")
public class Test_RetrySendMessagesWithDelay extends TestBaseUnitAsync {

  private static final String TEST_DELAY_REQ = "TestRoute10.testDelay.req";
  private static final String TEST_DELAY_RESP = "TestRoute10.testDelay.resp";

  private static class TimeInfo extends BaseModel implements Serializable {

    private long tInit;
    private long tEnd;

    public TimeInfo(long tInit, long tEnd) {
      this.tInit = tInit;
      this.tEnd = tEnd;
    }

    public long gettInit() {
      return tInit;
    }

    public void settInit(long tInit) {
      this.tInit = tInit;
    }

    public long gettEnd() {
      return tEnd;
    }

    public void settEnd(long tEnd) {
      this.tEnd = tEnd;
    }

    public long getDiff() {
      return (tEnd - tInit);
    }

    public String getDiffFormated() {
      long diff = getDiff();
      if (diff > 0) {
        return DurationFormatUtils.formatDuration(getDiff(), "HH:mm:ss,SSS");
      } else {
        return null;
      }
    }

    @Override
    public String toString() {
      return super.toString() + ", diff: " + getDiff() + ", diffFormated: " + getDiffFormated();
    }
  }

  /**
   * de define un processor de prueba
   */
  private static class TestProcessor extends BaseProcessor10 {

    public TestProcessor(BaseRoute10 route) {
      super(route);
    }

    public ProcessorRoute defaultProcessor() {
      return new ProcessorRoute<ExchangeData<TimeInfo>, ExchangeData<TimeInfo>>() {
        @Override
        public ExchangeData<TimeInfo> processExchange(long idTrx, ExchangeData<TimeInfo> req, Exchange exchange) throws Exception {

          req.retryCountNext();

          req.getData().settEnd(System.currentTimeMillis());

          System.out.println(req.getRetryCount() + " - defaultProcessor - REQ: " + req.getData());

          if (req.getRetryCount() > getMaxRetryCount()) {
            Endpoint endpoint = createJMSEndpoint(TEST_DELAY_RESP);
            return (ExchangeData<TimeInfo>)redirectRequestObject(endpoint, exchange, req);
          } else {
            Endpoint endpoint = createJMSEndpoint(TEST_DELAY_REQ);
            return (ExchangeData<TimeInfo>)redirectRequestObject(endpoint, exchange, req, getDelayTimeoutToRedirectForRetryCount(req.getRetryCount()));
          }
        }
      };
    }

    @Override
    protected long[] getArrayDelayTimeoutToRedirect() {
      long[] arrayDelayTimeoutToRedirect = {
        3000L, //representa el valor de tiempo de espera del 2do intento
        6000L, //representa el valor de tiempo de espera del 3er intento
        0L //el 4to intento significa que debe terminar el proceso, por eso es 0, no se debe esperar
      };
      return arrayDelayTimeoutToRedirect;
    }
  }

  @BeforeClass
  public static void beforeClass() throws Exception {

    TestBaseUnitAsync.beforeClass();

    //se crea una ruta de pruebas
    BaseRoute10 testRoute = new BaseRoute10() {
      @Override
      public void configure() {
        from(createJMSEndpoint(TEST_DELAY_REQ)).process(new TestProcessor(this).defaultProcessor()).end();
      }
    };

    camelFactory.getCamelContext().addRoutes(testRoute);
  }

  @Test
  public void retrySendMessagesWithDelay() throws Exception {

    long tInit = System.currentTimeMillis();

    Queue qReq = camelFactory.createJMSQueue(TEST_DELAY_REQ);

    ExchangeData<TimeInfo> req = new ExchangeData<>();
    req.setData(new TimeInfo(tInit, 0));

    String messageId = getRandomString(20);

    camelFactory.createJMSMessenger().putMessage(qReq, messageId, req);

    Thread.sleep(11000);

    Queue qResp = camelFactory.createJMSQueue(TEST_DELAY_RESP);
    ExchangeData<TimeInfo> resp = (ExchangeData<TimeInfo>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("deberia existir una respuesta", resp);

    System.out.println("DiffFormated: " + resp.getData().getDiffFormated());

    Assert.assertTrue("debe ser una diferencia mayor a 9 segundos", resp.getData().getDiff() > 9000);
  }

}
