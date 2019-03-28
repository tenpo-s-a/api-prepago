package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.dao.AccountDao;
import cl.multicaja.prepaid.model.v11.Account;
import org.apache.commons.lang3.RandomUtils;
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
    cuenta.setAccount(getRandomString(10));
    cuenta.setStatus("ACTIVA");
    cuenta.setBalanceInfo("");
    cuenta.setExpireBalance(0L);
    cuenta.setCreatedAt(LocalDateTime.now());
    cuenta.setUpdatedAt(LocalDateTime.now());
    cuenta.setProcessor("TECNOCOM");


    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());
  }

  @Test
  public void testSearch() {
    cuentaDao.setEm(createEntityManager());

    Account cuenta = new Account();
    cuenta.setAccount(getRandomString(10));
    cuenta.setStatus("ACTIVA");
    cuenta.setBalanceInfo("");
    cuenta.setExpireBalance(0L);
    cuenta.setCreatedAt(LocalDateTime.now());
    cuenta.setUpdatedAt(LocalDateTime.now());
    cuenta.setProcessor("TECNOCOM");

    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());

    Account cuenta2 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta2.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta2.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",cuenta.getUpdatedAt(),cuenta2.getUpdatedAt());
    Assert.assertEquals("Account Deben ser igules",cuenta.getAccount(),cuenta2.getAccount());

  }
  @Test
  public void testUpdate() {
    cuentaDao.setEm(createEntityManager());
    Account cuenta = new Account();
    cuenta.setAccount(getRandomString(10));
    cuenta.setStatus("ACTIVA");
    cuenta.setBalanceInfo("");
    cuenta.setExpireBalance(0L);
    cuenta.setCreatedAt(LocalDateTime.now());
    cuenta.setUpdatedAt(LocalDateTime.now());
    cuenta.setProcessor("TECNOCOM");

    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());

    Account cuenta2 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta2.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta2.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",cuenta.getUpdatedAt(),cuenta2.getUpdatedAt());
    Assert.assertEquals("Account Deben ser igules",cuenta.getAccount(),cuenta2.getAccount());


    // PRUEBA UPDATE
    cuenta2.setProcessor("OTRO");
    LocalDateTime localDateTime = LocalDateTime.now();
    cuenta2.setUpdatedAt(localDateTime);
    cuentaDao.update(cuenta2);

    Account cuenta3 = cuentaDao.find(cuenta.getId());
    Assert.assertEquals("Id Deben ser igules",cuenta.getId(),cuenta3.getId());
    Assert.assertEquals("UUID Deben ser igules",cuenta.getUuid(),cuenta3.getUuid());
    Assert.assertEquals("Actualizacion Deben ser igules",localDateTime,cuenta3.getUpdatedAt());
    Assert.assertEquals("Account Deben ser igules",cuenta.getAccount(),cuenta3.getAccount());
    Assert.assertNotEquals("Procesadora debe ser igual a OTRO","OTRO",cuenta3.getAccount());
  }

  @Test
  public void testFindByUserId() {
    cuentaDao.setEm(createEntityManager());

    Account cuenta = new Account();
    cuenta.setAccount(getRandomString(10));
    cuenta.setStatus("ACTIVA");
    cuenta.setBalanceInfo("");
    cuenta.setExpireBalance(0L);
    cuenta.setCreatedAt(LocalDateTime.now());
    cuenta.setUpdatedAt(LocalDateTime.now());
    cuenta.setProcessor("TECNOCOM");
    cuenta.setUserId(RandomUtils.nextLong());
    cuenta = cuentaDao.insert(cuenta);
    Assert.assertNotNull("No debe ser null",cuenta);
    Assert.assertNotNull("No debe ser null",cuenta.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,cuenta.getId().longValue());

    Account account2 = cuentaDao.findByUserId(cuenta.getUserId());

    Assert.assertNotNull("No debe ser null",account2);
    Assert.assertNotNull("No debe ser null",account2);
    Assert.assertNotEquals("El Id no debe ser 0",cuenta.getId(),account2);
    Assert.assertEquals("Id Usuario debe ser igual",cuenta.getUserId(),account2.getUserId());

  }

}
