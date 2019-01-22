package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.Date;

public class Test_PendingSendWithdrawMail10 extends TestBaseUnitAsync {

  @Test
  public void pendingSendWithdrawMailOk() throws Exception {

    User user = registerUser();

    PrepaidWithdraw10 withdraw = buildPrepaidWithdraw10(user);
    withdraw.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
    prepaidMovement10.setFecfac(new Date());

    String messageId = sendPendingWithdrawMail(user, withdraw,prepaidMovement10,0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_WITHDRAW_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertEquals("Debe tener mismo id", messageId, remote.getData().getPrepaidWithdraw10().getMessageId());
    Assert.assertNotNull("Debe contener un withdraw",remote.getData().getPrepaidWithdraw10());
  }

  @Test
  public void pendingSendWithdrawMailNotOk() throws Exception {

    User user = registerUser();

    PrepaidWithdraw10 withdraw = buildPrepaidWithdraw10(user);
    withdraw.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
    prepaidMovement10.setFecfac(new Date());


    String messageId = sendPendingWithdrawMail(user, withdraw,prepaidMovement10,3);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_SEND_MAIL_WITHDRAW_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertEquals("Debe tener mismo id", messageId, remote.getData().getPrepaidWithdraw10().getMessageId());
    Assert.assertNotNull("Debe contener un withdraw",remote.getData().getPrepaidWithdraw10());
  }
}
