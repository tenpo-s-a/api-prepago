package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.async.v10.routes.MailRoute10;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.ERROR_SEND_MAIL_WITHDRAW_FAILED_REQ;
import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.ERROR_SEND_MAIL_WITHDRAW_SUCCESS_REQ;
import static cl.multicaja.prepaid.model.v10.NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE;

/**
 * @autor abarazarte
 */
//TODO: Eliminar, ya no se enviará mail al usuario, verificar en vez de enviar mails se creen los eventos necesarios.
public class PendingSendMail10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingSendMail10.class);

  public PendingSendMail10(BaseRoute10 route) {
    super(route);
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

        /*
        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", StringUtils.capitalize(data.getUser().getName()));
        templateData.put("user_rut", RutUtils.getInstance().format(data.getUser().getRut().getValue(), data.getUser().getRut().getDv()));
        templateData.put("transaction_type_gloss", WEB_MERCHANT_CODE.equals(data.getPrepaidWithdraw10().getMerchantCode()) ? "Retiro por transferencia" : "Retiro en comercio");
        templateData.put("transaction_amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidWithdraw10().getAmount().getValue())));
        templateData.put("transaction_total_paid", NumberUtils.getInstance().toClp(data.getPrepaidWithdraw10().getTotal().getValue()));
        templateData.put("transaction_fee", NumberUtils.getInstance().toClp(data.getPrepaidWithdraw10().getFee().getValue()));

        LocalDateTime topupDateTime = prepaidMovement.getFechaCreacion().toLocalDateTime();
        ZonedDateTime local = ZonedDateTime.ofInstant(topupDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("America/Santiago"));
        templateData.put("transaction_date", local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

         */
        return req;
      }
    };
  }

  /**
   * Envio solcitud retiro TEF
   */
  public ProcessorRoute processPendingWebWithdrawRequestMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingWebWithdrawRequestMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        if(req.getRetryCount() > getMaxRetryCount()) {
          Endpoint endpoint = createJMSEndpoint(MailRoute10.ERROR_SEND_MAIL_WITHDRAW_REQ);
          return redirectRequest(endpoint, exchange, req, false);
        }

        /*
        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", data.getUser().getName());
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidWithdraw10().getTotal().getValue())));
        templateData.put("bank_name", data.getUserAccount().getBankName());
        templateData.put("account_number", data.getUserAccount().getCensoredAccount());

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WEB_WITHDRAW_REQUEST);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);
        */
        return req;
      }
    };
  }

  /**
   * Envio confirmacion de retiro tef exitoso
   */
  public ProcessorRoute processPendingWebWithdrawSuccessMail() {

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

        PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10();
        withdraw10.setAmount(new NewAmountAndCurrency10(data.getPrepaidMovement10().getMonto()));
        withdraw10.setMerchantCode(WEB_MERCHANT_CODE);
        getRoute().getPrepaidEJBBean10().calculateFeeAndTotal(withdraw10);

        /*
        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", StringUtils.capitalize(data.getUser().getName()));
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(withdraw10.getTotal())));
        templateData.put("bank_account", data.getUserAccount().getCensoredAccount());
        templateData.put("bank_name", data.getUserAccount().getBankName());

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW_SUCCESS);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);

         */
        return req;
      }
    };
  }

  /**
   * Envio confirmacion de retiro tef rechazado
   */
  public ProcessorRoute processPendingWebWithdrawFailedMail() {

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

        PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10();
        withdraw10.setAmount(new NewAmountAndCurrency10(prepaidMovement.getMonto()));
        withdraw10.setMerchantCode(WEB_MERCHANT_CODE);
        getRoute().getPrepaidEJBBean10().calculateFeeAndTotal(withdraw10);


        /*
        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", StringUtils.capitalize(data.getUser().getName()));
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(withdraw10.getTotal())));

        LocalDateTime topupDateTime = prepaidMovement.getFechaCreacion().toLocalDateTime();

        ZonedDateTime local = ZonedDateTime.ofInstant(topupDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("America/Santiago"));

        templateData.put("date", local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        templateData.put("time", local.format(DateTimeFormatter.ofPattern("HH:mm")));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_WITHDRAW_FAILED);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);


         */
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

        /*
        Map<String, Object> templateData = new HashMap<>();

        templateData.put("user_name", StringUtils.capitalize(data.getUser().getName()));
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidMovement10().getMonto())));

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_TOPUP_REFUND_COMPLETE);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);


         */
        return req;
      }
    };
  }

  /**
   * Envio comprobante de compra Ok
   */
  public ProcessorRoute processPendingPurchaseSuccessMail() {

    return new ProcessorRoute<ExchangeData<PrepaidTopupData10>, ExchangeData<PrepaidTopupData10>>() {
      @Override
      public ExchangeData<PrepaidTopupData10> processExchange(long idTrx, ExchangeData<PrepaidTopupData10> req, Exchange exchange) throws Exception {

        log.info("processPendingPurchaseSuccessMail - REQ: " + req);

        req.retryCountNext();

        PrepaidTopupData10 data = req.getData();

        Map<String, Object> templateData = new HashMap<>();

        /*
        templateData.put("user_name", StringUtils.capitalize(data.getUser().getName()));
        templateData.put("amount", String.valueOf(NumberUtils.getInstance().toClp(data.getPrepaidMovement10().getMonto())));
        templateData.put("merchant_name", data.getPrepaidMovement10().getCodcom());

        EmailBody emailBody = new EmailBody();
        emailBody.setTemplateData(templateData);
        emailBody.setTemplate(TEMPLATE_MAIL_PURCHASE_SUCCESS);
        emailBody.setAddress(data.getUser().getEmail().getValue());
        getRoute().getUserClient().sendMail(null, data.getUser().getId(), emailBody);


         */
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

    //document.open();
    //PdfContentByte cb = writer.getDirectContentUnder();
    //Image img = Image.getInstance("https://mcprepaid.blob.core.windows.net/tarjetaprepago/tarjeta.png");
    //img.scaleToFit(500, 600);
    //img.setAlignment(Image.MIDDLE);
    //document.add(getWatermarkedImage(cb, img, numTarjeta,fecha,cvc,name));
    //document.close();
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
