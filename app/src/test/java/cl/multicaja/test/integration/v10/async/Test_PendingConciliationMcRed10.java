package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Test_PendingConciliationMcRed10 extends TestBaseUnitAsync {

  private static String BASE_DIR = "src/test/resources/multicajared/";

  private ArrayList<WrongMovementInfo> wrongMovementInfos = new ArrayList<>();
  private String fileDate;
  private Timestamp startDateTs;
  private Timestamp endDateTs;

  private int reconciledExpectedCount;
  private int notReconciledExpectedCount;
  private int onlyFileMovements;


  @Before
  public void prepareDates() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", SCHEMA));

    startDateTs = Timestamp.valueOf("2018-08-03 04:00:00");
    endDateTs = Timestamp.valueOf("2018-08-04 03:59:59");

    // Fecha en que el archivo fue enviado
    // Incluira los movimientos del dia anterior
    fileDate = "20180804";

    // En la fecha 2018-08-03 la diferencia de horario America/Santiago vs UTC era -4
    // Por lo que los movimientos en la base de datos (utc) se preparan desde la 4am hasta las 03:59:59 del dia sgte.
    wrongMovementInfos.clear();
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-03 03:59:32", false, false)); // Fuera
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-03 04:00:00", false, false)); // Dentro, limite
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-03 07:43:54", false, false)); // Dentro
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-03 17:32:15", true,  false)); // Fuera por type
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-03 17:32:15", false, true));  // Fuera por indnorcor
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-03 21:14:09", false, false)); // Dentro
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-04 03:59:59", false, false)); // Dentro, limite
    wrongMovementInfos.add(new WrongMovementInfo("2018-08-04 04:00:01", false, false)); // Fuera

    reconciledExpectedCount = 3;
    notReconciledExpectedCount = 4;
    onlyFileMovements = 1;
  }

  @After
  public void cleanDB() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("delete from %s.prp_movimiento_investigar", SCHEMA));
  }

  @Test
  public void rendicionCargas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL,false, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.TOPUP);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.NORMAL);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber " + reconciledExpectedCount + " conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber " + notReconciledExpectedCount + " no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }

  @Test
  public void rendicionCargasNoConcilada() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL,true, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.fail("Nada debe estar conciliado");
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          boolean correctDate = includedInDates(movTmp.getFechaCreacion());
          boolean includedInFile = movementIndex < reconciledExpectedCount;
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.TOPUP);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.NORMAL);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }

  @Test
  public void rendicionCargasReversadas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA,false, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for (PrepaidMovement10 mov : movimientos) {
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.TOPUP);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.CORRECTORA);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber " + reconciledExpectedCount + " conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber " + notReconciledExpectedCount + " no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionCargasReversadasNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA,true, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.fail("Nada debe estar conciliado");
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          boolean correctDate = includedInDates(movTmp.getFechaCreacion());
          boolean includedInFile = movementIndex < reconciledExpectedCount;
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.TOPUP);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.CORRECTORA);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetiros() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,false, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.WITHDRAW);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.NORMAL);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber " + reconciledExpectedCount + " conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber " + notReconciledExpectedCount + " no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetirosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,true, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.fail("Nada debe estar conciliado");
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          boolean correctDate = includedInDates(movTmp.getFechaCreacion());
          boolean includedInFile = movementIndex < reconciledExpectedCount;
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.WITHDRAW);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.NORMAL);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetirosReversados() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,false, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", includedInDates(movTmp.getFechaCreacion()));
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.WITHDRAW);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.CORRECTORA);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber " + reconciledExpectedCount + " conciliados.", reconciledExpectedCount, reconciledCount);
    Assert.assertEquals("Debe haber " + notReconciledExpectedCount + " no conciliados.", notReconciledExpectedCount, notReconcilidedCount);
  }
  @Test
  public void rendicionRetirosReversadosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(reconciledExpectedCount, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,true, wrongMovementInfos, onlyFileMovements);
    Thread.sleep(1500);

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.fail("Nada debe estar conciliado");
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          boolean correctDate = includedInDates(movTmp.getFechaCreacion());
          boolean includedInFile = movementIndex < reconciledExpectedCount;
          Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas o estar incluidos en el archivo (ser los primeros N)", correctDate || includedInFile);
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.WITHDRAW);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.CORRECTORA);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + onlyFileMovements + " movimiento en research.", onlyFileMovements, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber " + (notReconciledExpectedCount + reconciledExpectedCount) + " no conciliados.", notReconciledExpectedCount + reconciledExpectedCount, notReconcilidedCount);
  }

  private ArrayList<PrepaidMovement10> createMovementAndFile(int cantidad, PrepaidMovementType type, IndicadorNormalCorrector indicadorNormalCorrector, Boolean withError, ArrayList<WrongMovementInfo> movementsInfo, int onlyFileMovementCount) throws Exception {

    ArrayList<PrepaidMovement10> lstPrepaidMovement10s = new ArrayList<>();
    ArrayList<PrepaidMovement10> lstPrepaidMovementInFile = new ArrayList<>();
    String fileName = null;
    String sDate = fileDate;

    int totalNumberOfMovements = cantidad + movementsInfo.size() + onlyFileMovementCount;
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

      if (i < cantidad || i >= (cantidad + onlyFileMovementCount)) {
        prepaidMovement10 = createPrepaidMovement10(prepaidMovement10); // Insert into DB
      } else {
        prepaidMovement10 = new PrepaidMovement10();
        // Creando los valores necesarios para el movimiento que se guarda en el archivo
        prepaidMovement10.setIdTxExterno(RandomStringUtils.random(10, true, true));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        prepaidMovement10.setFecfac(formatter.parse("20180913"));
        prepaidMovement10.setIdPrepaidUser(new Random().nextLong());
        prepaidMovement10.setMonto(BigDecimal.valueOf (new Random().nextLong()));
        prepaidMovement10.setId((long)new Random().nextInt());
      }

      lstPrepaidMovement10s.add(prepaidMovement10);
      if (i < cantidad + onlyFileMovementCount) {
        // No importa su fecha.
        // Sera agregado al archivo.
        // Los primeros "cantidad" seran conciliados, el resto no esta en la DB.
        lstPrepaidMovementInFile.add(prepaidMovement10);
      } else {
        // Movimientos que se setean con fecha, tipo e indnorcor especial.
        // NO se agregan al archivo
        // Pueden quedar PENDING o NOT_RECONCILED dependiendo de su fecha, tipo, indnorcor.
        WrongMovementInfo movementInfo = movementsInfo.get(i - (cantidad + onlyFileMovementCount));
        Date parsedDate = DateUtils.getInstance().dateStringToDate(movementInfo.creationDate, "yyyy-MM-dd hh:mm:ss");
        Timestamp movementTimestamp = new Timestamp(parsedDate.getTime());
        PrepaidMovementType nuevoType = setIncorrectMovementType(type, movementInfo.wrongType);
        IndicadorNormalCorrector nuevoIndice = setIncorrectIndNorCor(indicadorNormalCorrector, movementInfo.wrongIndNorCor);

        // Guardar los nuevos valores forzados
        // En la lista
        prepaidMovement10.setFechaCreacion(movementTimestamp);
        prepaidMovement10.setTipoMovimiento(nuevoType);
        prepaidMovement10.setIndnorcor(nuevoIndice);

        // Y en la BD
        changeMovement(prepaidMovement10.getId(), movementInfo.creationDate, nuevoType, nuevoIndice);
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

  private void changeMovement(Object idMovimiento, String newDate, PrepaidMovementType newType, IndicadorNormalCorrector newIndNorCor)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET "
        + "fecha_creacion = TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone, "
        + "tipo_movimiento = '" + newType.toString() + "', "
        + "indnorcor = " + newIndNorCor.getValue() + " "
        + "WHERE ID = " + idMovimiento.toString());
  }

  private PrepaidMovementType setIncorrectMovementType(PrepaidMovementType current, boolean forceError) {
    // Si hay que forzar un error, cambia el tipo de movimiento
    if (forceError) {
      if (current.equals(PrepaidMovementType.TOPUP)) {
        current = PrepaidMovementType.WITHDRAW;
      } else if (current.equals(PrepaidMovementType.WITHDRAW)) {
        current = PrepaidMovementType.TOPUP;
      }
    }
    return current;
  }

  private IndicadorNormalCorrector setIncorrectIndNorCor(IndicadorNormalCorrector current, boolean forceError) {
    // Si hay que forzar un error cambiar el indicador
    if (forceError) {
      if (current.equals(IndicadorNormalCorrector.CORRECTORA)) {
        current = IndicadorNormalCorrector.NORMAL;
      } else if (current.equals(IndicadorNormalCorrector.NORMAL)) {
        current = IndicadorNormalCorrector.CORRECTORA;
      }
    }
    return current;
  }

  private boolean includedInDates(Timestamp movTimestamp) {
    return !movTimestamp.before(this.startDateTs) && !movTimestamp.after(this.endDateTs);
  }

  private List findResearchMovements()  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    return DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento_investigar", SCHEMA));
  }

  // Clase auxiliar para determinar un movimiento a crear en la base de datos
  private class WrongMovementInfo {
    private String creationDate; // Forzar fecha de creacion
    private boolean wrongType; // Forzar tipo incorrector
    private boolean wrongIndNorCor; // Forzar indicador incorrecto

    private WrongMovementInfo(String creationDate, boolean wrongType, boolean wrongIndNorCor) {
      this.creationDate = creationDate;
      this.wrongType = wrongType;
      this.wrongIndNorCor = wrongIndNorCor;
    }
  }
}
