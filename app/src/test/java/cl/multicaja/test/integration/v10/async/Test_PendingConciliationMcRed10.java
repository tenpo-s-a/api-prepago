package cl.multicaja.test.integration.v10.async;

import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Test_PendingConciliationMcRed10 extends TestBaseUnitAsync {

  public static String BASE_DIR = "src/test/resources/";

  public void createMovementAndFile(int cantidad, PrepaidMovementType type, IndicadorNormalCorrector indicadorNormalCorrector) throws Exception {

    ArrayList<PrepaidMovement10> lstPrepaidMovement10s = new ArrayList<>();
    for (int i = 0; i <= cantidad; i++) {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      lstPrepaidMovement10s.add(prepaidMovement10);
    }
  }

  private void createCSV(String filename, ArrayList<PrepaidMovement10> lstPrepaidMovement10s){
    File file = new File(BASE_DIR+filename+".csv");
    try{
      FileWriter outputfile = new FileWriter(file);
      // create CSVWriter object filewriter object as parameter
      CSVWriter writer = new CSVWriter(outputfile);
      String[] header;
      if(filename.contains("reversa")) {
        header = new String[]{"CODIGO_MC", "FECHA_TRX", "ID_CLIENTE", "MONTO"};
      }
      else{
        header = new String[]{"CODIGO_MC", "FECHA_TRX", "ID_CLIENTE", "MONTO","CARGA_ID"};
      }
        writer.writeNext(header);
      for(PrepaidMovement10 mov:lstPrepaidMovement10s){
        String[] data;
        if(filename.contains("reversa")) {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFechaCreacion()),String.valueOf(mov.getMonto())};
        }else {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFechaCreacion()),String.valueOf(mov.getMonto()),String.valueOf(mov.getId())};
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
