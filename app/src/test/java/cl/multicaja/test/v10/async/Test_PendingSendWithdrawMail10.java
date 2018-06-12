package cl.multicaja.test.v10.async;

import cl.multicaja.camel.ResponseRoute;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDataRoute10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

public class Test_PendingSendWithdrawMail10 extends TestBaseUnitAsync {

  @Test
  public void pendingSendWithdrawMailOk() throws Exception {

    User user = registerUser();

    PrepaidWithdraw10 withdraw = buildPrepaidWithdraw10(user);

    String messageId = sendPendingWithdrawMail(user, withdraw,0);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_SEND_MAIL_WITHDRAW_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remote = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertEquals("Debe tener mismo id", messageId, remote.getData().getPrepaidWithdraw10().getMessageId());
    Assert.assertNotNull("Debe contener un withdraw",remote.getData().getPrepaidWithdraw10());
  }

  @Test
  public void pendingSendWithdrawMailNotOk() throws Exception {

    User user = registerUser();

    PrepaidWithdraw10 withdraw = buildPrepaidWithdraw10(user);

    String messageId = sendPendingWithdrawMail(user, withdraw,3);

    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.ERROR_SEND_MAIL_WITHDRAW_RESP);
    ResponseRoute<PrepaidTopupDataRoute10> remote = (ResponseRoute<PrepaidTopupDataRoute10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertEquals("Debe tener mismo id", messageId, remote.getData().getPrepaidWithdraw10().getMessageId());
    Assert.assertNotNull("Debe contener un withdraw",remote.getData().getPrepaidWithdraw10());
  }
}
