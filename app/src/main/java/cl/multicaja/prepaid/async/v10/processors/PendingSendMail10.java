package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.dto.Cvv2DTO;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10.ERROR_SEND_MAIL_CARD_REQ;
import static cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10.ERROR_SEND_MAIL_WITHDRAW_REQ;

/**
 * @autor abarazarte
 */
public class PendingSendMail10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingSendMail10.class);

  public PendingSendMail10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  private String replaceDataHTML(String htmlTemplate,String numtar, String fechavenc, String cvc) {
    return htmlTemplate.replace("${numtar}",numtar).replace("${venc}",fechavenc).replace("${cvc}",cvc);
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

    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingSendMailCard - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));


        if(req.getRetryCount() > 3) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }
        Cvv2DTO cvv2DTO = getTecnocomService().consultaCvv2(data.getPrepaidCard10().getProcessorUserId(),getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()));

        if (cvv2DTO.getRetorno().equals(CodigoRetorno._000)) {

          //TODO: Hay que verificar que funcione el obtener plantilla
          /*MailTemplate mailTemplate = getMailEjbBean10().getMailTemplateByAppAndName(null, "PREPAID", "PDF_CARD");
          if (mailTemplate == null) {
            Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
            data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
            req.setRetryCount(0);
            redirectRequest(endpoint, exchange, req);
          }
          */

          //String template = replaceDataHTML(mailTemplate.getTemplate(), getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()), "" + data.getPrepaidCard10().getExpiration(), ""+cvv2DTO.getClavegen());
          //String pdfB64 = getPdfUtils().protectedPdfInB64(template, "" + data.getUser().getRut(), "MULTICAJA-PREPAGO", "Multicaja Prepago", "Cliente", "Multicaja");
          //TODO: se debe llamar al servicio de envio de mail de Users

        } else if (cvv2DTO.getRetorno().equals(CodigoRetorno._1000)) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          redirectRequest(endpoint, exchange, req);
        } else {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_CARD_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
        }

        return new ResponseRoute<>(data);
      }
    };
  }

  /* Cola Errores Envio de tarjeta */
  public ProcessorRoute processErrorPendingSendMailCard() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processError - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        return new ResponseRoute<>(data);
      }
    };
  }


  /**
   * Envio recibo retiro
   */
  public ProcessorRoute processPendingWithdrawMail() {

    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("pendingSendMailWithdraw - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));


        if(req.getRetryCount() > 3) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_REQ);
          data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), endpoint.getEndpointUri(), true));
          req.setRetryCount(0);
          redirectRequest(endpoint, exchange, req);
          return new ResponseRoute<>(data);
        }

        PrepaidWithdraw10 withdraw = data.getPrepaidWithdraw10();

        Map<String, String> mailData = new HashMap<>();

        mailData.put("${amount}", withdraw.getAmount().getValue().toString());
        mailData.put("${fee}", withdraw.getFee().getValue().toString());

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

        return new ResponseRoute<>(data);
      }
    };
  }

  public ProcessorRoute processErrorPendingWithdrawMail() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("errorSendMailWithdraw - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupDataRoute10 data = req.getData();

        data.getProcessorMetadata().add(new ProcessorMetadata(req.getRetryCount(), exchange.getFromEndpoint().getEndpointUri()));

        return new ResponseRoute<>(data);
      }
    };
  }

}
