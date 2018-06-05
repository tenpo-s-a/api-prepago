package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import static cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10.*;

/**
 * @autor abarazarte
 */
public class PendingCardIssuanceFee10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCardIssuanceFee10.class);

  public PendingCardIssuanceFee10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
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

          issuanceFeeMovement = getPrepaidMovementEJBBean10().addPrepaidMovement(null, issuanceFeeMovement);

          req.getData().setIssuanceFeeMovement10(issuanceFeeMovement);
        }

        if(req.getRetryCount() > 3) {

          issuanceFeeMovement.setNumextcta(0);
          issuanceFeeMovement.setNummovext(0);
          issuanceFeeMovement.setClamone(0);
          issuanceFeeMovement.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE);

          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }

        String contrato = prepaidCard.getProcessorUserId();
        String pan = getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());
        CodigoMoneda clamon = prepaidMovement.getClamon();
        IndicadorNormalCorrector indnorcor = prepaidMovement.getIndnorcor();
        TipoFactura tipofac = prepaidMovement.getTipofac();
        BigDecimal impfac = prepaidMovement.getImpfac();
        String codcom = prepaidMovement.getCodcom();
        Integer codact = prepaidMovement.getCodact();
        CodigoPais codpais = prepaidMovement.getCodpais();
        String nomcomred = prepaidTopup.getMerchantName();
        String numreffac = issuanceFeeMovement.getId().toString();
        String numaut = numreffac;

        //solamente los 6 primeros digitos de numreffac
        if (numaut.length() > 6) {
          numaut = numaut.substring(numaut.length()-6);
        }

        InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(contrato,
          pan, clamon, indnorcor, tipofac, numreffac, impfac, numaut, codcom, nomcomred, codact, codpais);

        if (CodigoRetorno._000.equals(inclusionMovimientosDTO.getRetorno())) {

          issuanceFeeMovement.setNumextcta(inclusionMovimientosDTO.getNumextcta());
          issuanceFeeMovement.setNummovext(inclusionMovimientosDTO.getNummovext());
          issuanceFeeMovement.setClamone(inclusionMovimientosDTO.getClamone());
          issuanceFeeMovement.setEstado(PrepaidMovementStatus.PROCESS_OK); //realizado

          getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            inclusionMovimientosDTO.getNumextcta(),
            inclusionMovimientosDTO.getNummovext(),
            inclusionMovimientosDTO.getClamone(),
            issuanceFeeMovement.getEstado());

          // Activa la tarjeta luego de realizado el cobro de emision
          prepaidCard.setStatus(PrepaidCardStatus.ACTIVE);

          getPrepaidEJBBean10().updateCard(null,
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

          issuanceFeeMovement.setNumextcta(0);
          issuanceFeeMovement.setNummovext(0);
          issuanceFeeMovement.setClamone(0);
          issuanceFeeMovement.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE);

          getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            issuanceFeeMovement.getNumextcta(),
            issuanceFeeMovement.getNummovext(),
            issuanceFeeMovement.getClamone(),
            issuanceFeeMovement.getEstado());

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

        PrepaidMovement10 issuanceFeeMovement = data.getIssuanceFeeMovement10();
        issuanceFeeMovement.setNumextcta(0);
        issuanceFeeMovement.setNummovext(0);
        issuanceFeeMovement.setClamone(0);
        issuanceFeeMovement.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE);

        getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
          issuanceFeeMovement.getId(),
          issuanceFeeMovement.getNumextcta(),
          issuanceFeeMovement.getNummovext(),
          issuanceFeeMovement.getClamone(),
          issuanceFeeMovement.getEstado());

        return new ResponseRoute<>(data);
      }
    };
  }
}
