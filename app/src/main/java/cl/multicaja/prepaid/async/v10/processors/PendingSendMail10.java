package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.model.v10.MimeType;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.dto.Cvv2DTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            // TODO hay que cambiarlo por Parametros de prepago
            //MailTemplate mailTemplate = getRoute().getMailEJBBean10().getMailTemplateByAppAndName(null, APPNAME, TEMPLATE_PDF_CARD);
            Map<String, String> mailData = new HashMap<>();
            mailData.put("${numtar}", getRoute().getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()));
            mailData.put("${venc}", String.valueOf(data.getPrepaidCard10().getFormattedExpiration()));
            mailData.put("${cvc}", String.valueOf(cvv2DTO.getClavegen()));

            //String template = replaceDataHTML(mailTemplate.getTemplate(), mailData);

            //TODO el passwordOwner quizas debe externalizarse
            //TODO revisar licencia de itext
           // String pdfB64 = getRoute().getPdfUtils().protectedPdfInB64(template, data.getUser().getRut().getValue().toString(), "MULTICAJA-PREPAGO", "Multicaja Prepago", "Tarjeta Cliente", "Multicaja");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("client", data.getUser().getName() + " " + data.getUser().getLastname_1());
            if(data.getIssuanceFeeMovement10() != null){
              templateData.put("amount", NumberUtils.getInstance().toClp(data.getPrepaidTopup10().getTotal().getValue().subtract(data.getIssuanceFeeMovement10().getImpfac())));
            } else {
              templateData.put("amount", NumberUtils.getInstance().toClp(data.getPrepaidTopup10().getTotal().getValue()));
            }


            EmailBody emailBody = new EmailBody();
            emailBody.setTemplateData(templateData);

            emailBody.setTemplate(TEMPLATE_MAIL_CARD);
            emailBody.setAddress(data.getUser().getEmail().getValue());
            //emailBody.addAttached(pdfB64,MimeType.PDF.getValue(),"Tarjeta_" + Utils.uniqueCurrentTimeNano() + ".pdf");

            getRoute().getUserClient().sendMail(null, null, emailBody);

            return req;

          } catch(Exception ex) {
            log.error("Error al enviar email cvv", ex);
            Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
            return redirectRequest(endpoint, exchange, req, false);
          }

        } else if (cvv2DTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_SEND_MAIL_CARD_REQ);
          return redirectRequest(endpoint, exchange, req, true);
        } else {
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
        /**
         *  ENVIO DE MAIL ERROR ENVIO DE TARJETA
         */
        Map<String, Object> templateData = new HashMap<String, Object>();
        templateData.put("idUsuario", data.getUser().getId().toString());
        templateData.put("rutCliente", data.getUser().getRut().getValue().toString() + "-" + data.getUser().getRut().getDv());
        getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_CARD_ERROR, templateData);

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

        //TODO: Hay que verificar que funcione el obtener plantilla
        /*MailTemplate mailTemplate = getMailEjbBean10().getMailTemplateByAppAndName(null, "PREPAID", "WITHDRAW");
        if (mailTemplate == null) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
        }
        */

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
      //TODO falta implementar, no se sabe que hacer en este caso
      return req;
      }
    };
  }

}
