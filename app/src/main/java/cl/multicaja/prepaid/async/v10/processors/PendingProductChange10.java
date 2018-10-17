package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.core.model.Errors;
import cl.multicaja.prepaid.async.v10.model.PrepaidProductChangeData10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.TipoAlta;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.CambioProductoDTO;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10.ERROR_PRODUCT_CHANGE_REQ;
import static cl.multicaja.prepaid.async.v10.routes.ProductChangeRoute10.PENDING_PRODUCT_CHANGE_REQ;

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

          User user = data.getUser();
          PrepaidCard10 prepaidCard = data.getPrepaidCard();
          TipoAlta tipoAlta = data.getTipoAlta();

          if(req.getRetryCount() > getMaxRetryCount()) {
            return redirectRequestProductChange(createJMSEndpoint(ERROR_PRODUCT_CHANGE_REQ), exchange, req, false);
          }

          log.debug(String.format("Realizando el cambio de producto al usuario: %d", user.getId()));

          // se hace el cambio de producto
          CambioProductoDTO dto = getRoute().getTecnocomService().cambioProducto(prepaidCard.getProcessorUserId(), user.getRut().getValue().toString(), TipoDocumento.RUT, tipoAlta);

          if(dto.isRetornoExitoso()) {
            log.debug("********** Cambio de producto realizado **********");
          } else if (dto.getRetorno().equals(CodigoRetorno._200)) {
            if(dto.getDescRetorno().contains("MPA0928")) {
              log.debug("********** Cambio de producto realizado anteriormente **********");
              req.getData().setMsjError(dto.getDescRetorno());
            } else {
              log.debug("********** Cambio de producto rechazado rechazado **********");
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

  public ProcessorRoute processErrorProductChange() {
    return new ProcessorRoute<ExchangeData<PrepaidProductChangeData10>, ExchangeData<PrepaidProductChangeData10>>() {
      @Override
      public ExchangeData<PrepaidProductChangeData10> processExchange(long idTrx, ExchangeData<PrepaidProductChangeData10> req, Exchange exchange) throws Exception {
        log.info("processErrorProductChange - REQ: " + req);
        req.retryCountNext();
        //TODO: enviar mail error cambio de producto
        /*
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("idUsuario", req.getData().getUser().getId().toString());
        templateData.put("rutCliente", req.getData().getUser().getRut().getValue().toString() + "-" + req.getData().getUser().getRut().getDv());
        getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TOPUP_REVERSE, templateData);
        */
        return req;
      }
    };
  }
}
