package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.external.freshdesk.model.NewTicket;
import cl.multicaja.prepaid.external.freshdesk.model.Ticket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.FreshDeskServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_REQ;
import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_REQ;

public class PendingReverseWithdraw10 extends BaseProcessor10  {

  private static Log log = LogFactory.getLog(PendingReverseWithdraw10.class);

  private FreshDeskServiceHelper freshDeskServiceHelper = new FreshDeskServiceHelper();

  public PendingReverseWithdraw10(BaseRoute10 route) {
    super(route);
  }

  public ProcessorRoute processPendingWithdrawReversal() {
    return new ProcessorRoute<ExchangeData<PrepaidReverseData10>, ExchangeData<PrepaidReverseData10>>() {
      @Override
      public ExchangeData<PrepaidReverseData10> processExchange(long idTrx, ExchangeData<PrepaidReverseData10> req, Exchange exchange) throws Exception {
      try {
        log.info("processPendingWithdrawReversal - REQ: " + req);

        req.retryCountNext();
        PrepaidReverseData10 data = req.getData();
        PrepaidWithdraw10 prepaidWithdraw = data.getPrepaidWithdraw10();
        PrepaidMovement10 prepaidMovementReverse = data.getPrepaidMovementReverse();

        PrepaidUser10 prepaidUser10 = data.getPrepaidUser10();

        Account account = getRoute().getAccountEJBBean10().findByUserId(prepaidUser10.getId());

        PrepaidCard10 prepaidCard = getRoute().getPrepaidCardEJBBean11().getPrepaidCardByAccountId(account.getId());

        if(req.getRetryCount() > getMaxRetryCount()) {
          PrepaidMovementStatus status;
          if (Errors.TECNOCOM_ERROR_REINTENTABLE.equals(req.getData().getNumError())){
            status = PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE;
          } else if(Errors.TECNOCOM_TIME_OUT_CONEXION.equals(req.getData().getNumError())){
            status = PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION;
          } else if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(req.getData().getNumError())){
            status = PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE;
          } else {
            status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_WITHDRAW_REVERSE;
          }
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), status);
          prepaidMovementReverse.setEstado(status);
          Endpoint endpoint = createJMSEndpoint(ERROR_REVERSAL_WITHDRAW_REQ);
          return redirectRequestReverse(endpoint, exchange, req, false);
        }
        //NO SE OBTIENE DE LA TARJETA SI NO QUE DE LA CUENTA.
        String contrato = account.getAccountNumber();

        String pan = getRoute().getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());

        // Busca el movimiento de retiro original
        PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForReverse(prepaidUser10.getId(),
          prepaidWithdraw.getTransactionId(),
          PrepaidMovementType.WITHDRAW,
          TipoFactura.valueOfEnumByCodeAndCorrector(prepaidMovementReverse.getTipofac().getCode(), IndicadorNormalCorrector.NORMAL.getValue()));
        log.info("[originalMovement] "+originalMovement);

        if(PrepaidMovementStatus.PENDING.equals(originalMovement.getEstado()) || PrepaidMovementStatus.IN_PROCESS.equals(originalMovement.getEstado())) {
          log.debug(String.format("********** Movimiento original con id %s se encuentra en status: %s **********", originalMovement.getId(), originalMovement.getEstado()));
          return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_REQ), exchange, req, true);

        } else if (PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(originalMovement.getEstado()) || PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(originalMovement.getEstado()) ) {
          log.debug("********** Reintentando movimiento original **********");
          log.info(String.format("LLamando reversa mov original %s",account.getAccountNumber()));

          // Se intenta realizar nuevamente la inclusion del movimiento original .
          InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomServiceHelper().withdraw(contrato, pan, originalMovement.getCodcom(), originalMovement);

          log.info("Respuesta reversa mov original");
          log.info(inclusionMovimientosDTO.getRetorno());
          log.info(inclusionMovimientosDTO.getDescRetorno());

          // Se verifica la respuesta de tecnocom
          if (inclusionMovimientosDTO.isRetornoExitoso()) {
            log.debug("********** Movimiento original no existia previamente en Tecnocom **********");
            // Se actualiza el movimiento original
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.CONFIRMED);
            // Incluir datos en CDT.
            CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null, originalMovement.getIdMovimientoRef());
            callCDT(prepaidWithdraw, prepaidUser10, originalMovement.getIdMovimientoRef(), movRef.getCdtTransactionTypeConfirm());
          } else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())) {
            // La inclusion devuelve error, se evalua el error.
            if(inclusionMovimientosDTO.getDescRetorno().contains("MPE5501")) {
              log.debug("********** Movimiento original ya existia en Tecnocom **********");
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.CONFIRMED);
              // Incluir datos en CDT.
              CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null, originalMovement.getIdMovimientoRef());
              callCDT(prepaidWithdraw, prepaidUser10, originalMovement.getIdMovimientoRef(), movRef.getCdtTransactionTypeConfirm());
            } else {
              log.debug("********** Movimiento original rechazado por Tecnocom **********");
              log.debug(inclusionMovimientosDTO.getDescRetorno());
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.REJECTED);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REJECTED);
            }
          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1010)) {
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1020)) {
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
          }

          // Se envia el mensaje para ser procesado nuevamente
          return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_REQ), exchange, req, false);

        } else if(PrepaidMovementStatus.PROCESS_OK.equals(originalMovement.getEstado())) {
          log.debug("********** Realizando reversa de retiro **********");
          String numaut = prepaidMovementReverse.getNumaut();
          log.info(String.format("LLamando reversa %s", account.getAccountNumber()));

          // Se intenta realizar reversa del movimiento.
          InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomServiceHelper().reverse(contrato, pan, originalMovement.getCodcom(), prepaidMovementReverse);

          log.info("Respuesta reversa");
          log.info(inclusionMovimientosDTO.getRetorno());
          log.info(inclusionMovimientosDTO.getDescRetorno());

          // Si la reversa se realiza correctamente  se actualiza el movimiento original a reversado.
          if (inclusionMovimientosDTO.isRetornoExitoso()) {
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
            PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK;
            BusinessStatusType businessStatus = BusinessStatusType.CONFIRMED;

            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), status);
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), businessStatus);

            req.getData().getPrepaidMovementReverse().setEstadoNegocio(businessStatus);
            req.getData().getPrepaidMovementReverse().setEstado(status);

            //actualiza movimiento original en accounting y clearing
            getRoute().getPrepaidMovementEJBBean10().updateAccountingStatusReconciliationDateAndClearingStatus(originalMovement.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

            // Expira cache del saldo de la cuenta
            getRoute().getAccountEJBBean10().expireBalanceCache(account.getId());

            log.debug("********** Reversa de retiro realizada exitosamente **********");

            // Si estaba abierta, cerrar la transaccion en el CDT
            CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null, originalMovement.getIdMovimientoRef());
            CdtTransaction10 movRefConf = getRoute().getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, originalMovement.getIdTxExterno(), movRef.getCdtTransactionTypeConfirm());
            if(movRefConf == null) {
              callCDT(prepaidWithdraw, prepaidUser10, movRef.getId(), movRef.getCdtTransactionTypeConfirm());
            }

            // Agregar y confirmar reversa cdt
            CdtTransaction10 cdtTxReversa = callCDT(prepaidWithdraw, prepaidUser10, 0L, CdtTransactionType.REVERSA_RETIRO);
            cdtTxReversa = callCDT(prepaidWithdraw, prepaidUser10, cdtTxReversa.getTransactionReference(), cdtTxReversa.getCdtTransactionTypeConfirm());
            if(!"0".equals(cdtTxReversa.getNumError())) {
              log.error("Error al confirmar reversa en CDT");
            }

            return req;
          } else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())) {
            if(inclusionMovimientosDTO.getDescRetorno().contains("MPE5501")) {
              log.debug("********** Reversa de retiro ya existia **********");
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
              PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK;
              BusinessStatusType businessStatus = BusinessStatusType.CONFIRMED;

              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), status);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), businessStatus);

              req.getData().getPrepaidMovementReverse().setEstadoNegocio(businessStatus);
              req.getData().getPrepaidMovementReverse().setEstado(status);

              //actualiza movimiento original en accounting y clearing
              getRoute().getPrepaidMovementEJBBean10().updateAccountingStatusReconciliationDateAndClearingStatus(originalMovement.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

              // Expira cache del saldo de la cuenta
              getRoute().getAccountEJBBean10().expireBalanceCache(account.getId());

              // Si estaba abierta, cerrar la transaccion en el CDT
              CdtTransaction10 movRef = getRoute().getCdtEJBBean10().buscaMovimientoReferencia(null, originalMovement.getIdMovimientoRef());
              CdtTransaction10 movRefConf = getRoute().getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, originalMovement.getIdTxExterno(), movRef.getCdtTransactionTypeConfirm());
              if(movRefConf == null) {
                callCDT(prepaidWithdraw, prepaidUser10, movRef.getId(), movRef.getCdtTransactionTypeConfirm());
              }

              // Agregar y confirmar reversa cdt
              CdtTransaction10 cdtTxReversa = callCDT(prepaidWithdraw, prepaidUser10, 0L, CdtTransactionType.REVERSA_RETIRO);
              cdtTxReversa = callCDT(prepaidWithdraw, prepaidUser10, cdtTxReversa.getTransactionReference(), cdtTxReversa.getCdtTransactionTypeConfirm());
              if(!"0".equals(cdtTxReversa.getNumError())) {
                log.error("Error al confirmar reversa en CDT");
              }
              return req;
            } else {
              log.debug("********** Reversa de retiro rechazada **********");
              log.debug(inclusionMovimientosDTO.getDescRetorno());
              PrepaidMovementStatus status = PrepaidMovementStatus.REJECTED;
              BusinessStatusType businessStatus = BusinessStatusType.REJECTED;

              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), status);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), businessStatus);

              req.getData().getPrepaidMovementReverse().setEstadoNegocio(businessStatus);
              req.getData().getPrepaidMovementReverse().setEstado(status);
            }
          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
            req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
            req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_REQ), exchange, req, true);
          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1010)) {
            req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
            req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_REQ), exchange, req, true);
          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1020)) {
            req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
            req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
            return redirectRequestReverse(createJMSEndpoint(PENDING_REVERSAL_WITHDRAW_REQ), exchange, req, true);
          } else {
            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_WITHDRAW_REVERSE;
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovementReverse().getId(), status);
            data.getPrepaidMovementReverse().setEstado(status);
            return redirectRequestReverse(createJMSEndpoint(ERROR_REVERSAL_WITHDRAW_REQ), exchange, req, false);
          }
        } else {
          log.debug("********** La reversa no necesita ser ejecutada en Tecnocom **********");
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovementReverse.getId(), PrepaidMovementStatus.PROCESS_OK);
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, prepaidMovementReverse.getId(), BusinessStatusType.CONFIRMED);
          data.getPrepaidMovementReverse().setEstado(PrepaidMovementStatus.PROCESS_OK);
          data.getPrepaidMovementReverse().setEstadoNegocio(BusinessStatusType.CONFIRMED);

          // actualiza movimiento original en accounting y clearing
          getRoute().getPrepaidMovementEJBBean10().updateAccountingStatusReconciliationDateAndClearingStatus(originalMovement.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
        }
      } catch (Exception e) {
        e.printStackTrace();
        log.error(String.format("Error desconocido al realizar la reversa de retiro: %s",e.getMessage()));
        Endpoint endpoint = createJMSEndpoint(ERROR_REVERSAL_WITHDRAW_REQ);
        req.getData().setNumError(Errors.ERROR_INDETERMINADO);
        req.getData().setMsjError(e.getLocalizedMessage());
        return redirectRequestReverse(endpoint, exchange, req, true);
      }
      return req;
      }
    };
  }

  public ProcessorRoute processErrorWithdrawReversal() {
    return new ProcessorRoute<ExchangeData<PrepaidReverseData10>, ExchangeData<PrepaidReverseData10>>() {
      @Override
      public ExchangeData<PrepaidReverseData10> processExchange(long idTrx, ExchangeData<PrepaidReverseData10> req, Exchange exchange) throws Exception {
        log.info("processErrorWithdrawReversal - REQ: " + req);
        req.retryCountNext();
        PrepaidReverseData10 data = req.getData();
        PrepaidUser10 user = data.getPrepaidUser10();
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_1","v1.0");
          template = TemplateUtils.freshDeskTemplateColas1(template,"Error al realizar reversa de Retiro",String.format("%s %s",user.getName(),user.getLastName()),user.getDocumentNumber(),
            user.getId() , data.getPrepaidMovementReverse().getNumaut(),data.getPrepaidMovementReverse().getMonto().longValue());

          NewTicket newTicket = createTicket("Error al realizar reversa de Retiro",
            template,
            user.getDocumentNumber(),
            data.getPrepaidWithdraw10().getMessageId(),
            QueuesNameType.REVERSE_WITHDRAWAL,
            req.getReprocesQueue());

          newTicket.setUniqueExternalId(data.getPrepaidUser10().getUuid());
          Ticket ticket = getFreshDeskServiceHelper().createTicketInFreshdesk(newTicket);
          if (ticket != null && ticket.getId() != null) {
            log.info("[processErrorWithdrawReversal][Ticket_Success][ticketId]:"+ticket.getId());
          }else{
            log.info("[processErrorWithdrawReversal][Ticket_Fail][ticketData]:"+newTicket.toString());
          }

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
