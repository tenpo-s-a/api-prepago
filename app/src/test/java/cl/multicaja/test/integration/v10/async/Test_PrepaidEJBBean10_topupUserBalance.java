package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSMessenger;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.users.model.NameStatus;
import cl.multicaja.prepaid.helpers.users.model.RutStatus;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.helpers.users.model.UserIdentityStatus;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_topupUserBalance extends TestBaseUnitAsync {

  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static TecnocomServiceHelper tc;

  @BeforeClass
  public static void getTecnocomInstance(){
    tc = TecnocomServiceHelper.getInstance();
  }

  @AfterClass
  public static void disableAutomaticErrorInTecnocom(){
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.%s cascade", getSchema(), "prp_cuenta"));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
  }

  @Test
  public void topupUserBalance_not_ok_by_user_not_found() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    newPrepaidTopup.setRut(1);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario", CLIENTE_NO_EXISTE.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidUser_not_found() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario prepago", CLIENTE_NO_TIENE_PREPAGO.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidUser_disabled() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException nfex) {
      Assert.assertEquals("el usuario prepago esta bloqueado", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidCard_locked_hard() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.LOCKED_HARD);

    prepaidCard = createPrepaidCard10(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidCard_expired() throws Exception {

    User user = registerUser();

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10(prepaidUser);

    prepaidCard.setStatus(PrepaidCardStatus.EXPIRED);

    prepaidCard = createPrepaidCard10(prepaidCard);

    try {

      getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_validate_cdt() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    //se debe establecer la primera carga mayor a 3000 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(BigDecimal.valueOf(numberUtils.random(3119, 10000)));

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    if (CamelFactory.getInstance().isCamelRunning()) {
      Assert.assertNotNull("Debe tener messageId dado que camel si se encuentra en ejecucion", messageId);

      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());

      Assert.assertNotNull("debe tener un objeto de cdt", remoteTopup.getData().getCdtTransaction10());
      Assert.assertNotNull("debe tener un id de cdt", remoteTopup.getData().getCdtTransaction10().getExternalTransactionId());

      waitForAccountingToExist(prepaidTopup.getId());

    } else {
      Assert.assertNull("No debe tener messageId dado que camel no se encuentra en ejecucion", messageId);
    }
  }

  @Test
  public void topupUserBalance_validate_prepaidMovement() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10(user);

    //se debe establecer la primera carga mayor a 3000 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(BigDecimal.valueOf(numberUtils.random(4000, 10000)));

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null, newPrepaidTopup,true);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    if (CamelFactory.getInstance().isCamelRunning()) {
      Assert.assertNotNull("Debe tener messageId dado que camel si se encuentra en ejecucion", messageId);

      Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
      ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());

      Assert.assertNotNull("debe tener un objeto de prepaidMovement", remoteTopup.getData().getPrepaidMovement10());
      Assert.assertTrue("debe tener un id de prepaidMovement", remoteTopup.getData().getPrepaidMovement10().getId() > 0);

      waitForAccountingToExist(prepaidTopup.getId());

    } else {
      Assert.assertNull("No debe tener messageId dado que camel no se encuentra en ejecucion", messageId);
    }
  }

  @Test
  public void topupUserBalance_ok_first_topup_true_by_level_1() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertTrue("debe ser primera carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision(119) - comision de apertura (990))", 3000L, prepaidBalance10.getBalance().getValue().longValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision(0) - comision de apertura (990))", 3119L, prepaidBalance10.getBalance().getValue().longValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

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

    AccountingTxType txType = AccountingTxType.CARGA_POS;
    AccountingMovementType movementType = AccountingMovementType.CARGA_POS;
    if(TransactionOriginType.WEB.equals(prepaidTopup10.getTransactionOriginType())) {
      txType = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertTrue("Debe haber al menos 1 movimiento de account", accounting10s.size() >= 1);

      AccountingData10 accounting10 = accounting10s.stream().filter(a -> a.getIdTransaction().equals(topup.getId())).findFirst().get();
      Assert.assertEquals(String.format("Debe tener tipo %s", prepaidTopup10.getTransactionOriginType()), txType, accounting10.getType());
      Assert.assertEquals(String.format("Debe tener acc movement type %s", prepaidTopup10.getTransactionOriginType()), movementType, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", topup.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", topup.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", topup.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertTrue("Debe haber al menos 1 movimiento de clearing", clearing10s.size() >= 1);

      ClearingData10 clearing10 = clearing10s.stream().filter(c -> c.getAccountingId().equals(accounting10.getId())).findFirst().get();
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }

  @Test
  public void topupUserBalance_ok_first_topup_false_by_level_2() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (119) - comision de apertura (0))", BigDecimal.valueOf(3000), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision (0) - comision de apertura (0))", BigDecimal.valueOf(3119), prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

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

    AccountingTxType txType = AccountingTxType.CARGA_POS;
    AccountingMovementType movementType = AccountingMovementType.CARGA_POS;
    if(TransactionOriginType.WEB.equals(prepaidTopup10.getTransactionOriginType())) {
      txType = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de account", 1, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.get(0);
      Assert.assertEquals(String.format("Debe tener tipo %s", prepaidTopup10.getTransactionOriginType()), txType, accounting10.getType());
      Assert.assertEquals(String.format("Debe tener acc movement type %s", prepaidTopup10.getTransactionOriginType()), movementType, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", topup.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", topup.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", topup.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de clearing", 1, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.get(0);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_min() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }

    user.setNameStatus(NameStatus.VERIFIED);

    updateUser(user);

    //enesima carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_max_level_1() throws Exception {

    User user = registerUser();

    user.setNameStatus(NameStatus.UNVERIFIED);

    updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_max_level_2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(500001));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga web", Integer.valueOf(108201), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(100600));

      try {

        getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga pos", Integer.valueOf(108202), vex.getCode());
      }
    }
  }

  // Desde la 2da carga en adelante la carga se hace de manera sincrona
  @Test
  public void topupUserBalance_sync() throws Exception {

    Map<Long, PrepaidMovement10> movements = new HashMap<>();

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (119) - comision de apertura (0))", BigDecimal.valueOf(3000), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision (0) - comision de apertura (0))", BigDecimal.valueOf(3119), prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    movements.put(topup.getId(), topup);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, resp.getMessageId());

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", resp.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());

    Thread.sleep(2000);

    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10(user);

      PrepaidTopup10 resp2 = getPrepaidEJBBean10().topupUserBalance(null, secondTopup,true);

      Assert.assertNotNull("debe tener un id", resp2.getId());
      Assert.assertFalse("debe ser enesima carga", resp2.isFirstTopup());

      Map<String, Object> headers = new HashMap<>();
      headers.put("forceRefreshBalance", Boolean.TRUE);

      PrepaidBalance10 prepaidBalance2 = getPrepaidUserEJBBean10().getPrepaidUserBalance(headers, user.getId());

      Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance2.getBalance().getValue().longValue() > prepaidBalance10.getBalance().getValue().longValue()  );

      PrepaidMovement10 topup2 = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp2.getId());
      Assert.assertNotNull("debe tener un movimiento", topup);
      Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
      Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup2.getEstadoNegocio());

      movements.put(topup2.getId(), topup2);

      Queue qResp2 = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);
      ExchangeData<PrepaidTopupData10> remoteTopup2 = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(1000l, 1000l).getMessage(qResp2, resp2.getMessageId());

      Assert.assertNull("No Deberia existir un topup en la cola", remoteTopup2);
    }

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
      Assert.assertTrue("Debe haber al menos 2 movimientos de accounting", accounting10s.size() >= 2);

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertTrue("Debe haber al menos 2 movimientos de clearing", clearing10s.size() >= 2);

      movements.forEach((k,v)->{
        AccountingData10 acc = accounting10s.stream().filter(a -> a.getIdTransaction().equals(k)).findFirst().get();
        Assert.assertEquals("Debe tener el mismo imp fac", v.getImpfac().stripTrailingZeros(), acc.getAmount().getValue().stripTrailingZeros());
        Assert.assertEquals("debe tener la misma fecha de transaccion", v.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), acc.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

        ClearingData10 cle = clearing10s.stream().filter(c -> c.getAccountingId().equals(acc.getId())).findFirst().get();
        Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), cle.getUserBankAccount().getId());
        Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, cle.getStatus());
      });
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }
  }

  @Test
  public void topupUserBalance_sync_rejected() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor",  prepaidBalance10.getBalance().getValue().longValue() > 0  );

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, resp.getMessageId());

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", resp.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());


    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10(user);
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      PrepaidTopup10 resp2 = getPrepaidEJBBean10().topupUserBalance(null, secondTopup,true);

      Assert.assertNotNull("debe tener un id", resp2.getId());
      Assert.assertFalse("debe ser enesima carga", resp2.isFirstTopup());

      Map<String, Object> headers = new HashMap<>();
      headers.put("forceRefreshBalance", Boolean.TRUE);

      PrepaidBalance10 prepaidBalance2 = getPrepaidUserEJBBean10().getPrepaidUserBalance(headers, user.getId());

      Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance2.getBalance().getValue().longValue() > prepaidBalance10.getBalance().getValue().longValue()  );

      PrepaidMovement10 topup2 = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
      Assert.assertNotNull("debe tener un movimiento", topup);
      Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
      Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup2.getEstadoNegocio());

      Queue qResp2 = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);
      ExchangeData<PrepaidTopupData10> remoteTopup2 = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(1000l, 1000l).getMessage(qResp2, resp2.getMessageId());

      Assert.assertNull("No Deberia existir un topup en la cola", remoteTopup2);
    }

    // Tercera carga
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10(user);
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        getPrepaidEJBBean10().topupUserBalance(null, secondTopup,true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser10.getId(), PrepaidMovementStatus.REJECTED);
        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.REJECTED, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.REJECTED, topup2.getEstadoNegocio());
      }
    }
  }

  @Test
  public void topupUserBalance_sync_timeoutResponse() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10, true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance10.getBalance().getValue().longValue() > 0);

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, resp.getMessageId());

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", resp.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1020);
    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10(user);
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        getPrepaidEJBBean10().topupUserBalance(null, secondTopup, true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        tc.getTecnocomService().setAutomaticError(false);
        tc.getTecnocomService().setRetorno(null);
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        String messageId = rvex.getData()[1].getValue().toString();

        // primer intento
        {
          Queue qResp2 = camelFactory.createJMSQueue(PENDING_REVERSAL_TOPUP_RESP);
          ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp2, messageId);

          Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

          PrepaidMovement10 reverseMovement = remoteReverse.getData().getPrepaidMovementReverse();
          Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", reverseMovement);
          Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PENDING, reverseMovement.getEstado());
          Assert.assertEquals("El movimiento debe ser procesado", BusinessStatusType.IN_PROCESS, reverseMovement.getEstadoNegocio());
          Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNumextcta());
          Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getNummovext());
          Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), reverseMovement.getClamone());
        }

        // Segunda vez
        {
          //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
          Queue qResp3 = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
          ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp3, messageId);

          Assert.assertNotNull("Deberia existir un topup", remoteReverse);
          Assert.assertNotNull("Deberia existir un topup", remoteReverse.getData());
          Assert.assertEquals("Cantidad de intentos: ",2,remoteReverse.getRetryCount());

          Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", secondTopup.getTransactionId(), remoteReverse.getData().getPrepaidTopup10().getTransactionId());
          Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteReverse.getData().getPrepaidUser10().getId());
          Assert.assertNotNull("Deberia tener una PrepaidCard", remoteReverse.getData().getPrepaidCard10());

          PrepaidMovement10 prepaidMovementReverseResp = remoteReverse.getData().getPrepaidMovementReverse();
          Thread.sleep(2000);
          Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
          Assert.assertFalse("Deberia contener una codent", StringUtils.isAllBlank(prepaidMovementReverseResp.getCodent()));
          Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
          Assert.assertEquals("El movimiento debe ser procesado exitosamente", BusinessStatusType.CONFIRMED, prepaidMovementReverseResp.getEstadoNegocio());

        }

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(secondTopup.getTransactionId(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL);

        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> IN_PROCESS", BusinessStatusType.REVERSED, topup2.getEstadoNegocio());


        PrepaidMovement10 topupReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(secondTopup.getTransactionId(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA);

        Assert.assertNotNull("debe tener un movimiento", topupReverse);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topupReverse.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topupReverse.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topupReverse.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topupReverse.getEstadoNegocio());
      }
    }
  }

  @Test
  public void topupUserBalance_sync_timeoutRequest() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor",  prepaidBalance10.getBalance().getValue().longValue() > 0  );

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger().getMessage(qResp, resp.getMessageId());

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", resp.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser10.getId(), remoteTopup.getData().getPrepaidUser10().getId());

    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10(user);
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        tc.getTecnocomService().setAutomaticError(true);
        tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
        getPrepaidEJBBean10().topupUserBalance(null, secondTopup,true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        tc.getTecnocomService().setAutomaticError(true);
        tc.getTecnocomService().setRetorno(null);
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean10().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser10.getId(), PrepaidMovementStatus.REJECTED);
        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.REJECTED, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.REJECTED, topup2.getEstadoNegocio());
      }
    }
  }

  @Test
  public void topupUserBalance_cardCreated_event() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (119) - comision de apertura (0))", BigDecimal.valueOf(3000), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision (0) - comision de apertura (0))", BigDecimal.valueOf(3119), prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

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

    AccountingTxType txType = AccountingTxType.CARGA_POS;
    AccountingMovementType movementType = AccountingMovementType.CARGA_POS;
    if(TransactionOriginType.WEB.equals(prepaidTopup10.getTransactionOriginType())) {
      txType = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de account", 1, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.get(0);
      Assert.assertEquals(String.format("Debe tener tipo %s", prepaidTopup10.getTransactionOriginType()), txType, accounting10.getType());
      Assert.assertEquals(String.format("Debe tener acc movement type %s", prepaidTopup10.getTransactionOriginType()), movementType, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", topup.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", topup.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", topup.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de clearing", 1, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.get(0);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.CARD_CREATED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, prepaidCard10.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo id", prepaidCard10.getUuid(), cardEvent.getCard().getId());
    Assert.assertFalse("Debe tener accountId", StringUtils.isBlank(cardEvent.getAccountId()));
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc().toString(), cardEvent.getUserId());
    Assert.assertEquals("Debe tener el mismo pan", prepaidCard10.getPan(), cardEvent.getCard().getPan());
  }

  @Test
  public void topupUserBalance_accountCreated_event() throws Exception {

    User user = registerUser();
    user.setNameStatus(NameStatus.VERIFIED);
    user.getRut().setStatus(RutStatus.VERIFIED);
    user = updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3119));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatus(prepaidUser10, PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, user.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (119) - comision de apertura (0))", BigDecimal.valueOf(3000), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3119 pesos (carga inicial - comision (0) - comision de apertura (0))", BigDecimal.valueOf(3119), prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean10().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

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

    AccountingTxType txType = AccountingTxType.CARGA_POS;
    AccountingMovementType movementType = AccountingMovementType.CARGA_POS;
    if(TransactionOriginType.WEB.equals(prepaidTopup10.getTransactionOriginType())) {
      txType = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de account", 1, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.get(0);
      Assert.assertEquals(String.format("Debe tener tipo %s", prepaidTopup10.getTransactionOriginType()), txType, accounting10.getType());
      Assert.assertEquals(String.format("Debe tener acc movement type %s", prepaidTopup10.getTransactionOriginType()), movementType, accounting10.getAccountingMovementType());
      Assert.assertEquals("Debe tener el mismo imp fac", topup.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
      Assert.assertEquals("Debe tener el mismo id", topup.getId(), accounting10.getIdTransaction());
      Assert.assertEquals("debe tener la misma fecha de transaccion", topup.getFechaCreacion().toLocalDateTime().format(dateTimeFormatter), accounting10.getTransactionDate().toLocalDateTime().format(dateTimeFormatter));

      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      Assert.assertNotNull("No debe ser null", clearing10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de clearing", 1, clearing10s.size());

      ClearingData10 clearing10 = clearing10s.get(0);
      Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
      Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
      Assert.assertEquals("Debe estar en estado INITIAL", AccountingStatusType.INITIAL, clearing10.getStatus());
    } else {
      Assert.fail("No debe caer aqui. No encontro los datos en accounting y clearing");
    }

    List<Account> accounts = getAccountDao().findAll();

    Assert.assertEquals("Debe tener 1 cuenta", 1, accounts.size());

    Account account = accounts.get(0);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.ACCOUNT_CREATED_TOPIC);

    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, account.getUuid());

    Assert.assertNotNull("Deberia existir un evento de cuenta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de cuenta creada event", event.getData());

    AccountEvent accountEvent = getJsonParser().fromJson(event.getData(), AccountEvent.class);

    Assert.assertNotNull("Debe tener id", accountEvent.getAccount().getId());
    Assert.assertEquals("Debe tener el mismo id", account.getUuid(), accountEvent.getAccount().getId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUserIdMc().toString(), accountEvent.getUserId());
    Assert.assertEquals("Debe tener la misma fecha creacion", account.getCreatedAt(), accountEvent.getAccount().getTimestamps().getCreatedAt());
    Assert.assertEquals("Debe tener la misma fecha actualizacion", account.getUpdatedAt(), accountEvent.getAccount().getTimestamps().getUpdatedAt());
    Assert.assertEquals("Debe tener status ACTIVE", AccountStatus.ACTIVE.toString(), accountEvent.getAccount().getStatus());
  }

  void waitForAccountingToExist(Long trxId) throws Exception {
    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    for(int j = 0; j < 10; j++) {
      Thread.sleep(500);
      AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, trxId);
      if(accountingData10 != null) {
        break;
      }
    }
  }
}
