package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_REQ;
import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_REQ;

/**
 * @autor abarazarte
 */
public class PendingReverseTopup10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingReverseTopup10.class);

  public PendingReverseTopup10(BaseRoute10 route) {
    super(route);
  }

  public ProcessorRoute processPendingTopupReverse() {
    return new ProcessorRoute<ExchangeData<PrepaidReverseData10>, ExchangeData<PrepaidReverseData10>>() {
      @Override
      public ExchangeData<PrepaidReverseData10> processExchange(long idTrx, ExchangeData<PrepaidReverseData10> req, Exchange exchange) throws Exception {

        try {
          log.info("processPendingTopup - REQ: " + req);
          req.retryCountNext();
          PrepaidReverseData10 data = req.getData();
          PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();
          PrepaidMovement10 prepaidMovementReverse = data.getPrepaidMovementReverse();
          PrepaidCard10 prepaidCard = data.getPrepaidCard10();
          PrepaidUser10 prepaidUser10 = data.getPrepaidUser10();

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
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), status);
            req.getData().getPrepaidMovementReverse().setEstado(status);
            return redirectRequestReverse(createJMSEndpoint(ERROR_REVERSAL_TOPUP_REQ), exchange, req, false);
          }

          // Busca el movimiento de carga original
          PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForReverse(prepaidUser10.getId(),
            prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, TipoFactura.valueOfEnumByCodeAndCorrector(prepaidMovementReverse.getTipofac().getCode(),
              IndicadorNormalCorrector.NORMAL.getValue()));

          if(PrepaidMovementStatus.PENDING.equals(originalMovement.getEstado()) || PrepaidMovementStatus.IN_PROCESS.equals(originalMovement.getEstado())) {
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
          }
          else if (PrepaidMovementStatus.ERROR_TECNOCOM.equals(originalMovement.getEstado()) || PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(originalMovement.getEstado()) ){
            req.setRetryCount(0);
            //Ejecutar Metodo que reintenta la carga o reversa.
            //Redirect
          }
          else if(PrepaidMovementStatus.PROCESS_OK.equals(originalMovement.getEstado())) {

            // Se intenta realizar reversa del movimiento.
            InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(prepaidCard.getProcessorUserId(), prepaidCard.getPan(), prepaidTopup.getTotal().getCurrencyCode(),
              prepaidMovementReverse.getIndnorcor(), prepaidMovementReverse.getTipofac(), "", originalMovement.getImpfac(), originalMovement.getNumaut(), originalMovement.getCodcom(),
              originalMovement.getCodcom(), originalMovement.getCodact(), CodigoMoneda.fromValue(originalMovement.getClamondiv()), new BigDecimal(originalMovement.getImpliq()));

            // Si la reversa se realiza correctamente  se actualiza el movimiento original a reversado.
            if (inclusionMovimientosDTO.isRetornoExitoso()) {
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.REVERSED);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.PROCESS_OK);
            } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
              req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
              req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
              return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
            } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1010)) {
              req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
              req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
              return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
            } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1020)) {
              req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
              req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
              return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
            } else {
              PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP_REVERSE;
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovementReverse().getId(), status);
              data.getPrepaidMovementReverse().setEstado(status);
              return redirectRequestReverse(createJMSEndpoint(ERROR_REVERSAL_TOPUP_REQ), exchange, req, false);
            }
          }
        }catch (Exception e){
          log.error(String.format("Error desconocido al realizar carga %s",e.getLocalizedMessage()));
          req.getData().setNumError(Errors.ERROR_INDETERMINADO);
          req.getData().setMsjError(e.getLocalizedMessage());
          return redirectRequestReverse(createJMSEndpoint(ERROR_REVERSAL_TOPUP_REQ), exchange, req, false);
        }
        return req;
      }
    };
  }
  public ProcessorRoute processErrorTopupReverse() {
    return new ProcessorRoute<ExchangeData<PrepaidReverseData10>, ExchangeData<PrepaidReverseData10>>() {
      @Override
      public ExchangeData<PrepaidReverseData10> processExchange(long idTrx, ExchangeData<PrepaidReverseData10> req, Exchange exchange) throws Exception {
        //TODO: Ver que hacer en las colas de error
        return req;
      }
    };
  }
}
