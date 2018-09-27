package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

/**
 * @author abarazarte
 **/
public class PendingTecnocomReconciliationFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCurrencyModification10.class);

  public PendingTecnocomReconciliationFile10(BaseRoute10 route) {
    super(route);
  }

  public Processor processReconciliationFile() throws Exception {
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        log.info("Proccess file name : " + fileName);
        try{
          ReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
          if(file != null) {
            insertOrUpdateManualTrx(file.getDetails()
              .stream()
              .filter(detail -> detail.isFromSat())
              .collect(Collectors.toList())
            );

            validateTransactions(file.getDetails()
              .stream()
              .filter(detail -> !detail.isFromSat())
              .collect(Collectors.toList())
            );
          } else {
            String msg = String.format("Error processing file [%s]", fileName);
            log.error(msg);
            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
          }
        } catch (Exception ex){
          String msg = String.format("Error processing file [%s]", fileName);
          log.error(msg, ex);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }
      }
    };
  }

  /**
   * Insertar en la tabla prp_movimientos, las transcacciones realizadas de forma manual por SAT.
   * @param trxs
   */
  public void insertOrUpdateManualTrx(List<ReconciliationFileDetail> trxs) {
    for (ReconciliationFileDetail trx : trxs) {
      try{
        //Se obtiene el pan
        String pan = Utils.replacePan(trx.getPan());

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getRoute().getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null,
          pan,
          trx.getContrato());

        if(prepaidCard10 == null) {
          //TODO: Que hacer si la tarjeta es null?
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", trx.getContrato());
          log.error(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        //Se busca el movimiento
        PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(prepaidCard10.getIdUser(),
          trx.getNumaut(), Date.valueOf(trx.getFecfac()), trx.getTipoFac());

        if(originalMovement == null) {
          // Movimiento original no existe. Se agrega.
          //TODO: Agregar movimiento


        //} else if(originalMovement.getEstado().equals("sadasd")) {
          //Se verifica que no se ha conciliado anteriormente
        } else {
          if(!originalMovement.getMonto().equals(trx.getImpfac())){
            //TODO: Que hacer si no coincide?
          } else {
            //Movimiento ya existe. Se actualiza el estado a PROCESS_OK
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovementStatus(null, originalMovement.getId(), PrepaidMovementStatus.PROCESS_OK);

            //todo: Actualizar estado de conciliacion Tecnocom.
          }
        }
      } catch (Exception ex) {
        log.error(String.format("Error processing transaction [%s]", trx.getNumaut()));
        //TODO: lanzar excepcion?
      }
    }
  }

  public void validateTransactions(List<ReconciliationFileDetail> trxs) {
    for (ReconciliationFileDetail trx : trxs) {
      log.info(String.format("%s - %s", trx.getNumaut(), trx.toString()));
    }
  }


}
