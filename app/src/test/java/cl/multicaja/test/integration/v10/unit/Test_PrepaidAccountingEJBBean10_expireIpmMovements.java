package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import org.junit.*;

import java.util.ArrayList;

public class Test_PrepaidAccountingEJBBean10_expireIpmMovements extends TestBaseUnit {

  @Before
  @After
  public void afterEachTest() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", getSchemaAccounting()));
  }

  @Test
  public void expireIpmSuscriptions() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Se crean 10 movimientos, con 0, 1, 2... 9 archivos procesados despues de ellos.
    ArrayList<PrepaidMovement10> allMovements = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar archivo
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileId(getRandomString(10));
      ipmFile.setFileName("archivo");
      ipmFile.setStatus(IpmFileStatus.PROCESSED);
      getPrepaidAccountingEJBBean10().saveIpmFileRecord(null, ipmFile);

      // Insertar movimiento
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.SUSCRIPTION);
      prepaidMovement1 = createPrepaidMovement10(prepaidMovement1);
      allMovements.add(0, prepaidMovement1);

      Thread.sleep(10);
    }

    getPrepaidAccountingEJBBean10().expireIpmMovements();

    for(int i = 0; i < 10; i++) {
      PrepaidMovement10 prepaidMovement10 = allMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getId(), storedMovement.getId());

      if(i <= 6) {
        Assert.assertEquals("Las primeras deben tener estado PENDING", PrepaidMovementStatus.PENDING, storedMovement.getEstado());
      } else {
        Assert.assertEquals("Las ultimas deben tener estado EXPIRED", PrepaidMovementStatus.EXPIRED, storedMovement.getEstado());
      }
    }
  }

  @Test
  public void expireIpmPurchases() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Se crean 10 movimientos, con 0, 1, 2... 9 archivos procesados despues de ellos.
    ArrayList<PrepaidMovement10> allMovements = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar archivo
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileId(getRandomString(10));
      ipmFile.setFileName("archivo");
      ipmFile.setStatus(IpmFileStatus.PROCESSED);
      getPrepaidAccountingEJBBean10().saveIpmFileRecord(null, ipmFile);

      // Insertar movimiento
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, PrepaidMovementType.PURCHASE);
      prepaidMovement1 = createPrepaidMovement10(prepaidMovement1);
      allMovements.add(0, prepaidMovement1);

      Thread.sleep(10);
    }

    getPrepaidAccountingEJBBean10().expireIpmMovements();

    for(int i = 0; i < 10; i++) {
      PrepaidMovement10 prepaidMovement10 = allMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getId(), storedMovement.getId());

      if(i <= 6) {
        Assert.assertEquals("Las primeras deben tener estado PENDING", PrepaidMovementStatus.PENDING, storedMovement.getEstado());
      } else {
        Assert.assertEquals("Las ultimas deben tener estado EXPIRED", PrepaidMovementStatus.EXPIRED, storedMovement.getEstado());
      }
    }
  }

  @Test
  public void doNotExpireOtherMovements() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);

    // Se crean 10 movimientos, con 0, 1, 2... 9 archivos procesados despues de ellos.
    ArrayList<PrepaidMovement10> allMovements = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      // Insertar archivo
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileId(getRandomString(10));
      ipmFile.setFileName("archivo");
      ipmFile.setStatus(IpmFileStatus.PROCESSED);
      getPrepaidAccountingEJBBean10().saveIpmFileRecord(null, ipmFile);

      // Insertar movimiento
      PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
      PrepaidMovement10 prepaidMovement1 = buildPrepaidMovement10(prepaidUser, prepaidTopup, null, null, i % 2 == 0 ? PrepaidMovementType.TOPUP : PrepaidMovementType.WITHDRAW);
      prepaidMovement1 = createPrepaidMovement10(prepaidMovement1);
      allMovements.add(0, prepaidMovement1);

      Thread.sleep(10);
    }

    getPrepaidAccountingEJBBean10().expireIpmMovements();

    for(int i = 0; i < 10; i++) {
      PrepaidMovement10 prepaidMovement10 = allMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
      Assert.assertEquals("Debe tener el mismo id", prepaidMovement10.getId(), storedMovement.getId());
      Assert.assertEquals("Todo debe estar PENDING", PrepaidMovementStatus.PENDING, storedMovement.getEstado());
    }
  }
}
