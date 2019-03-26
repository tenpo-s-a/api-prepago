package cl.multicaja.test.integration.v10.api;

import cl.multicaja.prepaid.dao.CuentaDao;
import cl.multicaja.prepaid.model.v11.Cuenta;
import org.junit.Test;

public class TestHibernate extends TestBaseUnitApi {

  private CuentaDao cuentaDao = new CuentaDao();

  @Test
  public void test(){
    Cuenta cuenta = new Cuenta();
    cuenta.setCuenta(getRandomString(10));
    cuentaDao.setEm(createEntityManager());
    cuentaDao.insert(cuenta);

  }

}
