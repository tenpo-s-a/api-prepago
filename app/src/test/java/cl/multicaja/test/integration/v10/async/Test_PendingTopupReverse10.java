package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

/**
 * @autor abarazarte
 */
public class Test_PendingTopupReverse10 extends TestBaseUnitAsync {


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
  public void testPendingTopupReverseOkMovementTimeOutResponseError() throws Exception {

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

    //se verifica que el mensaje haya sido procesado y lo busca en la cola de respuestas Reversa de cargas pendientes
    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_TOPUP_RESP);
    ExchangeData<PrepaidReverseData10> remoteTopup = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup);
    Assert.assertNotNull("Deberia existir un topup", remoteTopup.getData());
    Assert.assertEquals("Deberia tener un reintento" ,2,remoteTopup.getRetryCount());
    System.out.println("Steps: " + remoteTopup.getProcessorMetadata());

    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidTopup.getId(), remoteTopup.getData().getPrepaidTopup10().getId());
    Assert.assertEquals("Deberia ser igual al enviado al procesdo por camel", prepaidUser.getId(), remoteTopup.getData().getPrepaidUser10().getId());
    Assert.assertNotNull("Deberia tener una PrepaidCard", remoteTopup.getData().getPrepaidCard10());

    PrepaidMovement10 prepaidMovementReverseResp = remoteTopup.getData().getPrepaidMovementReverse();

    Assert.assertNotNull("Deberia existir un prepaidMovement", prepaidMovementReverseResp);
    Assert.assertEquals("Deberia contener una codent", prepaidMovement.getCodent(), prepaidMovementReverseResp.getCodent());
    Assert.assertEquals("El movimiento debe ser procesado exitosamente", PrepaidMovementStatus.PROCESS_OK, prepaidMovementReverseResp.getEstado());

  }
}
