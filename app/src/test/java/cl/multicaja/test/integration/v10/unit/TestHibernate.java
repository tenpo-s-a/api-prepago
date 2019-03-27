package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.dao.CuentaDao;
import cl.multicaja.prepaid.model.v11.Cuenta;
import cl.multicaja.test.integration.v10.api.TestBaseUnitApi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class TestHibernate extends TestBaseUnit {

  private CuentaDao cuentaDao = new CuentaDao();

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete  from %s.%s",getSchema(),"prp_cuenta"));
  }
  @Test
  public void testInsert() {

    Cuenta cuenta = new Cuenta();
    cuenta.setCuenta(getRandomString(10));
    cuenta.setEstado("ACTIVA");
    cuenta.setSaldoInfo("");
    cuenta.setSaldoExpiracion(0L);
    cuenta.setCreacion(LocalDateTime.now());
    cuenta.setActualizacion(LocalDateTime.now());
    cuenta.setProcesador("TECNOCOM");

    cuentaDao.setEm(createEntityManager());
    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());
  }

  @Test
  public void testSearch() {

    Cuenta cuenta = new Cuenta();
    cuenta.setCuenta(getRandomString(10));
    cuenta.setEstado("ACTIVA");
    cuenta.setSaldoInfo("");
    cuenta.setSaldoExpiracion(0L);
    cuenta.setCreacion(LocalDateTime.now());
    cuenta.setActualizacion(LocalDateTime.now());
    cuenta.setProcesador("TECNOCOM");

    cuentaDao.setEm(createEntityManager());
    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());

    Cuenta cuenta2 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta2.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta2.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",cuenta.getActualizacion(),cuenta2.getActualizacion());
    Assert.assertEquals("Cuenta Deben ser igules",cuenta.getCuenta(),cuenta2.getCuenta());

  }

}
