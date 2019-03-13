package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.DateUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.test.integration.v10.async.TestBaseUnitAsync;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

  private String fileName = "file.test";

  //TODO: Agregar test que verifique especificamente movimientos no conciliados por expiracion.

  @Before
  public void prepareDates() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", SCHEMA));

  }

  @After
  public void deleteFile() {
    try{
      Files.deleteIfExists(Paths.get(BASE_DIR + this.fileName));
    } catch (Exception e) {

    }
  }

  @AfterClass
  public static void cleanDB() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", SCHEMA));
  }

  @Test
  public void rendicionCargas() throws Exception {

    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL,false, wrongMovementInfos, 1);
    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);

    } catch (Exception e) {
      Assert.fail("Should not be here");
    }


    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.TOPUP);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.NORMAL);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber 1 movimiento en research.", 1 , lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber 6 conciliados.", 6, reconciledCount);
  }

  @Test
  public void rendicionCargasNoConcilada() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL,true, wrongMovementInfos, 1);
    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos) {
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.fail("Nada debe estar conciliado");
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          notReconcilidedCount++;
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + 1 + " movimiento en research.", 1, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
  }

  @Test
  public void rendicionCargasReversadas() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA,false, wrongMovementInfos, 1);

    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for (PrepaidMovement10 mov : movimientos) {
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          //Assert.assertTrue("Los no conciliados deben estar entre las fechas indicadas", beforeDate(movTmp.getFechaCreacion()));
          notReconcilidedCount++;
        } else {
          boolean outsideDates = !includedInDates(movTmp.getFechaCreacion());
          boolean wrongType = !movTmp.getTipoMovimiento().equals(PrepaidMovementType.TOPUP);
          boolean wrongIndNorCor = !movTmp.getIndnorcor().equals(IndicadorNormalCorrector.CORRECTORA);
          Assert.assertTrue("Los que quedaron PENDING deben estar fuera de las fechas, type incorrecto o indnorcor incorrecto", outsideDates || wrongType || wrongIndNorCor);
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + 1 + " movimiento en research.", 1, lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber 6 conciliados.", 6, reconciledCount);
  }

  @Test
  public void rendicionCargasReversadasNoConciliado() throws Exception {

    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA,true, wrongMovementInfos, 1);
   

    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }


    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    int movementIndex = 0;
    for(PrepaidMovement10 mov : movimientos) {

      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());

      if (movTmp != null) {

        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.fail("Nada debe estar conciliado");
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          notReconcilidedCount++;
        }
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber 6 no conciliados.", 6, notReconcilidedCount);
  }

  @Test
  public void rendicionRetiros() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,false, wrongMovementInfos, 1);

    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
     System.out.println("rendicionRetiros_Err: "+ e.toString());
      Assert.fail("Should not be here");
    }

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          notReconcilidedCount++;
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber " + 1 + " movimiento en research.", 1, lstResearchList.size());
      }
    }
    Assert.assertEquals("Debe haber xx conciliados.", 6, reconciledCount);
  }

  @Test
  public void rendicionRetirosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL,true, wrongMovementInfos, 1);

    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

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
          notReconcilidedCount++;
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber 1 movimiento en research.", 1, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber 6 no conciliados.", 6 , notReconcilidedCount);
  }

  @Test
  public void rendicionRetirosReversados() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,false, wrongMovementInfos, 1);

    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

    int reconciledCount = 0;
    int notReconcilidedCount = 0;
    for(PrepaidMovement10 mov : movimientos){
      PrepaidMovement10 movTmp = getPrepaidMovementEJBBean10().getPrepaidMovementById(mov.getId());
      if (movTmp != null) {
        if (movTmp.getConSwitch().equals(ReconciliationStatusType.RECONCILED)) {
          Assert.assertTrue("Conciliado OK", true);
          reconciledCount++;
        } else if (movTmp.getConSwitch().equals(ReconciliationStatusType.NOT_RECONCILED)) {
          notReconcilidedCount++;
        }
      }
    }
    Assert.assertEquals("Debe haber XX conciliados.", 6, reconciledCount);
    Assert.assertEquals("Debe haber XX no conciliados.", 0, notReconcilidedCount);
  }

  @Test
  public void rendicionRetirosReversadosNoConciliado() throws Exception {
    ArrayList<PrepaidMovement10> movimientos = createMovementAndFile(6, PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA,true, wrongMovementInfos, 1);
    try {
      InputStream is = putSuccessFileIntoSftp(this.fileName);
      // Procesa el archivo y lo guarda en la tabla.
      ReconciliationFile10 reconciliationFile10 = getMcRedReconciliationEJBBean10().processFile(is, this.fileName);
      // Procesa la tabla y concilia
      getMcRedReconciliationEJBBean10().processSwitchData(reconciliationFile10);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

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
          notReconcilidedCount++;
        }
      } else {
        List lstResearchList = findResearchMovements();
        Assert.assertEquals("Debe haber 1 movimiento en research.", 1, lstResearchList.size());
      }
      movementIndex++;
    }
    Assert.assertEquals("Debe haber 0 conciliados.", 0, reconciledCount);
    Assert.assertEquals("Debe haber 6 no conciliados.", 6, notReconcilidedCount);
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

        LocalDateTime localDateTime = LocalDateTime.parse("2018-09-13T00:00:00");
        Timestamp currentTimeStamp1 = Timestamp.valueOf(localDateTime);
        java.sql.Date currentTimeStamp2 = java.sql.Date.valueOf(localDateTime.toLocalDate());

        prepaidMovement10.setFecfac(currentTimeStamp2);
        prepaidMovement10.setFechaActualizacion(currentTimeStamp1);
        prepaidMovement10.setFechaCreacion(currentTimeStamp1);

        prepaidMovement10.setIdPrepaidUser(numberUtils.random(1L,9999999L));
        prepaidMovement10.setMonto(new BigDecimal(numberUtils.random(3000,100000)));
        prepaidMovement10.setId(numberUtils.random(3000L,100000L));
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

    this.fileName = fileName;
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
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFechaCreacion()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue())};
        }else {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFechaCreacion()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue()),String.valueOf(mov.getId())};
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
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFechaCreacion()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue()+100)};
        }else {
          data = new String[]{mov.getIdTxExterno(),String.valueOf(mov.getFechaCreacion()),String.valueOf(mov.getIdPrepaidUser()),String.valueOf(mov.getMonto().longValue()+100),String.valueOf(mov.getId())};
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
  private boolean beforeDate(Timestamp movTimestamp) {
    return movTimestamp.before(this.startDateTs);
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

  private InputStream putSuccessFileIntoSftp(String filename) throws Exception {
    return new FileInputStream(new File(BASE_DIR + filename));
  }
}
