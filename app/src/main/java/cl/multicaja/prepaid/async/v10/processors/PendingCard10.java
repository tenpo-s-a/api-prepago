package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingEmission - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        if(req.getRetryCount() > 3) {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }

        User user = data.getUser();

        AltaClienteDTO altaClienteDTO = getRoute().getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);

        if (altaClienteDTO.isRetornoExitoso()) {

          PrepaidCard10 prepaidCard = new PrepaidCard10();
          prepaidCard.setIdUser(data.getPrepaidUser10().getId());
          prepaidCard.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
          prepaidCard = getRoute().getPrepaidCardEJBBean10().createPrepaidCard(null,prepaidCard);
          data.setPrepaidCard10(prepaidCard);

          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);

        } else if (altaClienteDTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          redirectRequest(endpoint, exchange, req);
        } else {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_EMISSION_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_EMISSION_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
        }

        return new ResponseRoute<>(data);
      }
    };
  }

  /**
   *
   * @return
   */
  public ProcessorRoute processPendingCreateCard(){
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingCreateCard - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        if(req.getRetryCount() > 3) {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CREATE_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
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

          boolean updated = getRoute().getPrepaidCardEJBBean10().updatePrepaidCard(null,
            data.getPrepaidCard10().getId(),
            data.getPrepaidCard10().getIdUser(),
            data.getPrepaidCard10().getStatus(),
            prepaidCard10);

          if (updated) {
            data.setPrepaidCard10(prepaidCard10);
            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_REQ);
            req.setData(data);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);

            redirectRequest(endpoint, exchange, req);
          } else {

            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD;
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(), status);
            data.getPrepaidMovement10().setEstado(status);

            Endpoint endpoint = createJMSEndpoint(ERROR_CREATE_CARD_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
          }

        } else if (datosTarjetaDTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CREATE_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          redirectRequest(endpoint, exchange, req);
        } else {

          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_CREATE_CARD;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(), status);
          data.getPrepaidMovement10().setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CREATE_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
        }

        return new ResponseRoute<>(data);
      }
    };

  }

  /* Cola Errores */
  public ProcessorRoute processErrorEmission() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
      log.info("processErrorEmission - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupDataRoute10 data = req.getData();
      data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

      //TODO falta implementar la devolucion

      return new ResponseRoute<>(data);
      }
    };
  }

  public ProcessorRoute processErrorCreateCard() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
      log.info("processErrorCreateCard - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupDataRoute10 data = req.getData();
      data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

      //TODO falta implementar la devolucion

      return new ResponseRoute<>(data);
      }
    };
  }
}
