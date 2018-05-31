package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @autor vutreras
 */
public class PendingCard10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCard10.class);

  public PendingCard10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
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

        if(req.getRetryCount() > 3) {
          req.setRetryCount(0);
          redirectRequest(createJMSEndpoint(getRoute().ERROR_EMISSION_REQ), exchange, req);
          return new ResponseRoute<>(data);
        }

        User user = data.getUser();

        AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), user.getRut().getValue().toString(), TipoDocumento.RUT);

        if (altaClienteDTO.getRetorno().equals(CodigoRetorno._000)) {

          PrepaidCard10 prepaidCard =  new PrepaidCard10();
          prepaidCard.setIdUser(data.getPrepaidUser10().getId());
          prepaidCard.setStatus(PrepaidCardStatus.PENDING);
          prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
          prepaidCard = getPrepaidEJBBean10().createPrepaidCard(null,prepaidCard);
          data.setPrepaidCard10(prepaidCard);
          req.setRetryCount(0);

          redirectRequest(createJMSEndpoint(getRoute().PENDING_CREATECARD_REQ), exchange, req);

        } else if (altaClienteDTO.getRetorno().equals(CodigoRetorno._1000)) {
          redirectRequest(createJMSEndpoint(getRoute().PENDING_EMISSION_REQ), exchange, req);
        } else {
          req.setRetryCount(0);
          redirectRequest(createJMSEndpoint(getRoute().ERROR_EMISSION_REQ), exchange, req);
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

        if(req.getRetryCount() > 3) {
          req.setRetryCount(0);
          redirectRequest(createJMSEndpoint(getRoute().ERROR_CREATECARD_REQ), exchange, req);
          return new ResponseRoute<>(data);
        }

        DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(data.getPrepaidCard10().getProcessorUserId());

        if (datosTarjetaDTO.getRetorno().equals(CodigoRetorno._000)) {

          PrepaidCard10 prepaidCard10 = getPrepaidEJBBean10().getPrepaidCardById(null, data.getPrepaidCard10().getId());

          prepaidCard10.setNameOnCard(data.getUser().getName() + " " + data.getUser().getLastname_1());
          prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
          prepaidCard10.setEncryptedPan(getEncryptUtil().encrypt(datosTarjetaDTO.getPan()));
          prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
          prepaidCard10.setExpiration(datosTarjetaDTO.getFeccadtar());

          boolean bUpdate = getPrepaidEJBBean10().updateCard(null,
            data.getPrepaidCard10().getId(),
            data.getPrepaidCard10().getIdUser(),
            data.getPrepaidCard10().getStatus(),
            prepaidCard10);

          if (!bUpdate) {
            req.setRetryCount(0);
            redirectRequest(createJMSEndpoint(getRoute().ERROR_CREATECARD_REQ), exchange, req);
          }

          data.setPrepaidCard10(prepaidCard10);

          redirectRequest(createJMSEndpoint(getRoute().PENDING_TOPUP_REQ), exchange, req);

        } else if (datosTarjetaDTO.getRetorno().equals(CodigoRetorno._1000)) {
          redirectRequest(createJMSEndpoint(getRoute().PENDING_CREATECARD_REQ), exchange, req);
        } else {
          req.setRetryCount(0);
          redirectRequest(createJMSEndpoint(getRoute().ERROR_CREATECARD_REQ), exchange, req);
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
        PrepaidTopupDataRoute10 data = req.getData();
        getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(),null,null,null,PrepaidMovementStatus.ERROR_IN_PROCESS);
        return new ResponseRoute<>(data);
      }
    };
  }

  public ProcessorRoute processErrorCreateCard() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
        log.info("processErrorCreateCard - REQ: " + req);
        PrepaidTopupDataRoute10 data = req.getData();
        getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(),null,null,null,PrepaidMovementStatus.ERROR_IN_PROCESS);
        return new ResponseRoute<>(data);
      }
    };
  }
}
