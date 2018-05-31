package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.*;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10.*;

/**
 * @autor abarazarte
 */
public class PendingCardIssuanceFee10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCardIssuanceFee10.class);

  public PendingCardIssuanceFee10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  /**
   *
   * @return
   */
  public ProcessorRoute processPendingIssuanceFee() {

    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingIssuanceFee - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        PrepaidMovement10 prepaidMovement10 = data.getPrepaidMovement10();

        if (prepaidMovement10 == null) {
          log.error("Error req.getData().getPrepaidMovement10() es null");
          return null;
        }

        if(req.getRetryCount() > 3) {
          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }

        //TODO: Verificar de donde sacar el monto de la comision de apertura

        //InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos("", "", CodigoMoneda.CHILE_CLP,
        //  IndicadorNormalCorrector.NORMAL, TipoFactura.COMISION_APERTURA, "", prepaidMovement10.getMonto(), "", "", "", 0, CodigoPais.CHILE);

        if (CodigoRetorno._000.equals(CodigoRetorno._000)) {
        //if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._000)) {
          /*
          getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            prepaidMovement10.getId(),
            inclusionMovimientosDTO.getNumextcta(),
            inclusionMovimientosDTO.getNummovext(),
            inclusionMovimientosDTO.getClamone(),
            PrepaidMovementStatus.PROCESS_OK);
            */
          req.setRetryCount(0);
        } else if (CodigoRetorno._000.equals(CodigoRetorno._1000)) {
        //} else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          redirectRequest(endpoint, exchange, req);
        } else {
          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
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

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
          data.getPrepaidMovement10().getId(),
          null,
          null,
          null,
          PrepaidMovementStatus.ERROR_IN_PROCESS);

        return new ResponseRoute<>(data);
      }
    };
  }
}
