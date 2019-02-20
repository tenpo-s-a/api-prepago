package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;
import cl.multicaja.test.integration.v10.async.TestBaseUnitAsync;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author abarazarte
 **/
public class Test_PendingTecnocomReconciliationFileAut10 extends TestBaseUnit {

  private List<String> pans = Arrays.asList("5176081135830583","5176081111866841");
  private List<String> contracts = Arrays.asList("09870001000000000012","09870001000000000013");
  private List<PrepaidUser10> users = new ArrayList<>();
  private List<PrepaidCard10> prepaidCards = new ArrayList<>();
  private static ReconciliationFile autFile;

  private void clearTransactions() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
  }

  private void prepareUsersAndCards() throws Exception {

    users.clear();
    prepaidCards.clear();

    for (int i = 0; i < pans.size(); i++) {
      String pan = pans.get(i);
      String processorUserId =  contracts.get(i);

      System.out.println(String.format("%s -> %s", pan, processorUserId));

      // Crea usuario
      User user = registerUser();

      // Crea usuario prepago
      PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10();
      prepaidCard10.setPan(Utils.replacePan(pan));
      prepaidCard10.setProcessorUserId(processorUserId);
      prepaidCard10 = createPrepaidCard10(prepaidCard10);

      users.add(prepaidUser10);
      prepaidCards.add(prepaidCard10);
    }
  }

  @Before
  public void beforeEach() throws Exception {
    clearTransactions();
    prepareUsersAndCards();
  }


  @Test
  public void processApiTransactions_statusProcessOk() throws Exception {

    List<PrepaidMovement10> movements = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, null, null,
      null, null, null, null, null, null);

    Assert.assertNull("No debe tener movimientos", movements);

    final String filename = "PLJ61110.FINT0004";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);

      getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      Assert.fail("Should not be here");
    }

    Thread.sleep(1500);

    List<PrepaidMovement10> purchase = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, PrepaidMovementType.PURCHASE, null,
      null, null, null, null, null, null, null, null, MovementOriginType.OPE,null);

    Assert.assertNotNull("Debe tener movimientos de compra", purchase);
    Assert.assertFalse("Debe tener movimientos de compra", purchase.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos de compra", 13, purchase.size());

    List<PrepaidMovement10> suscriptions = getPrepaidMovementEJBBean10().getPrepaidMovements(null, null, null, null, PrepaidMovementType.SUSCRIPTION, null,
      null, null, null, null, null, null, null, null, MovementOriginType.OPE,null);

    Assert.assertNotNull("Debe tener movimientos de suscripcion", suscriptions);
    Assert.assertFalse("Debe tener movimientos de suscripcion", suscriptions.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos de suscripcion", 5, suscriptions.size());

  }

  private InputStream putSuccessFileIntoSftp(String filename) throws Exception {
    return this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/" + filename);
  }

}
