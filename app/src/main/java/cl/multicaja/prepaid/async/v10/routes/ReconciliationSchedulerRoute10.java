package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.ReconciliationScheduler10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author abarazarte
 **/
public class ReconciliationSchedulerRoute10 extends BaseRoute10 {

  private Log log = LogFactory.getLog(ReconciliationSchedulerRoute10.class);

  public ReconciliationSchedulerRoute10() {
    super();
  }

  @Override
  public void configure() throws Exception {
    //TODO: definir cada cuanto se ejecuta esta tarea
    //from("quartz2://myGroup/myfirstrigger?cron=0/2+*+*+*+*+?")
     // .process(new ReconciliationScheduler10(this).processReconciliation());
  }
}
