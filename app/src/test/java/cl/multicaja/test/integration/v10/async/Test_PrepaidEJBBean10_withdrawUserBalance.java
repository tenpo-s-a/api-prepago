package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.Fee;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static cl.multicaja.core.model.Errors.TARJETA_ERROR_GENERICO_$VALUE;

/**
 * @author abarazarte
 **/
public class Test_PrepaidEJBBean10_withdrawUserBalance extends TestBaseUnitAsync {

  @Before
  @After
  public  void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_cuenta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario cascade", getSchema()));
  }

  @Test
  public void withdrawFail_timeoutResponse() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    Thread.sleep(2000);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    PrepaidWithdraw10 withdraw = null;

    String messageId = "";
    TecnocomServiceHelper.getInstance().getTecnocomService().setAutomaticError(Boolean.TRUE);
    TecnocomServiceHelper.getInstance().getTecnocomService().setRetorno("1020");
    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá");
    } catch(RunTimeValidationException rvex) {

      Assert.assertEquals("Debe tener codigo", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());
      Assert.assertEquals("Debe tener value y messageId", 2, rvex.getData().length);
      Assert.assertNotNull("Debe tener messageId",rvex.getData()[1].getValue());
      Assert.assertFalse("Debe tener messageId", StringUtils.isBlank(String.valueOf(rvex.getData()[1].getValue())));
      messageId = String.valueOf(rvex.getData()[1].getValue());
    }
    TecnocomServiceHelper.getInstance().getTecnocomService().setAutomaticError(Boolean.FALSE);
    TecnocomServiceHelper.getInstance().getTecnocomService().setRetorno("1020");


    Thread.sleep(1500);

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia tener status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia Business status " + BusinessStatusType.REVERSED, BusinessStatusType.REVERSED, dbPrepaidMovement.getEstadoNegocio());

    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
    ExchangeData<PrepaidReverseData10> reverseWithdraw = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un reversa", reverseWithdraw);
    Assert.assertNotNull("Deberia existir una reversa", reverseWithdraw.getData());

    PrepaidMovement10 dbPrepaidMovementReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status PROCESS_OK" + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovementReverse.getEstado());

  }


  @Test
  public void withdrawPOS_event() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(30000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    Thread.sleep(2000);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    PrepaidWithdraw10 withdraw = getPrepaidEJBBean10().withdrawUserBalance(null, prepaidUser.getUuid(), prepaidWithdraw,true);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000).getMessage(qResp, prepaidWithdraw.getTransactionId());
    Assert.assertNotNull("Debe existir el evento", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo monto", withdraw.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", "CASH_OUT_MULTICAJA", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status AUTHORIZED", "AUTHORIZED", transactionEvent.getTransaction().getStatus());

    assertFees(transactionEvent.getTransaction().getFees(), TransactionOriginType.POS);
  }

  @Test
  public void withdrawWEB_event() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(30000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    Thread.sleep(2000);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    PrepaidWithdraw10 withdraw = getPrepaidEJBBean10().withdrawUserBalance(null, prepaidUser.getUuid(), prepaidWithdraw,true);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_AUTHORIZED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000).getMessage(qResp, prepaidWithdraw.getTransactionId());
    Assert.assertNotNull("Debe existir el evento", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo monto", withdraw.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", "CASH_OUT_MULTICAJA", transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status AUTHORIZED", "AUTHORIZED", transactionEvent.getTransaction().getStatus());

    assertFees(transactionEvent.getTransaction().getFees(), TransactionOriginType.WEB);
  }

  void assertFees(List<Fee> feeList, TransactionOriginType transactionOriginType) {
    Assert.assertEquals("El evento debe tener 2 fees", 2, feeList.size());

    if(TransactionOriginType.POS.equals(transactionOriginType)) {
      Fee fee = feeList.stream().filter(f -> PrepaidMovementFeeType.WITHDRAW_POS_FEE.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de pos", fee);
      Assert.assertEquals("Debe tener un valor de 200", new BigDecimal(200), fee.getAmount().getValue().setScale(0, RoundingMode.HALF_UP));

      fee = feeList.stream().filter(f -> PrepaidMovementFeeType.IVA.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de iva", fee);
      Assert.assertEquals("Debe tener un valor de 38", new BigDecimal(38), fee.getAmount().getValue().setScale(0, RoundingMode.HALF_UP));

    } else if (TransactionOriginType.WEB.equals(transactionOriginType)) {
      Fee fee = feeList.stream().filter(f -> PrepaidMovementFeeType.WITHDRAW_WEB_FEE.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de web", fee);
      Assert.assertEquals("Debe tener un valor de 84", new BigDecimal(84), fee.getAmount().getValue().stripTrailingZeros());

      fee = feeList.stream().filter(f -> PrepaidMovementFeeType.IVA.toString().equals(f.getType())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un fee de iva", fee);
      Assert.assertEquals("Debe tener un valor de 16", new BigDecimal(16), fee.getAmount().getValue().stripTrailingZeros());
    }
  }

}
