package cl.multicaja.test.api.unit;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Test_PrepaidMovementEJBBean10 extends TestBaseUnit {

  private PrepaidMovement10 buildPrepaidMovement(PrepaidUser10 prepaidUser) {
    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setIdMovimientoRef(getUniqueLong());
    prepaidMovement.setIdPrepaidUser(prepaidUser.getId());
    prepaidMovement.setIdTxExterno(getUniqueLong().toString());
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setCodent("AA");
    prepaidMovement.setCentalta("A");
    prepaidMovement.setCuenta(getUniqueInteger().toString());
    prepaidMovement.setClamon(CodigoMoneda.CHILE_CLP);
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
    prepaidMovement.setTipofac(TipoFactura.ANULA_CARGA_EFECTIVO_COMERCIO_MULTICAJA);
    prepaidMovement.setFecfac(new Date(System.currentTimeMillis()));
    prepaidMovement.setNumreffac("123");
    prepaidMovement.setPan("123");
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(BigDecimal.valueOf(1000));
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut("123456");
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom("ABC");
    prepaidMovement.setCodact("XYZ");
    prepaidMovement.setImpliq(1L);
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setCodpais(CodigoPais.CHILE);
    prepaidMovement.setNompob("POB");
    prepaidMovement.setNumextcta(0);
    prepaidMovement.setNummovext(0);
    prepaidMovement.setClamone(CodigoMoneda.CHILE_CLP.getValue());
    prepaidMovement.setTipolin("1234");
    prepaidMovement.setLinref(1);
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(123L);
    return prepaidMovement;
  }

  @Test
  public void testeEjbAddMovement() throws Exception {

    PrepaidUser10 oUser = buildPrepaidUser();

    oUser = getPrepaidEJBBean10().createPrepaidUser(null, oUser);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(oUser);

    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);
    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId() > 0);
  }

  @Test
  public void testeEjbUpdate() throws Exception {

    // CREA USUARIOS
    PrepaidUser10 oUser = buildPrepaidUser();

    oUser = getPrepaidEJBBean10().createPrepaidUser(null, oUser);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(oUser);

    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);
    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId()>0);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,prepaidMovement10.getId(),null,null,null,PrepaidMovementStatus.IN_PROCESS);

    List lstMov = buscaMovimiento(prepaidMovement10.getId());

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser :"+PrepaidMovementStatus.IN_PROCESS.getValue(), PrepaidMovementStatus.IN_PROCESS.getValue(), fila.get("estado"));
  }

  @Test
  public void testeEjbUpdate2() throws Exception {

    // CREA USUARIOS
    PrepaidUser10 oUser = buildPrepaidUser();

    oUser = getPrepaidEJBBean10().createPrepaidUser(null, oUser);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement(oUser);

    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);
    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId()>0);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null, prepaidMovement10.getId(),1,2,CodigoMoneda.CHILE_CLP.getValue(), PrepaidMovementStatus.PROCESS_OK);

    List lstMov = buscaMovimiento(prepaidMovement10.getId());

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());

    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);

    Assert.assertEquals("El estado debe ser :" + PrepaidMovementStatus.PROCESS_OK.getValue(), PrepaidMovementStatus.PROCESS_OK.getValue(), fila.get("estado"));
    Assert.assertEquals("El Num Extracto debe ser 1",1,((BigDecimal)fila.get("numextcta")).intValue());
    Assert.assertEquals("El Num Extracto debe ser 1",2,((BigDecimal)fila.get("nummovext")).intValue());
    Assert.assertEquals("El Num Extracto debe ser 1", CodigoMoneda.CHILE_CLP.getValue().intValue(),((BigDecimal)fila.get("clamone")).intValue());
  }

  public List buscaMovimiento(Object idMovimiento)  {
    ConfigUtils configUtils = ConfigUtils.getInstance();
    String SCHEMA = configUtils.getProperty("schema");
    String SP_NAME = SCHEMA + ".prp_movimiento";
    return dbUtils.getJdbcTemplate().queryForList("SELECT * FROM "+SP_NAME+" WHERE ID ="+idMovimiento);
  }

}
