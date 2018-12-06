package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test_PrepaidAccountingEJBBean10_processMovementForAccounting extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void dateNull()throws Exception {
    getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), null);
    Assert.fail("Should not be here");
  }

  @Test
  public void processMovementForAccounting()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      Thread.sleep(1000);

      List<Accounting10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      Assert.assertEquals("Debe ser 3 ", 3,accountinMovements.size());

      for (Accounting10 m : accountinMovements) {
        Assert.assertNotNull("Debe tener id", m.getId());

        Long originalMovement = originalMovementsIds.stream()
          .filter(mov -> mov.equals(m.getIdTransaction()))
          .findAny()
          .orElse(null);

        Assert.assertNotNull("Debe estar entre los movimientos insertados", originalMovement);

      }
    }
  }

  @Test
  public void nothingToProcessMovementForAccounting()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

      List<Long> originalMovementsIds = new ArrayList<>();

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));
      originalMovementsIds.add(prepaidMovement10.getId());

      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      Thread.sleep(1000);

      List<Accounting10> accountinMovements = getPrepaidAccountingEJBBean10().processMovementForAccounting(getDefaultHeaders(), utc.toLocalDateTime());

      Assert.assertEquals("Debe ser 0", 0, accountinMovements.size());
    }
  }

  private String getNewDate(Integer days, Timestamp ts) {
    LocalDateTime dt = ts.toLocalDateTime();
    dt = dt.minusDays(days);
    return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private void updateDate(Long idMovimiento, String newDate)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento_conciliado SET fecha_registro = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')"
        + "WHERE id_mov_ref = " + idMovimiento.toString());
  }
}