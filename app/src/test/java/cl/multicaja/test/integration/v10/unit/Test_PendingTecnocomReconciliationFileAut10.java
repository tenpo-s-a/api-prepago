package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.utils.Utils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author abarazarte
 **/
public class Test_PendingTecnocomReconciliationFileAut10 extends TestBaseUnit {

  private List<String> pans = Arrays.asList("5176081135830583","5176081111866841");
  private List<String> contracts = Arrays.asList("09870001000000000012","09870001000000000013");
  private List<PrepaidUser10> users = new ArrayList<>();
  private List<PrepaidCard10> prepaidCards = new ArrayList<>();
  private List<Account> accounts = new ArrayList<>();

  private void clearTransactions() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom_hist CASCADE", getSchema()));
  }

  private void prepareUsersAndCards() throws Exception {

    users.clear();
    prepaidCards.clear();
    accounts.clear();

    for (int i = 0; i < pans.size(); i++) {
      String pan = pans.get(i);
      String processorUserId =  contracts.get(i);

      System.out.println(String.format("%s -> %s", pan, processorUserId));

      // Crea usuario prepago
      PrepaidUser10 prepaidUser10 = buildPrepaidUserv2(PrepaidUserLevel.LEVEL_2);
      prepaidUser10 = createPrepaidUserV2(prepaidUser10);

      Account account = createAccount(prepaidUser10.getId(),processorUserId);

      PrepaidCard10 prepaidCard10 = buildPrepaidCard10();
      prepaidCard10.setPan(Utils.replacePan(pan));
      prepaidCard10.setHashedPan(pan); // Para tests, se guarda en claro en vez del hash
      prepaidCard10.setAccountId(account.getId());
      prepaidCard10.setUuid(UUID.randomUUID().toString());
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      users.add(prepaidUser10);
      accounts.add(account);
      prepaidCards.add(prepaidCard10);
    }
  }

  @Before
  @After
  public void beforeEach() throws Exception {
    clearTransactions();
    prepareUsersAndCards();
  }

  //FIXME: ASD
  @Ignore
  @Test
  public void processApiTransactions_statusProcessOk() throws Exception {

    Long fileId = null;
    final String filename = "PLJ61110.FINT0004";
    try {
      InputStream is = putSuccessFileIntoSftp(filename);
      fileId = getTecnocomReconciliationEJBBean10().processFile(is, filename);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Should not be here");
    }
    // Procesa los datos insertados en la tabla
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(fileId);

    List<PrepaidMovement10> purchase = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null, null, PrepaidMovementType.PURCHASE,
      null, null, null, null, null, null,null, null, null, MovementOriginType.OPE, null, null, null);

    Assert.assertNotNull("Debe tener movimientos de compra", purchase);
    Assert.assertFalse("Debe tener movimientos de compra", purchase.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos de compra", 13, purchase.size());

    List<PrepaidMovement10> suscriptions = getPrepaidMovementEJBBean11().getPrepaidMovements(null, null, null, null, PrepaidMovementType.SUSCRIPTION,
      null, null, null, null, null, null,null, null, null, MovementOriginType.OPE, null, null, null);

    Assert.assertNotNull("Debe tener movimientos de suscripcion", suscriptions);
    Assert.assertFalse("Debe tener movimientos de suscripcion", suscriptions.isEmpty());
    Assert.assertEquals("Debe tener 16 movimientos de suscripcion", 5, suscriptions.size());

  }

  private InputStream putSuccessFileIntoSftp(String filename) throws Exception {
    return this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/" + filename);
  }

}
