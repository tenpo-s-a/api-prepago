package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
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
          PrepaidMovement10 movement10 = buildMovement(prepaidCard10.getIdUser(), pan, trx);
          movement10.setConTecnocom(ConciliationStatusType.CONCILATE);
          movement10.setConSwitch(ConciliationStatusType.PENDING);
          movement10.setOriginType(MovementOriginType.SAT);
          movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
          movement10.setIdMovimientoRef(Long.valueOf(0));
          movement10.setIdTxExterno("");
          getRoute().getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

        } else if(ConciliationStatusType.CONCILATE.equals(originalMovement.getConTecnocom())) {
          log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
        } else {
          if(!originalMovement.getMonto().equals(trx.getImpfac())){
            //TODO: Que hacer si no coincide?
            getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ConciliationStatusType.NO_CONCILIATE);
          } else {

            //Movimiento ya existe. Se actualiza el estado a PROCESS_OK
            getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, originalMovement.getId(), pan,
              trx.getCentalta(),
              trx.getCuenta(),
              numberUtils.toInteger(trx.getNumextcta()),
              numberUtils.toInteger(trx.getNummovext()),
              numberUtils.toInteger(trx.getClamon()),
              PrepaidMovementStatus.PROCESS_OK);

            //Actualiza el estado_con_tecnocom a conciliado
            getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ConciliationStatusType.CONCILATE);
          }
        }
      } catch (Exception ex) {
        log.error(String.format("Error processing transaction [%s]", trx.getNumaut()));
        //TODO: informar de error al procesar la transaccion?
      }
    }
  }

  public void validateTransactions(List<ReconciliationFileDetail> trxs) {
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
          PrepaidMovement10 movement10 = buildMovement(prepaidCard10.getIdUser(), pan, trx);
          movement10.setConTecnocom(ConciliationStatusType.CONCILATE);
          movement10.setConSwitch(ConciliationStatusType.PENDING);
          movement10.setOriginType(MovementOriginType.SAT);
          movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
          movement10.setIdMovimientoRef(Long.valueOf(0));
          movement10.setIdTxExterno("");
          getRoute().getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

        } else if(ConciliationStatusType.CONCILATE.equals(originalMovement.getConTecnocom())) {
          log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
        } else {
          if(!originalMovement.getMonto().equals(trx.getImpfac())){
            //TODO: Que hacer si no coincide?
          } else {


            switch (originalMovement.getEstado()) {
              case PROCESS_OK:
                getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                  originalMovement.getId(),
                  ConciliationStatusType.CONCILATE);
                break;
              case PENDING:
              case IN_PROCESS:
              case REJECTED:
                //TODO: Investigar movimiento.
                break;
              case ERROR_TECNOCOM_REINTENTABLE:
              case ERROR_TIMEOUT_RESPONSE:
              case ERROR_TIMEOUT_CONEXION:
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, originalMovement.getId(), pan,
                  trx.getCentalta(),
                  trx.getCuenta(),
                  numberUtils.toInteger(trx.getNumextcta()),
                  numberUtils.toInteger(trx.getNummovext()),
                  numberUtils.toInteger(trx.getClamon()),
                  PrepaidMovementStatus.PROCESS_OK);

                getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                  originalMovement.getId(),
                  ConciliationStatusType.CONCILATE);
                break;
            }


            //Movimiento ya existe. Se actualiza el estado a PROCESS_OK



          }
        }
      } catch (Exception ex) {
        log.error(String.format("Error processing transaction [%s]", trx.getNumaut()));
        //TODO: informar de error al procesar la transaccion?
      }
    }
  }

  private PrepaidMovement10 buildMovement(Long userId, String pan, ReconciliationFileDetail batchTrx) {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(null);
    prepaidMovement.setIdPrepaidUser(userId);
    prepaidMovement.setIdTxExterno(null);
    prepaidMovement.setTipoMovimiento(batchTrx.getMovementType());
    prepaidMovement.setMonto(batchTrx.getImpfac());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setCodent(batchTrx.getCodent());
    prepaidMovement.setCentalta(batchTrx.getCentalta());
    prepaidMovement.setCuenta(batchTrx.getCuenta());
    prepaidMovement.setClamon(CodigoMoneda.fromValue(numberUtils.toInteger(batchTrx.getClamon())));
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(batchTrx.getTipoFac().getCorrector()));
    prepaidMovement.setTipofac(batchTrx.getTipoFac());
    prepaidMovement.setFecfac(Date.valueOf(batchTrx.getFecfac()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(pan);
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(batchTrx.getImpfac());
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut(batchTrx.getNumaut());
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(batchTrx.getCodcom());
    prepaidMovement.setCodact(numberUtils.toInteger(batchTrx.getCodact()));
    prepaidMovement.setImpliq(0L);
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setCodpais(CodigoPais.fromValue(numberUtils.toInteger(batchTrx.getCodpais())));
    prepaidMovement.setNompob("");
    prepaidMovement.setNumextcta(numberUtils.toInteger(batchTrx.getNumextcta()));
    prepaidMovement.setNummovext(numberUtils.toInteger(batchTrx.getNummovext()));
    prepaidMovement.setClamone(numberUtils.toInteger(batchTrx.getClamon()));
    prepaidMovement.setTipolin(batchTrx.getTipolin());
    prepaidMovement.setLinref(numberUtils.toInteger(batchTrx.getLinref()));
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(0L);

    return prepaidMovement;
  }




}
