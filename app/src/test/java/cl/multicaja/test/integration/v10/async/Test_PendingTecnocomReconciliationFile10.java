package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.test.integration.v10.helper.sftp.TestTecnocomSftpServer;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author abarazarte
 **/
@Ignore
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.SAT);

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

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : onlineFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003.ONLINE";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003.ONLINE";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API);

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

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    Thread.sleep(1500);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
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
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.PENDING);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.PENDING,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    Thread.sleep(1500);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PENDING", PrepaidMovementStatus.PENDING, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusInProcess() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.IN_PROCESS);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.IN_PROCESS,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado IN_PROCESS", PrepaidMovementStatus.IN_PROCESS, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusRejected() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
      PrepaidMovement10 movement10 = TecnocomFileHelper.getInstance().buildMovement(userId, pan, trx);
      movement10.setConTecnocom(ReconciliationStatusType.PENDING);
      movement10.setConSwitch(ReconciliationStatusType.PENDING);
      movement10.setOriginType(MovementOriginType.API);
      movement10.setEstado(PrepaidMovementStatus.REJECTED);
      movement10.setIdMovimientoRef(Long.valueOf(0));
      movement10.setIdTxExterno("");
      getPrepaidMovementEJBBean10().addPrepaidMovement(null, movement10);

    }

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.REJECTED,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado REJECTED", PrepaidMovementStatus.REJECTED, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
    }
  }

  @Test
  public void processApiTransactions_statusErrorTecnocomReintentable() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado ERROR_TECNOCOM_REINTENTABLE", PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
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
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado ERROR_TIMEOUT_CONEXION", PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
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
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado ERROR_TIMEOUT_RESPONSE", PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom RECONCILED", ReconciliationStatusType.RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
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
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();

      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API);

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

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : apiFile.getDetails()) {

      Long userId = prepaidCards.stream()
        .filter(card -> trx.getContrato().equals(card.getProcessorUserId()))
        .findAny()
        .get()
        .getIdUser();


      String pan = Utils.replacePan(trx.getPan());
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
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 25 movimientos", 25, movements.size());


    final String filename = "PLJ61110.FINT0003";
    putSuccessFileIntoSftp(filename);

    Thread.sleep(1500);

    // Verifica movimientos NO conciliados

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.NOT_RECONCILED, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 8 movimientos", 8, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ReconciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertEquals("Debe ser tipofac " + inserted.get(movement.getId()), inserted.get(movement.getId()).getCode(), movement.getTipofac().getCode());
    }

    // Verifica movimientos pendientes de conciliar

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, PrepaidMovementStatus.PROCESS_OK,
      null, null, null, null, null, null, ReconciliationStatusType.PENDING, ReconciliationStatusType.PENDING, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 1 movimiento", 1, movements.size());
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
