package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.Test;

import java.math.BigDecimal;

public class Test_PrepaidMovementEJBBean10_processClearingResolution extends TestBaseUnit {
  @Test
  public void processClearingResolution() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);



    //getPrepaidMovementEJBBean10().processClearingResolution(prepaidMovement10, clearingData10);


  }
}
