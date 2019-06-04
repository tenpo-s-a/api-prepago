package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.external.freshdesk.model.NewTicket;
import cl.multicaja.prepaid.external.freshdesk.model.Ticket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.FreshdeskServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;

/**
 * @autor vutreras
 */
public class PendingCard10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCard10.class);

  public PendingCard10(BaseRoute10 route) {
    super(route);
  }

  /**
   *
   * @return
   */
  public ProcessorRoute processPendingEmission() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingEmission - REQ: " + req);
        try {
        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          log.info("Max Retry count");
          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        PrepaidUser10 user = data.getPrepaidUser10();

        log.info(String.format("Realizando alta de cliente %s", user.getDocumentNumber()));
        
        final TipoAlta tipoAlta = data.getPrepaidUser10().getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL2;
        AltaClienteDTO altaClienteDTO = getRoute().getTecnocomService().altaClientes(user.getName(), user.getLastName(), "", user.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);

        log.info(String.format("Respuesta alta de cliente [%s] [%s]",altaClienteDTO.getRetorno(),altaClienteDTO.getDescRetorno()));

        if (altaClienteDTO.isRetornoExitoso()) {

          // guarda la informacion de la cuenta
          Account account = getRoute().getAccountEJBBean10().insertAccount(data.getPrepaidUser10().getId(), altaClienteDTO.getContrato());
          log.info(String.format("Account [%s]",account.getAccountNumber()));
          // publica evento de contrato/cuenta creada
          getRoute().getAccountEJBBean10().publishAccountCreatedEvent(data.getPrepaidUser10().getUuid(), account);

          PrepaidCard10 prepaidCard = new PrepaidCard10();
          prepaidCard.setAccountId(account.getId());
          prepaidCard.setIdUser(data.getPrepaidUser10().getId());
          prepaidCard.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
          prepaidCard.setUuid(UUID.randomUUID().toString());

          prepaidCard = getRoute().getPrepaidCardEJBBean11().createPrepaidCard(null,prepaidCard);
          data.setPrepaidCard10(prepaidCard);

          req.getData().setAccount(account);

          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, false);

        } else if (CodigoRetorno._1000.equals(altaClienteDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
          req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
          req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }else if (CodigoRetorno._1010.equals(altaClienteDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
          return redirectRequest(endpoint, exchange, req, true);
        } else if (CodigoRetorno._1020.equals(altaClienteDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }  else {
          log.info("Error alta cliente: "+altaClienteDTO);
          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }
        }catch (Exception e) {
          log.info(e.getMessage());
          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          req.getData().setNumError(Errors.ERROR_INDETERMINADO);
          req.getData().setMsjError(Errors.ERROR_INDETERMINADO.name());
          e.printStackTrace();
          return redirectRequest(endpoint, exchange, req, true);
        }
      }
    };
  }

  /**
   *
   * @return
   */
  public ProcessorRoute processPendingCreateCard(){
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingCreateCard - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CREATE_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        log.info(String.format("Obeteniendo datos de tarjeta %s", data.getPrepaidCard10().getProcessorUserId()));
        Account account = data.getAccount();
        DatosTarjetaDTO datosTarjetaDTO = getRoute().getTecnocomService().datosTarjeta(account.getAccountNumber());

        log.info("Respuesta datos tarjeta");
        log.info(datosTarjetaDTO.getRetorno());
        log.info(datosTarjetaDTO.getDescRetorno());

        if (datosTarjetaDTO.isRetornoExitoso()) {

          PrepaidCard10 prepaidCard10 = getRoute().getPrepaidCardEJBBean11().getPrepaidCardById(null, data.getPrepaidCard10().getId());
          PrepaidUser10 prepaidUser10 = data.getPrepaidUser10();

          prepaidCard10.setNameOnCard(prepaidUser10.getName() + " " + prepaidUser10.getLastName());
          prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
          prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard10.setExpiration(datosTarjetaDTO.getFeccadtar());
          prepaidCard10.setProducto(datosTarjetaDTO.getProducto());
          prepaidCard10.setNumeroUnico(datosTarjetaDTO.getIdentclitar());

          // se trunca el pan
          prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));

          // se encripta el Pan
          prepaidCard10.setEncryptedPan(getRoute().getEncryptHelper().encryptPan(datosTarjetaDTO.getPan()));

          // se guarda un hash del pan utilizando como secret el accountNumber (contrato)
          prepaidCard10.setHashedPan(getRoute().getPrepaidCardEJBBean11().hashPan(account.getAccountNumber(), datosTarjetaDTO.getPan()));

          try {
            // Actualiza la tarjeta
            getRoute().getPrepaidCardEJBBean11().updatePrepaidCard(null, data.getPrepaidCard10().getId(),
              data.getAccount().getId(),
              prepaidCard10);

            data.setPrepaidCard10(prepaidCard10);
            req.setData(data);

            log.info(prepaidCard10.toString());

            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_REQ);
            return redirectRequest(endpoint, exchange, req, false);

          } catch(Exception ex) {

            log.error("Error al actualizar tarjeta", ex);

            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD;
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
            data.getPrepaidMovement10().setEstado(status);

            Endpoint endpoint = createJMSEndpoint(ERROR_CREATE_CARD_REQ);
            return redirectRequest(endpoint, exchange, req, false);
          }

        } else if (CodigoRetorno._1000.equals(datosTarjetaDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
          req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }else if (CodigoRetorno._1010.equals(datosTarjetaDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
          return redirectRequest(endpoint, exchange, req, true);
        } else if (CodigoRetorno._1020.equals(datosTarjetaDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }
        else {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CREATE_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }
      }
    };
  }

  /* Cola Errores */
  public ProcessorRoute processErrorEmission() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
      log.info("processErrorEmission - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupData10 data = req.getData();
      PrepaidUser10 prepaidUser10 = data.getPrepaidUser10();
      if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
        Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
        Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
        String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
        template = TemplateUtils.freshDeskTemplateColas2(template,"Error al dar de Alta a Cliente",String.format("%s %s",prepaidUser10.getName(),prepaidUser10.getLastName()),prepaidUser10.getDocumentNumber(),prepaidUser10.getId());

        NewTicket newTicket = createTicket("Error al dar de Alta a Cliente",template,prepaidUser10.getUuid(),data.getPrepaidTopup10().getMessageId(),QueuesNameType.PENDING_EMISSION,req.getReprocesQueue());

        Ticket ticket = FreshdeskServiceHelper.getInstance().getFreshdeskService().createTicket(newTicket);
        if(ticket.getId() != null){
          log.info("[processErrorWithdrawReversal][Ticket_Success][id]:"+ticket.getId());
        }else{
          log.info("[processErrorWithdrawReversal][Ticket_Fail][ticketData]:"+newTicket.toString());
        }

      } else {
        /**
         *  ENVIO DE MAIL ERROR ENVIO DE TARJETA
         */
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("idUsuario",prepaidUser10.getId());
        templateData.put("rutCliente", prepaidUser10.getDocumentNumber());
        //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_EMISSION_ERROR, templateData);
      }
      return req;
      }
    };
  }

  public ProcessorRoute processErrorCreateCard() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
      log.info("processErrorCreateCard - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupData10 data = req.getData();
      PrepaidUser10 prepaidUser10 = data.getPrepaidUser10();
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
          template = TemplateUtils.freshDeskTemplateColas2(template,"Error al obtener datos tarjeta",String.format("%s %s",prepaidUser10.getName(),prepaidUser10.getLastName()),prepaidUser10.getDocumentNumber(),prepaidUser10.getId());


          NewTicket newTicket = createTicket("Error al obtener datos tarjeta",
            template,
            prepaidUser10.getUuid(),
            data.getPrepaidTopup10().getMessageId(),
            QueuesNameType.CREATE_CARD,
            req.getReprocesQueue()
          );

          Ticket ticket = FreshdeskServiceHelper.getInstance().getFreshdeskService().createTicket(newTicket);
          if (ticket != null && ticket.getId() != null) {
            log.info("[processErrorCreateCard][Ticket_Success][ticketId]:"+ticket.getId());
          }else{
            log.info("[processErrorCreateCard][Ticket_Fail][ticketData]:"+newTicket.toString());
          }

        } else {
          Map<String, Object> templateData = new HashMap<>();
          templateData.put("idUsuario", prepaidUser10.getId());
          templateData.put("rutCliente", prepaidUser10.getDocumentNumber());
          //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_CREATE_CARD, templateData);
        }
        return req;
      }
    };
  }
}
