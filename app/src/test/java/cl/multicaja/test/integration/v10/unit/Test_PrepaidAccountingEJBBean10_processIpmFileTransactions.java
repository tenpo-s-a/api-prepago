package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_PrepaidAccountingEJBBean10_processIpmFileTransactions extends TestBaseUnit {

  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");

  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", SCHEMA));
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

      List<AccountingData10> trxs = getDbTransactions();
      List<ClearingData10> trxcle = getDbClearingTransactions();

      Assert.assertEquals("Debe tener 50 transacciones", Integer.valueOf(50), Integer.valueOf(trxs.size()));
      Assert.assertEquals("Debe tener 50 tx", Integer.valueOf(50),Integer.valueOf(trxcle.size()));
      Assert.assertEquals("Tamaños Iguales",trxs.size(),trxcle.size());

      for(AccountingData10 trx : trxs) {
        Assert.assertNotNull(String.format("Debe tener id [%s]", trx.getId()), trx.getId());
        Assert.assertEquals("Debe tener type [COMPRA_MONEDA]", AccountingTxType.COMPRA_MONEDA, trx.getType());
        Assert.assertEquals("Debe origin [IPM]", AccountingOriginType.IPM, trx.getOrigin());

        BigDecimal amountBalance = BigDecimal.ZERO
          .add(trx.getAmountMastercard().getValue())
          .add(trx.getFee())
          .add(trx.getFeeIva())
          .add(trx.getExchangeRateDif()).setScale(0, BigDecimal.ROUND_UP);
        Assert.assertEquals("El monto afecto a saldo debe ser la suma", amountBalance, trx.getAmountBalance().getValue().setScale(0, BigDecimal.ROUND_UP));
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

  private List<ClearingData10> getDbClearingTransactions() {
    List<ClearingData10> trxs = new ArrayList<>();

    List<Map<String, Object>> rows = DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.clearing", SCHEMA));

    for (Map row : rows) {
      ClearingData10 cle = new ClearingData10();

      cle.setId((Long)(row.get("id")));
      cle.setId((Long)(row.get("accounting_id")));
      cle.setStatus(AccountingStatusType.fromValue((String)row.get("status")));
      trxs.add(cle);
    }

    return trxs;
  }

  private List<AccountingData10> getDbTransactions() {
    List<AccountingData10> trxs = new ArrayList<>();


    List<Map<String, Object>> rows = DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.accounting", SCHEMA));

    for (Map row : rows) {
      AccountingData10 acc = new AccountingData10();
      acc.setId((Long)(row.get("id")));
      acc.setType(AccountingTxType.fromValue((String)row.get("type")));
      acc.setOrigin(AccountingOriginType.fromValue((String)row.get("origin")));
      acc.setAmount(new NewAmountAndCurrency10(BigDecimal.valueOf(numberUtils.toDouble(row.get("amount")))));
      acc.setAmountMastercard(new NewAmountAndCurrency10(BigDecimal.valueOf(numberUtils.toDouble(row.get("amount_mcar")))));
      acc.setAmountUsd(new NewAmountAndCurrency10(BigDecimal.valueOf(numberUtils.toDouble(row.get("amount_usd")))));
      acc.setAmountBalance(new NewAmountAndCurrency10(BigDecimal.valueOf(numberUtils.toDouble(row.get("amount_balance")))));

      acc.setFee(BigDecimal.valueOf(numberUtils.toDouble(row.get("fee"))));
      acc.setFeeIva(BigDecimal.valueOf(numberUtils.toDouble(row.get("fee_iva"))));
      acc.setCollectorFee(BigDecimal.valueOf(numberUtils.toDouble(row.get("collector_fee"))));
      acc.setCollectorFeeIva(BigDecimal.valueOf(numberUtils.toDouble(row.get("collector_fee_iva"))));
      acc.setExchangeRateDif(BigDecimal.valueOf(numberUtils.toDouble(row.get("exchange_rate_dif"))));

      trxs.add(acc);
    }

    return trxs;
  }

}
