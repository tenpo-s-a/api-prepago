package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.test.integration.v10.helper.sftp.TestTecnocomSftpServer;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 **/
public class Test_PendingTecnocomReconciliationFile10 extends TestBaseUnitAsync {
  private static Log log = LogFactory.getLog(Test_PendingTecnocomReconciliationFile10.class);


  private List<String> pans = Arrays.asList("5176081182052131", "5176081118047031", "5176081144225379");
  private List<String> contracts = Arrays.asList("09870001000000000091", "09870001000000000092", "09870001000000000093");
  private List<PrepaidUser10> users = new ArrayList<>();
  private List<PrepaidCard10> prepaidCards = new ArrayList<>();
  private static ReconciliationFile onlineFile;
  private static ReconciliationFile apiFile;

  private void clearTransactions() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
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

  @AfterClass
  public static void tearDown(){

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
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.RECONCILED, MovementOriginType.SAT);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ConciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen SAT", MovementOriginType.SAT, movement.getOriginType());
    }
  }

  @Test
  public void processOnlineExistingTransactions() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003.ONLINE";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ConciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotEquals("Debe tener nummovext", Integer.valueOf(0), movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processOnlineExistingTransactions_differentAmount() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();

      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      movement10.setMonto(movement10.getMonto().add(BigDecimal.valueOf(500)));
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003.ONLINE";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.NEED_VERIFICATION, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ConciliationStatusType.NEED_VERIFICATION, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusProcessOk() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.PROCESS_OK,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    Thread.sleep(1500);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ConciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotEquals("Debe tener nummovext", Integer.valueOf(0), movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_statusPending() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PENDING);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.PENDING,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    Thread.sleep(1500);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.NEED_VERIFICATION, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PENDING", PrepaidMovementStatus.PENDING, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NEED_VERIFICATION", ConciliationStatusType.NEED_VERIFICATION, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusInProcess() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.IN_PROCESS);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.IN_PROCESS,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.NEED_VERIFICATION, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado IN_PROCESS", PrepaidMovementStatus.IN_PROCESS, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NEED_VERIFICATION", ConciliationStatusType.NEED_VERIFICATION, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusRejected() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.REJECTED);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.REJECTED,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.NEED_VERIFICATION, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado REJECTED", PrepaidMovementStatus.REJECTED, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NEED_VERIFICATION", ConciliationStatusType.NEED_VERIFICATION, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTecnocomReintentable() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ConciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotEquals("Debe tener nummovext", Integer.valueOf(0), movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTimeoutRequest() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ConciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotEquals("Debe tener nummovext", Integer.valueOf(0), movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTimeoutResponse() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ConciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotEquals("Debe tener nummovext", Integer.valueOf(0), movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processApiTransactions_differentAmount() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();

      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ConciliationStatusType.PENDING);
      movement10.setConSwitch(ConciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      movement10.setMonto(movement10.getMonto().add(BigDecimal.valueOf(500)));
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.NEED_VERIFICATION, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NEED_VERIFICATION", ConciliationStatusType.NEED_VERIFICATION, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  private void putSuccessFileIntoSftp(String filename) throws Exception {
    try {
      final Map<String, Object> context = TestTecnocomSftpServer.getInstance().openChanel();
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/" + filename);
      ChannelSftp channelSftp = (ChannelSftp) context.get("channel");
      channelSftp.put(inputStream, TestTecnocomSftpServer.getInstance().BASE_DIR + "tecnocom/upload/" + filename);
      channelSftp.exit();
      ((Session) context.get("session")).disconnect();
      inputStream.close();
      log.info("Wait for camel process");
      Thread.sleep(3000);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }

  }
}