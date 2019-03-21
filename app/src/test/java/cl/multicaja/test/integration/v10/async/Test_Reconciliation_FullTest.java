package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.EncryptUtil;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.Ticket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.TicketType;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.TicketsResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.*;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class Test_Reconciliation_FullTest extends TestBaseUnitAsync {

  static User user;
  static UserAccount userAccount;
  static PrepaidUser10 prepaidUser;
  static PrepaidCard10 prepaidCard;
  static ReconciliationFile10 topupReconciliationFile10;
  static ReconciliationFile10 topupReverseReconciliationFile10;
  static ReconciliationFile10 withdrawReconciliationFile10;
  static ReconciliationFile10 withdrawReverseReconciliationFile10;
  static ReconciliationFile10 tecnocomReconciliationFile10;

  @BeforeClass
  public static void prepareDB() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_switch CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_switch_hist CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));

    try {
      Test_Reconciliation_FullTest test = new Test_Reconciliation_FullTest();

      user = test.registerUser();
      userAccount = test.createBankAccount(user);
      prepaidUser = test.buildPrepaidUser10(user);
      prepaidUser = test.createPrepaidUser10(prepaidUser);
      prepaidCard = test.buildPrepaidCard10FromTecnocom(user, prepaidUser);
      prepaidCard = test.createPrepaidCard10(prepaidCard);

      String contrato = prepaidCard.getProcessorUserId();
      String nomcomred = "Multi test";
      String pan = EncryptUtil.getInstance().decrypt(prepaidCard.getEncryptedPan());

      PrepaidTopup10 prepaidTopup = test.buildPrepaidTopup10(user);
      prepaidTopup.getAmount().setValue(new BigDecimal(25000));
      prepaidTopup.setMerchantCode("758374665428219");
      prepaidTopup.setFirstTopup(false);
      CdtTransaction10 cdtTransaction = test.buildCdtTransaction10(user, prepaidTopup);
      PrepaidMovement10 prepaidMovement = test.buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction, PrepaidMovementType.TOPUP);
      prepaidMovement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement.setNumaut(getRandomNumericString(6));
      prepaidMovement.setFechaCreacion(Timestamp.from(Instant.now()));
      prepaidMovement.setMonto(prepaidTopup.getAmount().getValue());
      prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
      prepaidMovement.setEstadoNegocio(BusinessStatusType.CONFIRMED);
      prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement.setConTecnocom(ReconciliationStatusType.RECONCILED);

      cdtTransaction = test.createCdtTransaction10(cdtTransaction);
      prepaidMovement.setIdMovimientoRef(cdtTransaction.getTransactionReference());
      prepaidMovement = test.createPrepaidMovement10(prepaidMovement);

      TecnocomServiceHelper.getInstance().topup(contrato, pan, nomcomred, prepaidMovement);

      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

      ReconciliationFile10 newReconciliationFile10 = new ReconciliationFile10();
      newReconciliationFile10.setStatus(FileStatus.OK);
      newReconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      newReconciliationFile10.setFileName("archivo_topup.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      topupReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_topup_reverse.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      topupReverseReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_withdraw.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      withdrawReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_withdraw_reverse.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      withdrawReverseReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_tecnocom.txt");
      newReconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      tecnocomReconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      Thread.sleep(10);

      // Insertar un archivo extra de cada tipo para expirar movimientos
      newReconciliationFile10.setFileName("archivo_topup_2.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_topup_reverse_2.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_withdraw_2.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_withdraw_reverse_2.txt");
      newReconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

      newReconciliationFile10.setFileName("archivo_tecnocom_2.txt");
      newReconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, newReconciliationFile10);

    } catch (Exception e) {
      Assert.fail("Error al crear el usuario y su tarjeta");
    }
  }

  @AfterClass
  public static void clearDB() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_switch CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_switch_hist CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
  }

  @Override
  @After
  public void after() {

  }

  /*
  @Test
  public void case1_topup_pos_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case1_topup_web_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, NewPrepaidTopup10.WEB_MERCHANT_CODE, IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case1_withdraw_pos_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case1_withdraw_web_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, NewPrepaidTopup10.WEB_MERCHANT_CODE, IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // No debe tocarlo, no se concilian los retiros webs
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case1_topup_reverse_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case1_withdraw_reverse_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case1_purchase_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.PURCHASE, "871237987123897", IndicadorNormalCorrector.NORMAL, null, tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // No debe tocarlo, no se concilian los purchases
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.INITIAL);
    assertReconciled(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case1_suscription_BD_ok_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.SUSCRIPTION, "871237987123897", IndicadorNormalCorrector.NORMAL, null, tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setConSwitch(ReconciliationStatusType.RECONCILED);
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // No debe tocarlo, no se concilian los suscription
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.INITIAL);
    assertReconciled(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case2_topup_BD_ok_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case2_topup_BD_ok_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null; // No viene en el archivo switch
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case2_withdraw_pos_BD_ok_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case2_withdraw_pos_BD_ok_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case2_topup_reverse_BD_ok_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case2_topup_reverse_BD_ok_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case2_withdraw_reverse_BD_ok_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case2_withdraw_reverse_BD_ok_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case3_topup_BD_ok_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR);
  }

  @Test
  public void case3_withdraw_BD_ok_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR);
  }

  @Test
  public void case3_topup_reverse_BD_ok_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR);
  }

  @Test
  public void case3_withdraw_reverse_BD_ok_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR);
  }

  @Test
  public void case4_topup_BD_ok_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_topup_BD_ok_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_withdraw_BD_ok_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_withdraw_BD_ok_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_topup_reverse_BD_ok_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_topup_reverse_BD_ok_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_withdraw_reverse_BD_ok_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case4_withdraw_reverse_BD_ok_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.IN_PROCESS, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case5_topup_pos_BD_error_tc_reintentable_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case5_topup_pos_BD_error_timeout_conexion_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case5_topup_pos_BD_error_timeout_response_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.PENDING);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  @Test
  public void case5_topup_reverse_pos_BD_error_tc_reintentable_SW_ok_TC_ok() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setIdTxExterno(setupData.prepaidMovement.getIdTxExterno());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement.setMcCode(testData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    PrepaidMovement10 setupMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(setupData.prepaidMovement.getId());
    Assert.assertEquals("Debe haber cambiado a estado reversado", BusinessStatusType.REVERSED, setupMovement.getEstadoNegocio());
  }

  @Test
  public void case5_topup_reverse_pos_BD_error_timeout_conexion_SW_ok_TC_ok() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setIdTxExterno(setupData.prepaidMovement.getIdTxExterno());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement.setMcCode(testData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    PrepaidMovement10 setupMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(setupData.prepaidMovement.getId());
    Assert.assertEquals("Debe haber cambiado a estado reversado", BusinessStatusType.REVERSED, setupMovement.getEstadoNegocio());
  }

  @Test
  public void case5_topup_reverse_pos_BD_error_timeout_response_SW_ok_TC_ok() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setIdTxExterno(setupData.prepaidMovement.getIdTxExterno());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement.setMcCode(testData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    PrepaidMovement10 setupMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(setupData.prepaidMovement.getId());
    Assert.assertEquals("Debe haber cambiado a estado reversado", BusinessStatusType.REVERSED, setupMovement.getEstadoNegocio());
  }

  @Test
  public void case5_withdraw_reverse_pos_BD_error_tc_reintentable_SW_ok_TC_ok() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setIdTxExterno(setupData.prepaidMovement.getIdTxExterno());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement.setMcCode(testData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    PrepaidMovement10 setupMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(setupData.prepaidMovement.getId());
    Assert.assertEquals("Debe haber cambiado a estado reversado", BusinessStatusType.REVERSED, setupMovement.getEstadoNegocio());
  }

  @Test
  public void case5_withdraw_reverse_pos_BD_error_timeout_conexion_SW_ok_TC_ok() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setIdTxExterno(setupData.prepaidMovement.getIdTxExterno());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement.setMcCode(testData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    PrepaidMovement10 setupMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(setupData.prepaidMovement.getId());
    Assert.assertEquals("Debe haber cambiado a estado reversado", BusinessStatusType.REVERSED, setupMovement.getEstadoNegocio());
  }

  @Test
  public void case5_withdraw_reverse_pos_BD_error_timeout_response_SW_ok_TC_ok() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setIdTxExterno(setupData.prepaidMovement.getIdTxExterno());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement.setMcCode(testData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

    PrepaidMovement10 setupMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(setupData.prepaidMovement.getId());
    Assert.assertEquals("Debe haber cambiado a estado reversado", BusinessStatusType.REVERSED, setupMovement.getEstadoNegocio());
  }

  @Test
  public void case6_topup_pos_BD_error_tc_reintentable_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case6_topup_pos_BD_error_timeout_conexion_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case6_topup_pos_BD_error_timeout_response_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case6_topup_pos_BD_error_tc_reintentable_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case6_topup_pos_BD_error_timeout_conexion_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case6_topup_pos_BD_error_timeout_response_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case6_topup_reverse_pos_BD_error_tc_reintentable_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_topup_reverse_pos_BD_error_timeout_conexion_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_topup_reverse_pos_BD_error_timeout_response_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_topup_reverse_pos_BD_error_tc_reintentable_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_topup_reverse_pos_BD_error_timeout_conexion_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_topup_reverse_pos_BD_error_timeout_response_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_withdraw_reverse_pos_BD_error_tc_reintentable_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_withdraw_reverse_pos_BD_error_timeout_conexion_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_withdraw_reverse_pos_BD_error_timeout_response_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_withdraw_reverse_pos_BD_error_tc_reintentable_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_withdraw_reverse_pos_BD_error_timeout_conexion_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }

  @Test
  public void case6_withdraw_reverse_pos_BD_error_timeout_response_SW_WrongAmount_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Hay que esperar que exista un nuevo movimiento original
    PrepaidMovement10 counterMovement = waitForExists(String.format("MC_%s", testData.prepaidMovement.getIdTxExterno()), testData.prepaidMovement.getTipoMovimiento(), testData.prepaidMovement.getIndnorcor() == IndicadorNormalCorrector.NORMAL ? IndicadorNormalCorrector.CORRECTORA : IndicadorNormalCorrector.NORMAL);
    // Con sus accounting datas
    AccountingData10 accountingData = waitForAccountingToExist(counterMovement.getId());
    ClearingData10 clearingData = waitForClearingToExist(accountingData.getId());

    // La reversa original debe quedar confirmada
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

    // Data contable debe estar pendiente e inicializada
    assertAccountingMovement(counterMovement.getId(), true, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
    assertClearingMovement(clearingData.getId(), true, AccountingStatusType.INITIAL);
  }
  */
  @Test
  public void case7_withdraw_pos_BD_error_tc_reintentable_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case7_withdraw_pos_BD_error_timeout_conexion_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);
  }

  @Test
  public void case7_withdraw_pos_BD_error_timeout_response_SW_Expired_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Tiene que existir la reversa
    PrepaidMovement10 foundReverse = waitForReverse(testData.prepaidMovement.getId());

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.REVERSED, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);
  }
  /*
  @Test
  public void case8_withdraw_pos_BD_error_tc_reintentable_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case8_withdraw_pos_BD_error_timeout_conexion_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case8_withdraw_pos_BD_error_timeout_response_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    // Estado de negocio debe cambiar a reversed
    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case9_topup_BD_error_tc_reintentable_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    waitForExists(testData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, PrepaidMovementStatus.PROCESS_OK);
  }

  @Test
  public void case9_topup_BD_error_timeout_conexion_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    waitForExists(testData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, PrepaidMovementStatus.PROCESS_OK);
  }

  @Test
  public void case9_topup_BD_error_timeout_response_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    waitForExists(testData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, PrepaidMovementStatus.PROCESS_OK);
  }

  @Test
  public void case9_topup_reverse_BD_error_tc_reintentable_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    waitForExists(testData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, PrepaidMovementStatus.PROCESS_OK);
  }

  @Test
  public void case9_topup_reverse_BD_error_timeout_conexion_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    waitForExists(testData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, PrepaidMovementStatus.PROCESS_OK);
  }

  @Test
  public void case9_topup_reverse_BD_error_timeout_response_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    waitForExists(testData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, PrepaidMovementStatus.PROCESS_OK);
  }

  @Test
  public void case10_topup_BD_error_tc_reintentable_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_tc_reintentable_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_tc_reintentable_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_tc_reintentable_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_conexion_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_conexion_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_conexion_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_conexion_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_response_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_response_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_response_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case10_topup_BD_error_timeout_response_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PROCESS_OK, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  // TODO: falta caso 10, topup_reverse

  @Test
  public void case11_withdraw_pos_BD_error_tc_reintentable_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case11_withdraw_pos_BD_error_tc_reintentable_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case11_withdraw_pos_BD_error_timeout_conexion_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case11_withdraw_pos_BD_error_timeout_conexion_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case11_withdraw_pos_BD_error_timeout_response_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case11_withdraw_pos_BD_error_timeout_response_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case12_withdraw_pos_BD_error_tc_reintentable_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_tc_reintentable_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_tc_reintentable_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_tc_reintentable_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_conexion_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_conexion_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_conexion_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_conexion_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_response_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_response_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_response_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case12_withdraw_pos_BD_error_timeout_response_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.NOT_EXECUTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
    assertResearch(testData.prepaidMovement.getId(), false);
  }

  @Test
  public void case13_topup_BD_rejected_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.TO_REFUND, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REFUND, ReconciliationStatusType.TO_REFUND);

    // Debe haberse confirmado el movimiento original en el cdt
    CdtTransaction10 foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, testData.prepaidMovement.getIdTxExterno(), CdtTransactionType.CARGA_POS_CONF);
    Assert.assertNotNull("Debe existir la confirmacion de carga", foundCdtTransaction);

    // Debe haberse iniciado una reversa en el cdt
    foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, testData.prepaidMovement.getIdTxExterno(), CdtTransactionType.REVERSA_CARGA);
    Assert.assertNotNull("Debe existir la reversa de carga", foundCdtTransaction);

    // Revisar si existse el ticket en freshdesk
    assertTicket(testData.prepaidMovement.getId(), TicketType.DEVOLUCION);
  }

  @Test
  public void case13_topup_BD_rejected_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.TO_REFUND, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.REFUND, ReconciliationStatusType.TO_REFUND);

    // Debe haberse confirmado el movimiento original en el cdt
    CdtTransaction10 foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, testData.prepaidMovement.getIdTxExterno(), CdtTransactionType.CARGA_POS_CONF);
    Assert.assertNotNull("Debe existir la confirmacion de carga", foundCdtTransaction);

    // Debe haberse iniciado una reversa en el cdt
    foundCdtTransaction = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, testData.prepaidMovement.getIdTxExterno(), CdtTransactionType.REVERSA_CARGA);
    Assert.assertNotNull("Debe existir la reversa de carga", foundCdtTransaction);

    // Revisar si existse el ticket en freshdesk
    assertTicket(testData.prepaidMovement.getId(), TicketType.DEVOLUCION);
  }

  @Test
  public void case14_topup_BD_rejected_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case14_topup_BD_rejected_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case14_topup_BD_rejected_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case14_topup_BD_rejected_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR);
  }

  @Test
  public void case15_withdraw_BD_rejected_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR);
  }

  @Test
  public void case15_withdraw_BD_rejected_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR);
  }

  @Test
  public void case16_withdraw_BD_rejected_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
  }

  @Test
  public void case16_withdraw_BD_rejected_SW_WrongAmount_TC_WExpired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement = null;
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
  }

  @Test
  public void case16_withdraw_BD_rejected_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
  }

  @Test
  public void case16_withdraw_BD_rejected_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.REJECTED);
    testData.tecnocomMovement = null;
    testData.switchMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.REJECTED, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.NOT_OK);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.NOT_SEND);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
  }

  @Test
  public void case17_topup_BD_no_SW_any_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    String researchId = String.format("ExtId:[%s]", testData.tecnocomMovement.getNumAut());
    assertResearch(researchId, true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB);
  }

  @Test
  public void case17_withdraw_BD_no_SW_any_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    String researchId = String.format("ExtId:[%s]", testData.tecnocomMovement.getNumAut());
    assertResearch(researchId, true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB);
  }

  @Test
  public void case17_topup_BD_no_SW_ok_TC_any() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);

    String researchId = String.format("ExtId:[%s]-McCode:[%s]", testData.switchMovement.getExternalId().toString(), testData.switchMovement.getMcCode());
    assertResearch(researchId, true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB);
  }

  @Test
  public void case17_withdraw_BD_no_SW_ok_TC_any() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);

    String researchId = String.format("ExtId:[%s]-McCode:[%s]", testData.switchMovement.getExternalId().toString(), testData.switchMovement.getMcCode());
    assertResearch(researchId, true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB);
  }

  @Test
  public void case18_topup_reverse_BD_no_SW_any_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    String researchId = String.format("ExtId:[%s]", testData.tecnocomMovement.getNumAut());
    assertResearch(researchId, true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB);
  }

  @Test
  public void case18_withdraw_reverse_BD_no_SW_any_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData = createTestData(testData);

    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());

    String researchId = String.format("ExtId:[%s]", testData.tecnocomMovement.getNumAut());
    assertResearch(researchId, true, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_DB);
  }

  @Test
  public void case18_topup_reverse_BD_no_SW_ok_TC_any() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData.switchMovement.setMcCode(setupData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);

    // Esperar a que exista el movimiento en la bd
    waitForExists(setupData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA);
  }

  @Test
  public void case18_withdraw_reverse_BD_no_SW_ok_TC_any() throws Exception {
    TestData setupData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    setupData = createTestData(setupData);

    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement = null;
    testData.switchMovement.setMcCode(setupData.prepaidMovement.getIdTxExterno());
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);

    // Esperar a que exista el movimiento en la bd
    waitForExists(setupData.prepaidMovement.getIdTxExterno(), PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA);
  }

  @Test
  public void case20_topup_BD_pending_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case20_withdraw_BD_pending_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case20_topup_reverse_BD_pending_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case20_withdraw_reverse_BD_pending_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case21_topup_BD_in_process_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case21_withdraw_BD_in_process_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case21_topup_reverse_BD_in_process_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case21_withdraw_reverse_BD_in_process_SW_ok_TC_ok() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_BD_pending_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_BD_pending_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_BD_pending_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_BD_pending_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_BD_pending_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_BD_pending_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_BD_pending_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_BD_pending_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_reverse_BD_pending_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_reverse_BD_pending_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_reverse_BD_pending_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_topup_reverse_BD_pending_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_reverse_BD_pending_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_reverse_BD_pending_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_reverse_BD_pending_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case22_withdraw_reverse_BD_pending_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_BD_in_process_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_BD_in_process_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_BD_in_process_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_BD_in_process_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_BD_in_process_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_BD_in_process_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_BD_in_process_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_BD_in_process_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_reverse_BD_in_process_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_reverse_BD_in_process_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_reverse_BD_in_process_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_topup_reverse_BD_in_process_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_reverse_BD_in_process_SW_WrongAmount_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_reverse_BD_in_process_SW_WrongAmount_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement.setAmount(testData.switchMovement.getAmount().add(new BigDecimal(1L)));
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_reverse_BD_in_process_SW_Expired_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case23_withdraw_reverse_BD_in_process_SW_Expired_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.switchMovement = null;
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.NOT_RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_topup_BD_pending_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_topup_BD_pending_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_withdraw_BD_pending_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_withdraw_BD_pending_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_topup_reverse_BD_pending_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_topup_reverse_BD_pending_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_withdraw_reverse_BD_pending_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case24_withdraw_reverse_BD_pending_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.PENDING, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_topup_BD_in_process_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_topup_BD_in_process_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.NORMAL, topupReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_withdraw_BD_in_process_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_withdraw_BD_in_process_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.NORMAL, withdrawReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertAccountingMovement(testData.prepaidMovement.getId(), true, AccountingStatusType.NOT_SEND, AccountingStatusType.RESEARCH);
    assertClearingMovement(testData.clearingData.getId(), true, AccountingStatusType.RESEARCH);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_topup_reverse_BD_in_process_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_topup_reverse_BD_in_process_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.TOPUP, "871237987123897", IndicadorNormalCorrector.CORRECTORA, topupReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(topupReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_withdraw_reverse_BD_in_process_SW_ok_TC_WrongAmount() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement.getImpFac().setValue(testData.tecnocomMovement.getImpFac().getValue().add(new BigDecimal(1L)));
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  @Test
  public void case25_withdraw_reverse_BD_in_process_SW_ok_TC_Expired() throws Exception {
    TestData testData = prepareTestData(PrepaidMovementType.WITHDRAW, "871237987123897", IndicadorNormalCorrector.CORRECTORA, withdrawReverseReconciliationFile10.getId(), tecnocomReconciliationFile10.getId());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.IN_PROCESS);
    testData.tecnocomMovement = null;
    testData = createTestData(testData);

    getMcRedReconciliationEJBBean10().processSwitchData(withdrawReverseReconciliationFile10);
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(tecnocomReconciliationFile10.getId());
    getPrepaidMovementEJBBean10().processReconciliationRules();

    assertPrepaidMovement(testData.prepaidMovement.getId(), true, PrepaidMovementStatus.IN_PROCESS, BusinessStatusType.OK, ReconciliationStatusType.RECONCILED, ReconciliationStatusType.NOT_RECONCILED);
    assertReconciled(testData.prepaidMovement.getId(), true, ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    assertResearch(testData.prepaidMovement.getId(), true, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }
  */

  ClearingData10 waitForClearingToExist(Long accountingId) throws Exception {
    ClearingData10 foundClearing = null;
    for(int i = 0; i < 50; i++) {
      // Esperar que el async ejecute la reversa
      foundClearing = getPrepaidClearingEJBBean10().searchClearingDataByAccountingId(null, accountingId);
      if(foundClearing != null) {
        return foundClearing;
      }
      System.out.println("Buscando...");
      Thread.sleep(100);
    }
    Assert.fail("No se encontro el accounting data esperado");
    return null;
  }

  AccountingData10 waitForAccountingToExist(Long movementId) throws Exception {
    AccountingData10 foundAccounting = null;
    for(int i = 0; i < 50; i++) {
      // Esperar que el async ejecute la reversa
      foundAccounting = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, movementId);
      if(foundAccounting != null) {
        return foundAccounting;
      }
      System.out.println("Buscando...");
      Thread.sleep(100);
    }
    Assert.fail("No se encontro el accounting data esperado");
    return null;
  }

  PrepaidMovement10 waitForExists(String externalId, PrepaidMovementType prepaidMovementType, IndicadorNormalCorrector indicadorNormalCorrector) throws Exception {
    return waitForExists(externalId, prepaidMovementType, indicadorNormalCorrector, null);
  }

  PrepaidMovement10 waitForExists(String externalId, PrepaidMovementType prepaidMovementType, IndicadorNormalCorrector indicadorNormalCorrector, PrepaidMovementStatus movementStatus) throws Exception {
    PrepaidMovement10 foundMovement = null;
    for(int i = 0; i < 50; i++) {
      // Esperar que el async ejecute la reversa
      foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(externalId, prepaidMovementType, indicadorNormalCorrector);
      if(foundMovement != null) {
        if(movementStatus == null || movementStatus.equals(foundMovement.getEstado())) {
          return foundMovement;
        }
      }
      System.out.println("Buscando...");
      Thread.sleep(100);
    }
    Assert.fail("No se encontro el movimiento esperado");
    return null;
  }

  PrepaidMovement10 waitForReverse(Long movementId) throws Exception {
    PrepaidMovement10 foundMovement = null;
    for(int i = 0; i < 50; i++) {
      // Esperar que el async ejecute la reversa
      foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(movementId);
      if(foundMovement != null && BusinessStatusType.REVERSED.equals(foundMovement.getEstadoNegocio())) {
        break;
      }
      System.out.println("Buscando...");
      Thread.sleep(100);
    }
    Assert.assertNotNull("Debe estar encontrado", foundMovement);
    Assert.assertEquals("Debe estar en estado reversado", BusinessStatusType.REVERSED, foundMovement.getEstadoNegocio());
    return foundMovement;
  }

  void assertTicket(Long movementId, TicketType ticketType) throws Exception {
    // Todo: como validar la existencia del ticket?
    /*
    LocalDateTime beginDate = LocalDateTime.now(ZoneId.of("UTC"));
    beginDate.minusMinutes(5);
    LocalDateTime endDate = beginDate.plusMinutes(5);
    TicketsResponse ticketsResponse = UserClient.getInstance().getFreshdeskTicketsByTypeAndCreatedDate(null, 0, ticketType, beginDate, endDate);
    Assert.assertTrue("Debe tener al menos un ticket", ticketsResponse.getTotal() > 0);

    boolean found = false;
    List<Ticket> ticketList = ticketsResponse.getResults();
    for(Ticket ticket : ticketList) {
      Map<String, Object> customFields = ticket.getCustomFields();
      Long ticketMovementId = NumberUtils.getInstance().toLong(customFields.get("cf_id_movimiento"));
      if(ticketMovementId.equals(movementId)) {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Debe encontrar el ticket de devolucion", found);
    */
  }

  private void assertResearch(Long movementId, Boolean exists) {
    assertResearch(movementId, exists, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB);
  }

  private void assertResearch(Long movementId, Boolean exists, ResearchMovementResponsibleStatusType responsible, ResearchMovementDescriptionType description) {
    ResearchMovement10 researchMovement = getResearchMovement(movementId);
    assertResearch(researchMovement, exists, responsible, description);
  }

  private void assertResearch(String idArchivoOrigen, Boolean exists, ResearchMovementResponsibleStatusType responsible, ResearchMovementDescriptionType description) {
    ResearchMovement10 researchMovement = getResearchMovement(idArchivoOrigen);
    assertResearch(researchMovement, exists, responsible, description);
  }

  private void assertResearch(ResearchMovement10 researchMovement, Boolean exists, ResearchMovementResponsibleStatusType responsible, ResearchMovementDescriptionType description) {
    if(!exists) {
      Assert.assertNull("No debe existir", researchMovement);
      return;
    }
    Assert.assertNotNull("Debe existir", researchMovement);
    Assert.assertEquals("Su responsable debe ser " + responsible.toString(), responsible, researchMovement.getResponsible());
    Assert.assertEquals("Su descripcion debe ser " + description.toString(), description, researchMovement.getDescription());
  }

  ResearchMovement10 getResearchMovement(Long movementId) {
    RowMapper rowMapper = (rs, rowNum) -> {
      ResearchMovement10 reconciliedResearch = new ResearchMovement10();
      reconciliedResearch.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedResearch.setMovRef(numberUtils.toLong(rs.getLong("mov_ref")));
      reconciliedResearch.setResponsible(ResearchMovementResponsibleStatusType.fromValue(String.valueOf(rs.getString("responsable"))));
      reconciliedResearch.setDescription(ResearchMovementDescriptionType.fromValue(String.valueOf(rs.getString("descripcion"))));
      return reconciliedResearch;
    };
    List<ResearchMovement10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where mov_ref = %d", getSchema(), movementId), rowMapper);
    return data.isEmpty() ? null : data.get(0);
  }

  ResearchMovement10 getResearchMovement(String idArchivoOrigen) {
    RowMapper rowMapper = (rs, rowNum) -> {
      ResearchMovement10 reconciliedResearch = new ResearchMovement10();
      reconciliedResearch.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedResearch.setMovRef(numberUtils.toLong(rs.getLong("mov_ref")));
      reconciliedResearch.setResponsible(ResearchMovementResponsibleStatusType.fromValue(String.valueOf(rs.getString("responsable"))));
      reconciliedResearch.setDescription(ResearchMovementDescriptionType.fromValue(String.valueOf(rs.getString("descripcion"))));
      return reconciliedResearch;
    };
    List<ResearchMovement10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where id_archivo_origen = '%s'", getSchema(), idArchivoOrigen), rowMapper);
    return data.isEmpty() ? null : data.get(0);
  }

  void assertReconciled(Long movementId, Boolean exists) throws BaseException, SQLException {
    assertReconciled(movementId, exists, ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
  }

  void assertReconciled(Long movementId, Boolean exists, ReconciliationActionType reconciliationAction, ReconciliationStatusType reconciliationStatus) throws BaseException, SQLException {
    ReconciliedMovement10 reconciliedMovement = getPrepaidMovementEJBBean10().getReconciliedMovementByIdMovRef(movementId);
    if(!exists) {
      Assert.assertNull("No debe existir", reconciliedMovement);
      return;
    }
    Assert.assertNotNull("Debe existir", reconciliedMovement);
    Assert.assertEquals("Su accion debe ser " + reconciliationAction.toString(), reconciliationAction, reconciliedMovement.getActionType());
    Assert.assertEquals("Su reconciliation status debe ser " + reconciliationStatus.toString(), reconciliationStatus, reconciliedMovement.getReconciliationStatusType());
  }

  void assertPrepaidMovement(Long movementId, boolean exists, PrepaidMovementStatus status, BusinessStatusType businessStatus, ReconciliationStatusType switchStatus, ReconciliationStatusType tecnocomStatus) throws Exception {
    PrepaidMovement10 prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(movementId);
    if(!exists) {
      Assert.assertNull("No debe existir", prepaidMovement10);
      return;
    }
    Assert.assertNotNull("Debe existir", prepaidMovement10);
    Assert.assertEquals("Debe tener estado " + status.toString(), status, prepaidMovement10.getEstado());
    Assert.assertEquals("Debe tener estado de negocio " + businessStatus.toString(), businessStatus, prepaidMovement10.getEstadoNegocio());
    Assert.assertEquals("Debe tener estado con switch " + switchStatus.toString(), switchStatus, prepaidMovement10.getConSwitch());
    Assert.assertEquals("Debe tener estado con tecnocom " + tecnocomStatus.toString(), tecnocomStatus, prepaidMovement10.getConTecnocom());
  }

  void assertAccountingMovement(Long movementId, boolean exists, AccountingStatusType status, AccountingStatusType accountingStatus) throws Exception {
    AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, movementId);
    if(!exists) {
      Assert.assertNull("No debe existir", accountingData10);
      return;
    }
    Assert.assertNotNull("Debe existir", accountingData10);
    Assert.assertEquals("Debe tener status " + status.toString(), status, accountingData10.getStatus());
    Assert.assertEquals("Debe tener accounting status " + accountingStatus.toString(), accountingStatus, accountingData10.getAccountingStatus());
  }

  void assertClearingMovement(Long clearingId, boolean exists, AccountingStatusType status) throws Exception {
    ClearingData10 clearingData10 = getPrepaidClearingEJBBean10().searchClearingDataById(null, clearingId);
    if(!exists) {
      Assert.assertNull("No debe existir", clearingData10);
      return;
    }
    Assert.assertNotNull("Debe existir", clearingData10);
    Assert.assertEquals("Debe tener status " + status.toString(), status, clearingData10.getStatus());
  }

  ClearingData10 createClearingData(AccountingData10 accountingData10, AccountingStatusType status) throws Exception {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(status);
    clearing10.setUserBankAccount(userAccount);
    clearing10.setAccountingId(accountingData10.getId());
    return clearing10;
  }

  AccountingData10 createAccountingData(PrepaidMovement10 prepaidMovement10) throws Exception {
    PrepaidAccountingMovement prepaidAccountingMovement = new PrepaidAccountingMovement();
    prepaidAccountingMovement.setPrepaidMovement10(prepaidMovement10);
    LocalDate localDate = LocalDate.now(ZoneId.systemDefault()).plusYears(100);
    prepaidAccountingMovement.setReconciliationDate(Timestamp.valueOf(localDate.atStartOfDay()));
    return getPrepaidAccountingEJBBean10().buildAccounting10(prepaidAccountingMovement, AccountingStatusType.PENDING, AccountingStatusType.PENDING);
  }

  McRedReconciliationFileDetail createSwitchMovement(Long fileId, PrepaidMovement10 prepaidMovement10) {
    McRedReconciliationFileDetail registroSwitch = new McRedReconciliationFileDetail();
    registroSwitch.setMcCode(prepaidMovement10.getIdTxExterno());
    registroSwitch.setClientId(user.getId()); //prepaidMovement10.getIdPrepaidUser());
    registroSwitch.setExternalId(0L);
    registroSwitch.setDateTrx(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
    registroSwitch.setFileId(fileId);
    registroSwitch.setAmount(prepaidMovement10.getMonto());
    return registroSwitch;
  }

  MovimientoTecnocom10 createMovimientoTecnocom(Long fileId, PrepaidMovement10 prepaidMovement10) {
    MovimientoTecnocom10 registroTecnocom = new MovimientoTecnocom10();
    registroTecnocom.setIdArchivo(fileId);
    registroTecnocom.setNumAut(prepaidMovement10.getNumaut());
    registroTecnocom.setTipoFac(prepaidMovement10.getTipofac());
    registroTecnocom.setIndNorCor(prepaidMovement10.getIndnorcor().getValue());
    registroTecnocom.setPan(prepaidCard.getEncryptedPan());
    registroTecnocom.setCentAlta(prepaidMovement10.getCentalta());
    registroTecnocom.setClamone(CodigoMoneda.fromValue(prepaidMovement10.getClamone()));
    registroTecnocom.setCmbApli(new BigDecimal(prepaidMovement10.getCmbapli()));
    registroTecnocom.setCodAct(prepaidMovement10.getCodact());
    registroTecnocom.setCodCom(prepaidMovement10.getCodcom());
    registroTecnocom.setCodEnt(prepaidMovement10.getCodent());
    registroTecnocom.setCodPais(prepaidMovement10.getCodpais().getValue());
    registroTecnocom.setContrato(prepaidCard.getProcessorUserId());
    registroTecnocom.setCuenta(prepaidMovement10.getCuenta());
    registroTecnocom.setFecFac(new Date(prepaidMovement10.getFecfac().getTime()).toLocalDate());
    registroTecnocom.setFecTrn(prepaidMovement10.getFechaCreacion());
    registroTecnocom.setImpautcon(new NewAmountAndCurrency10());
    registroTecnocom.setImpDiv(new NewAmountAndCurrency10(prepaidMovement10.getImpdiv(), prepaidMovement10.getClamon()));
    registroTecnocom.setImpFac(new NewAmountAndCurrency10(prepaidMovement10.getImpfac(), prepaidMovement10.getClamon()));
    registroTecnocom.setImpLiq(new NewAmountAndCurrency10(prepaidMovement10.getImpliq(), prepaidMovement10.getClamon()));
    registroTecnocom.setIndProaje(prepaidMovement10.getIndproaje().getValue());
    registroTecnocom.setLinRef(prepaidMovement10.getLinref());
    registroTecnocom.setNomPob(prepaidMovement10.getNompob());
    registroTecnocom.setNumExtCta(new Long(prepaidMovement10.getNumextcta()));
    registroTecnocom.setNumMovExt(new Long(prepaidMovement10.getNummovext()));
    registroTecnocom.setNumRefFac(prepaidMovement10.getNumreffac());
    registroTecnocom.setOriginOpe(OriginOpeType.API_ORIGIN.getValue());
    registroTecnocom.setTipoLin(prepaidMovement10.getTipolin());
    return registroTecnocom;
  }

  TestData prepareTestData(PrepaidMovementType movementType, String merchantCode, IndicadorNormalCorrector indnorcor, Long switchFileId, Long tecnocomFileId) throws Exception {
    TestData testData = new TestData();

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setMerchantCode(merchantCode);
    prepaidTopup.setFirstTopup(false);
    if(PrepaidMovementType.TOPUP.equals(movementType)) {
      testData.cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    } else {
      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setAmount(prepaidTopup.getAmount());
      prepaidWithdraw.setMerchantCode(prepaidTopup.getMerchantCode());
      prepaidWithdraw.setTransactionId(prepaidTopup.getTransactionId());
      testData.cdtTransaction = buildCdtTransaction10(user, prepaidWithdraw);
    }

    testData.prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, testData.cdtTransaction, movementType);
    testData.prepaidMovement.setIndnorcor(indnorcor);
    testData.prepaidMovement.setNumaut(getRandomNumericString(6));
    testData.prepaidMovement.setFechaCreacion(Timestamp.from(Instant.now()));
    testData.prepaidMovement.setMonto(prepaidTopup.getAmount().getValue());
    testData.prepaidMovement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    testData.prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);

    if(switchFileId != null) {
      testData.switchMovement = createSwitchMovement(switchFileId, testData.prepaidMovement);
    } else {
      testData.switchMovement = null;
    }

    if(tecnocomFileId != null) {
      testData.tecnocomMovement = createMovimientoTecnocom(tecnocomFileId, testData.prepaidMovement);
    } else {
      testData.tecnocomMovement = null;
    }

    if(testData.prepaidMovement != null) {
      testData.accountingData = IndicadorNormalCorrector.NORMAL.equals(indnorcor) ? createAccountingData(testData.prepaidMovement) : null;
    } else {
      testData.accountingData = null;
    }

    if(testData.prepaidMovement != null) {
      AccountingStatusType clearingStatus = TipoFactura.RETIRO_TRANSFERENCIA.equals(testData.prepaidMovement.getTipofac()) ? AccountingStatusType.PENDING : AccountingStatusType.INITIAL;
      testData.clearingData = IndicadorNormalCorrector.NORMAL.equals(indnorcor) ? createClearingData(testData.accountingData, clearingStatus) : null;
    } else {
      testData.clearingData = null;
    }

    return testData;
  }

  TestData createTestData(TestData preparedData) throws Exception {
    if(preparedData.cdtTransaction != null) {
      preparedData.cdtTransaction = createCdtTransaction10(preparedData.cdtTransaction);
      if (preparedData.prepaidMovement != null) {
        preparedData.prepaidMovement.setIdMovimientoRef(preparedData.cdtTransaction.getTransactionReference());
      }
    }

    if(preparedData.prepaidMovement != null) {
      preparedData.prepaidMovement = createPrepaidMovement10(preparedData.prepaidMovement);
    }

    if(preparedData.switchMovement != null) {
      preparedData.switchMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, preparedData.switchMovement);
    }

    if(preparedData.tecnocomMovement != null) {
      preparedData.tecnocomMovement = getTecnocomReconciliationEJBBean10().insertaMovimientoTecnocom(preparedData.tecnocomMovement);
    }

    if(preparedData.accountingData != null && preparedData.prepaidMovement != null) {
      preparedData.accountingData.setIdTransaction(preparedData.prepaidMovement.getId());
      preparedData.accountingData = getPrepaidAccountingEJBBean10().saveAccountingData(null, preparedData.accountingData);
    }

    if(preparedData.clearingData != null && preparedData.prepaidMovement != null) {
      preparedData.clearingData.setAccountingId(preparedData.accountingData.getId());
      preparedData.clearingData = getPrepaidClearingEJBBean10().insertClearingData(null, preparedData.clearingData);
    }

    return preparedData;
  }

  class TestData {
    PrepaidMovement10 prepaidMovement;
    CdtTransaction10 cdtTransaction;
    McRedReconciliationFileDetail switchMovement;
    MovimientoTecnocom10 tecnocomMovement;
    AccountingData10 accountingData;
    ClearingData10 clearingData;
  }

}
