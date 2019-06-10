package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.PendingConciliationMcRed10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConciliationMcRedRoute10 extends BaseRoute10 {

  private static Log log = LogFactory.getLog(CurrencyConvertionRoute10.class);
  public static final String PENDING_PROCESS_SWITCHMC_FILE_REQ = "TestRoutes.processSwitchMcFileResult";

  @Override
  public void configure() throws Exception {
    int concurrentConsumers = 10;
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_PROCESS_SWITCHMC_FILE_REQ, concurrentConsumers)))
      .process(new PendingConciliationMcRed10(this).processReconciliationsMcRed());
  }
}
