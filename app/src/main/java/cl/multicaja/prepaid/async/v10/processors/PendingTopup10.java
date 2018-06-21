package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.TRANSACCION_ERROR_GENERICO_$VALUE;
import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;

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

        log.info("processPendingTopup - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

        if(req.getRetryCount() > getMaxRetryCount()) {

          //segun la historia: https://www.pivotaltracker.com/story/show/157850744
          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);
          prepaidMovement.setEstado(status);

          Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_RETURNS_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return req;
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

        data.setPrepaidUser10(prepaidUser);

        PrepaidCard10 prepaidCard = getRoute().getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                                    PrepaidCardStatus.ACTIVE,
                                                                                                    PrepaidCardStatus.LOCKED,
                                                                                                    PrepaidCardStatus.PENDING);

        if (prepaidCard != null) {

          data.setPrepaidCard10(prepaidCard);

          String contrato = prepaidCard.getProcessorUserId();
          String pan = getRoute().getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());
          CodigoMoneda clamon = prepaidMovement.getClamon();
          IndicadorNormalCorrector indnorcor = prepaidMovement.getIndnorcor();
          TipoFactura tipofac = prepaidMovement.getTipofac();
          BigDecimal impfac = prepaidMovement.getImpfac();
          String codcom = prepaidMovement.getCodcom();
          Integer codact = prepaidMovement.getCodact();
          CodigoMoneda clamondiv = CodigoMoneda.NONE;
          String nomcomred = prepaidTopup.getMerchantName();
          String numreffac = prepaidMovement.getId().toString();
          String numaut = numreffac;

          //solamente los 6 primeros digitos de numreffac
          if (numaut.length() > 6) {
            numaut = numaut.substring(numaut.length()-6);
          }

          InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
                                                                                                      numreffac, impfac, numaut, codcom,
                                                                                                      nomcomred, codact, clamondiv,impfac);

          if (inclusionMovimientosDTO.isRetornoExitoso()) {

            Integer numextcta = inclusionMovimientosDTO.getNumextcta();
            Integer nummovext = inclusionMovimientosDTO.getNummovext();
            Integer clamone = inclusionMovimientosDTO.getClamone();
            PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK; //realizado

            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement.getId(), numextcta, nummovext, clamone, status);

            prepaidMovement.setNumextcta(numextcta);
            prepaidMovement.setNummovext(nummovext);
            prepaidMovement.setClamone(clamone);
            prepaidMovement.setEstado(status);

            CdtTransaction10 cdtTransaction = data.getCdtTransaction10();

            CdtTransaction10 cdtTransactionConfirm = new CdtTransaction10();
            cdtTransactionConfirm.setAmount(cdtTransaction.getAmount());
            cdtTransactionConfirm.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
            cdtTransactionConfirm.setAccountId(cdtTransaction.getAccountId());
            cdtTransactionConfirm.setTransactionReference(cdtTransaction.getTransactionReference());
            cdtTransactionConfirm.setIndSimulacion(false);
            //se debe agregar CONFIRM para evitar el constraint unique de esa columna
            cdtTransactionConfirm.setExternalTransactionId(cdtTransaction.getExternalTransactionId());
            cdtTransactionConfirm.setGloss(prepaidTopup.getCdtTransactionTypeConfirm().getName() + " " + cdtTransactionConfirm.getAmount());

            cdtTransactionConfirm = getRoute().getCdtEJBBean10().addCdtTransaction(null, cdtTransactionConfirm);

            data.setCdtTransactionConfirm10(cdtTransactionConfirm);

            //TODO que pasa si cdt da error?
            if(!cdtTransaction.isNumErrorOk()){
              int lNumError = cdtTransaction.getNumErrorInt();
              if(lNumError > TRANSACCION_ERROR_GENERICO_$VALUE.getValue()) {
                throw new ValidationException(lNumError).setData(new KeyValue("value", cdtTransaction.getMsjError()));
              } else {
                throw new ValidationException(TRANSACCION_ERROR_GENERICO_$VALUE).setData(new KeyValue("value", cdtTransaction.getMsjError()));
              }
            }

            //segun la historia: https://www.pivotaltracker.com/story/show/158044562

            if(PrepaidCardStatus.PENDING.equals(prepaidCard.getStatus())){
              Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
              data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
              req.setRetryCount(0);
              redirectRequest(endpoint, exchange, req);
            } else {
              return req;
            }

          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            redirectRequest(endpoint, exchange, req, getDelayTimeoutToRedirectForRetryCount(req.getRetryCount()));
          } else {

            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
            data.getPrepaidMovement10().setEstado(status);

            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_RETURNS_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
          }

        } else {

          //https://www.pivotaltracker.com/story/show/157816408
          //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

          prepaidCard = getRoute().getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                      PrepaidCardStatus.LOCKED_HARD,
                                                                                      PrepaidCardStatus.EXPIRED);

          if (prepaidCard == null) {
            Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
          } else {
            data.setPrepaidCard10(prepaidCard);
            return req;
          }
        }

        return req;
      }
    };
  }

  public ProcessorRoute processPendingTopupReturns() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
      log.info("processPendingTopupReturns - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupData10 data = req.getData();
      data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));
      //TODO falta implementar, no se sabe que hacer en este caso
      return req;
      }
    };
  }
}
