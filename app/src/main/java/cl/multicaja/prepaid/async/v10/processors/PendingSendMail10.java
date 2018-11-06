package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.model.v10.MimeType;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.prepaid.model.v10.QueuesNameType;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.dto.Cvv2DTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.*;

/**
 * @autor abarazarte
 */
public class PendingSendMail10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingSendMail10.class);

  public PendingSendMail10(BaseRoute10 route) {
    super(route);
  }

  private String replaceDataHTML(String template, Map<String, String> data) {
    for (Map.Entry<String, String> entry : data.entrySet())
    {
      template = template.replace(entry.getKey(), entry.getValue());
    }
    return template;
  }

  /**
   * ENVIO DE TARJETA
   */
  public ProcessorRoute processPendingSendMailCard() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingSendMailCard - REQ: " + req);
        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        Cvv2DTO cvv2DTO = getRoute().getTecnocomService().consultaCvv2(
                                                    data.getPrepaidCard10().getProcessorUserId(),
                                                  getRoute().getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()));

        if (cvv2DTO.isRetornoExitoso()) {
          try {
            String mailTemplate = getRoute().getParametersUtil().getString("api-prepaid", "card_pdf_template", "v1.0");
            Map<String, String> mailData = new HashMap<>();
            mailData.put("${numtar}", getRoute().getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()));
            mailData.put("${venc}", String.valueOf(data.getPrepaidCard10().getFormattedExpiration()));
            mailData.put("${cvc}", StringUtils.leftPad(String.valueOf(cvv2DTO.getClavegen()),3,"0"));
            log.debug(mailTemplate);
            String template = replaceDataHTML(new String(Base64.decodeBase64(mailTemplate.getBytes())), mailData);
            log.debug(template);
            String pdfB64 = getRoute().getPdfUtils().protectedPdfInB64(template, data.getUser().getRut().getValue().toString(), RandomStringUtils.random(15), "Multicaja Prepago", "Tarjeta Cliente", "Multicaja");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("client", data.getUser().getName() + " " + data.getUser().getLastname_1());
            if(data.getIssuanceFeeMovement10() != null){
              templateData.put("amount", getRoute().getNumberUtils().toClp(data.getPrepaidTopup10().getTotal().getValue().subtract(data.getIssuanceFeeMovement10().getImpfac())));
            } else {
              templateData.put("amount", getRoute().getNumberUtils().toClp(data.getPrepaidTopup10().getTotal().getValue()));
            }


            EmailBody emailBody = new EmailBody();
            emailBody.setTemplateData(templateData);

            emailBody.setTemplate(TEMPLATE_MAIL_CARD);
            emailBody.setAddress(data.getUser().getEmail().getValue());
            emailBody.addAttached(pdfB64,MimeType.PDF.getValue(),"Tarjeta_" + Utils.uniqueCurrentTimeNano() + ".pdf");

            getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

            return req;

          } catch(Exception ex) {
            log.error("Error al enviar email cvv", ex);
            Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
            return redirectRequest(endpoint, exchange, req, false);
          }

        } else if (CodigoRetorno._1000.equals(cvv2DTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_SEND_MAIL_CARD_REQ);
          req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
          req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }
        else if (CodigoRetorno._1010.equals(cvv2DTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_SEND_MAIL_CARD_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
          return redirectRequest(endpoint, exchange, req, true);
        } else if (CodigoRetorno._1020.equals(cvv2DTO.getRetorno())) {
          Endpoint endpoint = createJMSEndpoint(PENDING_SEND_MAIL_CARD_REQ);
          req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
          req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
          return redirectRequest(endpoint, exchange, req, true);
        }
        else {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }
      }
    };
  }

  /* Cola Errores Envio de tarjeta */
  public ProcessorRoute processErrorPendingSendMailCard() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("processErrorPendingSendMailCard - REQ: " + req);
        req.retryCountNext();
        PrepaidTopupData10 data = req.getData();
        if(Errors.TECNOCOM_TIME_OUT_RESPONSE.equals(data.getNumError()) ||
          Errors.TECNOCOM_TIME_OUT_CONEXION.equals(data.getNumError()) ||
          Errors.TECNOCOM_ERROR_REINTENTABLE.equals(data.getNumError()) &&
            data.getPrepaidTopup10() != null
        ) {
          String template = getRoute().getParametersUtil().getString("api-prepaid","template_ticket_cola_2","v1.0");
          template = TemplateUtils.freshDeskTemplateColas2(template,"Error al enviar tarjeta(CVV)",String.format("%s %s",data.getUser().getName(),data.getUser().getLastname_1()),String.format("%s-%s",data.getUser().getRut().getValue(),data
            .getUser().getRut().getDv()),data.getUser().getId());

          NewTicket newTicket = createTicket("Error al enviar tarjeta(CVV)",
            template,
            String.valueOf(data.getUser().getRut().getValue()),
            data.getPrepaidTopup10().getMessageId(),
            QueuesNameType.SEND_MAIL,
            req.getReprocesQueue());

          Ticket ticket = getRoute().getUserClient().createFreshdeskTicket(null,data.getUser().getId(),newTicket);
          if(ticket.getId() != null){
            log.info("Ticket Creado Exitosamente");
          }
        } else {
          /**
           *  ENVIO DE MAIL ERROR ENVIO DE TARJETA
           */
          Map<String, Object> templateData = new HashMap<String, Object>();
          templateData.put("idUsuario", data.getUser().getId().toString());
          templateData.put("rutCliente", data.getUser().getRut().getValue().toString() + "-" + data.getUser().getRut().getDv());
          getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_CARD_ERROR, templateData);
        }

        return req;
      }
    };
  }

  /**
   * Envio recibo retiro
   */
  public ProcessorRoute processPendingWithdrawMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingWithdrawMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        PrepaidWithdraw10 withdraw = data.getPrepaidWithdraw10();

        Map<String, String> mailData = new HashMap<>();

        mailData.put("${amount}", String.valueOf(withdraw.getAmount().getValue()));
        mailData.put("${fee}", String.valueOf(withdraw.getFee().getValue()));

        String template = replaceDataHTML("", mailData);
        //TODO: se debe llamar al servicio de envio de mail de Users

        return req;
      }
    };
  }

  public ProcessorRoute processErrorPendingWithdrawMail() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
      log.info("processErrorPendingWithdrawMail - REQ: " + req);
      req.retryCountNext();
      PrepaidTopupData10 data = req.getData();
      log.debug(data.getPrepaidTopup10());

      return req;
      }
    };
  }

}
