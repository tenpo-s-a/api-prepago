package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
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

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_EMISSION_ERROR;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_ERROR_CREATE_CARD;

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

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        User user = data.getUser();

        final TipoAlta tipoAlta = data.getPrepaidUser10().getUserLevel() == PrepaidUserLevel.LEVEL_2 ? TipoAlta.NIVEL2 : TipoAlta.NIVEL1;
        AltaClienteDTO altaClienteDTO = getRoute().getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);

        if (altaClienteDTO.isRetornoExitoso()) {

          PrepaidCard10 prepaidCard = new PrepaidCard10();
          prepaidCard.setIdUser(data.getPrepaidUser10().getId());
          prepaidCard.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
          prepaidCard = getRoute().getPrepaidCardEJBBean10().createPrepaidCard(null,prepaidCard);
          data.setPrepaidCard10(prepaidCard);

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

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          return redirectRequest(endpoint, exchange, req, false);
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

        DatosTarjetaDTO datosTarjetaDTO = getRoute().getTecnocomService().datosTarjeta(data.getPrepaidCard10().getProcessorUserId());

        if (datosTarjetaDTO.isRetornoExitoso()) {

          PrepaidCard10 prepaidCard10 = getRoute().getPrepaidCardEJBBean10().getPrepaidCardById(null, data.getPrepaidCard10().getId());

          prepaidCard10.setNameOnCard(data.getUser().getName() + " " + data.getUser().getLastname_1());
          prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
          prepaidCard10.setEncryptedPan(getRoute().getEncryptUtil().encrypt(datosTarjetaDTO.getPan()));
          prepaidCard10.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard10.setExpiration(datosTarjetaDTO.getFeccadtar());
          prepaidCard10.setProducto(datosTarjetaDTO.getProducto());
          prepaidCard10.setNumeroUnico(datosTarjetaDTO.getIdentclitar());

          try {

            getRoute().getPrepaidCardEJBBean10().updatePrepaidCard(null,
              data.getPrepaidCard10().getId(),
              data.getPrepaidCard10().getIdUser(),
              data.getPrepaidCard10().getStatus(),
              prepaidCard10);

            data.setPrepaidCard10(prepaidCard10);
            req.setData(data);

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

      if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
        Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
        Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
        String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
        template = TemplateUtils.freshDeskTemplateColas2(template,"Error al dar de Alta a Cliente",String.format("%s %s",data.getUser().getName(),data.getUser().getLastname_1()),String.format("%s-%s",data.getUser().getRut().getValue(),data
        .getUser().getRut().getDv()),data.getUser().getId());

        NewTicket newTicket = createTicket("Error al dar de Alta a Cliente",template,String.valueOf(data.getUser().getRut().getValue()),data.getPrepaidTopup10().getMessageId(),QueuesNameType.PENDING_EMISSION,req.getReprocesQueue());
        Ticket ticket = getRoute().getUserClient().createFreshdeskTicket(null,data.getUser().getId(),newTicket);
        if(ticket.getId() != null){
          log.info("Ticket Creado Exitosamente");
        }
      } else {
        /**
         *  ENVIO DE MAIL ERROR ENVIO DE TARJETA
         */
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("idUsuario", data.getUser().getId().toString());
        templateData.put("rutCliente", data.getUser().getRut().getValue().toString() + "-" + data.getUser().getRut().getDv());
        getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_EMISSION_ERROR, templateData);
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
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
          template = TemplateUtils.freshDeskTemplateColas2(template,"Error al obtener datos tarjeta",String.format("%s %s",data.getUser().getName(),data.getUser().getLastname_1()),String.format("%s-%s",data.getUser().getRut().getValue(),data
            .getUser().getRut().getDv()),data.getUser().getId());


          NewTicket newTicket = createTicket("Error al obtener datos tarjeta",
            template,
            String.valueOf(data.getUser().getRut().getValue()),
            data.getPrepaidTopup10().getMessageId(),
            QueuesNameType.CREATE_CARD,
            req.getReprocesQueue()
          );

          Ticket ticket = getRoute().getUserClient().createFreshdeskTicket(null,data.getUser().getId(),newTicket);
          if(ticket.getId() != null){
            log.info("Ticket Creado Exitosamente");
          }
        } else {
          Map<String, Object> templateData = new HashMap<>();
          templateData.put("idUsuario", data.getUser().getId().toString());
          templateData.put("rutCliente", data.getUser().getRut().getValue().toString() + "-" + data.getUser().getRut().getDv());
          getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_CREATE_CARD, templateData);
        }
        return req;
      }
    };
  }
}
