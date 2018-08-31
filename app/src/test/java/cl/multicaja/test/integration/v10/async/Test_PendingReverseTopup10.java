package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

/**
 * @autor abarazarte
 */
public class Test_PendingReverseTopup10 extends TestBaseUnitAsync {

  @Test
  public void reverseRetryCount4() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,4);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un mensaje en la cola de carga de retiro", remoteTopup);

      PrepaidMovement10 issuanceMovement = remoteTopup.getData().getPrepaidMovementReverse();
      Assert.assertNotNull("Deberia existir un mensaje en la cola de error de reversa de carga", issuanceMovement);
      Assert.assertEquals("El movimiento debe ser procesado", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP_REVERSE, issuanceMovement.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado", Integer.valueOf(0), issuanceMovement.getClamone());

    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un reverse", remoteTopup);
      Assert.assertNotNull("Deberia existir un reverse", remoteTopup.getData());

      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      PrepaidMovement10 prepaidMovementResp = remoteTopup.getData().getPrepaidMovementReverse();

      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementResp);

      PrepaidMovement10 prepaidMovementInDb = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovementResp.getId());

      Assert.assertNotNull("Deberia existir un prepaidMovement en la bd", prepaidMovementInDb);
      Assert.assertEquals("El movimiento debe ser procesado con error", PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP_REVERSE, prepaidMovementInDb.getEstado());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNumextcta());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getNummovext());
      Assert.assertEquals("El movimiento debe ser procesado con error", Integer.valueOf(0), prepaidMovementInDb.getClamone());

      //verifica que la ultima cola por la cual paso el mensaje sea ERROR_REVERSAL_WITHDRAW_REQ
      ProcessorMetadata lastProcessorMetadata = remoteTopup.getLastProcessorMetadata();
      String endpoint = TransactionReversalRoute10.ERROR_REVERSAL_TOPUP_REQ;

      Assert.assertEquals("debe ser intento 5", 5, lastProcessorMetadata.getRetry());
      Assert.assertFalse("no debe ser redirect", lastProcessorMetadata.isRedirect());
      Assert.assertTrue("debe ser endpoint " + endpoint, lastProcessorMetadata.getEndpoint().contains(endpoint));
    }
  }


  @Test
  public void testPendingTopupReverseOk() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    System.out.println(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,0);

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());

    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());

  }

  @Test
  public void testPendingTopupReverseOkMovementTimeOutResponseError1() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,0);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PENDING, prepaidMovementReverseResp.getEstado());
    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Cantidad de intentos: ",2,remoteTopup.getRetryCount());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
    }
  }

  @Test
  public void testPendingTopupReverseOkMovementTimeOutResponseError2() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);


    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));
    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);

    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction,PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);
    inclusionMovimientosTecnocom(prepaidCard,prepaidMovement);

    PrepaidMovement10 prepaidReverseMovement = buildReversePrepaidMovement10(prepaidUser,prepaidTopup);
    prepaidReverseMovement = createPrepaidMovement10(prepaidReverseMovement);

    String messageId = sendPendingTopupReverse(prepaidTopup, prepaidCard, user, prepaidUser, prepaidReverseMovement,0);
    // Primera vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PENDING, prepaidMovementReverseResp.getEstado());
    }
    // Segunda vez
    {
      //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
      Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
      ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

      Assert.assertNotNull("Deberia existir un topup", remoteTopup);
      Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
      Assert.assertEquals("Cantidad de intentos: ",2,remoteTopup.getRetryCount());
      System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
      Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
      Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

      PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();
      Thread.sleep(2000);
      Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
      Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
      Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());
    }
  }

}
