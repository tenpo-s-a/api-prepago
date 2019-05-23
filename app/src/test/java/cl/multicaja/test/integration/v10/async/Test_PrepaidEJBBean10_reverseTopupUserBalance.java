package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.TransactionStatus;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.UUID;

public class Test_PrepaidEJBBean10_reverseTopupUserBalance extends TestBaseUnitAsync{

  @Test
  public void reverseTopup_transactionEvent() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10();
    prepaidTopup.setMerchantCode(getRandomNumericString(15));
    prepaidTopup.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement11(prepaidUser, new PrepaidTopup10(prepaidTopup),prepaidCard10);
    originalTopup.setIdTxExterno(prepaidTopup.getTransactionId());
    originalTopup.setMonto(prepaidTopup.getAmount().getValue());
    originalTopup = createPrepaidMovement11(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    getPrepaidEJBBean10().reverseTopupUserBalance(null, prepaidUser.getUuid(), prepaidTopup, Boolean.TRUE);

    Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp3, prepaidTopup.getTransactionId());

    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo monto", prepaidTopup.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", TransactionType.CASH_IN_MULTICAJA.toString(), transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status REVERSED", TransactionStatus.REVERSED.toString(), transactionEvent.getTransaction().getStatus());

  }

}
