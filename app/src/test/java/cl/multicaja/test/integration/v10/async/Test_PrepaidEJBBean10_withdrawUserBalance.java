package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.core.exceptions.RunTimeValidationException;
import cl.multicaja.prepaid.async.v10.model.PrepaidReverseData10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Queue;
import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.TARJETA_ERROR_GENERICO_$VALUE;

/**
 * @author abarazarte
 **/
public class Test_PrepaidEJBBean10_withdrawUserBalance extends TestBaseUnitAsync {


  @Test
  public void withdrawFail_timeoutResponse() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    InclusionMovimientosDTO mov =  topupInTecnocom(account.getAccountNumber(), prepaidCard10, BigDecimal.valueOf(10000));
    Assert.assertEquals("Carga OK", "000", mov.getRetorno());
    Thread.sleep(2000);

    NewPrepaidWithdraw10 prepaidWithdraw = buildNewPrepaidWithdrawV2(getRandomNumericString(15));
    PrepaidWithdraw10 withdraw = null;

    String messageId = "";
    TecnocomServiceHelper.getInstance().getTecnocomService().setAutomaticError(Boolean.TRUE);
    TecnocomServiceHelper.getInstance().getTecnocomService().setRetorno("1020");
    try {
      withdraw = getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser.getUuid(), prepaidWithdraw,true);
      Assert.fail("No debe pasar por acá");
    } catch(RunTimeValidationException rvex) {

      Assert.assertEquals("Debe tener codigo", TARJETA_ERROR_GENERICO_$VALUE.getValue(), rvex.getCode());
      Assert.assertEquals("Debe tener value y messageId", 2, rvex.getData().length);
      Assert.assertNotNull("Debe tener messageId",rvex.getData()[1].getValue());
      Assert.assertFalse("Debe tener messageId", StringUtils.isBlank(String.valueOf(rvex.getData()[1].getValue())));
      messageId = String.valueOf(rvex.getData()[1].getValue());
    }
    TecnocomServiceHelper.getInstance().getTecnocomService().setAutomaticError(Boolean.FALSE);
    TecnocomServiceHelper.getInstance().getTecnocomService().setRetorno("1020");


    Thread.sleep(1500);

    PrepaidMovement10 dbPrepaidMovement = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia tener status " + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovement.getEstado());
    Assert.assertEquals("Deberia Business status " + BusinessStatusType.REVERSED, BusinessStatusType.REVERSED, dbPrepaidMovement.getEstadoNegocio());

    Queue qResp = camelFactory.createJMSQueue(TransactionReversalRoute10.PENDING_REVERSAL_WITHDRAW_RESP);
    ExchangeData<PrepaidReverseData10> reverseWithdraw = (ExchangeData<PrepaidReverseData10>) camelFactory.createJMSMessenger().getMessage(qResp, messageId);

    Assert.assertNotNull("Deberia existir un reversa", reverseWithdraw);
    Assert.assertNotNull("Deberia existir una reversa", reverseWithdraw.getData());

    PrepaidMovement10 dbPrepaidMovementReverse = getPrepaidMovementEJBBean10().getPrepaidMovementByIdTxExterno(prepaidWithdraw.getTransactionId(), PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.CORRECTORA);
    Assert.assertNotNull("Deberia tener un movimiento", dbPrepaidMovement);
    Assert.assertEquals("Deberia estar en status PROCESS_OK" + PrepaidMovementStatus.PROCESS_OK, PrepaidMovementStatus.PROCESS_OK, dbPrepaidMovementReverse.getEstado());

  }
}
