package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * @autor vutreras
 */
public class PendingTopup10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingTopup10.class);

  public PendingTopup10(PrepaidTopupRoute10 prepaidTopupRoute10) {
    super(prepaidTopupRoute10);
  }

  public ProcessorRoute processPendingTopup() {
    return new ProcessorRoute<RequestRoute<PrepaidTopupDataRoute10>, ResponseRoute<PrepaidTopupDataRoute10>>() {
      @Override
      public ResponseRoute<PrepaidTopupDataRoute10> processExchange(long idTrx, RequestRoute<PrepaidTopupDataRoute10> req, Exchange exchange) throws Exception {

        log.info("processPendingTopup - REQ: " + req);

        PrepaidTopupDataRoute10 data = req.getData();

        if (data.getUser() == null) {
          log.error("Error req.getUser() es null");
          return null;
        }

        if (data.getUser().getRut() == null) {
          log.error("Error req.getUser().getRut() es null");
          return null;
        }

        Integer rut = data.getUser().getRut().getValue();

        if (rut == null){
          log.error("Error req.getUser().getRut().getValue() es null");
          return null;
        }

        PrepaidUser10 prepaidUser = getPrepaidEJBBean10().getPrepaidUserByRut(null, rut);

        if (prepaidUser == null){
          log.error("Error al buscar PrepaidUser10 con rut: " + rut);
          return null;
        }

        req.getData().setPrepaidUser10(prepaidUser);

        PrepaidCard10 card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

        if (card == null) {
          card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED);
        }

        if (card != null) {

          req.getData().setPrepaidCard10(card);
          PrepaidTopup10 prepaidTopup = req.getData().getPrepaidTopup10();
          PrepaidMovement10 prepaidMovement = req.getData().getPrepaidMovement10();

          String codent = null;
          try {
            codent = getParametersUtil().getString("api-prepaid", "cod_entidad", "v10");
          } catch (SQLException e) {
            log.error("Error al cargar parametro cod_entidad");
            codent = getConfigUtils().getProperty("tecnocom.codEntity");
          }

          TipoFactura tipoFactura = null;

          if (TopupType.WEB.equals(prepaidTopup.getType())) {
            tipoFactura = TipoFactura.CARGA_TRANSFERENCIA;
          } else {
            tipoFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
          }

          if (prepaidMovement == null) {
            prepaidMovement = new PrepaidMovement10();
            prepaidMovement.setTipofac(tipoFactura);
            prepaidMovement.setCodent(codent);
            req.getData().setPrepaidMovement10(prepaidMovement);
          }

          String contrato = card.getProcessorUserId();
          String pan = getEncryptUtil().decrypt(card.getEncryptedPan());
          CodigoMoneda clamon = prepaidMovement.getClamon();
          IndicadorNormalCorrector indnorcor = prepaidMovement.getIndnorcor();
          TipoFactura tipofac = prepaidMovement.getTipofac();
          BigDecimal impfac = prepaidMovement.getImpfac();
          String codcom = prepaidMovement.getCodcom();
          Integer codact = prepaidMovement.getCodact();
          CodigoPais codpais = prepaidMovement.getCodpais();
          String nomcomred = prepaidTopup.getMerchantName();
          String numreffac = prepaidMovement.getId().toString();
          String numaut = numreffac;

          //solamente los 6 primeros digitos de numreffac
          if (numaut.length() > 6) {
            numaut = numaut.substring(numaut.length()-6);
          }

          InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac, numreffac, impfac, numaut, codcom, nomcomred, codact, codpais);

          if (inclusionMovimientosDTO.getRetorno().equals(CodigoRetorno._000)) {

            prepaidMovement.setNumextcta(inclusionMovimientosDTO.getNumextcta());
            prepaidMovement.setNummovext(inclusionMovimientosDTO.getNummovext());
            prepaidMovement.setClamone(inclusionMovimientosDTO.getClamone());
            prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK); //realizado

            getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement.getId(),
                                                                        prepaidMovement.getNumextcta(),
                                                                        prepaidMovement.getNummovext(),
                                                                        prepaidMovement.getClamone(),
                                                                        prepaidMovement.getEstado());

          } else {

          }

          // Si es 1era carga enviar a cola de cobro de emision
          if(prepaidTopup.isFirstTopup()){
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().PENDING_CARD_ISSUANCE_FEE_REQ), req, exchange.getIn().getHeaders());
          }

        } else {

          //https://www.pivotaltracker.com/story/show/157816408
          //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

          card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED_HARD);

          if (card == null) {
            card = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.EXPIRED);
          }

          if (card == null) {
            exchange.getContext().createProducerTemplate().sendBodyAndHeaders(createJMSEndpoint(getRoute().PENDING_EMISSION_REQ), req, exchange.getIn().getHeaders());
          } else {
            return null;
          }
        }

        return new ResponseRoute<>(req.getData());
      }
    };
  }
}
