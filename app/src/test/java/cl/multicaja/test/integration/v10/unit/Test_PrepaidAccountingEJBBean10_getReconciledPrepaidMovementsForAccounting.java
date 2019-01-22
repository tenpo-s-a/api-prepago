package cl.multicaja.test.integration.v10.unit;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Test_PrepaidAccountingEJBBean10_getReconciledPrepaidMovementsForAccounting extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
<<<<<<< HEAD
=======

>>>>>>> master
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void dateNull()throws Exception {
      List<PrepaidMovement10> lstMovement10s = getPrepaidAccountingEJBBean10().getReconciledPrepaidMovementsForAccounting(getDefaultHeaders(), null);
      Assert.fail("Should not be here");
  }

  @Test
  public void searchMovementForConciliate()throws Exception {
    {
      User user = registerUser();
      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);

      // CREA MOVIMIENTOS
      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.NEED_VERIFICATION);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      /**
       * Agrega movimiento en tabla de contabilidad con el mismo ID de movimiento pero con origen IPM
       */
      addAccountingIpmTrx(prepaidMovement10.getId());

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.PENDING);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));

      // Agrega movimiento reversado
      prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setEstadoNegocio(BusinessStatusType.REVERSED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      prepaidMovement10 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      getPrepaidMovementEJBBean10().createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.RECONCILED);
      updateDate(prepaidMovement10.getId(), getNewDate(2, prepaidMovement10.getFechaCreacion()));



      ZonedDateTime utc = Instant.now().atZone(ZoneId.of("UTC"));

      Thread.sleep(1000);
      List<PrepaidMovement10> lstMovement10s = getPrepaidAccountingEJBBean10().getReconciledPrepaidMovementsForAccounting(getDefaultHeaders(), utc.toLocalDateTime());
      Assert.assertEquals("Debe ser 3 ", 3,lstMovement10s.size());

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

  private void addAccountingIpmTrx(Long idMovimiento)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");

    DBUtils.getInstance().getJdbcTemplate().execute(
      String.format("INSERT INTO %s.accounting (id_tx, \"type\",accounting_mov, origin, amount, " +
        "currency, amount_usd,amount_mcar, exchange_rate_dif, fee, fee_iva,collector_fee,collector_fee_iva,amount_balance, " +
        "transaction_date,conciliation_date,status,file_id, create_date, update_date) " +
        "VALUES(%s, 'CARGA_POS','CARGA_POS', 'IPM', 3119.00, 152, 0.00, 0.00, 0.00, 119.00, 19.00,119.00, 19.00, 0 , " +
        "'2019-01-30 08:01:01.000','2019-01-30 08:01:01.000','OK',0,'2018-12-27 20:01:02.048', " +
        " '2018-12-27 20:01:02.048');", SCHEMA, idMovimiento));
  }
}
