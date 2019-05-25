package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class Test_PrepaidMovementEJBBean11_getPrepaidMovements extends TestBaseUnit {
  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", getSchema()));
  }

  @Test
  public void getPrepaidMovement_allOK() throws Exception {
    PrepaidUser10 prepaidUser10 = buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);

    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser10,account);
    card = createPrepaidCardV2(card);

    PrepaidTopup10 topup = buildPrepaidTopup10();

    PrepaidMovement10 movement = buildPrepaidMovement11(prepaidUser10, topup, card, null, PrepaidMovementType.TOPUP,false);
    movement.setCodcom(getRandomString(10));
    movement.setIdMovimientoRef(1L);
    movement.setIdTxExterno("uno");
    movement.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE);
    movement.setCuenta("");
    movement.setClamon(CodigoMoneda.CHILE_CLP);
    movement.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    movement.setTipofac(TipoFactura.COMPRA_COMERCIO_RELACIONADO);
    movement.setNumaut("");
    movement.setConSwitch(ReconciliationStatusType.COUNTER_MOVEMENT);
    movement.setConTecnocom(ReconciliationStatusType.NEED_VERIFICATION);
    movement.setOriginType(MovementOriginType.OPE);
    movement.setPan("123");
    movement = createPrepaidMovement11(movement);

    PrepaidMovement10 movement2 = buildPrepaidMovement11(prepaidUser10, topup, card, null, PrepaidMovementType.TOPUP,false);
    movement2.setCodcom(getRandomString(10));
    movement2.setIdMovimientoRef(2L);
    movement2.setIdTxExterno("dos");
    movement2.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_PENDING_TOPUP);
    movement2.setCuenta("");
    movement2.setClamon(CodigoMoneda.CHILE_CLP);
    movement2.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    movement2.setTipofac(TipoFactura.RETIRO_TRANSFERENCIA);
    movement2.setNumaut("2");
    movement2.setConSwitch(ReconciliationStatusType.COUNTER_MOVEMENT);
    movement2.setConTecnocom(ReconciliationStatusType.COUNTER_MOVEMENT);
    movement2.setOriginType(MovementOriginType.SAT);
    movement2.setPan("1234");
    movement2 = createPrepaidMovement11(movement2);

    PrepaidMovement10 movement3 = buildPrepaidMovement11(prepaidUser10, topup, card, null, PrepaidMovementType.WITHDRAW,false);
    movement3.setCodcom(getRandomString(10));
    movement3.setIdMovimientoRef(3L);
    movement3.setIdTxExterno("tres");
    movement3.setEstado(PrepaidMovementStatus.ERROR_IN_PROCESS_CARD_ISSUANCE_FEE);
    movement3.setCuenta("123");
    movement3.setClamon(CodigoMoneda.CHILE_CLP);
    movement3.setIndnorcor(IndicadorNormalCorrector.NORMAL);
    movement3.setTipofac(TipoFactura.CARGA_TRANSFERENCIA);
    movement3.setNumaut("3");
    movement3.setConSwitch(ReconciliationStatusType.NO_CASE);
    movement3.setConTecnocom(ReconciliationStatusType.RECONCILED);
    movement3.setOriginType(MovementOriginType.SAT);
    movement3.setPan("12345");
    movement3 = createPrepaidMovement11(movement3);

    List<PrepaidMovement10> getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 3", 3, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(movement.getId(), null, null,
      null, null, null, null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, movement.getIdMovimientoRef(), null,
      null, null, null, null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, movement.getIdPrepaidUser(),
      null, null, null, null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 3", 3, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null, movement.getIdTxExterno(),
      null, null, null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, PrepaidMovementType.TOPUP, null, null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 2", 2, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, movement.getEstado(), null, null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 2", 2, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, movement3.getCuenta(), null, null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, movement3.getClamon(), null, null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 3", 3, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, movement.getIndnorcor(), null, null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 3", 3, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, movement.getTipofac(), null, null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, (java.sql.Date)movement.getFecfac(), null, null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 3", 3, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2, movement3);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, movement.getNumaut(), null,
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, null, movement.getConSwitch(),
      null, null, null, null);

    Assert.assertEquals("Deberia tener largo 2", 2, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement, movement2);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, null, null,
      movement.getConTecnocom(), null, null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, null, null,
      null, movement.getOriginType(), null, null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, null, null,
      null, null, movement.getPan(), null);

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);

    getPrepaidMovement10List = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null,
      null, null, null, null, null, null, null, null, null, null,
      null, null, null, movement.getCodcom());

    Assert.assertEquals("Deberia tener largo 1", 1, getPrepaidMovement10List.size());
    compareMovements(getPrepaidMovement10List, movement);
  }

  private void compareMovements(List<PrepaidMovement10> foundList, PrepaidMovement10 ... insertedMovements) {
    for (PrepaidMovement10 insertedMovement : insertedMovements) {
      PrepaidMovement10 foundMovement = foundList.stream().filter(m -> m.getId().equals(insertedMovement.getId())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir un movimiento encontrado con el mismo id", foundMovement);
      Assert.assertEquals("Deben tener mismo id mov ref", insertedMovement.getIdMovimientoRef(), foundMovement.getIdMovimientoRef());
      Assert.assertEquals("Deben tener mismo id usuario", insertedMovement.getIdPrepaidUser(), foundMovement.getIdPrepaidUser());
      Assert.assertEquals("Deben tener mismo id externo", insertedMovement.getIdTxExterno(), foundMovement.getIdTxExterno());
      Assert.assertEquals("Deben tener mismo id tarjeta", insertedMovement.getCuenta(), foundMovement.getCuenta());
      Assert.assertEquals("Deben tener mismo tipo movimiento", insertedMovement.getTipoMovimiento(), foundMovement.getTipoMovimiento());
      Assert.assertEquals("Deben tener mismo monto", insertedMovement.getMonto(), foundMovement.getMonto());
      Assert.assertEquals("Deben tener mismo estado", insertedMovement.getEstado(), foundMovement.getEstado());
      Assert.assertEquals("Deben tener mismo estado con switch", insertedMovement.getConSwitch(), foundMovement.getConSwitch());
      Assert.assertEquals("Deben tener mismo estado con tecnocom", insertedMovement.getConTecnocom(), foundMovement.getConTecnocom());
      Assert.assertEquals("Deben tener mismo numaut", insertedMovement.getNumaut(), foundMovement.getNumaut());
    }
  }

}
