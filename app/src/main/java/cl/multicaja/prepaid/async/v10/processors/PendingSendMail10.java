package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

import static cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10.*;

/**
 * @autor abarazarte
 */
public class PendingSendMail10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingSendMail10.class);

  public PendingSendMail10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  /**
   *
   * @return
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
        //TODO: Se debe obtener cvc de tecnocom.
        if (true) {
          InputStream is = PendingSendMail10.class.getResourceAsStream("/pdf_template/card.html");
          String theString = IOUtils.toString(is, "UTF-8");
          String template = replaceDataHTML(theString,"8765272893876532","06/23","987");
          String pdfB64 = getPdfUtils().protectedPdfInB64(template,""+data.getUser().getRut(),"MULTICAJA-PREPAGO","Multicaja Prepago","Cliente","Multicaja");
          System.out.println(pdfB64);
          //TODO: se debe llamar al servicio de envio de mail de Users
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
  private String replaceDataHTML(String htmlTemplate,String numtar, String fechavenc, String cvc) {
   return htmlTemplate.replace("${numtar}",numtar).replace("${venc}",fechavenc).replace("${cvc}",cvc);
  }
  /* Cola Errores */
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
}
