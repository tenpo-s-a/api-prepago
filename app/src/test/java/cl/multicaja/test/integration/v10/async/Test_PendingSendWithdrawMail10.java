package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.Date;

import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.ERROR_SEND_MAIL_WITHDRAW_RESP;
import static cl.multicaja.prepaid.async.v10.routes.MailRoute10.PENDING_SEND_MAIL_WITHDRAW_RESP;

public class Test_PendingSendWithdrawMail10 extends TestBaseUnitAsync {

  @Test
  public void pendingSendWithdrawMailOk() throws Exception {


    PrepaidUser10 prepaidUser10 = buildPrepaidUser10();
    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    PrepaidWithdraw10 withdraw = buildPrepaidWithdraw10();
    withdraw.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, withdraw);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    String messageId = sendPendingWithdrawMail(prepaidUser10, withdraw,prepaidMovement10,0);

    Queue qResp = camelFactory.createJMSQueue(PENDING_SEND_MAIL_WITHDRAW_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertEquals("Debe tener mismo id", messageId, remote.getData().getPrepaidWithdraw10().getMessageId());
    Assert.assertNotNull("Debe contener un withdraw",remote.getData().getPrepaidWithdraw10());
  }

  //TODO: Ya no se enviaran mails
  @Ignore
  @Test
  public void pendingSendWithdrawMailNotOk() throws Exception {


    PrepaidWithdraw10 withdraw = buildPrepaidWithdraw10();
    withdraw.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
    prepaidMovement10.setFecfac(new Date());


    String messageId = sendPendingWithdrawMail(new PrepaidUser10(), withdraw,prepaidMovement10,3);

    Queue qResp = camelFactory.createJMSQueue(ERROR_SEND_MAIL_WITHDRAW_RESP);
    ExchangeData<PrepaidTopupData10> remote = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertEquals("Debe tener mismo id", messageId, remote.getData().getPrepaidWithdraw10().getMessageId());
    Assert.assertNotNull("Debe contener un withdraw",remote.getData().getPrepaidWithdraw10());
  }
}
