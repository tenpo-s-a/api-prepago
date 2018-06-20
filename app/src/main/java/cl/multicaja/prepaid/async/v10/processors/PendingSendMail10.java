package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.EmailParams;
import cl.multicaja.prepaid.model.v10.MimeType;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.dto.Cvv2DTO;
import cl.multicaja.users.model.v10.EmailBody;
import cl.multicaja.users.model.v10.MailTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;

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

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        if(req.getRetryCount() > 3) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return req;
        }

        Cvv2DTO cvv2DTO = getRoute().getTecnocomService().consultaCvv2(data.getPrepaidCard10().getProcessorUserId(),
                                      getRoute().getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()));

        if (cvv2DTO.isRetornoExitoso()) {

          MailTemplate mailTemplate = getRoute().getMailEJBBean10().getMailTemplateByAppAndName(null, getRoute().getConfigUtils().getProperty("prepaid.appname"), "card_pdf");
          EmailParams emailParams = getRoute().getParametersUtil().getObject(getRoute().getConfigUtils().getProperty("prepaid.appname"),"pdf_card","v10",EmailParams.class);

          if (mailTemplate == null || emailParams == null) {
            Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
            return req;
          }

          try {

            Map<String, String> mailData = new HashMap<>();
            mailData.put("${numtar}", getRoute().getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()));
            mailData.put("${venc}", String.valueOf(data.getPrepaidCard10().getExpiration()));
            mailData.put("${cvc}", String.valueOf(cvv2DTO.getClavegen()));

            String template = replaceDataHTML(mailTemplate.getTemplate(), mailData);

            //TODO el passwordOwner quizas debe externalizarse
            String pdfB64 = getRoute().getPdfUtils().protectedPdfInB64(template, data.getUser().getRut().getValue().toString(), "MULTICAJA-PREPAGO", "Multicaja Prepago", "Tarjeta Cliente", "Multicaja");

            EmailBody emailBody = new EmailBody();
            emailBody.setTemplateData("{ 'cliente' : '"+data.getUser().getName()+" "+data.getUser().getLastname_1()+"' }");

            emailBody.setTemplate(emailParams.getTemplateData());
            emailBody.setAddress(data.getUser().getEmail().getValue());
            emailBody.setFrom(emailParams.getMailFrom());
            emailBody.setSubject(emailParams.getMailSubject());
            emailBody.addAttached(pdfB64,MimeType.PDF.getValue(),"Tarjeta_" + Utils.uniqueCurrentTimeNano() + ".pdf");

            getRoute().getMailEJBBean10().sendMailAsync(null, null, emailBody);

          } catch(Exception ex) {
            Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            redirectRequest(endpoint, exchange, req);
          }

        } else if (cvv2DTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(PENDING_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          redirectRequest(endpoint, exchange, req);
        } else {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
        }

        return req;
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

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        //TODO falta implementar

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

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        if(req.getRetryCount() > 3) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return req;
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

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        //TODO falta implementar

        return req;
      }
    };
  }

}
