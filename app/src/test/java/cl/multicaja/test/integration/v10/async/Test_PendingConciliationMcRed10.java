package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;

public class Test_PendingConciliationMcRed10 extends TestBaseUnitAsync {

  public static String BASE_DIR = "src/test/resources/multicajared/";

  @Test
  public void rendicionCargas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.TOPUP,IndicadorNormalCorrector.NORMAL,false);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.CONCILIADO,movTmp.getConSwitch());
    }
  }

  @Test
  public void rendicionCargasNoConcilada() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.TOPUP,IndicadorNormalCorrector.NORMAL,true);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.NO_CONCILIADO,movTmp.getConSwitch());
    }
  }

  @Test
  public void rendicionCargasReversadas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.TOPUP,IndicadorNormalCorrector.CORRECTORA,false);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.CONCILIADO,movTmp.getConSwitch());
    }
  }
  @Test
  public void rendicionCargasReversadasNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.TOPUP,IndicadorNormalCorrector.CORRECTORA,true);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.NO_CONCILIADO,movTmp.getConSwitch());
    }
  }
  @Test
  public void rendicionRetiros() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.WITHDRAW,IndicadorNormalCorrector.NORMAL,false);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.CONCILIADO,movTmp.getConSwitch());
    }
  }
  @Test
  public void rendicionRetirosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.WITHDRAW,IndicadorNormalCorrector.NORMAL,true);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.NO_CONCILIADO,movTmp.getConSwitch());
    }
  }
  @Test
  public void rendicionRetirosReversados() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.WITHDRAW,IndicadorNormalCorrector.CORRECTORA,false);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.CONCILIADO,movTmp.getConSwitch());
    }
  }
  @Test
  public void rendicionRetirosReversadosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(3,PrepaidMovementType.WITHDRAW,IndicadorNormalCorrector.CORRECTORA,true);
    Thread.sleep(1500);
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      Assert.assertEquals("Debe estar conciliado",ConciliationStatusType.NO_CONCILIADO,movTmp.getConSwitch());
    }
  }
  public ArrayList<PrepaidMovement10> createMovementAndFile(int cantidad, PrepaidMovementType type, IndicadorNormalCorrector indicadorNormalCorrector,Boolean withError) throws Exception {

    ArrayList<PrepaidMovement10> lstPrepaidMovement10s = new ArrayList<>();
    String fileName = null;
    String sDate = DateUtils.getInstance().dateToStringFormat(new Date(),"yyyyMMdd");

    for (int i = 0; i < cantidad; i++) {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidMovement10 prepaidMovement10 = null;
      if(PrepaidMovementType.TOPUP.equals(type) && IndicadorNormalCorrector.NORMAL.equals(indicadorNormalCorrector)) {
        PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
        prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
        if(i+1 == cantidad)
          fileName="rendicion_cargas_mcpsa_mc_"+sDate+".csv";
      }
      else if (PrepaidMovementType.TOPUP.equals(type) && IndicadorNormalCorrector.CORRECTORA.equals(indicadorNormalCorrector)) {
        PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
        prepaidMovement10 = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
        if(i+1 == cantidad)
          fileName="rendicion_cargas_reversadas_mcpsa_mc_"+sDate+".csv";
      }
      else if (PrepaidMovementType.WITHDRAW.equals(type) && IndicadorNormalCorrector.NORMAL.equals(indicadorNormalCorrector)) {
        PrepaidWithdraw10 prepaidWithdraw10 = buildPrepaidWithdraw10(user);
        prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw10);
        if(i+1==cantidad)
          fileName="rendicion_retiros_mcpsa_mc_"+sDate+".csv";
      }
      else if (PrepaidMovementType.WITHDRAW.equals(type) && IndicadorNormalCorrector.CORRECTORA.equals(indicadorNormalCorrector)){
        PrepaidWithdraw10 prepaidWithdraw10 = buildPrepaidWithdraw10(user);
        prepaidMovement10 = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw10);
        if(i+1==cantidad)
          fileName="rendicion_retiros_reversados_mcpsa_mc_"+sDate+".csv";
      }
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      lstPrepaidMovement10s.add(prepaidMovement10);
    }
    if(withError){
      createCSVWithError(fileName,lstPrepaidMovement10s);
    }else{
      createCSV(fileName,lstPrepaidMovement10s);
    }
    return lstPrepaidMovement10s;
  }

  private void createCSV(String filename, ArrayList<PrepaidMovement10> lstPrepaidMovement10s){
    File file = new File(BASE_DIR+filename);
    try{
      FileWriter outputfile = new FileWriter(file);
      // create CSVWriter object filewriter object as parameter
      CSVWriter writer = new CSVWriter(outputfile,';');
      String[] header;
      if(filename.contains("reversa")) {
        header = new String[]{"CODIGO_MC", "FECHA_TRX", "ID_CLIENTE", "MONTO"};
      }
      else{
        header = new String[]{"CODIGO_MC", "FECHA_TRX", "ID_CLIENTE", "MONTO","CARGA_ID"};
      }
        writer.writeNext(header);
      for(PrepaidMovement10 mov:lstPrepaidMovement10s){
        System.out.println(mov);
        String[] data;
        if(filename.contains("reversa")) {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFecfac()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue())};
        }else {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFecfac()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue()),String.valueOf(mov.getId())};
        }
        writer.writeNext(data);
      }
      writer.close();
    }catch (Exception e){
      log.error("Exception : "+e);
      e.printStackTrace();
    }
  }

  private void createCSVWithError(String filename, ArrayList<PrepaidMovement10> lstPrepaidMovement10s){
    File file = new File(BASE_DIR+filename);
    try{
      FileWriter outputfile = new FileWriter(file);
      // create CSVWriter object filewriter object as parameter
      CSVWriter writer = new CSVWriter(outputfile,';');
      String[] header;
      if(filename.contains("reversa")) {
        header = new String[]{"CODIGO_MC", "FECHA_TRX", "ID_CLIENTE", "MONTO"};
      }
      else{
        header = new String[]{"CODIGO_MC", "FECHA_TRX", "ID_CLIENTE", "MONTO","CARGA_ID"};
      }
      writer.writeNext(header);
      for(PrepaidMovement10 mov:lstPrepaidMovement10s){
        System.out.println(mov);
        String[] data;
        if(filename.contains("reversa")) {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFecfac()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue()+100)};
        }else {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFecfac()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue()+100),String.valueOf(mov.getId())};
        }
        writer.writeNext(data);
      }
      writer.close();
    }catch (Exception e){
      log.error("Exception : "+e);
      e.printStackTrace();
    }
  }

}
