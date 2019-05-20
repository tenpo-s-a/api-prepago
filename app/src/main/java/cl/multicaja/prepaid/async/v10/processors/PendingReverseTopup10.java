package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.NewTicket;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
          Account account = getRoute().getAccountEJBBean10().findByUserId(prepaidUser10.getId());

          if(account == null) {
            log.error(String.format("Error cuenta no encontrada para el usuario [id=%d]",prepaidUser10.getId()));
            req.getData().setNumError(Errors.ERROR_INDETERMINADO);
            req.getData().setMsjError(Errors.ERROR_INDETERMINADO.name());
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
          }
          // El contrato se obtiene desde la cuenta.
          String contrato = account.getAccountNumber();

          String pan = getRoute().getCryptHelper().decryptPan(prepaidCard.getEncryptedPan());

          // Busca el movimiento de carga original
          PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForReverse(prepaidUser10.getId(),
            prepaidTopup.getTransactionId(), PrepaidMovementType.TOPUP, TipoFactura.valueOfEnumByCodeAndCorrector(prepaidMovementReverse.getTipofac().getCode(),IndicadorNormalCorrector.NORMAL.getValue()));

          if(PrepaidMovementStatus.PENDING.equals(originalMovement.getEstado()) || PrepaidMovementStatus.IN_PROCESS.equals(originalMovement.getEstado())) {
            log.debug(String.format("********** Movimiento original con id %s se encuentra en status: %s **********", originalMovement.getId(), originalMovement.getEstado()));
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, true);
          }
          else if (PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(originalMovement.getEstado()) || PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(originalMovement.getEstado()) ){
            log.debug("********** Reintentando movimiento original **********");
            log.info(String.format("LLamando reversa mov original %s", prepaidCard.getProcessorUserId()));

              // Se intenta realizar nuevamente la inclusion del movimiento original .
            InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomServiceHelper().topup(contrato, pan, originalMovement.getCodcom(), originalMovement);

            log.debug("Respuesta reversa mov original");
            log.debug(inclusionMovimientosDTO.getRetorno());
            log.debug(inclusionMovimientosDTO.getDescRetorno());

              // Se verifica la respuesta de tecnocom
            if (inclusionMovimientosDTO.isRetornoExitoso()) {
              log.debug("********** Movimiento original no existia previamente **********");
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.CONFIRMED);
              // Incluir datos en CDT.
              CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null,originalMovement.getIdMovimientoRef());
              callCDT(prepaidTopup,prepaidUser10,originalMovement.getIdMovimientoRef(),movRef.getCdtTransactionTypeConfirm());
            } else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())) {
              // La inclusion devuelve error, se evalua el error.
              if(inclusionMovimientosDTO.getDescRetorno().contains("MPE5501")) {
                log.debug("********** Movimiento original ya existia **********");
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.CONFIRMED);
                // Incluir datos en CDT.
                CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null,originalMovement.getIdMovimientoRef());
                callCDT(prepaidTopup,prepaidUser10,originalMovement.getIdMovimientoRef(),movRef.getCdtTransactionTypeConfirm());
              } else {
                log.debug("********** Movimiento original rechazado **********");
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.REJECTED);
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REJECTED);
              }
            } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
            } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1010)) {
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
            } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1020)) {
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
            } else { // Ningun error tipico
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP);
            }
            // Se envia el mensaje para ser procesado nuevamente
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_TOPUP_REQ), exchange, req, false);
          }
          else if(PrepaidMovementStatus.PROCESS_OK.equals(originalMovement.getEstado())) {
            log.debug("********** Realizando reversa de carga **********");
            log.info(String.format("LLamando reversa %s", prepaidCard.getProcessorUserId()));

            // Se intenta realizar reversa del movimiento.
            InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomServiceHelper().reverse(contrato, pan, originalMovement.getCodcom(), prepaidMovementReverse);

            log.info("Respuesta reversa");
            log.info(inclusionMovimientosDTO.getRetorno());
            log.info(inclusionMovimientosDTO.getDescRetorno());

            // Si la reversa se realiza correctamente  se actualiza el movimiento original a reversado.
            if (inclusionMovimientosDTO.isRetornoExitoso()) {
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.PROCESS_OK);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), BusinessStatusType.CONFIRMED);

              req.getData().getPrepaidMovementReverse().setEstado(PrepaidMovementStatus.PROCESS_OK);
              req.getData().getPrepaidMovementReverse().setEstadoNegocio(BusinessStatusType.CONFIRMED);

              log.debug("********** Reversa de carga realizada exitosamente **********");
              CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null,originalMovement.getIdMovimientoRef());
              CdtTransaction10 cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,0L,
                CdtTransactionType.PRIMERA_CARGA.equals(movRef.getTransactionType())?CdtTransactionType.REVERSA_PRIMERA_CARGA:CdtTransactionType.REVERSA_CARGA);
              cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,cdtTxReversa.getTransactionReference(),cdtTxReversa.getCdtTransactionTypeConfirm());
              if(!"0".equals(cdtTxReversa.getNumError())){
                log.error("Error al confirmar reversa en CDT");
              }

              //actualiza movimiento original en accounting y clearing
              getRoute().getPrepaidMovementEJBBean10().updateAccountingStatusReconciliationDateAndClearingStatus(originalMovement.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

              // Expira cache del saldo de la cuenta
              getRoute().getAccountEJBBean10().expireBalanceCache(account.getId());

              return req;
            } else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())) {
              if(inclusionMovimientosDTO.getDescRetorno().contains("MPE5501")) {
                log.debug("********** Reversa de carga ya existia **********");
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.PROCESS_OK);
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), BusinessStatusType.CONFIRMED);

                req.getData().getPrepaidMovementReverse().setEstado(PrepaidMovementStatus.PROCESS_OK);
                req.getData().getPrepaidMovementReverse().setEstadoNegocio(BusinessStatusType.CONFIRMED);
                CdtTransaction10 cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,0L, CdtTransactionType.REVERSA_CARGA);
                cdtTxReversa = callCDT(prepaidTopup,prepaidUser10,cdtTxReversa.getTransactionReference(),cdtTxReversa.getCdtTransactionTypeConfirm());
                if(!"0".equals(cdtTxReversa.getNumError())){
                  log.error("Error al confirmar reversa en CDT");
                }
                //actualiza movimiento original en accounting y clearing
                getRoute().getPrepaidMovementEJBBean10().updateAccountingStatusReconciliationDateAndClearingStatus(originalMovement.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

                // Expira cache del saldo de la cuenta
                getRoute().getAccountEJBBean10().expireBalanceCache(account.getId());
              } else {
                log.debug("********** Reversa de carga rechazada **********");
                log.debug(inclusionMovimientosDTO.getDescRetorno());
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.REJECTED);
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), BusinessStatusType.REJECTED);

                req.getData().getPrepaidMovementReverse().setEstado(PrepaidMovementStatus.REJECTED);
                req.getData().getPrepaidMovementReverse().setEstadoNegocio(BusinessStatusType.REJECTED);
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
          }else {
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), BusinessStatusType.CONFIRMED);

            req.getData().getPrepaidMovementReverse().setEstadoNegocio(BusinessStatusType.CONFIRMED);

            //actualiza movimiento original en accounting y clearing
            getRoute().getPrepaidMovementEJBBean10().updateAccountingStatusReconciliationDateAndClearingStatus(originalMovement.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

          }
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
        PrepaidReverseData10 data = req.getData();
        PrepaidUser10 user = data.getPrepaidUser10();
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {

          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_1","v1.0");
          template = TemplateUtils.freshDeskTemplateColas1(template,String.format("Error al realizar reversa carga %s",data.getPrepaidTopup10().getTransactionOriginType().name()),String.format("%s %s",user.getName(),
            user.getLastName()),user.getDocumentNumber(),user.getId(),data.getPrepaidMovementReverse().getNumaut(),data.getPrepaidMovementReverse().getMonto().longValue());

          NewTicket newTicket = createTicket("Error al realizar reversa carga",
            template,
            user.getDocumentNumber(),
            data.getPrepaidTopup10().getMessageId(),
            QueuesNameType.REVERSE_TOPUP,
            req.getReprocesQueue());
          //FIXME: Implementar la creación de tickets en freshdesk
          //Ticket ticket = getRoute().getUserClient().createFreshdeskTicket(null,user.getUuid(),newTicket);
          /*if(ticket.getId() != null){
            log.info("Ticket Creado Exitosamente");
          }
           */
        } else {
          Map<String, Object> templateData = new HashMap<>();
          templateData.put("idUsuario", user.getId());
          templateData.put("rutCliente", user.getDocumentNumber());
          //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TOPUP_REVERSE, templateData);
        }
        return req;
      }
    };
  }


}
