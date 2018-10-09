package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
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
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_REQ;
import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_REQ;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_ERROR_TOPUP_REVERSE;

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
              status = PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE;
            } else if(Errors.TECNOCOM_TIME_OUT_CONEXION.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION;
            } else if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE;
            } else {
              status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP_REVERSE;
            }
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), status);
            req.getData().getPrepaidMovementReverse().setEstado(status);
            return redirectRequestReverse(createJMSEndpoint(ERROR_REVERSAL_TOPUP_REQ), exchange, req, false);
          }

          // Busca el movimiento de carga original
          PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForReverse(prepaidUser10.getId(),
            prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, TipoFactura.valueOfEnumByCodeAndCorrector(prepaidMovementReverse.getTipofac().getCode(),IndicadorNormalCorrector.NORMAL.getValue()));
          if(PrepaidMovementStatus.PENDING.equals(originalMovement.getEstado()) || PrepaidMovementStatus.IN_PROCESS.equals(originalMovement.getEstado())) {
            log.debug(String.format("********** Movimiento original con id %s se encuentra en status: %s **********", originalMovement.getId(), originalMovement.getEstado()));
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
          }
          else if (PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(originalMovement.getEstado()) || PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(originalMovement.getEstado()) ){
            log.debug("********** Reintentando movimiento original **********");
            String numaut = originalMovement.getId().toString();
              //solamente los 6 primeros digitos de numreffac
              if (numaut.length() > 6) {
                numaut = numaut.substring(numaut.length()-6);
              }
             // Se intenta realizar nuevamente la inclusion del movimiento original .
              InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(prepaidCard.getProcessorUserId(), prepaidCard.getPan(), originalMovement.getClamon(),
                originalMovement.getIndnorcor(), originalMovement.getTipofac(), "", originalMovement.getImpfac(), numaut, originalMovement.getCodcom(),
                originalMovement.getCodcom(), originalMovement.getCodact(), CodigoMoneda.fromValue(originalMovement.getClamondiv()), new BigDecimal(originalMovement.getImpliq()));

              // Se verifica la respuesta de tecnocom
              if (inclusionMovimientosDTO.isRetornoExitoso()) {
                log.debug("********** Movimiento original no existia previamente **********");
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
                // Incluir datos en CDT.
                CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null,originalMovement.getIdMovimientoRef());
                callCDT(prepaidTopup,prepaidUser10,originalMovement.getIdMovimientoRef(),movRef.getCdtTransactionTypeConfirm());

              } else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())) {
                // La inclusion devuelve error, se evalua el error.
                if(inclusionMovimientosDTO.getDescRetorno().contains("MPE5501")) {
                  log.debug("********** Movimiento original ya existia **********");
                  getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
                  // Incluir datos en CDT.
                  CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null,originalMovement.getIdMovimientoRef());
                  callCDT(prepaidTopup,prepaidUser10,originalMovement.getIdMovimientoRef(),movRef.getCdtTransactionTypeConfirm());
                } else {
                  log.debug("********** Movimiento original rechazado **********");
                  getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.REJECTED);

                }
              } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
              } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1010)) {
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
              } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1020)) {
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
              }
              // Se envia el mensaje para ser procesado nuevamente
              return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, false);
          }
          else if(PrepaidMovementStatus.PROCESS_OK.equals(originalMovement.getEstado())) {
            log.debug("********** Realizando reversa de retiro **********");
            String numaut = prepaidMovementReverse.getId().toString();
            //solamente los 6 primeros digitos de numreffac
            if (numaut.length() > 6) {
              numaut = numaut.substring(numaut.length()-6);
            }
            // Se intenta realizar reversa del movimiento.
            InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(prepaidCard.getProcessorUserId(), prepaidCard.getPan(),originalMovement.getClamon(),
              prepaidMovementReverse.getIndnorcor(), prepaidMovementReverse.getTipofac(), "", originalMovement.getImpfac(), numaut, originalMovement.getCodcom(),
              originalMovement.getCodcom(), originalMovement.getCodact(), CodigoMoneda.fromValue(originalMovement.getClamondiv()), new BigDecimal(originalMovement.getImpliq()));

            // Si la reversa se realiza correctamente  se actualiza el movimiento original a reversado.
            if (inclusionMovimientosDTO.isRetornoExitoso()) {
              //TODO: la actualizacion del status del movimiento original a REVERSED deberia ser en el status de negocio
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.REVERSED);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.PROCESS_OK);
              req.getData().getPrepaidMovementReverse().setEstado(PrepaidMovementStatus.PROCESS_OK);
              log.debug("********** Reversa de retiro realizada exitosamente **********");
              CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null,originalMovement.getIdMovimientoRef());
              CdtTransaction10 cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,0L,
                CdtTransactionType.PRIMERA_CARGA.equals(movRef.getTransactionType())?CdtTransactionType.REVERSA_PRIMERA_CARGA:CdtTransactionType.REVERSA_CARGA);
              cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,cdtTxReversa.getTransactionReference(),cdtTxReversa.getCdtTransactionTypeConfirm());
              if(!"0".equals(cdtTxReversa.getNumError())){
                log.error("Error al confirmar reversa en CDT");
              }
              return req;
            } else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())) {
              if(inclusionMovimientosDTO.getDescRetorno().contains("MPE5501")) {
                log.debug("********** Reversa de retiro ya existia **********");
                //TODO: la actualizacion del status del movimiento original a REVERSED deberia ser en el status de negocio
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.REVERSED);
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.PROCESS_OK);
                CdtTransaction10 cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,0L, CdtTransactionType.REVERSA_CARGA);
                cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,cdtTxReversa.getTransactionReference(),cdtTxReversa.getCdtTransactionTypeConfirm());
                if(!"0".equals(cdtTxReversa.getNumError())){
                  log.error("Error al confirmar reversa en CDT");
                }
              } else {
                log.debug("********** Reversa de retiro rechazada **********");
                log.debug(inclusionMovimientosDTO.getDescRetorno());
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.REJECTED);

              }
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
          //TODO: Si la reversa no necesita ser ejecutadala, actualizar status
        }catch (Exception e){
          e.printStackTrace();
          log.error(String.format("Error desconocido al realizar carga %s",e.getMessage()));
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
        log.info("processErrorTopupReverse - REQ: " + req);
        req.retryCountNext();
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("idUsuario", req.getData().getUser().getId().toString());
        templateData.put("rutCliente", req.getData().getUser().getRut().getValue().toString() + "-" + req.getData().getUser().getRut().getDv());
        getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TOPUP_REVERSE, templateData);
        return req;
      }
    };
  }


}
