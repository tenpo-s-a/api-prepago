package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

/**
 * @author abarazarte
 **/
public class PendingTecnocomReconciliationFile10 extends BaseProcessor10 {

  private static Log log = LogFactory.getLog(PendingCurrencyModification10.class);

  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss Z");
  private static DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static ZoneId here = ZoneId.of("America/Santiago");

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

          if(file.isSuspicious()) {
            String msg = String.format("Error processing file [%s]. File seems suspicious", fileName);
            log.error(msg);
            processErrorSuspiciousFile(fileName);
            throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
          }

        insertOrUpdateManualTrx(fileName, file.getDetails()
          .stream()
          .filter(detail -> detail.isFromSat())
          .collect(Collectors.toList())
        );

        validateTransactions(fileName, file.getDetails()
          .stream()
          .filter(detail -> !detail.isFromSat())
          .collect(Collectors.toList())
        );

          /**
           * Se toma la fecha de envio del archivo y se marcan como NOT_RECONCILED los movimientos de 1 dia antes que no vinieron
           * el archivo actual o anterior.
           */

          String fileDate = getDateForNotReconciledTransactions(file.getHeader().getFecenvio(), file.getHeader().getHoraenvio());

        List<TipoFactura> tipFacs = Arrays.asList(TipoFactura.CARGA_TRANSFERENCIA,
          TipoFactura.ANULA_CARGA_TRANSFERENCIA,
          TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA,
          TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA,
          TipoFactura.RETIRO_TRANSFERENCIA,
          TipoFactura.ANULA_RETIRO_TRANSFERENCIA,
          TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA,
          TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA);

          for (TipoFactura type : tipFacs) {
            log.info(String.format("Changing status to not reconciled transaction from date [%s] and tipofac [%s]", fileDate, type.getDescription()));
            getRoute().getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, fileDate, fileDate, type, IndicadorNormalCorrector.fromValue(type.getCorrector()), ConciliationStatusType.NOT_RECONCILED);
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
  private void insertOrUpdateManualTrx(String fileName, List<ReconciliationFileDetail> trxs) {
    for (ReconciliationFileDetail trx : trxs) {
      try{
        //Se obtiene el pan
        String pan = Utils.replacePan(trx.getPan());

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getRoute().getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null,
          pan,
          trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileName, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        //Se busca el movimiento
        PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(prepaidCard10.getIdUser(),
          trx.getNumaut(), Date.valueOf(trx.getFecfac()), trx.getTipoFac());

        if(originalMovement == null) {
          // Movimiento original no existe.
          /**
           *           PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(prepaidCard10.getIdUser(), pan, trx);
           *           movement10.setConTecnocom(ConciliationStatusType.RECONCILED);
           *           movement10.setConSwitch(ConciliationStatusType.PENDING);
           *           movement10.setOriginType(MovementOriginType.SAT);
           *           movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
           *           movement10.setIdMovimientoRef(Long.valueOf(0));
           *           movement10.setIdTxExterno("");
           *           getRoute().getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);
           */

          String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
            prepaidCard10.getIdUser(), trx.getTipoFac().getCode(), trx.getTipoFac().getCorrector(),  trx.getNumaut(), trx.getFecfac(), trx.getImpfac());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);

          //TODO: Movimiento original no existe. Agregar informacion en tabla de movimientos a investigar

        } else if(ConciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
          if(!originalMovement.getMonto().equals(trx.getImpfac())){
            getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ConciliationStatusType.NOT_RECONCILED);
          } else {
            //Actualiza el estado_con_tecnocom a conciliado
            getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ConciliationStatusType.RECONCILED);
          }
        } else {
          log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
        }
      } catch (Exception ex) {
        log.error(String.format("Error processing transaction [%s]", trx.toString()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileName, trx);
      }
    }
  }

  private void validateTransactions(String fileName, List<ReconciliationFileDetail> trxs) {
    for (ReconciliationFileDetail trx : trxs) {
      try{
        //Se obtiene el pan
        String pan = Utils.replacePan(trx.getPan());

        //Se busca la tarjeta correspondiente al movimiento
        PrepaidCard10 prepaidCard10 = getRoute().getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null,
          pan,
          trx.getContrato());

        if(prepaidCard10 == null) {
          String msg = String.format("Error processing transaction - PrepaidCard not found with processorUserId [%s]", fileName, trx.getContrato());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);
          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);
        }

        //Se busca el movimiento
        PrepaidMovement10 originalMovement = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementForTecnocomReconciliation(prepaidCard10.getIdUser(),
          trx.getNumaut(), Date.valueOf(trx.getFecfac()), trx.getTipoFac());

        if(originalMovement == null) {
          TipoFactura tipofac = trx.getTipoFac();
          String msg = String.format("Error processing transaction - Transaction not found in database with userId = [%s], tipofac= [%s], indnorcor = [%s], numaut = [%s], fecfac = [%s], amount = [%s]",
          prepaidCard10.getIdUser(), tipofac.getCode(), tipofac.getCorrector(),  trx.getNumaut(), trx.getFecfac(), trx.getImpfac());
          log.error(msg);
          trx.setHasError(Boolean.TRUE);
          trx.setErrorDetails(msg);

          //TODO: Movimiento original no existe. Agregar informacion en tabla de movimientos a investigar

          throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), msg);

        } else if(ConciliationStatusType.PENDING.equals(originalMovement.getConTecnocom())) {
          if(!originalMovement.getMonto().equals(trx.getImpfac())){
            getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
              originalMovement.getId(),
              ConciliationStatusType.NOT_RECONCILED);
          } else {
            switch (originalMovement.getEstado()) {
              case PROCESS_OK:
                getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                  originalMovement.getId(),
                  ConciliationStatusType.RECONCILED);
                break;
              case PENDING:
              case IN_PROCESS:
              case REJECTED:
                getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                  originalMovement.getId(),
                  ConciliationStatusType.NOT_RECONCILED);
                break;
              case ERROR_TECNOCOM_REINTENTABLE:
              case ERROR_TIMEOUT_RESPONSE:
              case ERROR_TIMEOUT_CONEXION:
                //TODO: El estado de movimiento no debe ser actualizado en este proceso.
                /**
                getRoute().getPrepaidMovementEJBBean10().updatePrepaidMovement(null, originalMovement.getId(), pan,
                  trx.getCentalta(),
                  trx.getCuenta(),
                  numberUtils.toInteger(trx.getNumextcta()),
                  numberUtils.toInteger(trx.getNummovext()),
                  numberUtils.toInteger(trx.getClamon()),
                  null,
                  PrepaidMovementStatus.PROCESS_OK);
                */

                getRoute().getPrepaidMovementEJBBean10().updateStatusMovementConTecnocom(null,
                  originalMovement.getId(),
                  ConciliationStatusType.RECONCILED);
                break;
            }
          }
        } else  {
          log.info(String.format("Transaction already processed  id -> [%s]", originalMovement.getId()));
        }
      } catch (Exception ex) {
        log.error(String.format("Error processing transaction [%s]", trx.getNumaut()));
        if(StringUtils.isBlank(trx.getErrorDetails())) {
          trx.setErrorDetails(ex.getMessage());
        }
        processErrorTrx(fileName, trx);
      }
    }
  }

  /* Procesar errores*/

  private void processErrorSuspiciousFile(String fileName) {
    log.info(String.format("processErrorSuspiciousFile - %s", fileName));

    Map<String, Object> templateData = new HashMap<String, Object>();
    templateData.put("fileName", fileName);
    //TODO: definir template de correo
    //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TECNOCOM_FILE_SUSPICIOUS, templateData);
  }

  private void processErrorTrx(String fileName, ReconciliationFileDetail trx) {
    log.info("processErrorTrx");
    //TODO: definir como informar las transacciones
    Map<String, Object> templateData = new HashMap<String, Object>();
    //templateData.put("fileName", fileName);
    //TODO: definir template de correo
    //getRoute().getMailPrepaidEJBBean10().sendInternalEmail(TEMPLATE_MAIL_ERROR_TECNOCOM_FILE_SUSPICIOUS, templateData);
  }


  private String getDateForNotReconciledTransactions(String date, String time) {
    ZonedDateTime hereAndNow = Instant.now().atZone(here);
    String timezoneOffset = String.format("%tz", hereAndNow);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(String.format("%s %s %s", date, time, timezoneOffset), formatter);

    Instant instant = zonedDateTime.toInstant().minus(1,ChronoUnit.DAYS);

    //get date time only
    LocalDateTime result = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));

    return result.toLocalDate().format(dbFormatter);
  }
}
