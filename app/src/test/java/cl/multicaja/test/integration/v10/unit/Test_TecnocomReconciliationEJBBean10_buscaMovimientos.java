package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.MovimientoTecnocom10;
import cl.multicaja.prepaid.model.v10.OriginOpeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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

    List<MovimientoTecnocom10> movimientoTecnocom10s = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      MovimientoTecnocom10 movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.buildRandomTcMov(fileId);
      Assert.assertNotNull("Movimiento Tc no debe ser null", movTec);
      // Inserta movimiento
      movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.inserTcMov(movTec);
      movimientoTecnocom10s.add(movTec);
      Assert.assertNotNull("Movimiento Tc despues de insertar no debe ser null", movTec);
      Assert.assertNotEquals("Id debe ser != 0", 0, movTec.getId().intValue());
    }

    List<MovimientoTecnocom10> movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN);
    Assert.assertEquals("Debe existir 3 movimientos", 3, movimientoTecnocom10s2.size());

    for (int i = 0; i < movimientoTecnocom10s2.size(); i++) {
      Assert.assertEquals("Deben tener el mismo id", movimientoTecnocom10s.get(i).getId(), movimientoTecnocom10s2.get(i).getId());
      Assert.assertEquals("Deben tener el mismo pan", movimientoTecnocom10s.get(i).getPan(), movimientoTecnocom10s2.get(i).getPan());
      Assert.assertEquals("Deben tener el mismo numaut", movimientoTecnocom10s.get(i).getNumAut(), movimientoTecnocom10s2.get(i).getNumAut());
    }
  }


  @Test(expected = BadRequestException.class)
  public void testBuscaMovimientoNoOK_f1() throws BadRequestException {
    try {
      List<MovimientoTecnocom10> movTec2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(null,OriginOpeType.SAT_ORIGIN);
    } catch (BadRequestException e) {
      Assert.assertEquals("Debe ser: ", e.getData()[0].getValue(), "fileId");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

  @Test (expected = BadRequestException.class)
  public void testBuscaMovimientoNoOK_f2() throws BadRequestException {
    try {

      Map<String, Object> respFile = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.insertArchivoReconcialicionLog(getRandomString(10), "TEST", "TEST", "PEND");
      Long fileId = numberUtils.toLong(respFile.get("_r_id"));

      List<MovimientoTecnocom10> movTec2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId,null);

    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"originope");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }
}
