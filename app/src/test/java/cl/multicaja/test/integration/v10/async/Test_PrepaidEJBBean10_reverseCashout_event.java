package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.TransactionStatus;
import cl.multicaja.prepaid.kafka.events.model.TransactionType;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

public class Test_PrepaidEJBBean10_reverseCashout_event extends TestBaseUnitAsync{

  @Before
  @After
  public  void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_cuenta cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario cascade", getSchema()));
  }

  @Test
  public void reverseWithdraw_OriginalMovement_ProcessOk_POS_Event() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(), account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(getRandomNumericString(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidMovement10 originalTopup = buildPrepaidMovement11(prepaidUser, new PrepaidWithdraw10(prepaidWithdraw));
    originalTopup.setIdTxExterno(prepaidWithdraw.getTransactionId());
    originalTopup.setMonto(prepaidWithdraw.getAmount().getValue());
    originalTopup.setCardId(prepaidCard10.getId());
    originalTopup = createPrepaidMovement11(originalTopup);

    Assert.assertNotNull("Debe tener id", originalTopup.getId());
    Assert.assertTrue("Debe tener id", originalTopup.getId() > 0);

    getPrepaidEJBBean10().reverseWithdrawUserBalance(null, prepaidUser.getUuid(), prepaidWithdraw, Boolean.TRUE);

    Queue qResp3 = camelFactory.createJMSQueue(KafkaEventsRoute10.TRANSACTION_REVERSED_TOPIC);
    ExchangeData<String> event = (ExchangeData<String>) camelFactory.createJMSMessenger(30000, 60000)
      .getMessage(qResp3, prepaidWithdraw.getTransactionId());

    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event);
    Assert.assertNotNull("Deberia existir un evento de transaccion reversada", event.getData());

    TransactionEvent transactionEvent = getJsonParser().fromJson(event.getData(), TransactionEvent.class);

    Assert.assertEquals("Debe tener el mismo monto", prepaidWithdraw.getAmount().getValue(), transactionEvent.getTransaction().getPrimaryAmount().getValue());
    Assert.assertEquals("Debe tener el mismo tipo", TransactionType.CASH_OUT_MULTICAJA.toString(), transactionEvent.getTransaction().getType());
    Assert.assertEquals("Debe tener el status REVERSED", TransactionStatus.REVERSED.toString(), transactionEvent.getTransaction().getStatus());

  }

}
