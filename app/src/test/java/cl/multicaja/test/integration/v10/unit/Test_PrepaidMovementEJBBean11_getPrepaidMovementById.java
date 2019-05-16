package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;

import cl.multicaja.prepaid.model.v10.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.*;

public class Test_PrepaidMovementEJBBean11_getPrepaidMovementById extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void findById_movementId_null() throws Exception {
    try {
      getPrepaidMovementEJBBean11().getPrepaidMovementById(null);
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void findById_movement_null() throws Exception {
    PrepaidMovement10 movement = getPrepaidMovementEJBBean11().getPrepaidMovementById(Long.MAX_VALUE);
    Assert.assertNull(movement);
  }

  @Test
  public void findById() throws Exception {
    PrepaidUser10 user = buildPrepaidUserv2();
    user = createPrepaidUserV2(user);

    PrepaidTopup10 topup = buildPrepaidTopup10();

    PrepaidMovement10 movement = buildPrepaidMovement11(user, topup);
    movement.setConSwitch(ReconciliationStatusType.RECONCILED);
    movement.setConTecnocom(ReconciliationStatusType.RECONCILED);
    movement.setEstado(PrepaidMovementStatus.PROCESS_OK);
    movement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    movement.setCardId(Long.MAX_VALUE);

    movement = createPrepaidMovement10(movement);

    PrepaidMovement10 dbMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(movement.getId());

    Assert.assertNotNull(dbMovement);
    Assert.assertEquals("Debe ser el mismo movimiento", movement.getId(), dbMovement.getId());
    Assert.assertEquals("Debe ser el mismo movimiento", movement.getCentalta(), dbMovement.getCentalta());
    Assert.assertEquals("Debe ser el mismo movimiento", movement.getIdTxExterno(), dbMovement.getIdTxExterno());
    Assert.assertEquals("Debe ser el mismo movimiento", movement.getNumaut(), dbMovement.getNumaut());
    Assert.assertEquals("Debe ser el mismo movimiento", movement.getPan(), dbMovement.getPan());

  }
}
