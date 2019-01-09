package cl.multicaja.test.integration.v10.async;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Test;

import java.math.BigDecimal;

public class Test_PrepaidMovementEJB10_procesReconciliation10 extends TestBaseUnitAsync {

  // Se hace movimiento contrario al no estar conciliado con el switch (TOPUP)
  @Test
  public void processReconciliationCase2Topup() throws Exception {

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

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);


  }
  // Se hace movimiento contrario al no estar conciliado con el switch (WITHDRAW)
  @Test
  public void processReconciliationCase2Withdraw() throws Exception {

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

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

  }
  // Se hace movimiento contrario al no estar conciliado con el switch (TOPUP) STATUS ERROR
  @Test
  public void processReconciliationCase6Topup() throws Exception {

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

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

  }
  // Se hace movimiento contrario al no estar conciliado con el switch (WITHDRAW) STATUS ERROR RESPONSE
  @Test
  public void processReconciliationCase7Withdraw() throws Exception {

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

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);


  }


}
