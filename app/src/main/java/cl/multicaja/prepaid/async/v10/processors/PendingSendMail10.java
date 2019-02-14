package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.async.v10.routes.MailRoute10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.NewTicket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.Ticket;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.dto.Cvv2DTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.*;
import static cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10.*;
import static cl.multicaja.prepaid.model.v10.MailTemplates.*;
import static cl.multicaja.prepaid.model.v10.NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE;

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
            String pdfB64 = createPdf(data.getUser().getRut().getValue().toString(),RandomStringUtils.random(20),
              getRoute().getEncryptUtil().decrypt(data.getPrepaidCard10().getEncryptedPan()),
              String.valueOf(data.getPrepaidCard10().getFormattedExpiration()),
              StringUtils.leftPad(String.valueOf(cvv2DTO.getClavegen()),3,"0"),
              String.format("%s %s",data.getUser().getName(),data.getUser().getLastname_1()));

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("cliente", data.getUser().getName());

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
          Endpoint endpoint = createJMSEndpoint(MailRoute10.ERROR_SEND_MAIL_WITHDRAW_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", data.getUser().getName().toUpperCase() + " " + data.getUser().getLastname_1().toUpperCase());
        templateData.put("user_rut", RutUtils.getInstance().format(data.getUser().getRut().getValue(), data.getUser().getRut().getDv()));
        templateData.put("transaction_type_gloss", WEB_MERCHANT_CODE.equals(data.getPrepaidWithdraw10().getMerchantCode()) ? "Retiro por transferencia" : "Retiro en comercio");
        templateData.put("transaction_amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidWithdraw10().getAmount().getValue())));
        templateData.put("transaction_total_paid", NumberUtils.getInstance().toClp(data.getPrepaidWithdraw10().getTotal().getValue()));
        templateData.put("transaction_date", DateUtils.getInstance().dateToStringFormat(prepaidMovement.getFecfac(), "dd/MM/yyyy"));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

        return req;
      }
    };
  }

  /**
   * Envio recibo carga
   */
  public ProcessorRoute processPendingTopupMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopupMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_TOPUP_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();
        PrepaidTopup10 topup = data.getPrepaidTopup10();

        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", data.getUser().getName().toUpperCase() + " " + data.getUser().getLastname_1().toUpperCase());
        templateData.put("user_rut", RutUtils.getInstance().format(data.getUser().getRut().getValue(), data.getUser().getRut().getDv()));
        templateData.put("transaction_amount", String.valueOf(NumberUtils.getInstance().toClp(topup.getTotal().getValue())));
        templateData.put("transaction_total_paid", NumberUtils.getInstance().toClp(topup.getAmount().getValue()));
        templateData.put("transaction_fee", NumberUtils.getInstance().toClp(topup.getFee().getValue()));
        templateData.put("description", topup.getMerchantCode().equals(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE) ? "Carga web" : "Carga en comercio");

        LocalDateTime topupDateTime = prepaidMovement.getFechaCreacion().toLocalDateTime();

        ZonedDateTime local = ZonedDateTime.ofInstant(topupDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("America/Santiago"));

        templateData.put("transaction_date", local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_TOPUP);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

        return req;
      }
    };
  }

  /**
   * Envio confirmacion de retiro exitoso
   */
  public ProcessorRoute processPendingWithdrawSuccessMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingWithdrawConfirmedMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_SUCCESS_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", data.getUser().getName().toUpperCase() + " " + data.getUser().getLastname_1().toUpperCase());
        templateData.put("withdraw_amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidMovement10().getMonto())));
        templateData.put("bank_account", data.getUserAccount().getCensoredAccount());
        templateData.put("bank_name", data.getUserAccount().getBankName());

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW_SUCCESS);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

        return req;
      }
    };
  }

  /**
   * Envio confirmacion de retiro rechazado
   */
  public ProcessorRoute processPendingWithdrawFailedMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingWithdrawConfirmedMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          Endpoint endpoint = createJMSEndpoint(ERROR_SEND_MAIL_WITHDRAW_FAILED_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", data.getUser().getName().toUpperCase() + " " + data.getUser().getLastname_1().toUpperCase());
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(prepaidMovement.getMonto())));

        LocalDateTime topupDateTime = prepaidMovement.getFechaCreacion().toLocalDateTime();

        ZonedDateTime local = ZonedDateTime.ofInstant(topupDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("America/Santiago"));

        templateData.put("date", local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        templateData.put("time", local.format(DateTimeFormatter.ofPattern("HH:mm")));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW_FAILED);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

        return req;
      }
    };
  }

  /**
   * Envio confirmacion de devolucion
   */
  public ProcessorRoute processPendingTopupRefundConfirmationMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopupRefundConfirmationMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", data.getUser().getName());
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidMovement10().getMonto())));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW_FAILED);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

        return req;
      }
    };
  }

  public String createPdf(String passwordUser, String passwordOwner,String numTarjeta,String fecha, String cvc,String name) throws IOException, DocumentException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document();
    PdfWriter writer =  PdfWriter.getInstance(document, baos);
    writer.setEncryption(passwordUser.getBytes(), passwordOwner.getBytes(), PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
    writer.createXmpMetadata();
    document.addCreationDate();

    document.open();
    PdfContentByte cb = writer.getDirectContentUnder();
    Image img = Image.getInstance("https://mcprepaid.blob.core.windows.net/tarjetaprepago/tarjeta.png");
    img.scaleToFit(500, 600);
    img.setAlignment(Image.MIDDLE);
    document.add(getWatermarkedImage(cb, img, numTarjeta,fecha,cvc,name));
    document.close();
    return Base64Utils.encodeToString(baos.toByteArray());
  }

  public Image getWatermarkedImage(PdfContentByte cb, Image img, String numTarjeta,String fecha, String cvc,String name) throws DocumentException, IOException {
    float width = img.getScaledWidth();
    float height = img.getScaledHeight();
    PdfTemplate template = cb.createTemplate(width, height);
    template.addImage(img, width, 0, 0, height, 0, 0);
    Font font = FontFactory.getFont("/resources/font/tarjeta/TitilliumWeb-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 30, Font.BOLD, BaseColor.WHITE);
    ColumnText.showTextAligned(template, Element.ALIGN_CENTER, new Phrase(numTarjeta, font), 220, 150, 0);
    ColumnText.showTextAligned(template, Element.ALIGN_LEFT, new Phrase(fecha, font), 80, 95, 0);
    ColumnText.showTextAligned(template, Element.ALIGN_CENTER, new Phrase(cvc, font), 295, 95, 0);
    Font fontName = FontFactory.getFont("/resources/font/tarjeta/TitilliumWeb-SemiBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 28, Font.NORMAL, BaseColor.WHITE);
    ColumnText.showTextAligned(template, Element.ALIGN_LEFT, new Phrase(name, fontName), 70, 50, 0);
    return Image.getInstance(template);
  }


  public ProcessorRoute processErrorPendingWithdrawMail() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("Error sending withdraw mail");
      return req;
      }
    };
  }

  public ProcessorRoute processErrorPendingWithdrawSuccessMail() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("Error sending withdraw success mail");
        return req;
      }
    };
  }

  public ProcessorRoute processErrorPendingWithdrawFailedMail() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("Error sending withdraw failed mail");
        return req;
      }
    };
  }

  public ProcessorRoute processErrorPendingTopupMail() {
    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {
        log.info("Error sending topup mail");
        return req;
      }
    };
  }

}
