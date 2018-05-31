package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @autor abarazarte
 */
public class PendingSendMail10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingSendMail10.class);

  public PendingSendMail10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  /**
   *
   * @return
   */
  public ProcessorRoute processPendingSendMailCard() {

    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingIssuanceFee - REQ: " + req);
        PrepaidTopupDataRoute10 data = req.getData();
        PrepaidMovement10 prepaidMovement10 = data.getPrepaidMovement10();

        if (prepaidMovement10 == null) {
          log.error("Error req.getData().getPrepaidMovement10() es null");
          return null;
        }

        req.retryCountNext();

        if(req.getRetryCount() > 3) {
          req.setRetryCount(0);
          redirectRequest(createJMSEndpoint(getRoute().PENDING_SEND_MAIL_CARD_RESP), exchange, req);
          return new ResponseRoute<>(data);
        }

        //TODO: Se debe verificar si es necesario llamar a Tecnocom

        if (true) {
          //TODO: Se debe generar el PDF y enviar el mail a api user.

          req.setRetryCount(0);
        }
        else {
          req.setRetryCount(0);
          redirectRequest(createJMSEndpoint(getRoute().PENDING_SEND_MAIL_CARD_RESP), exchange, req);
        }

        return new ResponseRoute<>(data);
      }
    };
  }

  /* Cola Errores */
  public ProcessorRoute processError() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
        log.info("processError - REQ: " + req);

        PrepaidTopupDataRoute10 data = req.getData();

        return new ResponseRoute<>(data);
      }
    };
  }
}
