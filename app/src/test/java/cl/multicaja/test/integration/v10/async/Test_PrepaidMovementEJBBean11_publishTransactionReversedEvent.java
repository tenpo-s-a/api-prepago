package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.Fee;
import cl.multicaja.prepaid.kafka.events.model.TransactionStatus;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidMovementEJBBean11_publishTransactionReversedEvent extends TestBaseUnitAsync {

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_externalUserId_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(null, null, null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_externalUserId_empty() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent("", null, null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_accountUuid_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(UUID.randomUUID().toString(), null, null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_accountUuid_empty() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(UUID.randomUUID().toString(), "", null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_cardUuid_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_cardUuid_empty() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_movement_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionReversedEvent_type_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionReversedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new PrepaidMovement10(), null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test
  public void publishTransactionReversedEvent() throws Exception {

    String userUuid = UUID.randomUUID().toString();
    String accountUuid = UUID.randomUUID().toString();
    String cardUuid = UUID.randomUUID().toString();

    PrepaidUser10 user = buildPrepaidUserv2();
    PrepaidTopup10 topup = buildPrepaidTopup10();

    PrepaidMovement10 movement = buildPrepaidMovement11(user, topup);
    movement.setFechaCreacion(Timestamp.from(Instant.now()));
    movement.setFechaActualizacion(Timestamp.from(Instant.now()));

    List<PrepaidMovementFee10> feeList = new ArrayList<>();
    PrepaidMovementFee10 fee = new PrepaidMovementFee10();
    fee.setAmount(BigDecimal.TEN);
    fee.setFeeType(PrepaidMovementFeeType.TOPUP_POS_FEE);
    feeList.add(fee);

    getPrepaidMovementEJBBean11().publishTransactionReversedEvent(userUuid, accountUuid, cardUuid, movement, feeList, TransactionType.CASH_IN_MULTICAJA);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, movement.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", movement.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", accountUuid, transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", userUuid, transactionEvent.getUserId());

    Assert.assertEquals("Debe tener el mismo numaut", movement.getNumaut(), transactionEvent.getTransaction().getAuthCode());
    Assert.assertEquals("Debe tener el mismo monto", movement.getMonto(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", TransactionType.CASH_IN_MULTICAJA.toString(), transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status REVERSED", TransactionStatus.REVERSED.toString(), transactionEvent.getTransaction().getStatus());

    List<Fee> fees = transactionEvent.getTransaction().getFees();
    Assert.assertEquals("Debe tener 1 fee", 1, fees.size());
    Assert.assertEquals("Debe tener mismo fee", fee.getAmount(), fees.get(0).getAmount().getValue());
  }

  @Test
  public void publishTransactionReversedEvent_fee_null() throws Exception {

    String userUuid = UUID.randomUUID().toString();
    String accountUuid = UUID.randomUUID().toString();
    String cardUuid = UUID.randomUUID().toString();

    PrepaidUser10 user = buildPrepaidUserv2();
    PrepaidTopup10 topup = buildPrepaidTopup10();

    PrepaidMovement10 movement = buildPrepaidMovement11(user, topup);
    movement.setFechaCreacion(Timestamp.from(Instant.now()));
    movement.setFechaActualizacion(Timestamp.from(Instant.now()));

    getPrepaidMovementEJBBean11().publishTransactionReversedEvent(userUuid, accountUuid, cardUuid, movement, null, TransactionType.CASH_IN_MULTICAJA);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp, movement.getIdTxExterno());

    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion autorizada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo id", movement.getIdTxExterno(), transactionEvent.getTransaction().getRemoteTransactionId());
    Assert.assertEquals("Debe tener el mismo accountId", accountUuid, transactionEvent.getAccountId());
    Assert.assertEquals("Debe tener el mismo userId", userUuid, transactionEvent.getUserId());

    Assert.assertEquals("Debe tener el mismo numaut", movement.getNumaut(), transactionEvent.getTransaction().getAuthCode());
    Assert.assertEquals("Debe tener el mismo monto", movement.getMonto(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", TransactionType.CASH_IN_MULTICAJA.toString(), transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status REVERSED", TransactionStatus.REVERSED.toString(), transactionEvent.getTransaction().getStatus());

    Assert.assertEquals("No debe tener fees", 0, transactionEvent.getTransaction().getFees().size());

  }
}
