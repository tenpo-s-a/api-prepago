package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.MovimientoTecnocom10;
import cl.multicaja.prepaid.model.v10.OriginOpeType;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.SchemaOutputResolver;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_TecnocomReconciliationEJBBean10_buscaMovimientos extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.prp_movimientos_tecnocom", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.prp_movimientos_tecnocom_hist", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.prp_archivos_conciliacion", getSchema()));
  }

  @Test
  public void testBuscaMovimientoOk() throws Exception {

    Map<String, Object> respFile = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.insertArchivoReconcialicionLog(getRandomString(10), "TEST", "TEST", "PEND");
    Long fileId = numberUtils.toLong(respFile.get("_r_id"));

    respFile = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.insertArchivoReconcialicionLog(getRandomString(10), "TEST", "TEST", "PEND");
    Long fileId2 = numberUtils.toLong(respFile.get("_r_id"));

    List<MovimientoTecnocom10> movimientoTecnocom10s = new ArrayList<>();

    MovimientoTecnocom10 movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.buildRandomTcMov(fileId);
    movTec.setPan("8378623447");
    movTec.setIndNorCor(0);
    movTec.setTipoFac(TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA);
    movTec.setNumAut("834738");
    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.inserTcMov(movTec);
    Assert.assertNotEquals("Id debe ser != 0", 0, movTec.getId().intValue());
    movimientoTecnocom10s.add(movTec);

    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.buildRandomTcMov(fileId);
    movTec.setPan("2389739345");
    movTec.setIndNorCor(1);
    movTec.setTipoFac(TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA);
    movTec.setNumAut("756345");
    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.inserTcMov(movTec);
    Assert.assertNotEquals("Id debe ser != 0", 0, movTec.getId().intValue());
    movimientoTecnocom10s.add(movTec);

    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.buildRandomTcMov(fileId);
    movTec.setPan("47636355463");
    movTec.setIndNorCor(1);
    movTec.setTipoFac(TipoFactura.ANULA_COMISION_APERTURA);
    movTec.setNumAut("235334");
    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.inserTcMov(movTec);
    Assert.assertNotEquals("Id debe ser != 0", 0, movTec.getId().intValue());
    movimientoTecnocom10s.add(movTec);

    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.buildRandomTcMov(fileId2);
    movTec.setPan("57647385756748");
    movTec.setIndNorCor(1);
    movTec.setTipoFac(TipoFactura.ANULA_COMISION_APERTURA);
    movTec.setNumAut("457674");
    movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.inserTcMov(movTec);
    Assert.assertNotEquals("Id debe ser != 0", 0, movTec.getId().intValue());
    movimientoTecnocom10s.add(movTec);

    List<MovimientoTecnocom10> movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN);
    Assert.assertEquals("Debe existir 3 movimientos", 3, movimientoTecnocom10s2.size());
    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(i), movimientoTecnocom10s2.get(i)));
    }

    // Debe buscar en la tabla por default
    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN, null, null, null, null, null);
    Assert.assertEquals("Debe existir 3 movimientos", 3, movimientoTecnocom10s2.size());
    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(i), movimientoTecnocom10s2.get(i)));
    }

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom("prp_movimientos_tecnocom_hist", fileId, OriginOpeType.SAT_ORIGIN, null, null, null, null, null);
    Assert.assertEquals("Debe existir 3 movimientos", 3, movimientoTecnocom10s2.size());
    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(i), movimientoTecnocom10s2.get(i)));
    }

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, null, movimientoTecnocom10s.get(0).getPan(), null, null, null, null);
    Assert.assertEquals("Debe existir 1 movimiento", 1, movimientoTecnocom10s2.size());
    Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(0), movimientoTecnocom10s2.get(0)));

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, null, null, IndicadorNormalCorrector.NORMAL, null, null, null);
    Assert.assertEquals("Debe existir 1 movimiento", 1, movimientoTecnocom10s2.size());
    Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(0), movimientoTecnocom10s2.get(0)));

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, null, null, null, TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA, null, null);
    Assert.assertEquals("Debe existir 2 movimientos", 2, movimientoTecnocom10s2.size());
    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(i), movimientoTecnocom10s2.get(i)));
    }

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, null, null, null, null, java.sql.Date.valueOf(movimientoTecnocom10s.get(0).getFecFac()), null);
    Assert.assertEquals("Debe existir 4 movimientos", 4, movimientoTecnocom10s2.size());
    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(i), movimientoTecnocom10s2.get(i)));
    }

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, null, null, null, null, null, movimientoTecnocom10s.get(0).getNumAut());
    Assert.assertEquals("Debe existir 1 movimiento", 1, movimientoTecnocom10s2.size());
    Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(0), movimientoTecnocom10s2.get(0)));

    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, null, null, null, null, null, null);
    Assert.assertEquals("Debe existir 4 movimientos", 4, movimientoTecnocom10s2.size());
    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertTrue("Deben ser iguales", compareMovs(movimientoTecnocom10s.get(i), movimientoTecnocom10s2.get(i)));
    }

    // Buscar por tipo de factura
    String tiposFactura = TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.toString();
    movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN, tiposFactura);
    Assert.assertEquals("Debe existir 2 movimientos", 2, movimientoTecnocom10s2.size());
    for (MovimientoTecnocom10 foundMovement : movimientoTecnocom10s2) {
      MovimientoTecnocom10 insertedMovement = movimientoTecnocom10s.stream().filter(m -> m.getId().equals(foundMovement.getId())).findAny().orElse(null);
      Assert.assertNotNull("Debe existir", insertedMovement);
      Assert.assertTrue("Deben ser iguales", compareMovs(insertedMovement, foundMovement));
    }
  }

  boolean compareMovs(MovimientoTecnocom10 insertedMov, MovimientoTecnocom10 foundMov) {
    Assert.assertEquals("Deben tener mismo fileId", insertedMov.getIdArchivo(), foundMov.getIdArchivo());
    Assert.assertEquals("Deben tener mismo cuenta", insertedMov.getCuenta(), foundMov.getCuenta());
    Assert.assertEquals("Deben tener mismo pan", insertedMov.getPan(), foundMov.getPan());
    System.out.println("Tipo fac inserted: " + insertedMov.getTipoFac().getCode() + " : " + insertedMov.getTipoFac().getCorrector());
    System.out.println("Tipo fac found: " + foundMov.getTipoFac().getCode() + " : " + foundMov.getTipoFac().getCorrector());
    Assert.assertEquals("Deben tener mismo tipofac", insertedMov.getTipoFac(), foundMov.getTipoFac());
    Assert.assertEquals("Deben tener mismo numaut", insertedMov.getNumAut(), foundMov.getNumAut());
    Assert.assertEquals("Deben tener mismo impfac", insertedMov.getImpFac().getValue().stripTrailingZeros(), foundMov.getImpFac().getValue().stripTrailingZeros());
    return true;
  }


  @Test(expected = BadRequestException.class)
  public void testBuscaMovimientoNoOK_f1() throws BadRequestException {
    try {
      List<MovimientoTecnocom10> movTec2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null, 23L, OriginOpeType.SAT_ORIGIN, null, null, null, null, null);
    } catch (BadRequestException e) {
      Assert.assertEquals("Debe ser: ", e.getData()[0].getValue(), "tableName");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

}
