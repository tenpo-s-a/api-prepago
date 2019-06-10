package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.helpers.tenpo.TenpoApiCall;
import cl.multicaja.prepaid.helpers.tenpo.model.Level;
import cl.multicaja.prepaid.helpers.tenpo.model.Plan;
import cl.multicaja.prepaid.helpers.tenpo.model.State;
import cl.multicaja.prepaid.helpers.tenpo.model.TenpoUser;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.Fee;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.CodigoRetorno;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP;

/**
 * @autor vutreras
 */
@RunWith(MockitoJUnitRunner.class)
public class Test_PrepaidEJBBean10_topupUserBalance extends TestBaseUnitAsync {

  private final BigDecimal topupTotalAmount = BigDecimal.valueOf(3238); // Total carga

  private final BigDecimal posFeeTotalAmount = BigDecimal.valueOf(238); // Pos Fee base + iva
  private final BigDecimal posFeeBaseAmount = BigDecimal.valueOf(200); // Pos Fee base
  private final BigDecimal posFeeIvaAmount = BigDecimal.valueOf(38); // Web Iva

  private final BigDecimal webFeeTotalAmount = BigDecimal.valueOf(0); // Web Fee base + iva
  private final BigDecimal webFeeBaseAmount = BigDecimal.valueOf(0); // Web Fee base
  private final BigDecimal webFeeIvaAmount = BigDecimal.valueOf(0); // Web Iva

  private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static TecnocomServiceHelper tc;

  @Spy
  private TenpoApiCall  apiCall;


  @Spy
  @InjectMocks
  private PrepaidEJBBean10 prepaidEJBBean10 = getPrepaidEJBBean10();

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
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.%s cascade", getSchema(), "prp_cuenta"));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_cuenta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario cascade", getSchema()));
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidUser_not_found() throws Exception {

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();
    try {
      getPrepaidEJBBean10().topupUserBalance(null, UUID.randomUUID().toString(), newPrepaidTopup,true);
      Assert.fail("No debe pasar por aqui");
    } catch(NotFoundException nfex) {
      Assert.assertEquals("No debe existir el usuario prepago", CLIENTE_NO_TIENE_PREPAGO.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidUser_disabled() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(), account.getAccountNumber());

    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    card = createPrepaidCardV2(card);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();

    try {
      getPrepaidEJBBean10().topupUserBalance(null, prepaidUser.getUuid(),newPrepaidTopup,true);
      Assert.fail("No debe pasar por aqui");
    } catch(ValidationException nfex) {
      Assert.assertEquals("el usuario prepago esta bloqueado", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), nfex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidCard_locked_hard() throws Exception {

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(), account.getAccountNumber());

    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    card.setStatus(PrepaidCardStatus.LOCKED_HARD);
    card = createPrepaidCardV2(card);

    try {
      getPrepaidEJBBean10().topupUserBalance(null, prepaidUser.getUuid(), newPrepaidTopup,true);
      Assert.fail("No debe pasar por aqui");
    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_prepaidCard_expired() throws Exception {

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    card.setStatus(PrepaidCardStatus.EXPIRED);
    card = createPrepaidCardV2(card);

    try {
      getPrepaidEJBBean10().topupUserBalance(null,prepaidUser.getUuid(), newPrepaidTopup,true);
      Assert.fail("No debe pasar por aqui");
    } catch(ValidationException vex) {
      Assert.assertEquals("Debe lanzar excepcion con error de Tarjeta invalida", TARJETA_INVALIDA_$VALUE.getValue(), vex.getCode());
    }
  }

  @Test
  public void topupUserBalance_validate_cdt_prepaidMovement_and_pos_fees() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();
    newPrepaidTopup.setMerchantCode(getRandomNumericString(15));

    //se debe establecer la primera carga mayor a 3238 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser.getUuid(), newPrepaidTopup,true);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    waitForAccountingToExist(prepaidTopup.getId());

    // Valida que existan las fees almacenadas en la tabla prp_movimiento_comision
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidTopup.getId());
    Assert.assertEquals("Deben haber 2 fees asignadas a este movimiento", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 baseFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.TOPUP_POS_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee base", baseFee);
    Assert.assertEquals("Debe tener valor 200", posFeeBaseAmount, baseFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee iva", ivaFee);
    Assert.assertEquals("Debe tener valor 38", posFeeIvaAmount, ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));
  }

  @Test
  public void topupUserBalance_validate_cdt_prepaidMovement_and_web_fees() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();
    newPrepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    //se debe establecer la primera carga mayor a 3238 dado que es el valor minimo definido por un limite del CDT
    newPrepaidTopup.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 prepaidTopup = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser.getUuid(), newPrepaidTopup,true);

    Assert.assertNotNull("Debe tener id", prepaidTopup.getId());

    String messageId = prepaidTopup.getMessageId();

    // Valida que existan las fees almacenadas en la tabla prp_movimiento_comision
    List<PrepaidMovementFee10> prepaidMovementFee10List = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidTopup.getId());
    Assert.assertEquals("Deben haber 2 fees asignadas a este movimiento", 2, prepaidMovementFee10List.size());

    PrepaidMovementFee10 baseFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.TOPUP_WEB_FEE.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee base", baseFee);
    Assert.assertEquals("Debe tener valor 0", webFeeBaseAmount, baseFee.getAmount().setScale(0, RoundingMode.HALF_UP));

    PrepaidMovementFee10 ivaFee = prepaidMovementFee10List.stream().filter(f -> PrepaidMovementFeeType.IVA.equals(f.getFeeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe existir una fee iva", ivaFee);
    Assert.assertEquals("Debe tener valor 0", webFeeIvaAmount, ivaFee.getAmount().setScale(0, RoundingMode.HALF_UP));
  }

  @Test
  public void topupUserBalance_ok_first_topup_true_by_level_1() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
    prepaidTopup10.setMerchantCode(getRandomNumericString(15));

    //primera carga
    prepaidTopup10.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertTrue("debe ser primera carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("Debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision(238) - comision de apertura (0)", topupTotalAmount.subtract(posFeeTotalAmount), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3238 pesos (carga inicial - comision(0) - comision de apertura (0))", topupTotalAmount, prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
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
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();
    newPrepaidTopup.setMerchantCode(getRandomNumericString(15));

    //primera carga
    newPrepaidTopup.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidUser.getUuid(), newPrepaidTopup,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    switch (newPrepaidTopup.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (238) - comision de apertura (0))", topupTotalAmount.subtract(posFeeTotalAmount), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3238 pesos (carga inicial - comision (0) - comision de apertura (0))", topupTotalAmount, prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    Boolean dataFound = false;
    for(int j = 0; j < 10; j++) {
      Thread.sleep(2000);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        dataFound = true;
        break;
      }
    }

    AccountingTxType txType = AccountingTxType.CARGA_POS;
    AccountingMovementType movementType = AccountingMovementType.CARGA_POS;
    if(TransactionOriginType.WEB.equals(newPrepaidTopup.getTransactionOriginType())) {
      txType = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneOffset.UTC));
      Assert.assertNotNull("No debe ser null", accounting10s);
      Assert.assertEquals("Debe haber 1 solo movimiento de account", 1, accounting10s.size());

      AccountingData10 accounting10 = accounting10s.get(0);
      Assert.assertEquals(String.format("Debe tener tipo %s", newPrepaidTopup.getTransactionOriginType()), txType, accounting10.getType());
      Assert.assertEquals(String.format("Debe tener acc movement type %s", newPrepaidTopup.getTransactionOriginType()), movementType, accounting10.getAccountingMovementType());
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

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidUser10.getUuid(),prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }

    //enesima carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(1000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidUser10.getUuid(),prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("debe ser excepcion de validacion del CDT por carga minima", Integer.valueOf(108203), vex.getCode());
      }
    }
  }

  @Ignore
  @Test
  public void topupUserBalance_not_ok_by_topup_max_level_1() throws Exception {


    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(51000));

      try {

        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de primera carga", Integer.valueOf(108206), vex.getCode());
      }
    }
  }

  @Test
  public void topupUserBalance_not_ok_by_topup_max_level_2() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    //primera carga WEB
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.setMerchantCode(NewPrepaidTopup10.WEB_MERCHANT_CODE); //carga WEB
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(500001));

      try {

        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        //TODO: Revisar esto, limite de primera carga no existe!!!
        //Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga web", Integer.valueOf(108201), vex.getCode());
      }
    }

    //primera carga POS
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();
      prepaidTopup10.setMerchantCode(getUniqueLong().toString()); //carga POS
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(100600));

      try {

        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

        Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

      } catch(ValidationException vex) {
        //Assert.assertEquals("validacion del CDT: La carga supera el monto máximo de carga pos", Integer.valueOf(108202), vex.getCode());
      }
    }
  }

  @Test
  public void topupUserBalance_ok_expireBalanceCache() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    NewPrepaidTopup10 newPrepaidTopup = buildPrepaidTopup10();

    //primera carga
    newPrepaidTopup.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidUser.getUuid(), newPrepaidTopup,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser.getId());

    Assert.assertNotNull(account);
    Assert.assertTrue(account.getExpireBalance() > 0L && account.getExpireBalance() <= Instant.now().toEpochMilli());
  }

  // Desde la 2da carga en adelante la carga se hace de manera sincrona
  @Test
  public void topupUserBalance_sync() throws Exception {

    Map<Long, PrepaidMovement10> movements = new HashMap<>();

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (119) - comision de apertura (0))", topupTotalAmount.subtract(posFeeTotalAmount), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3238 pesos (carga inicial - comision (0) - comision de apertura (0))", topupTotalAmount, prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertNotEquals("Debe tener el id de tarjeta asignado", 0,topup.getCardId().intValue());
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    movements.put(topup.getId(), topup);

    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.getAmount().setValue(topupTotalAmount);

      PrepaidTopup10 resp2 = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);

      Assert.assertNotNull("debe tener un id", resp2.getId());
      Assert.assertFalse("debe ser enesima carga", resp2.isFirstTopup());

      Map<String, Object> headers = new HashMap<>();
      headers.put("forceRefreshBalance", Boolean.TRUE);

      PrepaidBalance10 prepaidBalance2 = getAccountEJBBean10().getBalance(headers, account.getId());

      Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance2.getBalance().getValue().longValue() > prepaidBalance10.getBalance().getValue().longValue()  );

      PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp2.getId());
      Assert.assertNotNull("debe tener un movimiento", topup);
      Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
      Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup2.getEstadoNegocio());

      movements.put(topup2.getId(), topup2);

      Queue qResp2 = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);
      ExchangeData<PrepaidTopupData10> remoteTopup2 = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(1000l, 1000l).getMessage(qResp2, resp2.getMessageId());

      Assert.assertNull("No Deberia existir un topup en la cola", remoteTopup2);
    }

    boolean dataFound = waitForClearingToExist();
    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
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

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor",  prepaidBalance10.getBalance().getValue().longValue() > 0  );

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      PrepaidTopup10 resp2 = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);

      Assert.assertNotNull("debe tener un id", resp2.getId());
      Assert.assertFalse("debe ser enesima carga", resp2.isFirstTopup());

      Map<String, Object> headers = new HashMap<>();
      headers.put("forceRefreshBalance", Boolean.TRUE);

      PrepaidBalance10 prepaidBalance2 = getAccountEJBBean10().getBalance(headers, account.getId());

      Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance2.getBalance().getValue().longValue() > prepaidBalance10.getBalance().getValue().longValue()  );

      PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
      Assert.assertNotNull("debe tener un movimiento", topup);
      Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
      Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup2.getEstadoNegocio());

      Queue qResp2 = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);
      ExchangeData<PrepaidTopupData10> remoteTopup2 = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(1000l, 1000l).getMessage(qResp2, resp2.getMessageId());

      Assert.assertNull("No Deberia existir un topup en la cola", remoteTopup2);
    }

    // Tercera carga
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser10.getId(), PrepaidMovementStatus.REJECTED);
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

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10, true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance10.getBalance().getValue().longValue() > 0);

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1020);
    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup, true);
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

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getPrepaidMovementByIdTxExterno(secondTopup.getTransactionId(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL);

        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> IN_PROCESS", BusinessStatusType.REVERSED, topup2.getEstadoNegocio());


        PrepaidMovement10 topupReverse = getPrepaidMovementEJBBean11().getPrepaidMovementByIdTxExterno(secondTopup.getTransactionId(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA);

        Assert.assertNotNull("debe tener un movimiento", topupReverse);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topupReverse.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topupReverse.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topupReverse.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topupReverse.getEstadoNegocio());
      }
    }

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @Test
  public void topupUserBalance_sync_timeoutRequest() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor",  prepaidBalance10.getBalance().getValue().longValue() > 0  );

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        tc.getTecnocomService().setAutomaticError(true);
        tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        tc.getTecnocomService().setAutomaticError(true);
        tc.getTecnocomService().setRetorno(null);
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser10.getId(), PrepaidMovementStatus.REJECTED);
        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.REJECTED, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.REJECTED, topup2.getEstadoNegocio());
      }
    }
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @Test
  public void topupUserBalance_sync_transactionEvent() throws Exception {

    Map<Long, PrepaidMovement10> movements = new HashMap<>();

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (238) - comision de apertura (0))", topupTotalAmount.subtract(posFeeTotalAmount), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3238 pesos (carga inicial - comision (0) - comision de apertura (0))", topupTotalAmount, prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertNotEquals("Debe tener el id de tarjeta asignado", 0,topup.getCardId().intValue());
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    movements.put(topup.getId(), topup);

    // Porque se espera si ya se sabe que el movimiento fue procesado
    //Thread.sleep(2000);

    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.getAmount().setValue(topupTotalAmount);

      PrepaidTopup10 resp2 = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);

      Assert.assertNotNull("debe tener un id", resp2.getId());
      Assert.assertFalse("debe ser enesima carga", resp2.isFirstTopup());

      Map<String, Object> headers = new HashMap<>();
      headers.put("forceRefreshBalance", Boolean.TRUE);

      PrepaidBalance10 prepaidBalance2 = getAccountEJBBean10().getBalance(headers, account.getId());

      Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance2.getBalance().getValue().longValue() > prepaidBalance10.getBalance().getValue().longValue()  );

      PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp2.getId());
      Assert.assertNotNull("debe tener un movimiento", topup);
      Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
      Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup2.getEstadoNegocio());

      movements.put(topup2.getId(), topup2);

      Queue qResp2 = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);
      ExchangeData<PrepaidTopupData10> remoteTopup2 = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(1000l, 1000l).getMessage(qResp2, resp2.getMessageId());

      Assert.assertNull("No Deberia existir un topup en la cola", remoteTopup2);

      Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC);
      ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
        .getMessage(qResp3, topup2.getIdTxExterno());

      Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
      Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

      TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

      Assert.assertEquals("Debe tener el mismo monto", prepaidTopup10.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
      Assert.assertEquals("Debe tener el mismo tipo", "CASH_IN_MULTICAJA", transactionEvent.getTransaction().getType());
      Assert.assertEquals("Debe tener el status AUTHORIZED", "AUTHORIZED", transactionEvent.getTransaction().getStatus());

      assertFees(transactionEvent.getTransaction().getFees(), secondTopup.getTransactionOriginType());
    }

    boolean dataFound = waitForClearingToExist();
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
  public void topupUserBalance_sync_rejected_transactionEvent() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor",  prepaidBalance10.getBalance().getValue().longValue() > 0  );

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      PrepaidTopup10 resp2 = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);

      Assert.assertNotNull("debe tener un id", resp2.getId());
      Assert.assertFalse("debe ser enesima carga", resp2.isFirstTopup());

      Map<String, Object> headers = new HashMap<>();
      headers.put("forceRefreshBalance", Boolean.TRUE);

      PrepaidBalance10 prepaidBalance2 = getAccountEJBBean10().getBalance(headers, account.getId());

      Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance2.getBalance().getValue().longValue() > prepaidBalance10.getBalance().getValue().longValue()  );

      PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
      Assert.assertNotNull("debe tener un movimiento", topup);
      Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
      Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup2.getEstadoNegocio());

      Queue qResp2 = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REQ);
      ExchangeData<PrepaidTopupData10> remoteTopup2 = (ExchangeData<PrepaidTopupData10>) camelFactory.createJMSMessenger(1000l, 1000l).getMessage(qResp2, resp2.getMessageId());

      Assert.assertNull("No Deberia existir un topup en la cola", remoteTopup2);
    }

    // Tercera carga
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser10.getId(), PrepaidMovementStatus.REJECTED);
        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.REJECTED, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.REJECTED, topup2.getEstadoNegocio());
      }

      Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REJECTED_TOPIC);
      ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
        .getMessage(qResp3, secondTopup.getTransactionId());

      Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
      Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

      TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

      Assert.assertEquals("Debe tener el mismo monto", secondTopup.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
      Assert.assertEquals("Debe tener el mismo tipo", "CASH_IN_MULTICAJA", transactionEvent.getTransaction().getType());
      Assert.assertEquals("Debe tener el status REJECTED", "REJECTED", transactionEvent.getTransaction().getStatus());

      assertFees(transactionEvent.getTransaction().getFees(), secondTopup.getTransactionOriginType());
    }
  }

  @Test
  public void topupUserBalance_sync_timeoutResponse_transactionEvent() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10, true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor", prepaidBalance10.getBalance().getValue().longValue() > 0);

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    tc.getTecnocomService().setAutomaticError(true);
    tc.getTecnocomService().setRetorno(CodigoRetorno._1020);
    // Segunda carga debe ser sincrona
    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup, true);
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

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getPrepaidMovementByIdTxExterno(secondTopup.getTransactionId(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL);

        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> IN_PROCESS", BusinessStatusType.REVERSED, topup2.getEstadoNegocio());

        Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REJECTED_TOPIC);
        ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
          .getMessage(qResp3, topup2.getIdTxExterno());

        Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
        Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

        TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

        Assert.assertEquals("Debe tener el mismo monto", secondTopup.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
        Assert.assertEquals("Debe tener el mismo tipo", "CASH_IN_MULTICAJA", transactionEvent.getTransaction().getType());
        Assert.assertEquals("Debe tener el status REJECTED", "REJECTED", transactionEvent.getTransaction().getStatus());

        assertFees(transactionEvent.getTransaction().getFees(), secondTopup.getTransactionOriginType());

        PrepaidMovement10 topupReverse = getPrepaidMovementEJBBean11().getPrepaidMovementByIdTxExterno(secondTopup.getTransactionId(), PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA);

        Assert.assertNotNull("debe tener un movimiento", topupReverse);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topupReverse.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topupReverse.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topupReverse.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topupReverse.getEstadoNegocio());
      }
    }

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  @Test
  public void topupUserBalance_sync_timeoutRequest_transactionEvent() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(50000));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    Assert.assertTrue("El saldo del usuario debe ser mayor",  prepaidBalance10.getBalance().getValue().longValue() > 0  );

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    {
      NewPrepaidTopup10 secondTopup = buildNewPrepaidTopup10();
      secondTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);
      secondTopup.getAmount().setValue(BigDecimal.valueOf(400000));

      try {
        tc.getTecnocomService().setAutomaticError(true);
        tc.getTecnocomService().setRetorno(CodigoRetorno._1010);
        getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), secondTopup,true);
        Assert.fail("Should not be here");
      } catch (RunTimeValidationException rvex) {
        tc.getTecnocomService().setAutomaticError(true);
        tc.getTecnocomService().setRetorno(null);
        Assert.assertEquals("Debe ser error de tecnocom", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());

        PrepaidMovement10 topup2 = getPrepaidMovementEJBBean11().getLastPrepaidMovementByIdPrepaidUserAndOneStatus(prepaidUser10.getId(), PrepaidMovementStatus.REJECTED);
        Assert.assertNotNull("debe tener un movimiento", topup2);
        Assert.assertEquals("debe ser del mismo monto", secondTopup.getAmount().getValue(), topup2.getImpfac());
        Assert.assertEquals("debe ser del id tx externa", secondTopup.getTransactionId(), topup2.getIdTxExterno());
        Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.REJECTED, topup2.getEstado());
        Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.REJECTED, topup2.getEstadoNegocio());
      }

      Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REJECTED_TOPIC);
      ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(45000, 60000)
        .getMessage(qResp3, secondTopup.getTransactionId());

      Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
      Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

      TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

      Assert.assertEquals("Debe tener el mismo monto", secondTopup.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
      Assert.assertEquals("Debe tener el mismo tipo", "CASH_IN_MULTICAJA", transactionEvent.getTransaction().getType());
      Assert.assertEquals("Debe tener el status REJECTED", "REJECTED", transactionEvent.getTransaction().getStatus());

      assertFees(transactionEvent.getTransaction().getFees(), secondTopup.getTransactionOriginType());
    }
    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);
  }

  //TODO: se ignora test ya que de momento no se genera este evento en cash-in
  @Ignore
  @Test
  public void topupUserBalance_cardCreated_event() throws Exception {

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);

    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    prepaidCard10 = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (238) - comision de apertura (0))", topupTotalAmount.subtract(posFeeTotalAmount), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3238 pesos (carga inicial - comision (0) - comision de apertura (0))", topupTotalAmount, prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());

    boolean dataFound = waitForClearingToExist();

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
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(45000, 60000)
      .getMessage(qResp, prepaidCard10.getUuid());

    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de tarjeta creada event", event.getData());

    CardEvent cardEvent = getJsonParser().fromJson(event.getData(), CardEvent.class);

    Assert.assertEquals("Debe tener el mismo id", prepaidCard10.getUuid(), cardEvent.getCard().getId());
    Assert.assertFalse("Debe tener accountId", StringUtils.isBlank(cardEvent.getAccountId()));
    Assert.assertEquals("Debe tener el mismo pan", prepaidCard10.getPan(), cardEvent.getCard().getPan());
  }

  @Test
  public void topupUserBalance_accountCreated_event() throws Exception {

    tc.getTecnocomService().setAutomaticError(false);
    tc.getTecnocomService().setRetorno(null);

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(topupTotalAmount);

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    PrepaidBalance10 prepaidBalance10 = getAccountEJBBean10().getBalance(null, account.getId());

    switch (prepaidTopup10.getTransactionOriginType()){
      case POS:
        Assert.assertEquals("El saldo del usuario debe ser 3000 pesos (carga inicial - comision (238) - comision de apertura (0))", topupTotalAmount.subtract(posFeeTotalAmount), prepaidBalance10.getBalance().getValue());
        break;
      case WEB:
        Assert.assertEquals("El saldo del usuario debe ser 3238 pesos (carga inicial - comision (0) - comision de apertura (0))", topupTotalAmount, prepaidBalance10.getBalance().getValue());
        break;
    }

    PrepaidMovement10 topup = getPrepaidMovementEJBBean11().getPrepaidMovementById(resp.getId());
    Assert.assertNotNull("debe tener un movimiento", topup);
    Assert.assertEquals("debe tener status -> PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, topup.getEstado());
    Assert.assertEquals("debe tener estado negocio -> CONFIRMED", BusinessStatusType.CONFIRMED, topup.getEstadoNegocio());
    Assert.assertNotEquals("Debe tener el id de tarjeta asignado", 0,topup.getCardId().intValue());

    boolean dataFound = waitForClearingToExist();

    AccountingTxType txType = AccountingTxType.CARGA_POS;
    AccountingMovementType movementType = AccountingMovementType.CARGA_POS;
    if(TransactionOriginType.WEB.equals(prepaidTopup10.getTransactionOriginType())) {
      txType = AccountingTxType.CARGA_WEB;
      movementType = AccountingMovementType.CARGA_WEB;
    }

    if (dataFound) {
      List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now(ZoneId.of("UTC")));
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

    account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.ACCOUNT_CREATED_TOPIC);

    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(45000, 60000)
      .getMessage(qResp, account.getUuid());

    Assert.assertNotNull("Deberia existir un evento de cuenta creada event", event);
    Assert.assertNotNull("Deberia existir un evento de cuenta creada event", event.getData());

    AccountEvent accountEvent = getJsonParser().fromJson(event.getData(), AccountEvent.class);

    Assert.assertNotNull("Debe tener id", accountEvent.getAccount().getId());
    Assert.assertEquals("Debe tener el mismo id", account.getUuid(), accountEvent.getAccount().getId());
    Assert.assertEquals("Debe tener el mismo userId", prepaidUser10.getUuid(), accountEvent.getUserId());
    Assert.assertEquals("Debe tener la misma fecha creacion", account.getCreatedAt(), accountEvent.getAccount().getTimestamps().getCreatedAt());
    Assert.assertEquals("Debe tener la misma fecha actualizacion", account.getUpdatedAt(), accountEvent.getAccount().getTimestamps().getUpdatedAt());
    Assert.assertEquals("Debe tener status ACTIVE", AccountStatus.ACTIVE.toString(), accountEvent.getAccount().getStatus());
  }

  @Test
  public void topupUserBalance_sync_expireBalanceCache() throws Exception {

    Map<Long, PrepaidMovement10> movements = new HashMap<>();

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3238));

    PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);

    Assert.assertNotNull("debe tener un id", resp.getId());
    //Assert.assertFalse("debe ser enesima carga", resp.isFirstTopup());

    PrepaidCard10 prepaidCard10 = waitForLastPrepaidCardInStatusV11(prepaidUser10.getId(), PrepaidCardStatus.ACTIVE);

    Assert.assertNotNull("debe tener una tarjeta", prepaidCard10);
    Assert.assertEquals("debe ser tarjeta activa", PrepaidCardStatus.ACTIVE, prepaidCard10.getStatus());

    Account account = getAccountEJBBean10().findByUserId(prepaidUser10.getId());

    Assert.assertNotNull("Debe existir la cuenta", account);

    Assert.assertTrue(account.getExpireBalance() > 0L && account.getExpireBalance() <= Instant.now().toEpochMilli());
  }


  void waitForAccountingToExist(Long trxId) throws Exception {
    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    for(int j = 0; j < 20; j++) {
      Thread.sleep(500);
      AccountingData10 accountingData10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, trxId);
      if(accountingData10 != null) {
        break;
      }
    }
  }

  boolean waitForClearingToExist() throws Exception {
    // Revisar/esperar que existan los datos en accounting y clearing (esperando que se ejecute metodo async)
    for(int j = 0; j < 20; j++) {
      Thread.sleep(500);
      List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.INITIAL, null);
      if (clearing10s.size() > 0) {
        return true;
      }
    }
    return false;
  }

  void assertFees(List<Fee> feeList, TransactionOriginType transactionOriginType) {
    Assert.assertEquals("El evento debe tener 2 fees", 2, feeList.size());

    if(TransactionOriginType.POS.equals(transactionOriginType)) {
      Fee fee = feeList.stream().filter(f -> PrepaidMovementFeeType.TOPUP_POS_FEE.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de pos", fee);
      Assert.assertEquals("Debe tener un valor de 200", posFeeBaseAmount, fee.getAmount().getValue());

      fee = feeList.stream().filter(f -> PrepaidMovementFeeType.IVA.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de iva", fee);
      Assert.assertEquals("Debe tener un valor de 38", posFeeIvaAmount, fee.getAmount().getValue());

    } else if (TransactionOriginType.WEB.equals(transactionOriginType)) {
      Fee fee = feeList.stream().filter(f -> PrepaidMovementFeeType.TOPUP_WEB_FEE.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de pos", fee);
      Assert.assertEquals("Debe tener un valor de 0", webFeeBaseAmount, fee.getAmount().getValue());

      fee = feeList.stream().filter(f -> PrepaidMovementFeeType.IVA.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de iva", fee);
      Assert.assertEquals("Debe tener un valor de 0", webFeeIvaAmount, fee.getAmount().getValue());
    }
  }

  @Test
  public void topupUserBalance_sync_userNull_getFromTenpo() throws Exception{
    Map<Long, PrepaidMovement10> movements = new HashMap<>();

    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10();

    //primera carga
    prepaidTopup10.getAmount().setValue(topupTotalAmount);

    //Mock de la llamada a tenpo!!
    Mockito.doReturn(getTempoUser(prepaidUser10)).when(apiCall).getUserById(UUID.fromString(prepaidUser10.getUuid()));

    PrepaidTopup10 resp = prepaidEJBBean10.topupUserBalance(null,prepaidUser10.getUuid(), prepaidTopup10,true);
    PrepaidUser10 prepaidFull = getPrepaidUserEJBBean10().findByExtId(null,prepaidUser10.getUuid());
    Assert.assertNotNull("Debe existir respuesta de carga", resp);
    Assert.assertNotNull("debe tener un id", resp.getId());
    Assert.assertNotNull("El usuario debe tener un id", prepaidFull.getId());

    PrepaidCard10 prepaidCard10  = waitForLastPrepaidCardInStatusV11(prepaidFull.getId(),PrepaidCardStatus.ACTIVE);
    Assert.assertNotNull("La tarjeta no debe ser nula", prepaidCard10);
    Assert.assertEquals("El status debe ser active",PrepaidCardStatus.ACTIVE,prepaidCard10.getStatus());

  }

  private TenpoUser getTempoUser(PrepaidUser10 prepaidUser10){

    TenpoUser tenpoUser = new TenpoUser();
    tenpoUser.setDocumentNumber(prepaidUser10.getDocumentNumber());
    //tenpoUser.setDocumentType(prepaidUser10.getDocumentType().name());
    tenpoUser.setFirstName(prepaidUser10.getName());
    tenpoUser.setLastName(prepaidUser10.getLastName());
    tenpoUser.setTributaryIdentifier(prepaidUser10.getDocumentNumber());
    tenpoUser.setLevel(Level.LEVEL_1);
    tenpoUser.setState(State.ACTIVE);
    tenpoUser.setId(UUID.fromString(prepaidUser10.getUuid()));
    //tenpoUser.setUserId(UUID.fromString(prepaidUser10.getUuid()));
    tenpoUser.setPlan(Plan.FREE);
    return tenpoUser;
  }

}
