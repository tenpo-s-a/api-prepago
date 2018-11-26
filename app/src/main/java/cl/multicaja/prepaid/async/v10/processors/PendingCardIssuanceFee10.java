package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_ERROR_ISSUANCE_FEE;

/**
 * @autor abarazarte
 */
public class PendingCardIssuanceFee10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCardIssuanceFee10.class);

  public PendingCardIssuanceFee10(BaseRoute10 route) {
    super(route);
  }

  private CalculationsHelper getCalculationsHelper(){
    return CalculationsHelper.getInstance();
  }
  /**
   *
   * @returnr
   */
  public ProcessorRoute processPendingIssuanceFee() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingIssuanceFee - REQ: " + req);

        PrepaidTopupData10 data = req.getData();

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

        PrepaidMovement10 issuanceFeeMovement = data.getIssuanceFeeMovement10();

        if (issuanceFeeMovement == null) {
          issuanceFeeMovement = (PrepaidMovement10) prepaidMovement.clone();
          issuanceFeeMovement.setTipoMovimiento(PrepaidMovementType.ISSUANCE_FEE);
          issuanceFeeMovement.setTipofac(TipoFactura.COMISION_APERTURA);
          issuanceFeeMovement.setId(null);
          issuanceFeeMovement.setEstado(PrepaidMovementStatus.PENDING);
          issuanceFeeMovement.setImpfac(getCalculationsHelper().getCalculatorParameter10().getOPENING_FEE());
          issuanceFeeMovement.setConTecnocom(ReconciliationStatusType.PENDING);
          issuanceFeeMovement.setConSwitch(ReconciliationStatusType.PENDING);
          issuanceFeeMovement.setNumaut(null);

          issuanceFeeMovement = getRoute().getPrepaidMovementEJBBean10().addPrepaidMovement(null, issuanceFeeMovement);
          issuanceFeeMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementById(issuanceFeeMovement.getId());

          req.getData().setIssuanceFeeMovement10(issuanceFeeMovement);
        }

        if(req.getRetryCount() > getMaxRetryCount()) {

          Integer numextcta = 0;
          Integer nummovext = 0;
          Integer clamone = 0;
          PrepaidMovementStatus  status = PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            prepaidCard.getPan(),
            null,
            null,
            numextcta,
            nummovext,
            clamone,
            null,
            status);

          issuanceFeeMovement.setPan(prepaidCard.getPan());
          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        String contrato = prepaidCard.getProcessorUserId();
        String pan = getRoute().getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());
        CodigoMoneda clamon = issuanceFeeMovement.getClamon();
        IndicadorNormalCorrector indnorcor = issuanceFeeMovement.getIndnorcor();
        TipoFactura tipofac = issuanceFeeMovement.getTipofac();
        BigDecimal impfac = issuanceFeeMovement.getImpfac();
        String codcom = issuanceFeeMovement.getCodcom();
        Integer codact = issuanceFeeMovement.getCodact();
        CodigoMoneda clamondiv = CodigoMoneda.NONE;
        String nomcomred = prepaidTopup.getMerchantName();
        String numreffac = issuanceFeeMovement.getId().toString(); //Se hace internamente en Tecnocom.
        String numaut = TecnocomServiceHelper.getNumautFromIdMov( issuanceFeeMovement.getId().toString());
        log.info(String.format("LLamando cobro emision %s", prepaidCard.getProcessorUserId()));
        InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(contrato,
          pan, clamon, indnorcor, tipofac, numreffac, impfac, numaut, codcom, nomcomred, codact, clamondiv,impfac);

        log.info("Respuesta inclusion");
        log.info(inclusionMovimientosDTO.getRetorno());
        log.info(inclusionMovimientosDTO.getDescRetorno());

        if (inclusionMovimientosDTO.isRetornoExitoso()) {

          String centalta = inclusionMovimientosDTO.getCenalta();
          String cuenta = inclusionMovimientosDTO.getCuenta();
          Integer numextcta = inclusionMovimientosDTO.getNumextcta();
          Integer nummovext = inclusionMovimientosDTO.getNummovext();
          Integer clamone = inclusionMovimientosDTO.getClamone();
          PrepaidMovementStatus  status = PrepaidMovementStatus.PROCESS_OK;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            prepaidCard.getPan(),
            centalta,
            cuenta,
            numextcta,
            nummovext,
            clamone,
            null,
            status);

          issuanceFeeMovement.setPan(prepaidCard.getPan());
          issuanceFeeMovement.setCentalta(centalta);
          issuanceFeeMovement.setCuenta(cuenta);
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
          return redirectRequest(endpoint, exchange, req, false);

        } else if (CodigoRetorno._1000.equals(inclusionMovimientosDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
          req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
          req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }
        else if (CodigoRetorno._1010.equals(inclusionMovimientosDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
          return redirectRequest(endpoint, exchange, req, true);
        } else if (CodigoRetorno._1020.equals(inclusionMovimientosDTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
          return redirectRequest(endpoint, exchange, req, true);
        }
        else {

          Integer numextcta = 0;
          Integer nummovext = 0;
          Integer clamone = 0;
          PrepaidMovementStatus  status = PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            prepaidCard.getPan(),
            null,
            null,
            numextcta,
            nummovext,
            clamone,
            null,
            status);

          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);

          Endpoint endpoint = createJMSEndpoint(ERROR_CARD_ISSUANCE_FEE_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }
      }
    };
  }

  /* Cola Errores */
  public ProcessorRoute processErrorPendingIssuanceFee() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
      log.info("processErrorPendingIssuanceFee - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupData10 data = req.getData();
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {
          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
          template = TemplateUtils.freshDeskTemplateColas2(template,"Error al cobrar comisión Apertura",String.format("%s %s",data.getUser().getName(),data.getUser().getLastname_1()),String.format("%s-%s",data.getUser().getRut().getValue(),data
            .getUser().getRut().getDv()),data.getUser().getId());

          NewTicket newTicket = new NewTicket();
          newTicket.setDescription(template);
          newTicket.setGroupId(GroupId.OPERACIONES);
          newTicket.setUniqueExternalId(String.valueOf(data.getUser().getRut().getValue()));
          newTicket.setType(TicketType.COLAS_NEGATIVAS);
          newTicket.setStatus(StatusType.OPEN);
          newTicket.setPriority(PriorityType.URGENT);
          newTicket.setSubject("Error al cobrar comisión Apertura");
          // Ticket Custom Fields:
          newTicket.addCustomField(CustomFieldsName.ID_COLA,data.getPrepaidTopup10().getMessageId());
          newTicket.addCustomField(CustomFieldsName.NOMBRE_COLA, QueuesNameType.ISSUANCE_FEE.getValue());
          newTicket.addCustomField(CustomFieldsName.REINTENTOS, req.getReprocesQueue());


          Ticket ticket = getRoute().getUserClient().createFreshdeskTicket(null,data.getUser().getId(),newTicket);
          if(ticket.getId() != null){
            log.info("Ticket Creado Exitosamente");
          }
        } else {
          Map<String, Object> templateData = new HashMap<String, Object>();
          templateData.put("idUsuario", data.getUser().getId().toString());
          templateData.put("rutCliente", data.getUser().getRut().getValue().toString() + "-" + data.getUser().getRut().getDv());
          getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_ISSUANCE_FEE, templateData);
        }
      return req;
      }
    };
  }
}
