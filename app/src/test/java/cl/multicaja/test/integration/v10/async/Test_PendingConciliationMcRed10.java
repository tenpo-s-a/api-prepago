package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVWriter;
import org.h2.mvstore.DataUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class Test_PendingConciliationMcRed10 extends TestBaseUnitAsync {

  public static String BASE_DIR = "src/test/resources/multicajared/";

  private ArrayList<String> movementDates = new ArrayList<String>();
  private String fileDate;
  private Timestamp startDateTs;
  private Timestamp endDateTs;
  private int reconciledExpectedCount;
  private int notReconciledExpectedCount;

  @Before
  public void prepareDates() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));

    startDateTs = Timestamp.valueOf("2018-08-03 04:00:00");
    endDateTs = Timestamp.valueOf("2018-08-04 03:59:59");

    fileDate = "20180804"; // Date the file was "sent"

    movementDates.clear();
    movementDates.add("2018-08-03 21:14:09"); // Dentro
    movementDates.add("2018-08-03 17:43:54"); // Dentro
    movementDates.add("2018-08-03 04:00:00"); // Dentro
    movementDates.add("2018-08-04 03:59:59"); // Dentro
    movementDates.add("2018-08-03 03:59:32"); // Fuera
    movementDates.add("2018-08-04 04:00:01"); // Fuera

    reconciledExpectedCount = 3;
    notReconciledExpectedCount = 4;
  }

  @After
  public void cleanDB() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
  }

  @Test
  public void rendicionCargas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL,false, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.assertTrue("Conciliado OK", true);
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
    }
    Assert.assertEquals("Debe haber N conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber N no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }

  @Test
  public void rendicionCargasNoConcilada() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL,true, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento", movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.fail("Nada debe estar conciliado");
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        boolean correctDate = includedInDates(movTmp.getFechaCreacion());
        boolean includedInFile = movementIndex < reconciledExpectedCount;
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }

  @Test
  public void rendicionCargasReversadas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA,false, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.assertTrue("Conciliado OK", true);
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
    }
    Assert.assertEquals("Debe haber N conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber N no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionCargasReversadasNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA,true, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento", movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.fail("Nada debe estar conciliado");
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        boolean correctDate = includedInDates(movTmp.getFechaCreacion());
        boolean includedInFile = movementIndex < reconciledExpectedCount;
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetiros() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,false, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.assertTrue("Conciliado OK", true);
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
    }
    Assert.assertEquals("Debe haber N conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber N no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetirosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,true, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento", movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.fail("Nada debe estar conciliado");
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        boolean correctDate = includedInDates(movTmp.getFechaCreacion());
        boolean includedInFile = movementIndex < reconciledExpectedCount;
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetirosReversados() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,false, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento",movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.assertTrue("Conciliado OK", true);
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
    }
    Assert.assertEquals("Debe haber N conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber N no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetirosReversadosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,true, movementDates);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      Assert.assertNotNull("Debe existir el movimiento", movTmp);
      if (movTmp.getConSwitch() == ConciliationStatusType.RECONCILED) {
        Assert.fail("Nada debe estar conciliado");
        reconciledCount++;
      } else if (movTmp.getConSwitch() == ConciliationStatusType.NOT_RECONCILED) {
        boolean correctDate = includedInDates(movTmp.getFechaCreacion());
        boolean includedInFile = movementIndex < reconciledExpectedCount;
        Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
        notReconcilidedCount++;
      } else {
        Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas", !includedInDates(movTmp.getFechaCreacion()));
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }

  public ArrayList<PrepaidMovement10> createMovementAndFile(int cantidad, PrepaidMovementType type, IndicadorNormalCorrector indicadorNormalCorrector, Boolean withError, ArrayList<String> movementDates) throws Exception {

    ArrayList<PrepaidMovement10> lstPrepaidMovement10s = new ArrayList<>();
    ArrayList<PrepaidMovement10> lstPrepaidMovementInFile = new ArrayList<>();
    String fileName = null;
    String sDate = fileDate;

    int totalNumberOfMovements = cantidad + movementDates.size();
    for (int i = 0; i < totalNumberOfMovements; i++) {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidMovement10 prepaidMovement10 = null;
      if(PrepaidMovementType.TOPUP.equals(type) && IndicadorNormalCorrector.NORMAL.equals(indicadorNormalCorrector)) {
        PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
        prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
        if(i + 1 == totalNumberOfMovements)
          fileName="rendicion_cargas_mcpsa_mc_"+sDate+".csv";
      }
      else if (PrepaidMovementType.TOPUP.equals(type) && IndicadorNormalCorrector.CORRECTORA.equals(indicadorNormalCorrector)) {
        PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
        prepaidMovement10 = buildReversePrepaidMovement10(prepaidUser, prepaidTopup);
        if(i + 1 == totalNumberOfMovements)
          fileName="rendicion_cargas_reversadas_mcpsa_mc_"+sDate+".csv";
      }
      else if (PrepaidMovementType.WITHDRAW.equals(type) && IndicadorNormalCorrector.NORMAL.equals(indicadorNormalCorrector)) {
        PrepaidWithdraw10 prepaidWithdraw10 = buildPrepaidWithdraw10(user);
        prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw10);
        if(i + 1 == totalNumberOfMovements)
          fileName="rendicion_retiros_mcpsa_mc_"+sDate+".csv";
      }
      else if (PrepaidMovementType.WITHDRAW.equals(type) && IndicadorNormalCorrector.CORRECTORA.equals(indicadorNormalCorrector)){
        PrepaidWithdraw10 prepaidWithdraw10 = buildPrepaidWithdraw10(user);
        prepaidMovement10 = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw10);
        if(i + 1 == totalNumberOfMovements)
          fileName="rendicion_retiros_reversados_mcpsa_mc_"+sDate+".csv";
      }

      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

      lstPrepaidMovement10s.add(prepaidMovement10);
      if (i < cantidad) {
        // Movimiento normal.
        // No importa su fecha.
        // Sera agregado al archivo y conciliado.
        lstPrepaidMovementInFile.add(prepaidMovement10);
      } else {
        // Movimientos que se setean con fecha especial.
        // NO se agregan al archivo
        // Pueden quedar PENDING o NOT_RECONCILED dependiendo de su fecha.
        String movementDate = movementDates.get(i - cantidad);
        Date parsedDate = DateUtils.getInstance().dateStringToDate(movementDate, "yyyy-MM-dd hh:mm:ss");
        Timestamp movementTimestamp = new Timestamp(parsedDate.getTime());
        prepaidMovement10.setFechaCreacion(movementTimestamp); // En la lista
        changeMovement(prepaidMovement10.getId(), movementDate); // Y en la BD
      }
    }
    if (withError) {
      createCSVWithError(fileName, lstPrepaidMovementInFile);
    } else {
      createCSV(fileName, lstPrepaidMovementInFile);
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

  private void changeMovement(Object idMovimiento, String newDate)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET fecha_creacion = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone "
        + "WHERE ID = " + idMovimiento.toString());
  }

  private boolean includedInDates(Timestamp movTimestamp) {
    return !movTimestamp.before(this.startDateTs) && !movTimestamp.after(this.endDateTs);
  }
}
