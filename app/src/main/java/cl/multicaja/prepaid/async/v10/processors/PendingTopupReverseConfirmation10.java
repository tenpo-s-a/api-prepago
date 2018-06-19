package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @autor abarazarte
 */
public class PendingTopupReverseConfirmation10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingTopupReverseConfirmation10.class);

  public PendingTopupReverseConfirmation10(BaseRoute10 route) {
    super(route);
  }

  public ProcessorRoute processPendingTopupReverseConfirmation() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopupReverseConfirmation - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        CdtTransaction10 cdtTransaction = data.getCdtTransaction10();

        if (cdtTransaction == null) {
          log.error("Error req.getCdtTransaction10() es null");
          return null;
        }

        PrepaidTopup10 topupRequest = data.getPrepaidTopup10();
        if (topupRequest == null) {
          log.error("Error req.getPrepaidTopup10() es null");
          return null;
        }
        // TODO: revisar este proceso
        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
        cdtTransaction.setGloss(CdtTransactionType.REVERSA_CARGA.getName());

        cdtTransaction = getRoute().getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

        // Si hay error
        //TODO: que hacer si falla?
        if(!cdtTransaction.isNumErrorOk()){
          log.debug(String.format("Error code: %s", cdtTransaction.getNumErrorInt()));
          log.debug(String.format("Error msg: %s", cdtTransaction.getMsjError()));
        }
        return req;
      }
    };
  }

}
