package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.ERROR_CARD_ISSUANCE_FEE_REQ;
import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.PENDING_CARD_ISSUANCE_FEE_REQ;
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
        PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();
        PrepaidCard10 prepaidCard = data.getPrepaidCard10();
        PrepaidUser10 user = data.getPrepaidUser10();
        Account account = data.getAccount();

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

        if (user == null) {
          log.error("Error req.getData().getUser() es null");
          return null;
        }

        if (user.getId() == null || user.getId() == 0L) {
          log.error("Error req.getData().getUser().getId() es null");
          return null;
        }

        if (account == null) {
          log.error("Error req.getData().getAccount() es null");
          return null;
        }
        if (StringUtils.isAllBlank(account.getUuid())) {
          log.error("Error req.getData().getAccount().getUuid() es null o empty");
          return null;
        }

        req.retryCountNext();

        PrepaidMovement10 issuanceFeeMovement = data.getIssuanceFeeMovement10();

        BigDecimal amount = getCalculationsHelper().getCalculatorParameter10().getOPENING_FEE().intValue() > 1 ? getCalculationsHelper().getCalculatorParameter10().getOPENING_FEE() : BigDecimal.ONE;

        if (issuanceFeeMovement == null) {
          issuanceFeeMovement = (PrepaidMovement10) prepaidMovement.clone();
          issuanceFeeMovement.setTipoMovimiento(PrepaidMovementType.ISSUANCE_FEE);
          issuanceFeeMovement.setTipofac(TipoFactura.COMISION_APERTURA);
          issuanceFeeMovement.setId(null);
          issuanceFeeMovement.setEstado(PrepaidMovementStatus.PENDING);
          issuanceFeeMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
          issuanceFeeMovement.setImpfac(amount);
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

        //String contrato = prepaidCard.getProcessorUserId();
        String pan = getRoute().getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());

        //TODO: para el cobro de emision se toma el mismo merchant name de la carga? o se debe colocar el de prepago?
        String nomcomred = prepaidTopup.getMerchantName();
        log.info(String.format("Account [%s]",account.getAccountNumber()));
        InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomServiceHelper().issuanceFee(account.getAccountNumber(), pan, nomcomred, issuanceFeeMovement);

        log.info(String.format("Respuesta alta de issuanceFee [%s] [%s]",inclusionMovimientosDTO.getRetorno(),inclusionMovimientosDTO.getDescRetorno()));

        if (inclusionMovimientosDTO.isRetornoExitoso()) {

          String centalta = inclusionMovimientosDTO.getCenalta();
          String cuenta = inclusionMovimientosDTO.getCuenta();
          Integer numextcta = inclusionMovimientosDTO.getNumextcta();
          Integer nummovext = inclusionMovimientosDTO.getNummovext();
          Integer clamone = inclusionMovimientosDTO.getClamone();
          PrepaidMovementStatus  status = PrepaidMovementStatus.PROCESS_OK;
          BusinessStatusType businessStatus = BusinessStatusType.CONFIRMED;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            prepaidCard.getPan(),
            centalta,
            cuenta,
            numextcta,
            nummovext,
            clamone,
            businessStatus,
            status);

          issuanceFeeMovement.setPan(prepaidCard.getPan());
          issuanceFeeMovement.setCentalta(centalta);
          issuanceFeeMovement.setCuenta(cuenta);
          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);
          issuanceFeeMovement.setEstadoNegocio(businessStatus);

          // Activa la tarjeta luego de realizado el cobro de emision
          prepaidCard = getRoute().getPrepaidCardEJBBean11().updatePrepaidCardStatus(prepaidCard.getId(),PrepaidCardStatus.ACTIVE);

          req.getData().setPrepaidCard10(prepaidCard);
          // publica evento de tarjeta creada
          getRoute().getPrepaidCardEJBBean11().publishCardEvent(
            user.getUuid(),
            data.getAccount().getUuid(),
            prepaidCard.getId(),
            KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT
          );
          req.getData().setPrepaidCard10(prepaidCard);

          // Expira cache del saldo de la cuenta
          getRoute().getAccountEJBBean10().expireBalanceCache(account.getId());

          return req;

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
          BusinessStatusType businessStatus = BusinessStatusType.REJECTED;

          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
            issuanceFeeMovement.getId(),
            prepaidCard.getPan(),
            null,
            null,
            numextcta,
            nummovext,
            clamone,
            businessStatus,
            status);

          issuanceFeeMovement.setNumextcta(numextcta);
          issuanceFeeMovement.setNummovext(nummovext);
          issuanceFeeMovement.setClamone(clamone);
          issuanceFeeMovement.setEstado(status);
          issuanceFeeMovement.setEstadoNegocio(businessStatus);

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
      PrepaidUser10 user = data.getPrepaidUser10();
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError())
        ) {

          if(user == null){
            return req;
          }
          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
          template = TemplateUtils.freshDeskTemplateColas2(template,"Error al cobrar comisión Apertura",String.format("%s %s",user.getName(),user.getLastName()),user.getDocumentNumber(),user.getId());

          NewTicket newTicket = new NewTicket();
          newTicket.setDescription(template);
          newTicket.setGroupId(GroupId.OPERACIONES);
          newTicket.setUniqueExternalId(user.getDocumentNumber());
          newTicket.setType(TicketType.COLAS_NEGATIVAS);
          newTicket.setStatus(StatusType.OPEN);
          newTicket.setPriority(PriorityType.URGENT);
          newTicket.setSubject("Error al cobrar comisión Apertura");
          // Ticket Custom Fields:
          newTicket.addCustomField(CustomFieldsName.ID_COLA,data.getPrepaidTopup10().getMessageId());
          newTicket.addCustomField(CustomFieldsName.NOMBRE_COLA, QueuesNameType.ISSUANCE_FEE.getValue());
          newTicket.addCustomField(CustomFieldsName.REINTENTOS, req.getReprocesQueue());

          //TODO: Se debe verificar si este id seria el uuid de Tempo
          Ticket ticket = null; //getRoute().getUserClient().createFreshdeskTicket(null,user.getId(),newTicket);
          if(ticket.getId() != null){
            log.info("Ticket Creado Exitosamente");
          }
        } else {
          Map<String, Object> templateData = new HashMap<String, Object>();
          templateData.put("idUsuario", user.getId());
          templateData.put("rutCliente", user.getDocumentNumber());
          //TODO: Verificar como enviar mail
          //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_ISSUANCE_FEE, templateData);
        }
      return req;
      }
    };
  }
}
