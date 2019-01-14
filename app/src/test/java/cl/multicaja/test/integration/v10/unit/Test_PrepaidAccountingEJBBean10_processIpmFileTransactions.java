package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.accounting.model.v10.AccountingOriginType;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_PrepaidAccountingEJBBean10_processIpmFileTransactions extends TestBaseUnit {

  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");

  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", SCHEMA));
  }

  @Test
  public void processIpmFileTransactions_file_null() {
    try {
      getPrepaidAccountingEJBBean10().processIpmFileTransactions(null, null);
    } catch(Exception e) {
      Assert.assertEquals("Debe ser error [IpmFile object null]", "IpmFile object null", e.getMessage());
    }

  }

  @Test
  public void processIpmFileTransactions_transactionsEmpty() {
    try {
      IpmFile ipmFile = new IpmFile();
      ipmFile.setFileName("test.csv");

      getPrepaidAccountingEJBBean10().processIpmFileTransactions(null, ipmFile);
    } catch (Exception e) {
      Assert.fail("No debe estar aca");
    }

  }

  @Test
  public void processIpmFileTransactions() {
    IpmFile ipmFile = new IpmFile();
    ipmFile.setFileName("test.ipm");

    try {
      File file = new File("src/test/resources/mastercard/files/ipm/good.ipm.csv");

      ipmFile = getPrepaidAccountingEJBBean10().processIpmFile(null, file, ipmFile);

      List<IpmFile> bdIpmFiles = getPrepaidAccountingEJBBean10().findIpmFile(null, null, ipmFile.getFileName(), null, null);

      Assert.assertEquals("Debe tener 1 archivo", Long.valueOf(1), Long.valueOf(bdIpmFiles.size()));

      IpmFile bdIpmFile = bdIpmFiles.get(0);

      Assert.assertNotNull("Debe tener id", bdIpmFile.getId());
      Assert.assertNotNull("Debe tener timestamps", bdIpmFile.getTimestamps());
      Assert.assertNotNull("Debe tener timestamps.created_at", bdIpmFile.getTimestamps().getCreatedAt());
      Assert.assertNotNull("Debe tener timestamps.updated_at", bdIpmFile.getTimestamps().getUpdatedAt());
      Assert.assertEquals("Debe tener mismo fileName", ipmFile.getFileName(), bdIpmFile.getFileName());
      Assert.assertEquals("Debe tener status [PROCESSING]", IpmFileStatus.PROCESSING, bdIpmFile.getStatus());

      getPrepaidAccountingEJBBean10().processIpmFileTransactions(null, ipmFile);

      List<Accounting10> trxs = getDbTransactions();

      Assert.assertEquals("Debe tener 50 transacciones", Integer.valueOf(50), Integer.valueOf(trxs.size()));

      for(Accounting10 trx : trxs) {
        Assert.assertNotNull(String.format("Debe tener id [%s]", trx.getId()), trx.getId());
        Assert.assertEquals("Debe tener type [COMPRA_MONEDA]", AccountingTxType.COMPRA_MONEDA, trx.getType());
        Assert.assertEquals("Debe origin [IPM]", AccountingOriginType.IPM, trx.getOrigin());
      }

      {
        bdIpmFiles = getPrepaidAccountingEJBBean10().findIpmFile(null, null, ipmFile.getFileName(), null, null);

        Assert.assertEquals("Debe tener 1 archivo", Long.valueOf(1), Long.valueOf(bdIpmFiles.size()));

        bdIpmFile = bdIpmFiles.get(0);

        Assert.assertNotNull("Debe tener id", bdIpmFile.getId());
        Assert.assertNotNull("Debe tener timestamps", bdIpmFile.getTimestamps());
        Assert.assertNotNull("Debe tener timestamps.created_at", bdIpmFile.getTimestamps().getCreatedAt());
        Assert.assertNotNull("Debe tener timestamps.updated_at", bdIpmFile.getTimestamps().getUpdatedAt());
        Assert.assertEquals("Debe tener mismo fileName", ipmFile.getFileName(), bdIpmFile.getFileName());
        Assert.assertEquals("Debe tener status [PROCESSING]", IpmFileStatus.PROCESSED, bdIpmFile.getStatus());
      }

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("No debe estar aca");
    }


  }

  private List<Accounting10> getDbTransactions() {
    List<Accounting10> trxs = new ArrayList<>();


    List<Map<String, Object>> rows = DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.accounting", SCHEMA));

    for (Map row : rows) {
      Accounting10 acc = new Accounting10();
      acc.setId((Long)(row.get("id")));
      acc.setType(AccountingTxType.fromValue((String)row.get("type")));
      acc.setOrigin(AccountingOriginType.fromValue((String)row.get("origin")));

      trxs.add(acc);
    }

    return trxs;
  }

}
