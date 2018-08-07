package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;

/**
 * @autor abarazarte
 */
public class Test_PendingTopupReverseConfirmation10 extends TestBaseUnitAsync {

  @Test
  public void pendingTopupReverseConfirmation_CdtTransactionIsNull() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup = buildPrepaidTopup10(user);

    String messageId = sendTopUpReverseConfirmation(topup, user, null);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REVERSE_CONFIRMATION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNull("No deberia existir un topup", remoteTopup);
  }

  @Ignore
  @Test
  public void pendingTopupReverseConfirmation() throws Exception {

    User user = registerUser();

    PrepaidTopup10 topup10 = buildPrepaidTopup10(user);

    topup10.setFirstTopup(Boolean.FALSE);

    CdtTransaction10 cdtTransaction = new CdtTransaction10();
    cdtTransaction.setAmount(topup10.getAmount().getValue());
    cdtTransaction.setTransactionType(topup10.getCdtTransactionType());
    cdtTransaction.setAccountId("TestPrepaid_" + user.getRut().getValue());
    cdtTransaction.setGloss(topup10.getCdtTransactionType().getName()+" "+topup10.getAmount().getValue());
    cdtTransaction.setTransactionReference(0L);
    cdtTransaction.setExternalTransactionId(topup10.getTransactionId());
    cdtTransaction.setIndSimulacion(false);
    cdtTransaction = getCdtEJBBean10().addCdtTransaction(null,cdtTransaction);

    cdtTransaction.setExternalTransactionId("R" + cdtTransaction.getExternalTransactionId());

    String messageId = sendTopUpReverseConfirmation(topup10, user, cdtTransaction);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(PrepaidTopupRoute10.PENDING_TOPUP_REVERSE_CONFIRMATION_RESP);
    ExchangeData<PrepaidTopupData10> remoteTopup = (ExchangeData<PrepaidTopupData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    CdtTransaction10 cdt = remoteTopup.getData().getCdtTransaction10();

    Assert.assertEquals("Deberia tener numError = 0", 0, cdt.getNumErrorInt());
    Assert.assertEquals("No deberia tener mensaje de rror", "", cdt.getMsjError());
  }
}
