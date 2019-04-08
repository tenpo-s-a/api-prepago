package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.Fee;
import cl.multicaja.prepaid.kafka.events.model.TransactionStatus;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_PrepaidMovementEJBBean11_publishTransactionRejectedEvent extends TestBaseUnitAsync {

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_externalUserId_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(null, null, null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_externalUserId_empty() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent("", null, null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_accountUuid_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(UUID.randomUUID().toString(), null, null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_accountUuid_empty() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(UUID.randomUUID().toString(), "", null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_cardUuid_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_cardUuid_empty() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_movement_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void publishTransactionRejectedEvent_type_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new PrepaidMovement10(), null,null);
      Assert.fail("Should not be here");
    } catch (BadRequestException brex) {
      Assert.assertEquals("Error de parametro faltante",PARAMETRO_FALTANTE_$VALUE.getValue(), brex.getCode());
      throw brex;
    }
  }

  @Test
  public void publishTransactionRejectedEvent() throws Exception {

    String userUuid = UUID.randomUUID().toString();
    String accountUuid = UUID.randomUUID().toString();
    String cardUuid = UUID.randomUUID().toString();

    PrepaidUser10 user = buildPrepaidUserv2();
    PrepaidTopup10 topup = buildPrepaidTopup10();

    PrepaidMovement10 movement = buildPrepaidMovement10(user, topup);
    movement.setFechaCreacion(Timestamp.from(Instant.now()));
    movement.setFechaActualizacion(Timestamp.from(Instant.now()));

    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10(BigDecimal.TEN);

    getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(userUuid, accountUuid, cardUuid, movement, fee, TransactionType.CASH_IN_MULTICAJA);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REJECTED_TOPIC);
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
    Assert.assertEquals("Debe tener el status REJECTED", TransactionStatus.REJECTED.toString(), transactionEvent.getTransaction().getStatus());

    List<Fee> fees = transactionEvent.getTransaction().getFees();
    Assert.assertEquals("Debe tener 1 fee", 1, fees.size());
    Assert.assertEquals("Debe tener mismo fee", fee.getCurrencyCode(), fees.get(0).getAmount().getCurrencyCode());
    Assert.assertEquals("Debe tener mismo fee", fee.getValue(), fees.get(0).getAmount().getValue());
  }

  @Test
  public void publishTransactionRejectedEvent_fee_null() throws Exception {

    String userUuid = UUID.randomUUID().toString();
    String accountUuid = UUID.randomUUID().toString();
    String cardUuid = UUID.randomUUID().toString();

    PrepaidUser10 user = buildPrepaidUserv2();
    PrepaidTopup10 topup = buildPrepaidTopup10();

    PrepaidMovement10 movement = buildPrepaidMovement10(user, topup);
    movement.setFechaCreacion(Timestamp.from(Instant.now()));
    movement.setFechaActualizacion(Timestamp.from(Instant.now()));

    getPrepaidMovementEJBBean11().publishTransactionRejectedEvent(userUuid, accountUuid, cardUuid, movement, null, TransactionType.CASH_IN_MULTICAJA);

    Queue qResp = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REJECTED_TOPIC);
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
    Assert.assertEquals("Debe tener el status REJECTED", TransactionStatus.REJECTED.toString(), transactionEvent.getTransaction().getStatus());

    Assert.assertEquals("No debe tener fees", 0, transactionEvent.getTransaction().getFees().size());

  }
}
