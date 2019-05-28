package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.model.v10.MovimientoTecnocom10;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.OriginOpeType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoPais;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_TecnocomReconciliationEJBBean10_eliminarMovimientos extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void beforeClass(){
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_archivos_conciliacion CASCADE", getSchema()));
  }

  @Test
  public void testEliminaMovimientosOk() throws Exception {

    Map<String, Object> respFile = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.insertArchivoReconcialicionLog(getRandomString(10),"TEST","TEST","PEND");
    Long fileId = numberUtils.toLong(respFile.get("_r_id"));

    List<MovimientoTecnocom10> movimientoTecnocom10s = new ArrayList<>();

    for(int i=0;i<3;i++){
      MovimientoTecnocom10 movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.buildRandomTcMov(fileId);
      Assert.assertNotNull("Movimiento Tc no debe ser null",movTec);
      // Inserta movimiento
      movTec = Test_TecnocomReconciliationEJBBean10_insertaMovimientos.inserTcMov(movTec);
      movimientoTecnocom10s.add(movTec);
      Assert.assertNotNull("Movimiento Tc despues de insertar no debe ser null",movTec);
      Assert.assertNotEquals("Id debe ser != 0",0,movTec.getId().intValue());
    }

    List<MovimientoTecnocom10> movimientoTecnocom10s2 = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN);
    Assert.assertEquals("Debe existir 3 movimientos",3, movimientoTecnocom10s2.size());
    
    getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(fileId);

    List<MovimientoTecnocom10> respPosElim = getTecnocomReconciliationEJBBean10().buscaMovimientosTecnocom(fileId, OriginOpeType.SAT_ORIGIN);
    Assert.assertNull("No deben existir movimientos Resp Null",  respPosElim);

  }

  @Test (expected = BadRequestException.class)
  public void testEliminaMovimientosNoOK_f1() throws BadRequestException {
    try{
      getTecnocomReconciliationEJBBean10().eliminaMovimientosTecnocom(null);
    }catch (BadRequestException e){
      Assert.assertEquals("Debe ser: ",e.getData()[0].getValue(),"fileId");
      throw new BadRequestException();
    } catch (Exception e) {
      Assert.fail("No debe caer aca");
    }
  }

}
