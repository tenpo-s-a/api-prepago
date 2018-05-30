package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.camel.ProcessorRoute;
import cl.multicaja.camel.RequestRoute;
import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
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

        User user = data.getUser();

        log.info("processPendingTopup user: " + user);

        if (user == null) {
          log.error("Error user es null");
          return null;
        }

        if (user.getRut() == null) {
          log.error("Error user.getRut() es null");
          return null;
        }

        Integer rut = user.getRut().getValue();

        if (rut == null){
          log.error("Error rut es null");
          return null;
        }

        PrepaidUser10 prepaidUser = getPrepaidEJBBean10().getPrepaidUserByRut(null, rut);

        log.info("processPendingTopup prepaidUser: " + prepaidUser);

        if (prepaidUser == null){
          log.error("Error al buscar PrepaidUser10 con rut: " + rut);
          return null;
        }

        data.setPrepaidUser10(prepaidUser);

        PrepaidCard10 prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

        if (prepaidCard == null) {
          prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED);
        }

        log.info("processPendingTopup prepaidCard10: " + prepaidCard);

        if (prepaidCard != null) {

          data.setPrepaidCard10(prepaidCard);
          PrepaidTopup10 prepaidTopup = data.getPrepaidTopup10();
          PrepaidMovement10 prepaidMovement = data.getPrepaidMovement10();

          log.info("processPendingTopup prepaidMovement: " + prepaidMovement);

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
            data.setPrepaidMovement10(prepaidMovement);
          }

          String contrato = prepaidCard.getProcessorUserId();
          String pan = getEncryptUtil().decrypt(prepaidCard.getEncryptedPan());
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

          InclusionMovimientosDTO inclusionMovimientosDTO = getTecnocomService().inclusionMovimientos(contrato, pan, clamon, indnorcor, tipofac,
                                                                                                      numreffac, impfac, numaut, codcom,
                                                                                                      nomcomred, codact, codpais);

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

            // Si es 1era carga enviar a cola de cobro de emision
            if(prepaidTopup.isFirstTopup()){
              redirectRequest(createJMSEndpoint(getRoute().PENDING_CARD_ISSUANCE_FEE_REQ), exchange, req);
            }

          } else {
            //TODO falta implementar
          }

        } else {

          //https://www.pivotaltracker.com/story/show/157816408
          //3-En caso de tener estado bloqueado duro o expirada no se deberá seguir ningún proceso

          prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.LOCKED_HARD);

          if (prepaidCard == null) {
            prepaidCard = getPrepaidEJBBean10().getPrepaidCardByUserId(null, prepaidUser.getId(), PrepaidCardStatus.EXPIRED);
          }

          if (prepaidCard == null) {
            redirectRequest(createJMSEndpoint(getRoute().PENDING_EMISSION_REQ), exchange, req);
          } else {
            return null;
          }
        }

        return new ResponseRoute<>(data);
      }
    };
  }
}
