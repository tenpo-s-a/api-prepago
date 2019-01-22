package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.MovementOriginType;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.*;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class Test_PendingStoreWithdrawToAccounting extends TestBaseUnitAsync {

  protected static final String SCHEMA_ACCOUNTING = ConfigUtils.getInstance().getProperty("schema.acc");

  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils dbUtils = DBUtils.getInstance();
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", SCHEMA_ACCOUNTING));
  }

  @Test
  public void pendingWithdrawToAccount_ok() throws Exception {
    Date dateNow = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(dateNow.toInstant(), ZoneId.systemDefault());

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();
    prepaidMovement.setId(100L);
    prepaidMovement.setTipofac(TipoFactura.RETIRO_TRANSFERENCIA);
    prepaidMovement.setFecfac(dateNow);
    prepaidMovement.setImpfac(new BigDecimal(10000));
    prepaidMovement.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement.setOriginType(MovementOriginType.API);
    prepaidMovement.setFechaCreacion(new Timestamp(dateNow.getTime()));

    UserAccount userAccount = new UserAccount();
    userAccount.setBankId(10L);

    sendWithdrawToAccounting(prepaidMovement, userAccount);

    List<Accounting10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, localDateTime);
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 solo movimiento de account", 1, accounting10s.size());

  }

}
