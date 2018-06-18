package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;

/**
 * @autor abarazarte
 */
public class PendingCardIssuanceFee10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCardIssuanceFee10.class);

  public PendingCardIssuanceFee10(BaseRoute10 route) {
    super(route);
  }

  /**
   *
   * @return
   */
  public ProcessorRoute processPendingIssuanceFee() {

    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingIssuanceFee - REQ: " + req);

        PrepaidTopupDataRoute10 data = req.getData();

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();
        PrepaidTopup10 prepaidTopup = req.getData().getPrepaidTopup10();
        PrepaidCard10 prepaidCard = req.getData().getPrepaidCard10();

        if (prepaidTopup == null) {
          log.error("Error req.getData().getPrepaidTopup10() es null");
          return null;
        }

        if (prepaidCard == null) {
          log.error("Error req.getData().getPrepaidCard10() es null");
          return null;
        }

        if(!PrepaidCardStatus.PENDING.equals(prepaidCard.getStatus())){
          log.error("Error req.getData().getPrepaidCard10().getStatus() es " + prepaidCard.getStatus().toString());
          return null;
        }
        if (prepaidMovement == null) {
          log.error("Error req.getData().getPrepaidMovement10() es null");
          return null;
        }

        req.retryCountNext();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        PrepaidMovement10 issuanceFeeMovement = data.getIssuanceFeeMovement10();

        if (issuanceFeeMovement == null) {
          issuanceFeeMovement = (PrepaidMovement10) prepaidMovement.clone();
          issuanceFeeMovement.setTipoMovimiento(PrepaidMovementType.ISSUANCE_FEE);
          issuanceFeeMovement.setTipofac(TipoFactura.COMISION_APERTURA);
          issuanceFeeMovement.setId(null);
          issuanceFeeMovement.setEstado(PrepaidMovementStatus.PENDING);

          issuanceFeeMovement = getRoute().getPrepaidMovementEJBBean10().addPrepaidMovement(null, issuanceFeeMovement);

          req.getData().setIssuanceFeeMovement10(issuanceFeeMovement);
        }

        if(req.getRetryCount() > 3) {

          Integer numextcta = 0;
          Integer nummovext = 0;
          Integer clamone = 0;
          PrepaidMovementStatus  status = PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            numextcta,
            nummovext,
            clamone,
            status);

          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }

        String contrato = prepaidCard.getProcessorUserId();
        String pan = getRoute().getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());
        CodigoMoneda clamon = issuanceFeeMovement.getClamon();
        IndicadorNormalCorrector indnorcor = issuanceFeeMovement.getIndnorcor();
        TipoFactura tipofac = issuanceFeeMovement.getTipofac();
        BigDecimal impfac = issuanceFeeMovement.getImpfac();
        String codcom = issuanceFeeMovement.getCodcom();
        Integer codact = issuanceFeeMovement.getCodact();
        CodigoPais codpais = issuanceFeeMovement.getCodpais();
        String nomcomred = prepaidTopup.getMerchantName();
        String numreffac = issuanceFeeMovement.getId().toString();
        String numaut = numreffac;

        //solamente los 6 primeros digitos de numreffac
        if (numaut.length() > 6) {
          numaut = numaut.substring(numaut.length()-6);
        }

        InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(contrato,
          pan, clamon, indnorcor, tipofac, numreffac, impfac, numaut, codcom, nomcomred, codact, codpais);

        if (inclusionMovimientosDTO.isRetornoExitoso()) {

          Integer numextcta = inclusionMovimientosDTO.getNumextcta();
          Integer nummovext = inclusionMovimientosDTO.getNummovext();
          Integer clamone = inclusionMovimientosDTO.getClamone();
          PrepaidMovementStatus  status = PrepaidMovementStatus.PROCESS_OK;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            numextcta,
            nummovext,
            clamone,
            status);

          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);

          // Activa la tarjeta luego de realizado el cobro de emision
          prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);

          getRoute().getPrepaidCardEJBBean10().updatePrepaidCard(null,
            prepaidCard.getId(),
            prepaidCard.getIdUser(),
            PrepaidCardStatus.PENDING,
            prepaidCard);

          // Envia a la cola de envio de email con la informacion de la tarjeta
          Endpoint endpoint = createJMSEndpoint(PENDING_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);

        } else if (CodigoRetorno._1000.equals(inclusionMovimientosDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          redirectRequest(endpoint, exchange, req);
        } else {

          Integer numextcta = 0;
          Integer nummovext = 0;
          Integer clamone = 0;
          PrepaidMovementStatus  status = PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            numextcta,
            nummovext,
            clamone,
            status);

          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
        }
        return new ResponseRoute<>(data);
      }
    };
  }

  /* Cola Errores */
  public ProcessorRoute processErrorPendingIssuanceFee() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processErrorPendingIssuanceFee - REQ: " + req);

        req.retryCountNext();
        PrepaidTopupDataRoute10 data = req.getData();
        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        return new ResponseRoute<>(data);
      }
    };
  }
}
