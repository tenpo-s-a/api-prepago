package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.dao.AccountDao;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class Test_dao_account extends TestBaseUnit {

  private AccountDao cuentaDao = new AccountDao();

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete  from %s.%s",getSchema(),"prp_cuenta"));

  }
  @Test
  public void testInsert() {
    cuentaDao.setEm(createEntityManager());

    Account cuenta = new Account();
    cuenta.setCuenta(getRandomString(10));
    cuenta.setEstado("ACTIVA");
    cuenta.setSaldoInfo("");
    cuenta.setSaldoExpiracion(0L);
    cuenta.setCreacion(LocalDateTime.now());
    cuenta.setActualizacion(LocalDateTime.now());
    cuenta.setProcesador("TECNOCOM");


    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());
  }

  @Test
  public void testSearch() {
    cuentaDao.setEm(createEntityManager());

    Account cuenta = new Account();
    cuenta.setCuenta(getRandomString(10));
    cuenta.setEstado("ACTIVA");
    cuenta.setSaldoInfo("");
    cuenta.setSaldoExpiracion(0L);
    cuenta.setCreacion(LocalDateTime.now());
    cuenta.setActualizacion(LocalDateTime.now());
    cuenta.setProcesador("TECNOCOM");

    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());

    Account cuenta2 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta2.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta2.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",cuenta.getActualizacion(),cuenta2.getActualizacion());
    Assert.assertEquals("Account Deben ser igules",cuenta.getCuenta(),cuenta2.getCuenta());

  }
  @Test
  public void testUpdate() {
    cuentaDao.setEm(createEntityManager());
    Account cuenta = new Account();
    cuenta.setCuenta(getRandomString(10));
    cuenta.setEstado("ACTIVA");
    cuenta.setSaldoInfo("");
    cuenta.setSaldoExpiracion(0L);
    cuenta.setCreacion(LocalDateTime.now());
    cuenta.setActualizacion(LocalDateTime.now());
    cuenta.setProcesador("TECNOCOM");

    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());

    Account cuenta2 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta2.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta2.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",cuenta.getActualizacion(),cuenta2.getActualizacion());
    Assert.assertEquals("Account Deben ser igules",cuenta.getCuenta(),cuenta2.getCuenta());


    // PRUEBA UPDATE
    cuenta2.setProcesador("OTRO");
    LocalDateTime localDateTime = LocalDateTime.now();
    cuenta2.setActualizacion(localDateTime);
    cuentaDao.update(cuenta2);

    Account cuenta3 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta3.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta3.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",localDateTime,cuenta3.getActualizacion());
    Assert.assertEquals("Account Deben ser igules",cuenta.getCuenta(),cuenta3.getCuenta());
    Assert.assertNotEquals("Procesadora debe ser igual a OTRO","OTRO",cuenta3.getCuenta());


  }


}
