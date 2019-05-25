package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.ReconciliationStatusType;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10_updatePendingPrepaidMovementsTecnocomStatus extends TestBaseUnit {

  @Before
  @After
  public void afterEachTest() {

    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest1() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null,null,null, null, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest2() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "20180803", null, null, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest3() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "20180803", "20180803", null, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest4() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "20180803", "20180803", TipoFactura.CARGA_TRANSFERENCIA, null, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest5() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "20180803", "20180803", TipoFactura.CARGA_TRANSFERENCIA, IndicadorNormalCorrector.NORMAL, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest6() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "2018003", "20180803", TipoFactura.CARGA_TRANSFERENCIA, IndicadorNormalCorrector.NORMAL, null);
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateMovementBadRequest7() throws Exception {
    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "20180803", "2018-08-03", TipoFactura.CARGA_TRANSFERENCIA, IndicadorNormalCorrector.NORMAL, null);
  }

  @Test
  public void updateOk() throws  Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 23:04:58", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 14:22:03", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 09:02:51", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-04 23:59:59", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-06 00:00:00", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 15:59:59", TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 00:00:00", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 23:59:59", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 11:59:59", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.CORRECTORA.getValue());

    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
    changeMovement(prepaidMovement10.getId(), "2018-07-05 14:06:13", TipoFactura.CARGA_TRANSFERENCIA.getCode(), IndicadorNormalCorrector.NORMAL.getValue());

    getPrepaidMovementEJBBean10().updatePendingPrepaidMovementsTecnocomStatus(null, "20180705", "20180705", TipoFactura.CARGA_TRANSFERENCIA, IndicadorNormalCorrector.NORMAL, ReconciliationStatusType.NOT_RECONCILED);

    List resultList = searchAllMovements();

    Integer tipofac = TipoFactura.CARGA_TRANSFERENCIA.getCode();
    Integer indnorcor = IndicadorNormalCorrector.NORMAL.getValue();
    Timestamp startDateTs = Timestamp.valueOf("2018-07-05 00:00:00");
    Timestamp endDateTs = Timestamp.valueOf("2018-07-05 23:59:59");

    int notReconciledCount = 0;
    for (Object object: resultList) {
      Map<String, Object> movement = (Map<String, Object>) object;
      Timestamp movementCreationDate = (Timestamp) movement.get("fecha_creacion");
      Integer movementTipoFac = ((BigDecimal)movement.get("tipofac")).intValue();
      Integer movementIndNorCor = ((BigDecimal)movement.get("indnorcor")).intValue();
      String switchStatus = (String) movement.get("estado_con_tecnocom");

      if (switchStatus.equals(ReconciliationStatusType.NOT_RECONCILED.getValue())) {
        boolean includedBetweenDates = !movementCreationDate.before(startDateTs) && !movementCreationDate.after(endDateTs);
        //Assert.assertTrue("Debe estar adentro de las fechas [2018/08/03-2018/08/04[", includedBetweenDates);
        Assert.assertEquals("Debe ser tipo fac " + tipofac, tipofac, movementTipoFac);
        Assert.assertEquals("Debe tener indnorcor " + indnorcor, indnorcor, movementIndNorCor);
        notReconciledCount++;
      }
      else {
        boolean excludedFromDates = movementCreationDate.before(startDateTs) || movementCreationDate.after(endDateTs);
        boolean wrongTipoFac = !tipofac.equals(movementTipoFac);
        boolean wrongIndNorCor = !indnorcor.equals(movementTipoFac);
        Assert.assertTrue("Debe estar fuera de fecha o tener distinto tipofac o distinto indnorcor.", excludedFromDates || wrongTipoFac || wrongIndNorCor);
      }
    }

    Assert.assertEquals("Deben haber 5 movimientos no conciliados", 6, notReconciledCount);
  }

  static public void changeMovement(Object idMovimiento, String newDate, Integer tipofac, Integer indnorcor)  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    DBUtils.getInstance().getJdbcTemplate().execute(
      "UPDATE " + SCHEMA + ".prp_movimiento SET fecha_creacion = "
        + "TO_TIMESTAMP('" + newDate + "', 'YYYY-MM-DD HH24:MI:SS'), "
        + "indnorcor = " + indnorcor + ", "
        + "tipofac = " + tipofac + " "
        + "WHERE ID = " + idMovimiento.toString());
  }

  static public List searchAllMovements()  {
    final String SCHEMA = ConfigUtils.getInstance().getProperty("schema");
    return DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.prp_movimiento", SCHEMA));
  }
}
