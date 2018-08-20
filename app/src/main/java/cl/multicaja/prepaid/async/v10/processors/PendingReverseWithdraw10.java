package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.PENDING_TOPUP_REQ;

public class PendingReverseWithdraw10 extends BaseProcessor10  {

  private static Log log = LogFactory.getLog(PendingReverseWithdraw10.class);

  public PendingReverseWithdraw10(BaseRoute10 route) {
    super(route);
  }

  public ProcessorRoute processPendingWithdrawReverse() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        try {
          log.info("processPendingTopup - REQ: " + req);
          req.retryCountNext();

          PrepaidTopupData10 data = req.getData();
          PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();
          PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();
          CdtTransaction10 cdtTransaction = data.getCdtTransaction10();

          if (cdtTransaction == null) {
            log.error("Error req.getCdtTransaction10() es null");
            return null;
          }
          if (prepaidTopup == null) {
            log.error("Error req.getPrepaidTopup10() es null");
            return null;
          }

          if(req.getRetryCount() > getMaxRetryCount()) {
            PrepaidMovementStatus status;
            if (Errors.TECNOCOM_ERROR_REINTENTABLE.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TECNOCOM;
            } else if(Errors.TECNOCOM_TIME_OUT_CONEXION.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION;
            } else if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE;
            } else {
              status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
            }
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);
            prepaidMovement.setEstado(status);
            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_RETURNS_REQ);
            return redirectRequest(endpoint, exchange, req, false);
          }

          // Se envia TX a CDT para revertir datos de acumuladores
          cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_RETIRO);
          cdtTransaction.setGloss(CdtTransactionType.REVERSA_RETIRO.getName());
          cdtTransaction = getRoute().getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

          cdtTransaction.setTransactionType(CdtTransactionType.REVERSA_RETIRO_CONF);
          cdtTransaction.setGloss(CdtTransactionType.REVERSA_RETIRO_CONF.getName());
          cdtTransaction = getRoute().getCdtEJBBean10().addCdtTransaction(null, cdtTransaction);

          if(!cdtTransaction.isNumErrorOk()){
            log.debug(String.format("Error code: %s", cdtTransaction.getNumErrorInt()));
            log.debug(String.format("Error msg: %s", cdtTransaction.getMsjError()));
          }

        }catch (Exception e){
          log.error(String.format("Error desconocido al realizar carga %s",e.getLocalizedMessage()));
          Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_REQ);
          req.getData().setNumError(Errors.ERROR_INDETERMINADO);
          req.getData().setMsjError(e.getLocalizedMessage());
          return redirectRequest(endpoint, exchange, req, true);
        }
        return req;
      }
    };
  }
  public ProcessorRoute processErrorWithdrawReverse() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        return req;
      }
    };
  }

}
