package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementFee10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidMovementEJBBean11_getPrepaidMovementFeeByMovementId extends TestBaseUnit {
  @Test
  public void getPrepaidMovementFeeByMovementId_ok() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    ArrayList<PrepaidMovementFee10> insertedMovements = new ArrayList<>();

    // Insertar 2 fees a este movimiento
    PrepaidMovementFee10 fee = buildPrepaidMovementFee10(prepaidMovement);
    fee = createPrepaidMovementFee10(fee);
    insertedMovements.add(fee);

    fee = buildPrepaidMovementFee10(prepaidMovement);
    fee.setFeeType(PrepaidMovementFeeType.TOPUP_POS_FEE);
    fee = createPrepaidMovementFee10(fee);
    insertedMovements.add(fee);

    // Insertar una tercera fee de otro movimiento
    PrepaidMovement10 prepaidMovement2 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
    prepaidMovement2 = createPrepaidMovement10(prepaidMovement2);
    fee = buildPrepaidMovementFee10(prepaidMovement2);
    createPrepaidMovementFee10(fee);

    // Buscar con el primer id
    List<PrepaidMovementFee10> results = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(prepaidMovement.getId());

    Assert.assertNotNull("Debe existir", results);
    Assert.assertEquals("Debe tener tamaño 2", 2, results.size());

    for(PrepaidMovementFee10 insertedFee : insertedMovements) {
      PrepaidMovementFee10 foundFee = results.stream().filter(f -> f.getId().equals(insertedFee.getId())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir", foundFee);
      Assert.assertEquals("Debe tener mismo id mov", insertedFee.getMovementId(), foundFee.getMovementId());
      Assert.assertEquals("Debe tener mismo tipo fee", insertedFee.getFeeType(), foundFee.getFeeType());
      Assert.assertEquals("Debe tener mismo monto", insertedFee.getAmount(), foundFee.getAmount());
      Assert.assertEquals("Debe tener mismo iva", insertedFee.getIva(), foundFee.getIva());
    }

    // Buscar con Id que no existe
    List<PrepaidMovementFee10> emptyResults = getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(Long.MAX_VALUE);

    Assert.assertNotNull("Debe existir", emptyResults);
    Assert.assertEquals("Debe tener tamaño 0", 0, emptyResults.size());
  }

  @Test(expected = BadRequestException.class)
  public void getPrepaidMovementFeeByMovementId_nullId() throws Exception {
    getPrepaidMovementEJBBean11().getPrepaidMovementFeesByMovementId(null);
  }
}
