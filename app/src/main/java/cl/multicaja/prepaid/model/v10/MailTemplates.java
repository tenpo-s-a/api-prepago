package cl.multicaja.prepaid.model.v10;

public class MailTemplates {

  public static final String APPNAME = "Prepago";

  public static final String TEMPLATE_MAIL_SEND_CARD = "Prepago/EnvioTarjeta"; //Solo para redireccionar al proceso asicnrono de envio de tarjeta
  public static final String TEMPLATE_MAIL_CARD = "Prepago/mail_card";//Correo que contiene la tarjeta.
  public static final String TEMPLATE_MAIL_CARD_ERROR = "Prepago/ErrorEnvioTarjeta";
  public static final String TEMPLATE_MAIL_EMISSION_ERROR = "Prepago/ErrorAltaRapida";

  public static final String TEMPLATE_PDF_CARD = "card_pdf";//Template para el PDF
  public static final String TEMPLATE_MAIL_TOPUP = String.format("%s/RecepcionCarga", APPNAME);

}
