package cl.multicaja.test.api.unit;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStateType;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class Test_PrepaidEJBBean10 extends TestBaseUnit {


  @Test
  public void testeEjbAddMovement() throws Exception {

      PrepaidUser10 oUser = new PrepaidUser10();
      oUser.setIdUserMc(getUniqueLong());
      oUser.setStatus(PrepaidUserStatus.ACTIVE);
      oUser.setRut(getUniqueRutNumber());

      getPrepaidEJBBean10().createPrepaidUser(null,oUser);

      PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
      prepaidMovement10.setIdMovimientoRef(getUniqueLong());
      prepaidMovement10.setIdUsuario(oUser.getId());
      prepaidMovement10.setIdTxExterno(""+getUniqueLong());
      prepaidMovement10.setTipoMovimiento("CARGA");
      prepaidMovement10.setMonto(BigDecimal.valueOf(getUniqueInteger()));
      prepaidMovement10.setMoneda("USD");
      prepaidMovement10.setEstado(PrepaidMovementStateType.PENDING);
      prepaidMovement10.setCodEntidad("AA");
      prepaidMovement10.setCenAlta("A");
      prepaidMovement10.setCuenta(""+getUniqueInteger());
      prepaidMovement10.setCodMoneda(152);
      prepaidMovement10.setIndNorcor(1);
      prepaidMovement10.setTipoFactura(1);
      prepaidMovement10.setFechaFactura(new Timestamp(System.currentTimeMillis()));
      prepaidMovement10.setNumFacturaRef("123");
      prepaidMovement10.setPan("123");
      prepaidMovement10.setCodMondiv(1);
      prepaidMovement10.setImpDiv(1L);
      prepaidMovement10.setImpFac(1L);
      prepaidMovement10.setCmpApli(1);
      prepaidMovement10.setNumAutorizacion("123456");
      prepaidMovement10.setIndProaje("A");
      prepaidMovement10.setCodComercio("A");
      prepaidMovement10.setCodActividad("A");
      prepaidMovement10.setImpLiq(1L);
      prepaidMovement10.setCodMonliq(1);
      prepaidMovement10.setCodPais(56);
      prepaidMovement10.setNomPoblacion("POB");
      prepaidMovement10.setNumExtracto(1);
      prepaidMovement10.setNumMovExtracto(1);
      prepaidMovement10.setClaveMoneda(1);
      prepaidMovement10.setTipoLinea("1");
      prepaidMovement10.setReferenciaLinea(1);
      prepaidMovement10.setNumBenefCta(1);
      prepaidMovement10.setNumeroPlastico(123L);
      prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);
      Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
      Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId()>0);
  }

  @Test
  public void testeEjbUpdate() throws Exception {

    // CREA USUARIOS
    PrepaidUser10 oUser = new PrepaidUser10();
    oUser.setIdUserMc(getUniqueLong());
    oUser.setStatus(PrepaidUserStatus.ACTIVE);
    oUser.setRut(getUniqueRutNumber());

    getPrepaidEJBBean10().createPrepaidUser(null,oUser);
    // CREA MOVIMIENTO
    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
    prepaidMovement10.setIdMovimientoRef(getUniqueLong());
    prepaidMovement10.setIdUsuario(oUser.getId());
    prepaidMovement10.setIdTxExterno(""+getUniqueLong());
    prepaidMovement10.setTipoMovimiento("CARGA");
    prepaidMovement10.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement10.setMoneda("USD");
    prepaidMovement10.setEstado(PrepaidMovementStateType.PENDING);
    prepaidMovement10.setCodEntidad("AA");
    prepaidMovement10.setCenAlta("A");
    prepaidMovement10.setCuenta(""+getUniqueInteger());
    prepaidMovement10.setCodMoneda(152);
    prepaidMovement10.setIndNorcor(1);
    prepaidMovement10.setTipoFactura(1);
    prepaidMovement10.setFechaFactura(new Timestamp(System.currentTimeMillis()));
    prepaidMovement10.setNumFacturaRef("123");
    prepaidMovement10.setPan("123");
    prepaidMovement10.setCodMondiv(1);
    prepaidMovement10.setImpDiv(1L);
    prepaidMovement10.setImpFac(1L);
    prepaidMovement10.setCmpApli(1);
    prepaidMovement10.setNumAutorizacion("123456");
    prepaidMovement10.setIndProaje("A");
    prepaidMovement10.setCodComercio("A");
    prepaidMovement10.setCodActividad("A");
    prepaidMovement10.setImpLiq(1L);
    prepaidMovement10.setCodMonliq(1);
    prepaidMovement10.setCodPais(56);
    prepaidMovement10.setNomPoblacion("POB");
    prepaidMovement10.setNumExtracto(1);
    prepaidMovement10.setNumMovExtracto(1);
    prepaidMovement10.setClaveMoneda(1);
    prepaidMovement10.setTipoLinea("1");
    prepaidMovement10.setReferenciaLinea(1);
    prepaidMovement10.setNumBenefCta(1);
    prepaidMovement10.setNumeroPlastico(123L);
    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);
    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId()>0);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,prepaidMovement10.getId(),null,null,null,PrepaidMovementStateType.INPROCESS);

    List lstMov = buscaMovimiento(prepaidMovement10.getId());

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);
    Assert.assertEquals("El estado debe ser :"+PrepaidMovementStateType.INPROCESS.getState(), PrepaidMovementStateType.INPROCESS.getState(), fila.get("estado"));


  }
  @Test
  public void testeEjbUpdate2() throws Exception {

    // CREA USUARIOS
    PrepaidUser10 oUser = new PrepaidUser10();
    oUser.setIdUserMc(getUniqueLong());
    oUser.setStatus(PrepaidUserStatus.ACTIVE);
    oUser.setRut(getUniqueRutNumber());

    getPrepaidEJBBean10().createPrepaidUser(null,oUser);
    // CREA MOVIMIENTO
    PrepaidMovement10 prepaidMovement10 = new PrepaidMovement10();
    prepaidMovement10.setIdMovimientoRef(getUniqueLong());
    prepaidMovement10.setIdUsuario(oUser.getId());
    prepaidMovement10.setIdTxExterno(""+getUniqueLong());
    prepaidMovement10.setTipoMovimiento("CARGA");
    prepaidMovement10.setMonto(BigDecimal.valueOf(getUniqueInteger()));
    prepaidMovement10.setMoneda("USD");
    prepaidMovement10.setEstado(PrepaidMovementStateType.PENDING);
    prepaidMovement10.setCodEntidad("AA");
    prepaidMovement10.setCenAlta("A");
    prepaidMovement10.setCuenta(""+getUniqueInteger());
    prepaidMovement10.setCodMoneda(152);
    prepaidMovement10.setIndNorcor(1);
    prepaidMovement10.setTipoFactura(1);
    prepaidMovement10.setFechaFactura(new Timestamp(System.currentTimeMillis()));
    prepaidMovement10.setNumFacturaRef("123");
    prepaidMovement10.setPan("123");
    prepaidMovement10.setCodMondiv(1);
    prepaidMovement10.setImpDiv(1L);
    prepaidMovement10.setImpFac(1L);
    prepaidMovement10.setCmpApli(1);
    prepaidMovement10.setNumAutorizacion("123456");
    prepaidMovement10.setIndProaje("A");
    prepaidMovement10.setCodComercio("A");
    prepaidMovement10.setCodActividad("A");
    prepaidMovement10.setImpLiq(1L);
    prepaidMovement10.setCodMonliq(1);
    prepaidMovement10.setCodPais(56);
    prepaidMovement10.setNomPoblacion("POB");
    prepaidMovement10.setNumExtracto(1);
    prepaidMovement10.setNumMovExtracto(1);
    prepaidMovement10.setClaveMoneda(1);
    prepaidMovement10.setTipoLinea("1");
    prepaidMovement10.setReferenciaLinea(1);
    prepaidMovement10.setNumBenefCta(1);
    prepaidMovement10.setNumeroPlastico(123L);
    prepaidMovement10 = getPrepaidMovementEJBBean10().addPrepaidMovement(null, prepaidMovement10);
    Assert.assertNotNull("Debe Existir prepaidMovement10",prepaidMovement10);
    Assert.assertTrue("Debe Contener el Id",prepaidMovement10.getId()>0);

    // ACTUALIZA MOVIMIENTO
    getPrepaidMovementEJBBean10().updatePrepaidMovement(null,prepaidMovement10.getId(),1,2,152,PrepaidMovementStateType.PROCESSOK);

    List lstMov = buscaMovimiento(prepaidMovement10.getId());

    Assert.assertNotNull("La lista debe ser not null",lstMov);
    Assert.assertEquals("El tamaño de la lista debe ser 1",1,lstMov.size());
    Map<String ,Object>  fila = (Map<String, Object>) lstMov.get(0);
    Assert.assertEquals("El estado debe ser :"+PrepaidMovementStateType.PROCESSOK.getState(), PrepaidMovementStateType.PROCESSOK.getState(), fila.get("estado"));
    Assert.assertEquals("El Num Extracto debe ser 1",1,((BigDecimal)fila.get("num_extracto")).intValue());
    Assert.assertEquals("El Num Extracto debe ser 1",2,((BigDecimal)fila.get("num_mov_extracto")).intValue());
    Assert.assertEquals("El Num Extracto debe ser 1",152,((BigDecimal)fila.get("clave_moneda")).intValue());
  }

  public List buscaMovimiento(Object idMovimiento)  {
    ConfigUtils configUtils = ConfigUtils.getInstance();
    String SCHEMA = configUtils.getProperty("schema");
    String SP_NAME = SCHEMA + ".prp_movimiento";
    return dbUtils.getJdbcTemplate().queryForList("SELECT * FROM "+SP_NAME+" WHERE ID ="+idMovimiento);
  }

}
