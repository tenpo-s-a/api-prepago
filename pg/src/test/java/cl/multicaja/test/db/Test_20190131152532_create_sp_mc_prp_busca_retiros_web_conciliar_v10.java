package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class Test_20190131152532_create_sp_mc_prp_busca_retiros_web_conciliar_v10 extends TestDbBasePg {

  //@AfterClass
  public static void beforeAndAfterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", SCHEMA));
  }

  @Before
  public void clearData() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.clearing", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.accounting", SCHEMA_ACCOUNTING));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento_conciliado", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_movimiento", SCHEMA));
  }


  public static Map<String, Object> searchMovements() throws SQLException {
    return dbUtils.execute(SCHEMA + ".mc_prp_busca_retiros_web_conciliar_v10");
  }

  private Long insertMovement(String tecnocomConStatus, Boolean isReconcilied, String clearingStatus) throws Exception {

    // Se crea movimiento
    Map<String, Object> mov = Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertRandomMovement();
    Assert.assertNotNull("Data no debe ser null", mov);
    Assert.assertEquals("No debe ser 0", "0", mov.get("_error_code"));
    Assert.assertEquals("Deben ser iguales", "", mov.get("_error_msg"));

    Long movementId = numberUtils.toLong(mov.get("_id"));

    // Se actualiza el estado de conciliacion con Tecnocom
    Map<String, Object> updateStatus = Test_20180925145632_create_sp_mc_prp_actualiza_movimiento_estado_tecnocom_v10.updateTecnocomStatus(movementId, tecnocomConStatus);
    Assert.assertNotNull("Data no debe ser null", updateStatus);
    Assert.assertEquals("No debe ser 0", "0", updateStatus.get("_error_code"));
    Assert.assertEquals("Deben ser iguales", "", updateStatus.get("_error_msg"));

    if(isReconcilied) {
      // Se crea movimiento conciliado
      Map<String, Object> movReconcilied = Test_20181009113559_create_sp_crea_movimiento_conciliado.creaMovimientoConciliado(movementId, "CARGA", "OK");
      Assert.assertNotNull("Data no debe ser null", movReconcilied);
      Assert.assertEquals("No debe ser 0", "0", movReconcilied.get("_error_code"));
      Assert.assertEquals("Deben ser iguales", "", movReconcilied.get("_error_msg"));
    }

    // Se crea movimiento accounting
    Map<String, Object> randomAccounting = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.createRandomAccounting(movementId);
    Map<String, Object> accountingMovement = Test_20181126083955_create_sp_mc_prp_insert_accounting_data.insertAccount(randomAccounting);
    Assert.assertNotNull("Data no debe ser null", accountingMovement);
    Assert.assertEquals("No debe ser 0", "0", accountingMovement.get("_error_code"));
    Assert.assertEquals("Deben ser iguales", "", accountingMovement.get("_error_msg"));

    // Se crea movimiento clearing
    Map<String, Object> clearingMovement = Test_20190114151738_mc_acc_create_clearing_data_v10.createClearingData(numberUtils.toLong(accountingMovement.get("id")),numberUtils.random(1L,9999L),numberUtils.random(1L,9999L),clearingStatus);
    Assert.assertNotNull("Data no debe ser null", clearingMovement);
    Assert.assertEquals("No debe ser 0", "0", clearingMovement.get("_error_code"));
    Assert.assertEquals("Deben ser iguales", "", clearingMovement.get("_error_msg"));

    return movementId;
  }


  @Test
  public void searchWebWithdrawForReconciliation() throws Exception {
    List<Long> okMovements = new ArrayList<>();

    //mov1
    {
      Long id = this.insertMovement("PENDING", Boolean.FALSE, "OK");
    }

    //mov 2
    {
      Long id = this.insertMovement("RECONCILIED", Boolean.FALSE, "SENT");
    }

    //mov 3
    {
      Long id = this.insertMovement("RECONCILIED", Boolean.FALSE, "OK");
      okMovements.add(id);
    }

    //mov 4
    {
      Long id = this.insertMovement("NOT_RECONCILIED", Boolean.FALSE, "OK");
      okMovements.add(id);
    }

    //mov 5
    {
      Long id = this.insertMovement("RECONCILIED", Boolean.TRUE, "OK");
    }

    //mov 6
    {
      Long id = this.insertMovement("RECONCILIED", Boolean.TRUE, "REJECTED");
    }

    Map<String, Object> resp = searchMovements();

    List result = (List)resp.get("result");

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", okMovements.size(), result.size());

    result.forEach(r -> {
      Map<String, Object> o = (Map<String, Object>) r;
      Assert.assertTrue(okMovements.contains(numberUtils.toLong(o.get("_id_tx"))));
    });

  }

}
