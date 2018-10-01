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
import org.apache.commons.lang3.StringUtils;
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
  private static ReconciliationFile file;

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
    file = null;
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003.ONLINE");
    file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();
    prepareUsersAndCards();
  }

  @Test
  public void processOnlineTransactions() throws Exception {
    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    final String filename = "PLJ61110.FINT0003.ONLINE";
    putSuccessFileIntoSftp(filename);

    Thread.sleep(5000);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.CONCILATE, MovementOriginType.SAT);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom CONCILATE", ConciliationStatusType.CONCILATE, movement.getConTecnocom());
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
    for (ReconciliationFileDetail trx : file.getDetails()) {

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

    Thread.sleep(5000);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.CONCILATE, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom CONCILATE", ConciliationStatusType.CONCILATE, movement.getConTecnocom());
      Assert.assertEquals("Debe tener estado conciliacion switch PENDING", ConciliationStatusType.PENDING, movement.getConSwitch());
      Assert.assertEquals("Debe tener origen API", MovementOriginType.API, movement.getOriginType());
      Assert.assertNotNull("Debe tener nummovext", movement.getNummovext());
      Assert.assertNotEquals("Debe tener nummovext", Integer.valueOf(0), movement.getNummovext());
      Assert.assertNotNull("Debe tener numextcta", movement.getNumextcta());
      Assert.assertNotEquals("Debe tener numextcta", Integer.valueOf(0), movement.getNumextcta());
    }
  }

  @Test
  public void processOnlineExistingTransactions_differetAmount() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    // Se agregan transacciones con monto diferente
    for (ReconciliationFileDetail trx : file.getDetails()) {

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

    Thread.sleep(5000);

    movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null, ConciliationStatusType.PENDING, ConciliationStatusType.NO_CONCILIATE, MovementOriginType.API);

    Assert.assertNotNull("Debe tener movimientos", movements);
    Assert.assertFalse("Debe tener movimientos", movements.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos", 16, movements.size());

    for (PrepaidMovement10 movement: movements) {
      Assert.assertEquals("Debe tener estado PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, movement.getEstado());
      Assert.assertEquals("Debe tener estado conciliacion tecnocom NO_CONCILIATE", ConciliationStatusType.NO_CONCILIATE, movement.getConTecnocom());
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
