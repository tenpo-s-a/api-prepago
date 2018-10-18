package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_updatePendingPrepaidMovementsSwitchStatus extends TestBaseUnit {

  @After
  public void afterEachTest() {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", SCHEMA));
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest1() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null,null,null, null, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest2() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "20180803000000", null, null, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest3() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "20180803000000", "20180803000000", null, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest4() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "20180803000000", "20180803000000", PrepaidMovementType.TOPUP, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest5() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "20180803000000", "20180803000000", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest6() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "2018003", "20180803000000", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest7() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "20180803000000", "2018-08-03", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, null);
  }

  @Test
  public void updateOk() throws  Exception {
    fillDB();

    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsSwitchStatus(null, "20180803000000", "20180803235959999", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL, ConciliationStatusType.NOT_RECONCILED);

    List resultList = searchAllMovements();

    String tipoMovimiento = PrepaidMovementType.TOPUP.toString();
    Integer indnorcor = IndicadorNormalCorrector.NORMAL.getValue();
    Timestamp startDateTs = Timestamp.valueOf("2018-08-03 00:00:00");
    Timestamp endDateTs = Timestamp.valueOf("2018-08-03 23:59:59.999");

    int notReconciledCount = 0;

    for (Object object: resultList) {
      Map<String, Object> movement = (Map<String, Object>) object;
      Timestamp movementCreationDate = (Timestamp) movement.get("fecha_creacion");
      String movementTipoMov = (String) movement.get("tipo_movimiento");
      Integer movementIndNorCor = ((BigDecimal)movement.get("indnorcor")).intValue();
      String switchStatus = (String) movement.get("estado_con_switch");

      if (switchStatus.equals(ConciliationStatusType.NOT_RECONCILED.getValue())) {
        boolean includedBetweenDates = !movementCreationDate.before(startDateTs) && !movementCreationDate.after(endDateTs);
        Assert.assertTrue("Debe estar adentro de las fechas [2018/08/03-2018/08/04[", includedBetweenDates);
        Assert.assertEquals("Debe ser tipo mov " + tipoMovimiento, tipoMovimiento, movementTipoMov);
        Assert.assertEquals("Debe tener indnorcor " + indnorcor, indnorcor, movementIndNorCor);
        notReconciledCount++;
      }
      else {
        boolean excludedFromDates = movementCreationDate.before(startDateTs) || movementCreationDate.after(endDateTs);
        boolean wrongTipoMovimiento = !tipoMovimiento.equals(movementTipoMov);
        boolean wrongIndNorCor = !indnorcor.equals(movementIndNorCor);
        boolean alreadyReconciled = switchStatus.equals(ConciliationStatusType.RECONCILED.getValue());
        Assert.assertTrue("Debe estar fuera de fecha, distinto tipofac o distinto indnorcor.", excludedFromDates || wrongTipoMovimiento || wrongIndNorCor || alreadyReconciled);
      }
    }

    Assert.assertEquals("Deben haber 4 movimientos no conciliados", 4, notReconciledCount);
  }

  static public void changeMovement(Object idMovimiento, String newDate, PrepaidMovementType tipoMovimiento, Integer indnorcor)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET fecha_creacion = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS')::timestamp without time zone, "
        + "indnorcor = " + indnorcor + ", "
        + "tipo_movimiento = '" + tipoMovimiento.toString() + "' "
        + "WHERE ID = " + idMovimiento.toString());
  }

  static public List searchAllMovements()  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    return DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento", SCHEMA));
  }

  public void fillDB() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    // Agregamos movimientos con fecha de modo que 4 movimientos validos esten dentro del dia 2018-08-03.

    // Dentro
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 21:14:09", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Dentro
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 17:43:54", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Dentro, limite
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 00:00:00", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Dentro, limite
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 23:59:59", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Fuera, por fecha
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-02 10:05:16", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Fuera, por fecha
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-04 15:52:42", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Fuera, por fecha limite
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-02 23:59:59", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Fuera, por fecha limite
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-04 00:00:00", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());

    // Fuera, por tipo movimiento
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 11:52:10", PrepaidMovementType.WITHDRAW, IndicadorNormalCorrector.NORMAL.getValue());

    // Fuera, por indnorcor
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 07:43:54", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.CORRECTORA.getValue());

    // Fuera, porque ya esta reconciliado
    prepaidMovement10.setConSwitch(ConciliationStatusType.RECONCILED);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-08-03 14:06:13", PrepaidMovementType.TOPUP, IndicadorNormalCorrector.NORMAL.getValue());
  }
}
