package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

import static cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10.*;

/**
 * @autor vutreras
 */
public class PendingTopup10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingTopup10.class);

  public PendingTopup10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  public ProcessorRoute processPendingTopup() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopup - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();

        log.info("processPendingTopup prepaidTopup: " + prepaidTopup);

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

        log.info("processPendingTopup prepaidMovement: " + prepaidMovement);

        if(req.getRetryCount() > 3) {

          //segun la historia: https://www.pivotaltracker.com/story/show/157850744
          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
          getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement.getId(), status);
          prepaidMovement.setEstado(status);

          Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_RETURNS_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }

        User user = data.getUser();

        log.info("processPendingTopup user: " + user);

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

        PrepaidUser10 prepaidUser = getPrepaidEJBBean10().getPrepaidUserByRut(null, rut);

        log.info("processPendingTopup prepaidUser: " + prepaidUser);

        if (prepaidUser == null){
          log.error("Error al buscar PrepaidUser10 con rut: " + rut);
          return null;
        }

        data.setPrepaidUser10(prepaidUser);

        PrepaidCard10 prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

        if (prepaidCard == null) {
          prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED);
        }

        if (prepaidCard != null) {

          data.setPrepaidCard10(prepaidCard);

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
          String numreffac = prepaidMovement.getId().toString();
          String numaut = numreffac;

          //solamente los 6 primeros digitos de numreffac
          if (numaut.length() > 6) {
            numaut = numaut.substring(numaut.length()-6);
          }

          InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
                                                                                                      numreffac, impfac, numaut, codcom,
                                                                                                      nomcomred, codact, codpais);

          if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._000)) {

            Integer numextcta = inclusionMovimientosDTO.getNumextcta();
            Integer nummovext = inclusionMovimientosDTO.getNummovext();
            Integer clamone = inclusionMovimientosDTO.getClamone();
            PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK; //realizado

            getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement.getId(), numextcta, nummovext, clamone, status);

            prepaidMovement.setNumextcta(numextcta);
            prepaidMovement.setNummovext(nummovext);
            prepaidMovement.setClamone(clamone);
            prepaidMovement.setEstado(status);

            CdtTransaction10 cdtTransaction = data.getCdtTransaction10();

            log.info("CDT ::" + cdtTransaction);

            CdtTransaction10 cdtTransactionConfirm = new CdtTransaction10();
            cdtTransactionConfirm.setAmount(cdtTransaction.getAmount());
            cdtTransactionConfirm.setTransactionType(prepaidTopup.getCdtTransactionTypeConfirm());
            cdtTransactionConfirm.setAccountId(cdtTransaction.getAccountId());
            cdtTransactionConfirm.setGloss(cdtTransaction.getGloss());
            cdtTransactionConfirm.setTransactionReference(cdtTransaction.getTransactionReference());
            //se debe agregar CONFIRM para evitar el constraint unique de esa columna
            cdtTransactionConfirm.setExternalTransactionId(cdtTransaction.getExternalTransactionIdConfirm());

            cdtTransactionConfirm = getCdtEJBBean10().addCdtTransaction(null, cdtTransactionConfirm);

            data.setCdtTransactionConfirm10(cdtTransactionConfirm);

            //TODO que pasa si cdt da error?

            log.info("CDT Confirm:: " + cdtTransactionConfirm);

            if(!cdtTransactionConfirm.getNumError().equals("0")){
              long lNumError = numberUtils.toLong(cdtTransactionConfirm.getNumError(),-1L);
              if(lNumError != -1 && lNumError > 10000) {
                throw new ValidationException(107000).setData(new KeyValue("value", cdtTransactionConfirm.getMsjError()));
              } else {
                throw new ValidationException(101006).setData(new KeyValue("value", cdtTransactionConfirm.getMsjError()));
              }
            }

            //segun la historia: https://www.pivotaltracker.com/story/show/157442267
            // Si es 1era carga enviar a cola de cobro de emision
            if(prepaidTopup.isFirstTopup()){
              Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
              data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
              req.setRetryCount(0);
              redirectRequest(endpoint, exchange, req);
            } else {
              return new ResponseRoute<>(data);
            }

          } else if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._1000)) {
            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            redirectRequest(endpoint, exchange, req);
          } else {

            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
            getPrepaidMovementEJBBean10().updatePrepaidMovement(null, data.getPrepaidMovement10().getId(), status);
            data.getPrepaidMovement10().setEstado(status);

            Endpoint endpoint = createJMSEndpoint(PENDING_TOPUP_RETURNS_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
          }

        } else {

          //https://www.pivotaltracker.com/story/show/157816408
          //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

          prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED_HARD);

          if (prepaidCard == null) {
            prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.EXPIRED);
          }

          if (prepaidCard == null) {
            Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
          } else {
            data.setPrepaidCard10(prepaidCard);
            return new ResponseRoute<>(data);
          }
        }

        return new ResponseRoute<>(data);
      }
    };
  }

  public ProcessorRoute processPendingTopupReturns() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopupReturns - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        //TODO falta implementar la devolucion

        return new ResponseRoute<>(data);
      }
    };
  }
}
