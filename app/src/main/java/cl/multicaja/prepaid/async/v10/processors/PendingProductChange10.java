package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.CambioProductoDTO;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10.ERROR_PRODUCT_CHANGE_REQ;
import static cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10.PENDING_PRODUCT_CHANGE_REQ;
import static cl.multicaja.prepaid.model.v10.MailTemplates.*;

/**
 * @author abarazarte
 **/
public class PendingProductChange10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingProductChange10.class);

  public PendingProductChange10(BaseRoute10 route) {
    super(route);
  }

  public ProcessorRoute processPendingProductChange() {
    return new ProcessorRoute<ExchangeData<PrepaidProductChangeData10>, ExchangeData<PrepaidProductChangeData10>>() {
      @Override
      public ExchangeData<PrepaidProductChangeData10> processExchange(long idTrx, ExchangeData<PrepaidProductChangeData10> req, Exchange exchange) throws Exception {

        try {

          log.info("processPendingProductChange - REQ: " + req);
          req.retryCountNext();
          PrepaidProductChangeData10 data = req.getData();

          PrepaidUser10 user = data.getPrepaidUser();
          PrepaidCard10 prepaidCard = data.getPrepaidCard();
          TipoAlta tipoAlta = data.getTipoAlta();
          Account account = data.getAccount();

          if(req.getRetryCount() > getMaxRetryCount()) {
            return redirectRequestProductChange(createJMSEndpoint(ERROR_PRODUCT_CHANGE_REQ), exchange, req, false);
          }

          if(prepaidCard == null) {
            log.debug("No se debe realizar cambio de producto ya que el cliente todavia no tiene tarjeta prepago");
          } else {
            log.debug(String.format("Realizando el cambio de producto al usuario: %d", user.getId()));

            log.info(String.format("LLamando cambio de producto %s", account.getAccountNumber()));

            // se hace el cambio de producto
            //FixMe: debe usar getDocumentType en vez de tipoDocumento.RUT
            CambioProductoDTO dto = getRoute().getTecnocomService().cambioProducto(account.getAccountNumber(), user.getDocumentNumber(), TipoDocumento.RUT, tipoAlta);

            log.info("Respuesta cambio de producto");
            log.info(dto.getRetorno());
            log.info(dto.getDescRetorno());

            if(dto.isRetornoExitoso()) {
              log.debug("********** Cambio de producto realizado **********");
              // Tecnocom responde OK, hacemos los cambios de estado en la bd y las notificaciones
              changeProduct(user, account, prepaidCard, tipoAlta);
            } else if (dto.getRetorno().equals(CodigoRetorno._200)) {
              log.debug("********** Cambio de producto rechazado rechazado **********");
              // Si tecnocom responde que el nivel ya fue cambiado
              if(dto.getDescRetorno().contains("MPA0928")) {
                // Revisamos si tenemos cambiado el nivel en nuestra DB
                PrepaidUser10 storedUser10 = getRoute().getPrepaidUserEJBBean10().findById(null, user.getId());
                if(PrepaidUserLevel.LEVEL_2.equals(storedUser10.getUserLevel())) {
                  req.getData().setNumError(Errors.CLIENTE_YA_TIENE_NIVEL_2);
                  req.getData().setMsjError("MPA0928 - EL NUEVO PRODUCTO DEBE SER DIFERENTE AL ANTERIOR");
                } else {
                  // Si no lo tenemos cambiado, hacemos los cambios y lanzamos las notificaciones
                  changeProduct(user, account, prepaidCard, tipoAlta);
                }
              } else {
                req.getData().setNumError(Errors.ERROR_INDETERMINADO);
                req.getData().setMsjError(dto.getDescRetorno());
                return redirectRequestProductChange(createJMSEndpoint(ERROR_PRODUCT_CHANGE_REQ), exchange, req, false);
              }
            } else if (CodigoRetorno._1000.equals(dto.getRetorno())) {
              req.getData().setNumError(Errors.TECNOCOM_ERROR_REINTENTABLE);
              req.getData().setMsjError(Errors.TECNOCOM_ERROR_REINTENTABLE.name());
              return redirectRequestProductChange(createJMSEndpoint(PENDING_PRODUCT_CHANGE_REQ), exchange, req, true);
            }else if (CodigoRetorno._1010.equals(dto.getRetorno())) {
              req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_CONEXION);
              req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_CONEXION.name());
              return redirectRequestProductChange(createJMSEndpoint(PENDING_PRODUCT_CHANGE_REQ), exchange, req, true);
            } else if (CodigoRetorno._1020.equals(dto.getRetorno())) {
              req.getData().setNumError(Errors.TECNOCOM_TIME_OUT_RESPONSE);
              req.getData().setMsjError(Errors.TECNOCOM_TIME_OUT_RESPONSE.name());
              return redirectRequestProductChange(createJMSEndpoint(PENDING_PRODUCT_CHANGE_REQ), exchange, req, true);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          log.error(String.format("Error desconocido al realizar el cambio de producto %s",e.getMessage()));
          req.getData().setNumError(Errors.ERROR_INDETERMINADO);
          req.getData().setMsjError(e.getLocalizedMessage());
          return redirectRequestProductChange(createJMSEndpoint(ERROR_PRODUCT_CHANGE_REQ), exchange, req, false);
        }
        return req;
      }
    };
  }

  private void changeProduct(PrepaidUser10 user, Account account, PrepaidCard10 prepaidCard, TipoAlta tipoAlta) throws Exception {
    String accountUuid = account != null ? account.getUuid() : "[noUuid]";

    // Subir el nivel del usuario
    getRoute().getPrepaidUserEJBBean10().updatePrepaidUserLevel(user.getId(), PrepaidUserLevel.LEVEL_2);

    // Notificar el cierre de la tarjeta antigua
    getRoute().getPrepaidCardEJBBean11().publishCardEvent(user.getUserIdMc().toString(), accountUuid, prepaidCard.getId(), KafkaEventsRoute10.SEDA_CARD_CLOSED_EVENT);

    // Notificar que se ha creado una tarjeta nueva
    getRoute().getPrepaidCardEJBBean11().publishCardEvent(user.getUserIdMc().toString(), accountUuid, prepaidCard.getId(), KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);
  }

  private void sendSuccessMail(User user, Boolean hasCard) throws Exception {

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("user_name", user.getName());

    EmailBody emailBody = new EmailBody();
    emailBody.setTemplateData(templateData);
    emailBody.setAddress(user.getEmail().getValue());

    emailBody.setTemplate(hasCard ? TEMPLATE_MAIL_IDENTITY_VALIDATION_OK_WITH_CARD : TEMPLATE_MAIL_IDENTITY_VALIDATION_OK_WITHOUT_CARD);

    getRoute().getUserClient().sendMail(null, user.getId(), emailBody);
  }

  public ProcessorRoute processErrorProductChange() {
    return new ProcessorRoute<ExchangeData<PrepaidProductChangeData10>, ExchangeData<PrepaidProductChangeData10>>() {
      @Override
      public ExchangeData<PrepaidProductChangeData10> processExchange(long idTrx, ExchangeData<PrepaidProductChangeData10> req, Exchange exchange) throws Exception {

        //TODO: se debera generrar un ticket en freshdesk si el cambio de producto no se pudo realizar o si ocurrio un error indesperado?
        log.info("processErrorProductChange - REQ: " + req);
        req.retryCountNext();
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("idUsuario", req.getData().getPrepaidUser().getId().toString());
        templateData.put("rutCliente", req.getData().getPrepaidUser().getDocumentNumber());
        getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_PRODUCT_CHANGE, templateData);
        return req;
      }
    };
  }
}
