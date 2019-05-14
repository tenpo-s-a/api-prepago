package cl.multicaja.prepaid.async.v10.routes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author abarazarte
 **/
public class ResearchFileSchedulerRoute10 extends BaseRoute10 {

  private Log log = LogFactory.getLog(ResearchFileSchedulerRoute10.class);

  public ResearchFileSchedulerRoute10() {
    super();
  }

  @Override
  public void configure() throws Exception {
    //FIXME: Este scheduler debe estar en el proyecto prepaid-batch-router.
    // En este caso se debe escuchar una cola Activemq para procesar dicho archivo.
    //from("quartz2://myGroup/myfirstrigger?cron=0 0/5 * 1/1 * ? *")
    //  .process(new ResearchFileScheduler10(this).sendResearchEmail());
  }
}
