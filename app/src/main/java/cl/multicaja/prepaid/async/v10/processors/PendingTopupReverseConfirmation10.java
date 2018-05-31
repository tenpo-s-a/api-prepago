package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
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

  public PendingTopupReverseConfirmation10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  public ProcessorRoute processPendingTopupReverseConfirmation() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopupReverseConfirmation - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

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

        cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_CARGA);
        cdtTransaction.setGloss(CdtTransactionType.REVERSA_CARGA.getName());

        cdtTransaction = getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

        // Si hay error
        //TODO: que hacer si falla?
        if(!cdtTransaction.getNumError().equals("0")){
          log.debug(String.format("Error code: %s", cdtTransaction.getNumError()));
          log.debug(String.format("Error msg: %s", cdtTransaction.getMsjError()));
        }
        return new ResponseRoute<>(data);
      }
    };
  }

}
