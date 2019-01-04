package cl.multicaja.accounting.async.v10.routes;

import cl.multicaja.accounting.async.v10.processors.AccountingScheduler10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author abarazarte
 **/
public class AccountingSchedulerRoute10 extends BaseRoute10 {

  private Log log = LogFactory.getLog(AccountingSchedulerRoute10.class);

  public AccountingSchedulerRoute10() {
    super();
  }

  @Override
  public void configure() throws Exception {
    //TODO: definir cada cuanto se ejecuta esta tarea

    // Cada 5to dia del mes a las 22H
    //from("quartz2://myGroup/myfirstrigger?cron=0+0+22+5+1/1+?+*")
    // .process(new AccountingScheduler10(this).generateAccountingFile());
  }
}
