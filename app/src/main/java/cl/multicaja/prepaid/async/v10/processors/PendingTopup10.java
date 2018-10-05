package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
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
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_ERROR_TOPUP;
import static cl.multicaja.prepaid.model.v10.MailTemplates.TEMPLATE_MAIL_TOPUP;

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
        try {
        log.info("processPendingTopup - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

        if(req.getRetryCount() > getMaxRetryCount()) {
          PrepaidMovementStatus status;
          if (Errors.TECNOCOM_ERROR_REINTENTABLE.equals(req.getData().getNumError())){
            status = PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE;
          } else if(Errors.TECNOCOM_TIME_OUT_CONEXION.equals(req.getData().getNumError())){
            status = PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION;
          } else if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(req.getData().getNumError())){
            status = PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE;
          } else {
            status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
          }
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);
          prepaidMovement.setEstado(status);

          return redirectRequest(createJMSEndpoint(ERROR_TOPUP_REQ), exchange, req, false);
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

        if(user.getIsBlacklisted()) {
          log.error(String.format("Error usuario %s en lista negra", user.getId()));
          PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
          getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, prepaidMovement.getId(), status);
          prepaidMovement.setEstado(status);
          return redirectRequest(createJMSEndpoint(ERROR_TOPUP_REQ), exchange, req, false);
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
        prepaidUser = getRoute().getPrepaidUserEJBBean10().getUserLevel(user, prepaidUser);

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
          String numreffac = prepaidMovement.getId().toString(); // Se hace internamente en Tecnocomç
          String numaut = numreffac;

          //solamente los 6 primeros digitos de numreffac
          if (numaut.length() > 6) {
            numaut = numaut.substring(numaut.length()-6);
          }

          InclusionMovimientosDTO inclusionMovimientosDTO = getRoute().getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
                                                                                                      numreffac, impfac, numaut, codcom,
                                                                                                      nomcomred, codact, clamondiv,impfac);

          if (inclusionMovimientosDTO.isRetornoExitoso()) {

            String centalta = inclusionMovimientosDTO.getCenalta();
            String cuenta = inclusionMovimientosDTO.getCuenta();
            Integer numextcta = inclusionMovimientosDTO.getNumextcta();
            Integer nummovext = inclusionMovimientosDTO.getNummovext();
            Integer clamone = inclusionMovimientosDTO.getClamone();
            PrepaidMovementStatus status = PrepaidMovementStatus.PROCESS_OK; //realizado
            log.info("Prepaid Movement Status: "+status.name() );
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null,
              prepaidMovement.getId(),
              prepaidCard.getPan(),
              centalta,
              cuenta,
              numextcta,
              nummovext,
              clamone,
              status);

            prepaidMovement.setPan(prepaidCard.getPan());
            prepaidMovement.setCentalta(centalta);
            prepaidMovement.setCuenta(cuenta);
            prepaidMovement.setNumextcta(numextcta);
            prepaidMovement.setNummovext(nummovext);
            prepaidMovement.setClamone(clamone);
            prepaidMovement.setEstado(status);
            data.setPrepaidMovement10(prepaidMovement);
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

            if (!cdtTransaction.isNumErrorOk()) {
              log.error(String.format("Error en CDT %s", cdtTransaction.getMsjError()));
            }
            log.info(prepaidTopup);
            //Envio de comprobante de carga por mail
            Map<String, Object> templateData = new HashMap<>();

            templateData.put("user_name", data.getUser().getName().toUpperCase() + " " + data.getUser().getLastname_1().toUpperCase());
            templateData.put("user_rut", RutUtils.getInstance().format(data.getUser().getRut().getValue(), data.getUser().getRut().getDv()));
            templateData.put("transaction_amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidTopup10().getTotal().getValue())));
            templateData.put("transaction_total_paid", NumberUtils.getInstance().toClp(data.getPrepaidTopup10().getAmount().getValue()));
            templateData.put("transaction_date", DateUtils.getInstance().dateToStringFormat(prepaidMovement.getFecfac(), "dd/MM/yyyy"));

            EmailBody emailBody = new EmailBody();
            emailBody.setTemplateData(templateData);
            emailBody.setTemplate(TEMPLATE_MAIL_TOPUP);
            emailBody.setAddress(data.getUser().getEmail().getValue());
            getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

            //segun la historia: https://www.pivotaltracker.com/story/show/158044562
            if (PrepaidCardStatus.PENDING.equals(prepaidCard.getStatus())) {
              Endpoint endpoint = createJMSEndpoint(PENDING_CARD_ISSUANCE_FEE_REQ);
              return redirectRequest(endpoint, exchange, req, false);
            } else {
              req.setData(data);
              return req;
            }

          //TODO: se debe manejar la posibilidad que el movimiento devuelva error por "Operacion realizada previamente" si el intento anterior tuvo error TECNOCOM_TIME_OUT_RESPONSE
          //} else if(CodigoRetorno._200.equals(inclusionMovimientosDTO.getRetorno())){
          } else if (CodigoRetorno._1000.equals(inclusionMovimientosDTO.getRetorno())) {
            req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
            req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
            return redirectRequest(createJMSEndpoint(PENDING_TOPUP_REQ), exchange, req, true);
          }else if (CodigoRetorno._1010.equals(inclusionMovimientosDTO.getRetorno())) {
            req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
            req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
            return redirectRequest(createJMSEndpoint(PENDING_TOPUP_REQ), exchange, req, true);
          } else if (CodigoRetorno._1020.equals(inclusionMovimientosDTO.getRetorno())) {
            req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
            req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
            return redirectRequest(createJMSEndpoint(PENDING_TOPUP_REQ), exchange, req, true);
          }
          else {
            PrepaidMovementStatus status = PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP;
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, data.getPrepaidMovement10().getId(), status);
            data.getPrepaidMovement10().setEstado(status);

            Endpoint endpoint = createJMSEndpoint(ERROR_TOPUP_REQ);
            return redirectRequest(endpoint, exchange, req, false);
          }

        } else {

          //https://www.pivotaltracker.com/story/show/157816408
          //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

          prepaidCard = getRoute().getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
                                                                                      PrepaidCardStatus.LOCKED_HARD,
                                                                                      PrepaidCardStatus.EXPIRED);

          if (prepaidCard == null) {
            Endpoint endpoint = createJMSEndpoint(PENDING_EMISSION_REQ);
            return redirectRequest(endpoint, exchange, req, false);
          } else {
            data.setPrepaidCard10(prepaidCard);
            return req;
          }
        }
        }catch (Exception e){
          e.printStackTrace();
          log.error(String.format("Error desconocido al realizar carga %s",e));
          req.getData().setNumError(Errors.ERROR_INDETERMINADO);
          req.getData().setMsjError(e.getLocalizedMessage());
          return redirectRequest(createJMSEndpoint(ERROR_TOPUP_REQ), exchange, req, true);
        }
      }
    };
  }

  public ProcessorRoute processErrorTopup() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
      log.info("processPendingTopupReturns - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupData10 data = req.getData();

      Map<String, Object> templateData = new HashMap<String, Object>();
      templateData.put("idUsuario", data.getUser().getId().toString());
      templateData.put("rutCliente", data.getUser().getRut().getValue().toString() + "-" + data.getUser().getRut().getDv());
      getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TOPUP, templateData);
      return req;
      }
    };
  }
}
