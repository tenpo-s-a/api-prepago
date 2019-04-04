package cl.multicaja.test.integration.v10.api;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserStatus;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

/**
 * @author abarazarte
 */
public class Test_withdrawUserBalance_v10 extends TestBaseUnitApi {

  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private HttpResponse withdrawUserBalance(NewPrepaidWithdraw10 newPrepaidWithdraw10) {
    HttpResponse respHttp = apiPOST("/1.0/prepaid/withdrawal", toJson(newPrepaidWithdraw10));
    System.out.println("respHttp: " + respHttp);
    return respHttp;
  }

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn201_OnPosWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(1000);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 2 movimientos de account", 2, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(withdraw.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", accounting10);
      Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_POS, accounting10.getType());
      Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", withdraw.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", dbPrepaidMovement.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", dbPrepaidMovement.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 2 movimientos de clearing", 2, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", clearing10);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn201_OnPosWithdraw_merchantCode_5() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    String merchantCode = getRandomNumericString(5);
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, merchantCode);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());
    Assert.assertEquals("El merchant code debe estar completado con 0", "0000000000" + merchantCode, dbPrepaidMovement.getCodcom());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(1000);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 2 movimientos de account", 2, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(withdraw.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", accounting10);
      Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_POS, accounting10.getType());
      Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", withdraw.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", dbPrepaidMovement.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", dbPrepaidMovement.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 2 movimientos de clearing", 2, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", clearing10);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }


  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn201_OnPosWithdraw_merchantCode_18() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    String merchantCode = getRandomNumericString(15);
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, "000" + merchantCode);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());
    Assert.assertEquals("Debe tener el merchantCode truncado con los ultimos 15 digitos", merchantCode, dbPrepaidMovement.getCodcom());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(1000);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 2 movimientos de account", 2, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(withdraw.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", accounting10);
      Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_POS, accounting10.getType());
      Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", withdraw.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", dbPrepaidMovement.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", dbPrepaidMovement.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 2 movimientos de clearing", 2, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", clearing10);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }


  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn201_OnWebWithdraw() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.IN_PROCESS, BusinessStatusType.IN_PROCESS, dbPrepaidMovement.getEstadoNegocio());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(500);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 2 movimientos de account", 2, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(withdraw.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", accounting10);
      Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
      Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", withdraw.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", dbPrepaidMovement.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", dbPrepaidMovement.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 1 movimientos de clearing", 1, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.get(0);
      Assert.assertNotNull("deberia tener un retiro", clearing10);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", prepaidWithdraw.getBankAccountId(), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado PENDING", AccountingStatusType.PENDING, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_OnWithdraw_MinAmount() throws Exception {
    // POS
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108303", 108303, errorObj.get("code"));
    }

    //WEB
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108303", 108303, errorObj.get("code"));
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_OnWithdraw_MaxAmount() throws Exception {
    // POS
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(101585));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108302", 108302, errorObj.get("code"));
    }

    //WEB
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500101));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 108301", 108301, errorObj.get("code"));
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_OnWithdraw_InsufficientFounds() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = createPrepaidCard10(buildPrepaidCard10FromTecnocom(user, prepaidUser));


    InclusionMovimientosDTO mov =  topupInTecnocom(prepaidCard, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password);
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(10000));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106001", 106001, errorObj.get("code"));

    // Verifica la transaccion
    PrepaidMovement10 movement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.ERROR_POS_WITHDRAW,
      PrepaidMovementStatus.ERROR_WEB_WITHDRAW,
      PrepaidMovementStatus.REJECTED);

    Assert.assertNotNull("Debe existir un movimiento", movement);
    Assert.assertEquals("Debe tener el mismo idTxExterno", prepaidWithdraw.getTransactionId(), movement.getIdTxExterno());
    Assert.assertEquals("Debe estar en status " + PrepaidMovementStatus.REJECTED, PrepaidMovementStatus.REJECTED, movement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.REJECTED, BusinessStatusType.REJECTED, movement.getEstadoNegocio());
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingBody() {

    HttpResponse resp = withdrawUserBalance(null);
    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingRut() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingTransactionId() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingMerchantCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingAmount() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingAmountCurrencyCode() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(new BigDecimal("9999.90"));
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingAmountValue() {

    NewPrepaidWithdraw10 prepaidWithdraw = new NewPrepaidWithdraw10();
    prepaidWithdraw.setTransactionId("123456789");
    prepaidWithdraw.setRut(11111111);
    prepaidWithdraw.setMerchantCode("987654321");
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    prepaidWithdraw.setAmount(amount);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMissingPassword() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null, null, getRandomNumericString(15));
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(null);
    prepaidWithdraw.setMerchantCode(getUniqueLong().toString());

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 101004", 101004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn400_OnMerchantCodeFormat() throws Exception {

    User user = registerUser();

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(RandomStringUtils.randomNumeric(4));
    prepaidWithdraw.setMerchantCode(getRandomString(10));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 400", 400, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", PARAMETRO_NO_CUMPLE_FORMATO_$VALUE.getValue(), errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn404_McUserNull() throws Exception {

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(null, null, getRandomNumericString(15));
    prepaidWithdraw.setRut(getUniqueRutNumber());
    prepaidWithdraw.setPassword(RandomStringUtils.randomNumeric(4));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102001", 102001, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_McUserListaNegra() throws Exception {

    User user = registerUserBlackListed();
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102016", 102016, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn404_PrepaidUserNull() throws Exception {

    User user = registerUser();
    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 404", 404, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_PrepaidUserDisabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser.setStatus(PrepaidUserStatus.DISABLED);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "1245", getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102004", 102004, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_InvalidPassword() throws Exception {

    User user = registerUser();

    user = updateUserPassword(user, "1234");

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, "4321", getRandomNumericString(15));
    prepaidWithdraw.setRut(user.getRut().getValue());
    prepaidWithdraw.setMerchantCode(getUniqueLong().toString());

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102053", 102053, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_PrepaidCardNull() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 102003", 102003, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_PrepaidCardPending() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.PENDING);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_PrepaidCardExpired() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_PrepaidCardHardLocked() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaiduser = buildPrepaidUser10(user);
    prepaiduser = createPrepaidUser10(prepaiduser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaiduser);
    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());

    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106000", 106000, errorObj.get("code"));
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_TecnocomError_UserDoesntExists() throws Exception {

    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    createPrepaidCard10(buildPrepaidCard10(prepaidUser));

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 422", 422, resp.getStatus());
    Map<String, Object> errorObj = resp.toMap();
    Assert.assertNotNull("Deberia tener error", errorObj);
    Assert.assertEquals("Deberia tener error code = 106001", 106001, errorObj.get("code"));

    // Verifica la transaccion
    PrepaidMovement10 movement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(),
      PrepaidMovementStatus.ERROR_POS_WITHDRAW,
      PrepaidMovementStatus.ERROR_WEB_WITHDRAW,
      PrepaidMovementStatus.REJECTED);

    Assert.assertNotNull("Debe existir un movimiento", movement);
    Assert.assertEquals("Debe tener el mismo idTxExterno", prepaidWithdraw.getTransactionId(), movement.getIdTxExterno());
    Assert.assertEquals("Debe estar en status " + PrepaidMovementStatus.REJECTED, PrepaidMovementStatus.REJECTED, movement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.REJECTED, BusinessStatusType.REJECTED, movement.getEstadoNegocio());
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_OnWithdraw_Reversed() throws Exception {
    // POS
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10(prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 130005", REVERSA_MOVIMIENTO_REVERSADO.getValue(), errorObj.get("code"));

      List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.NORMAL, TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movements);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movements.size());
      PrepaidMovement10 prepaidMovement10 = movements.get(0);

      Assert.assertEquals("Debe tener el mismo id externo", prepaidWithdraw.getTransactionId(), prepaidMovement10.getIdTxExterno());
      Assert.assertEquals("Debe tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());
      Assert.assertEquals("Debe tener businessStatus REVERSED", BusinessStatusType.REVERSED, prepaidMovement10.getEstadoNegocio());
      Assert.assertEquals("Debe tener conTecnocom RECONCILIED", ReconciliationStatusType.RECONCILED, prepaidMovement10.getConTecnocom());
    }

    // WEB
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      createPrepaidCard10(buildPrepaidCard10(prepaidUser));

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
      prepaidMovement = createPrepaidMovement10(prepaidMovement);

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 422", 422, resp.getStatus());
      Map<String, Object> errorObj = resp.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj);
      Assert.assertEquals("Deberia tener error code = 130005", REVERSA_MOVIMIENTO_REVERSADO.getValue(), errorObj.get("code"));

      List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null,
        prepaidUser.getId(), prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.NORMAL, TipoFactura.RETIRO_TRANSFERENCIA, null, null);

      Assert.assertNotNull("Debe tener 1 movimiento de reversa", movements);
      Assert.assertEquals("Debe tener 1 movimiento de reversa", 1, movements.size());
      PrepaidMovement10 prepaidMovement10 = movements.get(0);

      Assert.assertEquals("Debe tener el mismo id externo", prepaidWithdraw.getTransactionId(), prepaidMovement10.getIdTxExterno());
      Assert.assertEquals("Debe tener status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, prepaidMovement10.getEstado());
      Assert.assertEquals("Debe tener businessStatus REVERSED", BusinessStatusType.REVERSED, prepaidMovement10.getEstadoNegocio());
      Assert.assertEquals("Debe tener conTecnocom RECONCILIED", ReconciliationStatusType.RECONCILED, prepaidMovement10.getConTecnocom());
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn422_OnWithdraw_AlreadyReceived() throws Exception {
    // POS
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      // se hace una carga
      topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

      PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
      Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

      Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
      Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
      Assert.assertNotNull("Deberia tener id", withdraw.getId());
      Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
      Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
      Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
      Assert.assertNull("No deberia tener rut", withdraw.getRut());
      Assert.assertNull("No deberia tener password", withdraw.getPassword());

      // Segunda vez
      HttpResponse resp1 = withdrawUserBalance(prepaidWithdraw);
      Assert.assertEquals("status 422", 422, resp1.getStatus());
      Map<String, Object> errorObj1 = resp1.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj1);
      Assert.assertEquals("Deberia tener error code = 108000", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), errorObj1.get("code"));
      Assert.assertTrue("Deberia tener error message = Transacción duplicada", errorObj1.get("message").toString().contains("Transacción duplicada"));


    }

    // WEB
    {
      String password = RandomStringUtils.randomNumeric(4);
      User user = registerUser();
      user = updateUserPassword(user, password);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

      prepaidUser = createPrepaidUser10(prepaidUser);

      // se hace una carga
      topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

      PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
      Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

      NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

      HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

      Assert.assertEquals("status 201", 201, resp.getStatus());

      PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

      Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
      Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
      Assert.assertNotNull("Deberia tener id", withdraw.getId());
      Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
      Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
      Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
      Assert.assertNull("No deberia tener rut", withdraw.getRut());
      Assert.assertNull("No deberia tener password", withdraw.getPassword());

      // Segunda vez
      HttpResponse resp1 = withdrawUserBalance(prepaidWithdraw);
      Assert.assertEquals("status 422", 422, resp1.getStatus());
      Map<String, Object> errorObj1 = resp1.toMap();
      Assert.assertNotNull("Deberia tener error", errorObj1);
      Assert.assertEquals("Deberia tener error code = 108000", TRANSACCION_ERROR_GENERICO_$VALUE.getValue(), errorObj1.get("code"));
      Assert.assertTrue("Deberia tener error message = Transacción duplicada", errorObj1.get("message").toString().contains("Transacción duplicada"));
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn201_OnWithdraw_Reversed_DifferentAmount_POS() throws Exception {
    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, getRandomNumericString(15));

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement.setImpfac(prepaidMovement.getImpfac().add(BigDecimal.TEN));
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.CONFIRMED, BusinessStatusType.CONFIRMED, dbPrepaidMovement.getEstadoNegocio());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(1000);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 2 movimientos de account", 2, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(withdraw.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", accounting10);
      Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_POS, accounting10.getType());
      Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_POS, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", withdraw.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", dbPrepaidMovement.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", dbPrepaidMovement.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 2 movimientos de clearing", 2, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", clearing10);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }
  //TODO: Verificar si esto se seguira usando
  @Ignore
  @Test
  public void shouldReturn201_OnWithdraw_Reversed_DifferentAmount_WEB() throws Exception {
    String password = RandomStringUtils.randomNumeric(4);
    User user = registerUser();
    user = updateUserPassword(user, password);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    // se hace una carga
    topupUserBalance(prepaidUser.getUuid(), BigDecimal.valueOf(10000));

    PrepaidCard10 prepaidCard = waitForLastPrepaidCardInStatus(prepaidUser, PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("Deberia tener una tarjeta", prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user, password, NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    prepaidMovement.setImpfac(prepaidMovement.getImpfac().add(BigDecimal.TEN));
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    HttpResponse resp = withdrawUserBalance(prepaidWithdraw);

    Assert.assertEquals("status 201", 201, resp.getStatus());

    PrepaidWithdraw10 withdraw = resp.toObject(PrepaidWithdraw10.class);

    Assert.assertNotNull("Deberia ser un PrepaidWithdraw10",withdraw);
    Assert.assertNotNull("Deberia tener timestamps", withdraw.getTimestamps());
    Assert.assertNotNull("Deberia tener id", withdraw.getId());
    Assert.assertNotNull("Deberia tener userId", withdraw.getUserId());
    Assert.assertFalse("Deberia tener status", StringUtils.isBlank(withdraw.getStatus()));
    Assert.assertEquals("Deberia tener status = exitoso", "exitoso", withdraw.getStatus());
    Assert.assertNull("No deberia tener rut", withdraw.getRut());
    Assert.assertNull("No deberia tener password", withdraw.getPassword());

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertEquals("Deberia tener el data", 2, withdraw.getMcVoucherData().size());

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));

    Map<String, String> rutData = withdraw.getMcVoucherData().get(1);
    Assert.assertNotNull("Deberia tener data", rutData);

    Assert.assertTrue("Deberia tener el atributo name", rutData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", rutData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = rut","rut", rutData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", rutData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", rutData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value", RutUtils.getInstance().format(prepaidWithdraw.getRut(), null), rutData.get("value"));

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser.getId(), PrepaidMovementStatus.PROCESS_OK);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia estar en estado negocio " + BusinessStatusType.IN_PROCESS, BusinessStatusType.IN_PROCESS, dbPrepaidMovement.getEstadoNegocio());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(1000);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 2 movimientos de account", 2, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(withdraw.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", accounting10);
      Assert.assertEquals("Debe tener tipo WEB", AccountingTxType.RETIRO_WEB, accounting10.getType());
      Assert.assertEquals("Debe tener acc movement type WEB", AccountingMovementType.RETIRO_WEB, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", withdraw.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", dbPrepaidMovement.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", dbPrepaidMovement.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.PENDING, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 1 movimientos de clearing", 1, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
      Assert.assertNotNull("deberia tener un retiro", clearing10);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertNotEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado PENDING", AccountingStatusType.PENDING, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }

}
