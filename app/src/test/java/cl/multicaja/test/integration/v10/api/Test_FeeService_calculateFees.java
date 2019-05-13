package cl.multicaja.test.integration.v10.api;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.helpers.fees.FeeService;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.junit.*;

import java.util.concurrent.TimeoutException;

public class Test_FeeService_calculateFees extends TestBaseUnitApi {
  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_comision CASCADE", getSchema()));
  }

  @Ignore
  @Test
  public void calculateFees_respondsOk() throws TimeoutException, BaseException {
    Fee fee = FeeService.getInstance().calculateFees(PrepaidMovementType.PURCHASE, CodigoMoneda.CHILE_CLP, 1000L);
    Assert.assertNotNull("Debe existir la respuesta", fee);
    Assert.assertEquals("Debe tener fee total 20", new Long(20L), fee.getFee());
    Assert.assertEquals("Debe tener fee total 4", new Long(4L), fee.getIva());
    Assert.assertEquals("Debe tener fee total 16", new Long(16L), fee.getCommission());
  }
}
