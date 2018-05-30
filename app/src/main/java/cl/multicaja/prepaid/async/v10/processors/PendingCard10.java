package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
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
        req.retryCountNext();

        if(req.getRetryCount()<= 3) {

          PrepaidTopupDataRoute10 prepaidTopup10 = req.getData();
          User user = prepaidTopup10.getUser();
          System.out.println(user);
          AltaClienteDTO altaClienteDTO = getTecnocomService().altaClientes(user.getName(), user.getLastname_1(), user.getLastname_2(), "" + user.getRut().getValue(), TipoDocumento.RUT);

          if (altaClienteDTO.getRetorno().equals(CodigoRetorno._000)) {

            PrepaidCard10 prepaidCard =  new PrepaidCard10();
            prepaidCard.setIdUser(prepaidTopup10.getPrepaidUser10().getId());
            prepaidCard.setStatus(PrepaidCardStatus.PENDING);
            prepaidCard.setProcessorUserId(altaClienteDTO.getContrato());
            prepaidCard =getPrepaidEJBBean10().createPrepaidCard(null,prepaidCard);
            req.getData().setPrepaidCard10(prepaidCard);
            req.setRetryCount(0);
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().PENDING_CREATECARD_REQ), req, exchange.getIn().getHeaders());

          } else if (altaClienteDTO.getRetorno().equals(CodigoRetorno._1000)) {
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().PENDING_EMISSION_REQ), req, exchange.getIn().getHeaders());
          }
          else {
            req.setRetryCount(0);
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().ERROR_EMISSION_REQ), req, exchange.getIn().getHeaders());
          }
        } else {
          req.setRetryCount(0);
          exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().ERROR_EMISSION_REQ), req, exchange.getIn().getHeaders());
        }
        log.info("processPendingEmission - REQ: " + req);
        return new ResponseRoute<>(req.getData());
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
        req.retryCountNext();
        if(req.getRetryCount() <= 3) {
          DatosTarjetaDTO datosTarjetaDTO = getTecnocomService().datosTarjeta(req.getData().getPrepaidCard10().getProcessorUserId());
          if (datosTarjetaDTO.getRetorno().equals(CodigoRetorno._000)) {

            PrepaidCard10 prepaidCard10 = getPrepaidEJBBean10().getPrepaidCardById(null,req.getData().getPrepaidCard10().getId());

            prepaidCard10.setNameOnCard(req.getData().getUser().getName() + " " + req.getData().getUser().getLastname_1());
            prepaidCard10.setPan(Utils.replacePan(datosTarjetaDTO.getPan()));
            prepaidCard10.setEncryptedPan(getEncryptUtil().encrypt(datosTarjetaDTO.getPan()));
            prepaidCard10.setStatus(PrepaidCardStatus.ACTIVE);
            prepaidCard10.setExpiration(datosTarjetaDTO.getFeccadtar());
            boolean bUpdate = getPrepaidEJBBean10().updateCard(null, req.getData().getPrepaidCard10().getId(),req.getData().getPrepaidCard10().getIdUser(),
              req.getData().getPrepaidCard10().getStatus(),prepaidCard10);

            if (!bUpdate) {
              req.setRetryCount(0);
              exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().ERROR_CREATECARD_REQ), req, exchange.getIn().getHeaders());
            }
            req.getData().setPrepaidCard10(prepaidCard10);
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().PENDING_TOPUP_REQ), req, exchange.getIn().getHeaders());

          }
          else if (datosTarjetaDTO.getRetorno().equals(CodigoRetorno._1000)) {
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().PENDING_CREATECARD_REQ), req, exchange.getIn().getHeaders());
          }
          else {
            req.setRetryCount(0);
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().ERROR_CREATECARD_REQ), req, exchange.getIn().getHeaders());
          }
        }
        else {
          req.setRetryCount(0);
          exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().ERROR_CREATECARD_REQ), req, exchange.getIn().getHeaders());
        }

        return new ResponseRoute<>(req.getData());
      }
    };

  }

  /* Cola Errores */
  public ProcessorRoute processErrorEmission() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
        log.info("processPendingEmission - REQ: " + req);
        getPrepaidMovementEJBBean10().updatePrepaidMovement(null,req.getData().getPrepaidMovement10().getId(),null,null,null,PrepaidMovementStatus.ERROR_IN_PROCESS);
        return new ResponseRoute<>(req.getData());
      }
    };
  }

  public ProcessorRoute processErrorCreateCard() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {
        log.info("processPendingEmission - REQ: " + req);
        getPrepaidMovementEJBBean10().updatePrepaidMovement(null,req.getData().getPrepaidMovement10().getId(),null,null,null,PrepaidMovementStatus.ERROR_IN_PROCESS);
        return new ResponseRoute<>(req.getData());
      }
    };

  }
}
