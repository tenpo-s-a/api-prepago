package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_CARD_ERROR;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_EMISSION_ERROR;

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

        } else if (altaClienteDTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
          return redirectRequest(endpoint, exchange, req, true);
        } else {

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

        } else if (datosTarjetaDTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, true);
        } else {

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
      //TODO falta implementar, no se sabe que hacer en este caso
      /**
       *  ENVIO DE MAIL ERROR ENVIO DE TARJETA
       */
      Map<String, Object> templateData = new HashMap<String, Object>();
      templateData.put("idUsuario", data.getUser().getId().toString());
      templateData.put("rutCliente", data.getUser().getRut().getValue().toString()+ "-" + data.getUser().getRut().getDv());
      getRoute().getMailEJBBean10().sendEmail(TEMPLATE_MAIL_EMISSION_ERROR, templateData, "soporte-prepago@multicaja.cl", data.getUser().getId());

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
      //TODO falta implementar, no se sabe que hacer en este caso
      return req;
      }
    };
  }
}
