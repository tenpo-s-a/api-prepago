package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.test.integration.v10.async.TestBaseUnitAsync;
import org.junit.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author abarazarte
 **/
public class Test_PendingTecnocomReconciliationFile10 extends TestBaseUnitAsync {

  private List<String> pans = Arrays.asList("5176081182052131", "5176081118047031", "5176081144225379","5176081135830583","5176081111866841");
  private List<String> contracts = Arrays.asList("09870001000000000091", "09870001000000000092", "09870001000000000093","09870001000000000012","09870001000000000013");
  private List<PrepaidUser10> users = new ArrayList<>();
  private List<PrepaidCard10> prepaidCards = new ArrayList<>();
  private static TecnocomReconciliationFile onlineFile;
  private static TecnocomReconciliationFile apiFile;

  private void clearTransactions() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
  }

  private void prepareUsersAndCards() throws Exception {

    users.clear();
    prepaidCards.clear();

    for (int i = 0; i < pans.size(); i++) {
      String pan = pans.get(i);
      String processorUserId =  contracts.get(i);

      System.out.println(String.format("%s -> %s", pan, processorUserId));

      // Crea usuario
      User user = registerUser();

      // Crea usuario prepago
      PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10();
      prepaidCard10.setPan(Utils.replacePan(pan));
      prepaidCard10.setProcessorUserId(processorUserId);
      prepaidCard10 = createPrepaidCard10(prepaidCard10);

      users.add(prepaidUser10);
      prepaidCards.add(prepaidCard10);
    }
  }

  @Before
  public void beforeEach() throws Exception {
    clearTransactions();

    {
      onlineFile = null;
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003.ONLINE");
      onlineFile = TecnocomFileHelper.getInstance().validateFile(inputStream);
      inputStream.close();
    }

    {
      apiFile = null;
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003");
      apiFile= TecnocomFileHelper.getInstance().validateFile(inputStream);
      inputStream.close();
    }
    prepareUsersAndCards();
  }

  @Test
  public void processOnlineTransactions() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    final String filename = "PLJ61110.FINT0003.ONLINE";

    Long fileId = null;
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId =getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.SAT,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen SAT", MovementOriginType.SAT, movement.getOriginType());
    }

  }

  @Test
  public void processOnlineExistingTransactions() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);
    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();

      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());

    Long fileId=null;
    final String filename = "PLJ61110.FINT0003.ONLINE";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processOnlineExistingTransactions_differentAmount() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx :satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();

      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      movement10.setMonto(movement10.getMonto().add(BigDecimal.valueOf(500)));
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());

    Long fileId = null;
    final String filename = "PLJ61110.FINT0003.ONLINE";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusProcessOk() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.PROCESS_OK,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());

    Long fileId = null;
    final String filename = "PLJ61110.FINT0003";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTecnocomReintentable() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }



    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());


    Long fileId = null;

    final String filename = "PLJ61110.FINT0003";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      //Assert.fail("Should not be here");
    }
    // Se procesa lo guardado en la tabla de Tecnocom
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado ERROR_TECNOCOM_REINTENTABLE", PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTimeoutRequest() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());

    Long fileId = null;
    final String filename = "PLJ61110.FINT0003";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado ERROR_TIMEOUT_CONEXION", PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTimeoutResponse() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());

    Long fileId = null;
    final String filename = "PLJ61110.FINT0003";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado ERROR_TIMEOUT_RESPONSE", PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_differentAmount() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();

      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      movement10.setMonto(movement10.getMonto().add(BigDecimal.valueOf(500)));
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());

    Long fileId = null;
    final String filename = "PLJ61110.FINT0003";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_NotReConciled() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10));
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
    reconciliationFile10.setProcess(ReconciliationOriginType.TECNOCOM);

    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null,reconciliationFile10);

    Assert.assertNotNull("Debe ser != null", reconciliationFile10);
    Assert.assertNotEquals("Debe tener id",0L,reconciliationFile10.getId().longValue());

    // Insertar movimientos en tecnocom
    getTecnocomReconciliationEJBBean10().insertTecnocomMovement(reconciliationFile10.getId(),onlineFile.getDetails());

    // Se buscan movimientos SAT
    List<MovimientoTecnocom10> satList = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(reconciliationFile10.getId(),OriginOpeType.SAT_ORIGIN);

    // Se agregan transacciones con monto diferente
    for (MovimientoTecnocom10 trx : satList) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(encryptUtil.decrypt(trx.getPan()));
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.PENDING);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
    prepaidMovement10.setOriginType(MovementOriginType.API);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);

    String date = apiFile.getHeader().getFecenvio();
    String time = apiFile.getHeader().getHoraenvio();
    time = time.replaceAll("\\.", ":");

    System.out.println("============================================");
    System.out.println("date: " + date);
    System.out.println("time: " + time);
    System.out.println("============================================");

    String newDate = getNewDateForPastMovement(date, time, Calendar.DAY_OF_WEEK, -1);

    Map<Long, TipoFactura> inserted = new HashMap<>();

    TipoFactura type = TipoFactura.CARGA_TRANSFERENCIA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.ANULA_CARGA_TRANSFERENCIA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.RETIRO_TRANSFERENCIA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.ANULA_RETIRO_EFECTIVO_COMERCIO_MULTICJA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    type = TipoFactura.ANULA_RETIRO_TRANSFERENCIA;
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, type.getCode(), IndicadorNormalCorrector.NORMAL.getValue());
    inserted.put(prepaidMovement10.getId(), type);

    newDate = getNewDateForPastMovement(date, time, Calendar.HOUR_OF_DAY, -3);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    changeMovement(prepaidMovement10.getId(), newDate, TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.PROCESS_OK,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 25 movimientos", 25, movements.size());


    //Se eliminan movimientos de la tabla intermedia
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(reconciliationFile10.getId());


    Long fileId = null;
    final String filename = "PLJ61110.FINT0003";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    // Verifica movimientos NO conciliados
    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API,null);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 8 movimientos", 9, movements.size());



  }

  private InputStream putSuccessFileIntoSftp(String filename) throws Exception {
    return this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/" + filename);
  }

  private void changeMovement(Object idMovimiento, String newDate, Integer tipofac, Integer indnorcor)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET fecha_creacion = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS'), "
        + "indnorcor = " + indnorcor + ", "
        + "tipofac = " + tipofac + " "
        + "WHERE ID = " + idMovimiento.toString());
  }

  private String getNewDateForPastMovement(String date, String time, Integer unit, Integer amount){

    Timestamp ts = Timestamp.valueOf(String.format("%s %s", date, time));
    Calendar cal = Calendar.getInstance();
    cal.setTime(ts);
    cal.add(unit, amount);
    ts.setTime(cal.getTime().getTime());
    return ts.toString();
  }
}
