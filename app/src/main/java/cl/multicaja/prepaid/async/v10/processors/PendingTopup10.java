package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.NewTicket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.Ticket;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.PENDING_SEND_MAIL_TOPUP_REQ;
import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.SEDA_PENDING_SEND_MAIL_TOPUP;
import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.*;

/**
 * @autor vutreras
 */
public class PendingTopup10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingTopup10.class);

  public PendingTopup10(BaseRoute10 route) {
    super(route);
  }


  public ProcessorRoute processPendingTopup() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        try {
          log.info("processPendingTopup - REQ: " + req);

          req.retryCountNext();

          PrepaidTopupData10 data = req.getData();

          PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();

          PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

          if(req.getRetryCount() > getMaxRetryCount()) {
            PrepaidMovementStatus status;
            if (Errors.TECNOCOM_ERROR_REINTENTABLE.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE;
            } else if(Errors.TECNOCOM_TIME_OUT_CONEXION.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION;
            } else if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(req.getData().getNumError())){
              status = PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE;
            } else {
              status = PrepaidMovementStatus.REJECTED;
            }
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);
            prepaidMovement.setEstado(status);

            System.out.println("Rediriegiendo a erro topup req");
            return redirectRequest(createJMSEndpoint(ERROR_TOPUP_REQ), exchange, req, false);
          }

          User user = data.getUser();

          if (user == null) {
            log.error("Error user es null");
            return null;
          }

          if (user.getRut() == null) {
            log.error("Error user.getRut() es null");
            return null;
          }

          if(user.getIsBlacklisted()) {
            log.error(String.format("Error usuario %s en lista negra", user.getId()));
            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);
            prepaidMovement.setEstado(status);
            return redirectRequest(createJMSEndpoint(ERROR_TOPUP_REQ), exchange, req, false);
          }

          Integer rut = user.getRut().getValue();

          if (rut == null){
            log.error("Error rut es null");
            return null;
          }

          PrepaidUser10 prepaidUser = getRoute().getPrepaidUserEJBBean10().getPrepaidUserByRut(null, rut);

          if (prepaidUser == null){
            log.error("Error al buscar PrepaidUser10 con rut: " + rut);
            return null;
          }
          prepaidUser = getRoute().getPrepaidUserEJBBean10().getUserLevel(user, prepaidUser);

          data.setPrepaidUser10(prepaidUser);

          PrepaidCard10 prepaidCard = getRoute().getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                                      PrepaidCardStatus.ACTIVE,
                                                                                                      PrepaidCardStatus.LOCKED,
                                                                                                      PrepaidCardStatus.PENDING);

           log.info("[BEFORE prepaidMovement] "+prepaidMovement);
           //prepaidMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementById2(prepaidMovement.getId());
           log.info("[AFTER prepaidMovement] "+prepaidMovement);
          if (prepaidCard != null) {

            data.setPrepaidCard10(prepaidCard);

            String contrato = prepaidCard.getProcessorUserId();
            String nomcomred = prepaidTopup.getMerchantName();
            String pan = getRoute().getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());

            InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomServiceHelper().topup(contrato, pan, nomcomred, prepaidMovement);

            log.info(String.format("Respuesta inclusion: Codigo -> %s, Descripcion -> %s", inclusionMovimientosDTO.getRetorno(), inclusionMovimientosDTO.getDescRetorno()));

            // Responde OK || Responde que ya el movimiento existia (cod. 200 + MPE5501)
            if (inclusionMovimientosDTO.isRetornoExitoso() ||
               (CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno()) && inclusionMovimientosDTO.getDescRetorno().contains("MPE5501"))) {

              String centalta = inclusionMovimientosDTO.getCenalta();
              String cuenta = inclusionMovimientosDTO.getCuenta();
              Integer numextcta = inclusionMovimientosDTO.getNumextcta();
              Integer nummovext = inclusionMovimientosDTO.getNummovext();
              Integer clamone = inclusionMovimientosDTO.getClamone();
              PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK; //realizado
              BusinessStatusType businessStatus = BusinessStatusType.CONFIRMED;

              log.info("Prepaid Movement Status: "+status.name() );
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
                prepaidMovement.getId(),
                prepaidCard.getPan(),
                centalta,
                cuenta,
                numextcta,
                nummovext,
                clamone,
                businessStatus,
                status);

              prepaidMovement.setPan(prepaidCard.getPan());
              prepaidMovement.setCentalta(centalta);
              prepaidMovement.setCuenta(cuenta);
              prepaidMovement.setNumextcta(numextcta);
              prepaidMovement.setNummovext(nummovext);
              prepaidMovement.setClamone(clamone);
              prepaidMovement.setEstado(status);
              prepaidMovement.setEstadoNegocio(businessStatus);
              data.setPrepaidMovement10(prepaidMovement);
              CdtTransaction10 cdtTransaction = data.getCdtTransaction10();

              CdtTransaction10 cdtTransactionConfirm = new CdtTransaction10();
              cdtTransactionConfirm.setAmount(cdtTransaction.getAmount().subtract(prepaidTopup.getFee().getValue()));
              cdtTransactionConfirm.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
              cdtTransactionConfirm.setAccountId(cdtTransaction.getAccountId());
              cdtTransactionConfirm.setTransactionReference(cdtTransaction.getTransactionReference());
              cdtTransactionConfirm.setIndSimulacion(false);
              //se debe agregar CONFIRM para evitar el constraint unique de esa columna
              cdtTransactionConfirm.setExternalTransactionId(cdtTransaction.getExternalTransactionId());
              cdtTransactionConfirm.setGloss(prepaidTopup.getCdtTransactionTypeConfirm().getName() + " " + cdtTransactionConfirm.getAmount());

              cdtTransactionConfirm = getRoute().getCdtEJBBean10().addCdtTransaction(null, cdtTransactionConfirm);

              data.setCdtTransactionConfirm10(cdtTransactionConfirm);

              //TODO: Se debe guardar el movimiento en accounting -> PENDING y clearing -> INITIAL
              log.info("Enviando comprobante de carga por mail");
              Endpoint toAccounting = createJMSEndpoint(PENDING_SEND_MOVEMENT_TO_ACCOUNTING_REQ);
              redirectRequest(toAccounting, exchange, req, Boolean.FALSE);

              if (!cdtTransaction.isNumErrorOk()) {
                log.error(String.format("Error en CDT %s", cdtTransaction.getMsjError()));
              }

              log.info("Enviando comprobante de carga por mail");
              Endpoint mailEndpoint = createJMSEndpoint(PENDING_SEND_MAIL_TOPUP_REQ);
              redirectRequest(mailEndpoint, exchange, req, Boolean.FALSE);

              //segun la historia: https://www.pivotaltracker.com/story/show/158044562
              if (PrepaidCardStatus.PENDING.equals(prepaidCard.getStatus())) {
                Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
                return redirectRequest(endpoint, exchange, req, false);
              }

              req.setData(data);
              return req;

            } else if (CodigoRetorno._1000.equals(inclusionMovimientosDTO.getRetorno())) {
              req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
              req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
              return redirectRequest(createJMSEndpoint(PENDING_TOPUP_REQ), exchange, req, true);
            } else if (CodigoRetorno._1010.equals(inclusionMovimientosDTO.getRetorno())) {
              req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
              req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
              return redirectRequest(createJMSEndpoint(PENDING_TOPUP_REQ), exchange, req, true);
            } else if (CodigoRetorno._1020.equals(inclusionMovimientosDTO.getRetorno())) {
              req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
              req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
              return redirectRequest(createJMSEndpoint(PENDING_TOPUP_REQ), exchange, req, true);
            } else {
              // Todos los errores restantes de tecnocom se consideran rechazo y se iran a devolucion
              PrepaidMovementStatus status = PrepaidMovementStatus.REJECTED;
              BusinessStatusType businessStatus = BusinessStatusType.REJECTED;

              getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
              getRoute().getPrepaidMovementEJBBean10().updatePrepaidBusinessStatus(null, data.getPrepaidMovement10().getId(), businessStatus);
              data.getPrepaidMovement10().setEstado(status);
              data.getPrepaidMovement10().setEstadoNegocio(businessStatus);

              Endpoint endpoint = createJMSEndpoint(ERROR_TOPUP_REQ);
              return redirectRequest(endpoint, exchange, req, false);
            }
          }
          else {

            //https://www.pivotaltracker.com/story/show/157816408
            //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

            prepaidCard = getRoute().getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                        PrepaidCardStatus.LOCKED_HARD,
                                                                                        PrepaidCardStatus.EXPIRED);

            if (prepaidCard == null) {
              Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
              return redirectRequest(endpoint, exchange, req, false);
            } else {
              data.setPrepaidCard10(prepaidCard);
              return req;
            }
          }
        }catch (Exception e){
          e.printStackTrace();
          log.error(String.format("Error desconocido al realizar carga %s",e));
          req.getData().setNumError(Errors.ERROR_INDETERMINADO);
          req.getData().setMsjError(e.getLocalizedMessage());
          return redirectRequest(createJMSEndpoint(ERROR_TOPUP_REQ), exchange, req, true);
        }
      }
    };
  }

  public ProcessorRoute processErrorTopup() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("processPendingTopupReturns - REQ: " + req);
        req.retryCountNext();
        PrepaidTopupData10 data = req.getData();
        if (Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
          String template = getRoute().getParametersUtil().getString("api-prepaid", "template_ticket_cola_1", "v1.0");
          template = TemplateUtils.freshDeskTemplateColas1(template, "Error al realizar carga", String.format("%s %s", data.getUser().getName(), data.getUser().getLastname_1()), String.format("%s-%s", data.getUser().getRut().getValue(), data
            .getUser().getRut().getDv()), data.getUser().getId(), data.getPrepaidMovement10().getNumaut(), data.getPrepaidTopup10().getAmount().getValue().longValue());

          NewTicket newTicket = createTicket(String.format("Error al realizar carga %s", data.getPrepaidTopup10().getTransactionOriginType().name()),
            template,
            String.valueOf(data.getUser().getRut().getValue()),
            data.getPrepaidTopup10().getMessageId(),
            QueuesNameType.TOPUP,
            req.getReprocesQueue());

          Ticket ticket = getRoute().getUserClient().createFreshdeskTicket(null, data.getUser().getId(), newTicket);
          if (ticket.getId() != null) {
            log.info("Ticket Creado Exitosamente");
          }
        } else if (Errors.ERROR_INDETERMINADO.equals(data.getNumError())) {
          //TODO: que hacer con los errores indeterminados? deberian devolverse? investigarse?
          // Estos son errores de excepcion no esperados. Probablemente no deberian devolverse
          // tan rapido. Investigar?
        } else if (PrepaidMovementStatus.REJECTED.equals(data.getPrepaidMovement10().getEstado())) {
          // Comienza el proceso de devolucion

          // Se le envia un correo al usuario notificandole que hubo un problema con la carga
          Map<String, Object> templateDataToUser = new HashMap<String, Object>();
          templateDataToUser.put("user_name", data.getUser().getName());
          templateDataToUser.put("monto_carga", String.valueOf(data.getPrepaidTopup10().getTotal().getValue().doubleValue()));

          PrepaidMovement10 refundMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementById(data.getPrepaidMovement10().getId());
          LocalDateTime topupDateTime = refundMovement.getFechaCreacion().toLocalDateTime();
          templateDataToUser.put("fecha_topup", topupDateTime.toLocalDate());

          EmailBody emailBody = new EmailBody();
          emailBody.setTemplateData(templateDataToUser);
          emailBody.setTemplate(TEMPLATE_MAIL_ERROR_TOPUP_TO_USER);
          emailBody.setAddress(data.getUser().getEmail().getValue());
          getRoute().getMailPrepaidEJBBean10().sendMailAsync(null, data.getUser().getId(), emailBody);
        }
        return req;
      }
    };
  }
}
