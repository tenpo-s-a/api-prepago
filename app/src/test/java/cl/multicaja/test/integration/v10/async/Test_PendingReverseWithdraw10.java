package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

/**
 * @author abarazarte
 **/
public class Test_PendingReverseWithdraw10 extends TestBaseUnitAsync {

  @Test
  public void reverseRetryCount4() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 prepaidMovement = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);

    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, prepaidMovement, 4);

    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 issuanceMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", issuanceMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_WITHDRAW_REVERSE, issuanceMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());
    }

    {
      //mensaje procesado por processPendingWithdrawReversal pero que falla y deja en cola de error
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un reverse", remoteReverse);
      Assert.assertNotNull("Deberia existir un reverse", remoteReverse.getData());

      System.out.println("Steps: " + remoteReverse.getProcessorMetadata());

      PrepaidMovement10 prepaidMovementResp = remoteReverse.getData().getPrepaidMovementReverse();

      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);

      PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

      Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
      Assert.assertEquals("El movimiento debe ser procesado con error", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_WITHDRAW_REVERSE, prepaidMovementInDb.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getClamone());

      //verifica que la ultima cola por la cual paso el mensaje sea ERROR_REVERSAL_WITHDRAW_REQ
      ProcessorMetadata lastProcessorMetadata = remoteReverse.getLastProcessorMetadata();
      String endpoint = TransactionReversalRoute10.ERROR_REVERSAL_WITHDRAW_REQ;

      Assert.assertEquals("debe ser intento 0", 0, lastProcessorMetadata.getRetry());
      Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
      Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
    }
  }

  @Test
  public void reverseWithdraw_OriginalMovement_ProcessOk() throws Exception {
    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdraw10(user);
    prepaidWithdraw.setMerchantCode(RandomStringUtils.randomAlphanumeric(15));
    prepaidWithdraw.getAmount().setValue(BigDecimal.valueOf(500));

    PrepaidWithdraw10 withdraw10 = new PrepaidWithdraw10(prepaidWithdraw);

    PrepaidMovement10 originalWithdraw = buildPrepaidMovement10(prepaidUser, withdraw10);
    originalWithdraw.setEstado(PrepaidMovementStatus.PROCESS_OK);
    originalWithdraw.setIdTxExterno(withdraw10.getTransactionId());
    originalWithdraw.setMonto(withdraw10.getAmount().getValue());
    originalWithdraw = createPrepaidMovement10(originalWithdraw);

    PrepaidMovement10 reverse = buildReversePrepaidMovement10(prepaidUser, prepaidWithdraw);
    reverse.setIdTxExterno(withdraw10.getTransactionId());
    reverse.setMonto(withdraw10.getAmount().getValue());
    reverse = createPrepaidMovement10(reverse);

    String messageId = sendPendingWithdrawReversal(withdraw10, prepaidUser, reverse, 0);

    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);

      PrepaidMovement10 issuanceMovement = remoteReverse.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de retiro", issuanceMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.PROCESS_OK, issuanceMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());


      PrepaidMovement10 originalDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalWithdraw.getId());
      Assert.assertEquals("Deberia estar con status REVERSED", PrepaidMovementStatus.REVERSED, originalDb.getEstado());
      PrepaidMovement10 reverseDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(reverse.getId());
      Assert.assertEquals("Deberia estar con status PROCESS_OK", PrepaidMovementStatus.PROCESS_OK, reverseDb.getEstado());
    }

    {
      //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de procesados
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_REQ);
      ExchangeData<PrepaidReverseData10> remoteReverse = (ExchangeData<PrepaidReverseData10>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNull("No deberia existir un mensaje en la cola de reversa de retiro", remoteReverse);
    }
  }


}
