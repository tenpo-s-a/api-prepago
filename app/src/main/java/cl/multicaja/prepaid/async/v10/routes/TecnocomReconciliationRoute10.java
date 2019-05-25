package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.prepaid.async.v10.processors.PendingTecnocomReconciliationFile10;

/**
 * @author abarazarte
 **/
public class TecnocomReconciliationRoute10 extends BaseRoute10 {

  //public static final String PENDING_PROCESS_TECNOCOM_FILE_REQ = "TecnocomReconciliationRoute10.pendingProcessTecnocomFile.req";
  public static final String PENDING_PROCESS_TECNOCOM_FILE_REQ = "TestRoutes.processTecnocomFileResult";

  @Override
  public void configure() throws Exception {
    int concurrentConsumers = 10;
    from(createJMSEndpoint(String.format("%s?concurrentConsumers=%s", PENDING_PROCESS_TECNOCOM_FILE_REQ, concurrentConsumers)))
      .process(new PendingTecnocomReconciliationFile10(this).processReconciliationFile());
  }
}
