package cl.multicaja.prepaid.async.v10.processors;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.ConciliationMcRed10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import com.opencsv.CSVReader;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

public class PendingConciliationMcRed10 extends BaseProcessor10  {

  private static Log log = LogFactory.getLog(PendingConciliationMcRed10.class);

  public PendingConciliationMcRed10(BaseRoute10 route) {
    super(route);
  }


  public Processor processReconciliationsMcRed(){
    return new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        log.info("Proces Files Multicaja Red");
        final InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        log.error(exchange.getIn().getBody());
        String fileName = exchange.getIn().getBody(GenericFile.class).getFileName();
        List<ConciliationMcRed10> lstReconciliationMcRed10s = getCsvData(fileName, inputStream);
        if (fileName.contains("rendicion_cargas_mcpsa_mc")) {
          conciliationTopup(lstReconciliationMcRed10s);
          String sFecha = fileName.substring(26, 35);
        } else if (fileName.contains("rendicion_cargas_rechazadas_mcpsa_mc")) {
          concilitationTopupReject(lstReconciliationMcRed10s);
          String sFecha = fileName.substring(37, 46);
        } else if (fileName.contains("rendicion_cargas_reversadas_mcpsa_mc")) {
          conciliationTopupReverse(lstReconciliationMcRed10s);
          String sFecha = fileName.substring(37, 46);
        } else if (fileName.contains("rendicion_retiros_mcpsa_mc")) {
          conciliationWithdrawal(lstReconciliationMcRed10s);
          String sFecha = fileName.substring(27, 36);
        } else if (fileName.contains("rendicion_retiros_rechazados_mcpsa_mc")) {
          conciliationWithdrawalReject(lstReconciliationMcRed10s);
          String sFecha = fileName.substring(39, 48);
        } else if (fileName.contains("rendicion_retiros_reversados_mcpsa_mc")) {
          conciliationWithdrawalReverse(lstReconciliationMcRed10s);
          String sFecha = fileName.substring(39, 48);
        }
      }
    };
  }
  private void conciliationTopup(List<ConciliationMcRed10> lstReconciliationMcRed10s) throws Exception{
     try {
       for (ConciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
         PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode());
         if (prepaidMovement10 == null) {
           log.error("No conciliado");
           //TODO: se debe realizar movimiento indicado por el switch
           continue;

         }else if (!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PROCESS_OK)||!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PENDING)||!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.IN_PROCESS)) {
           log.error("No conciliado");
           //TODO: se debe realizar movimiento indicado por el switch
           continue;
         }
         else {
            if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
              log.error("No conciliado");
              //TODO: se deberia dejar como no conciliado ya que no cuadran los montos
              continue;
            }
            log.info("Conciliado");
            //TODO getRoute().getPrepaidMovementEJBBean10().updateStatusConciliacionSwitch();
         }
       }
     }catch (Exception e){
       throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
     }
  }
  private void conciliationWithdrawal(List<ConciliationMcRed10> lstReconciliationMcRed10s) throws Exception{
    try {
      for (ConciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
        PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode());
        if (prepaidMovement10 == null) {
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }else if (!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PROCESS_OK)){
          log.info("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }
        else {
          if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
            log.error("No conciliado");
            //TODO: se deberia dejar como no conciliado ya que no cuadran los montos
            continue;

          }
          log.info("Conciliado");
          //TODO getRoute().getPrepaidMovementEJBBean10().updateStatusConciliacionSwitch();
        }
      }
    }catch (Exception e){
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }
  private void concilitationTopupReject(List<ConciliationMcRed10> lstReconciliationMcRed10s)throws Exception{
    try {
      for (ConciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
        PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode());
        if (prepaidMovement10 == null) {
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }else if (!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PROCESS_OK)){
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }
        else {
          if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
            log.error("No conciliado");
            //TODO: se deberia dejar como no conciliado ya que no cuadran los montos
            continue;

          }
          log.info("Conciliado");
          //TODO getRoute().getPrepaidMovementEJBBean10().updateStatusConciliacionSwitch();
        }
      }
    }catch (Exception e){
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }

  private void conciliationWithdrawalReject(List<ConciliationMcRed10> lstReconciliationMcRed10s)throws Exception{
    try {
      for (ConciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
        PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode());
        if (prepaidMovement10 == null) {
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }else if (!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PROCESS_OK)){
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }
        else {
          if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
            log.error("No conciliado");
            //TODO: se deberia dejar como no conciliado ya que no cuadran los montos
            continue;

          }
          log.info("Conciliado");
          //TODO getRoute().getPrepaidMovementEJBBean10().updateStatusConciliacionSwitch();
        }
      }
    }catch (Exception e){
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }
  private void conciliationTopupReverse(List<ConciliationMcRed10> lstReconciliationMcRed10s)throws Exception{
    try {
      for (ConciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
        PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode());
        if (prepaidMovement10 == null) {
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }else if (!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PROCESS_OK)){
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }
        else {
          if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
            log.error("No conciliado");
            //TODO: se deberia dejar como no conciliado ya que no cuadran los montos
            continue;

          }
          log.info("Conciliado");
          //TODO getRoute().getPrepaidMovementEJBBean10().updateStatusConciliacionSwitch();
        }
      }
    }catch (Exception e){
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }
  private void conciliationWithdrawalReverse(List<ConciliationMcRed10> lstReconciliationMcRed10s) throws Exception{
    try {
      for (ConciliationMcRed10 recTmp : lstReconciliationMcRed10s) {
        PrepaidMovement10 prepaidMovement10 = getRoute().getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(recTmp.getMcCode());
        if (prepaidMovement10 == null) {
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }else if (!prepaidMovement10.getEstado().equals(PrepaidMovementStatus.PROCESS_OK)){
          log.error("No conciliado");
          //TODO: se debe realizar movimiento indicado por el switch
          continue;

        }
        else {
          if (!recTmp.getAmount().equals(prepaidMovement10.getMonto().longValue())) {
            log.error("No conciliado");
            //TODO: se deberia dejar como no conciliado ya que no cuadran los montos
            continue;

          }
          log.info("Conciliado");
          //TODO getRoute().getPrepaidMovementEJBBean10().updateStatusConciliacionSwitch();
        }
      }
    }catch (Exception e){
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
  }

  /**
   * Lee los archivos CSV
   * @param fileName
   * @param is
   * @return
   */
  public static List<ConciliationMcRed10> getCsvData(String fileName, InputStream is) throws Exception {
    List<ConciliationMcRed10> lstReconciliationMcRed10;
    log.info("IN");
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,';');
      csvReader.readNext();
      String[] record;
      lstReconciliationMcRed10 = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        ConciliationMcRed10 conciliationMcRed10 = new ConciliationMcRed10();
        conciliationMcRed10.setMcCode(record[0]);
        conciliationMcRed10.setDateTrx(record[1]);
        conciliationMcRed10.setClientId(Long.valueOf(record[2]));
        conciliationMcRed10.setAmount(Long.valueOf(record[3]));
        if(!fileName.contains("reversa")) {
          conciliationMcRed10.setExternalId(Long.valueOf(record[4]));
        }
        lstReconciliationMcRed10.add(conciliationMcRed10);
      }
    }catch (Exception e){
      log.error("Exception: "+e);
      e.printStackTrace();
      System.out.println("Exception: "+e);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
    log.info("OUT");
    return lstReconciliationMcRed10;
  }
}
