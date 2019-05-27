package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFileStatus;
import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO: eliminar ya que el IPM se leera en prepaid-batch-worker
@Deprecated
public class Test_PrepaidAccountingEJBBean10_processIpmFileTransactions extends TestBaseUnit {

  public static void clearData() {

    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
  }

  @Before
  @After
  public void clear(){
    clearData();
  }


  private void prepareUsersAndCards() throws Exception {

    String pan = "5176081118013603";
    String processorUserId = "09870001000000000014";

    System.out.println(String.format("%s -> %s", pan, processorUserId));

    // Crea usuario prepago
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10();
    prepaidCard10.setPan(Utils.replacePan(pan));
    prepaidCard10.setProcessorUserId(processorUserId);
    prepaidCard10.setEncryptedPan(encryptUtil.encrypt(pan));
    prepaidCard10.setIdUser(prepaidUser.getId());
    createPrepaidCard10(prepaidCard10);

    //
    String movement = "INSERT INTO %s.prp_movimiento VALUES (1030, 0, %d, '275175', 'PURCHASE', 166, 'PENDING', 'IN_PROCESS', 'RECONCILED', 'RECONCILED', 'OPE', '2019-02-25 15:12:55.415489', '2019-02-25 15:12:55.415489', '', '0001', '000000000014', 152, 0, 3007, '2018-08-08', '', '5176081118013603', 840, 25, 166, 0, '275175', 'A', 'USA', 0, 0, 0, 152, '', 0, 0, 0, '', 0, 1, 0, '');";
    DBUtils.getInstance().getJdbcTemplate().execute(String.format(movement, getSchema(),prepaidUser.getId()));

    String accounting = "INSERT INTO %s.accounting VALUES (575, 1030, 'COMPRA_OTRA_MONEDA', 'Cargo por compra cm', 'IpmFile', 0, 152, 0, 0, 0, 0, 0, 0.00, 0.00, 0, 'PENDING', 0, 'PENDING', '2018-08-08 05:28:21', '2019-02-25 12:12:55', '2019-02-25 15:12:55.489402', '2019-02-25 15:12:55.489402');";
    DBUtils.getInstance().getJdbcTemplate().execute(String.format(accounting, getSchemaAccounting()));

    String clearing = "INSERT INTO prepaid_accounting.clearing VALUES (464, 575, 0, 0, 'PENDING', '2019-02-25 15:12:55.670868', '2019-02-25 15:12:55.670868');";
    DBUtils.getInstance().getJdbcTemplate().execute(String.format(clearing, getSchemaAccounting()));
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


  @Test
  public void processIpmFileTransactions2() throws Exception {
    // Limpia los datos para no tener problemas con ID
    clearData();
    // Prepara la data de prueba
    prepareUsersAndCards();

    IpmFile ipmFile = new IpmFile();
    ipmFile.setFileName("test2.ipm");

    try {
      File file = new File("src/test/resources/mastercard/files/ipm/good.ipm2.csv");

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


      // ANTES DE PROCESAR EL IPM LOS VALORES FUERON INSERTADOS COMO QUE FUERA DESDE UN OP DIARIAS
      List<AccountingData10> trxs = getDbTransactions();
      List<ClearingData10> trxcle = getDbClearingTransactions();

      Assert.assertNotNull("No debe ser null: ",trxs);
      Assert.assertEquals("No debe tener 1 trx Accounting: ",1,trxs.size());
      Assert.assertEquals("No debe tener 1 trx Clearing: ",1,trxcle.size());
      Assert.assertEquals("El valor del ammount MCAR 0", BigDecimal.ZERO,trxs.get(0).getAmountMastercard().getValue().setScale(0));
      Assert.assertEquals("El valor del ammount USD 0", BigDecimal.ZERO.longValue(),trxs.get(0).getAmountUsd().getValue().longValue());
      Assert.assertEquals("El valor del fee", BigDecimal.ZERO.longValue(),trxs.get(0).getFee().longValue());
      Assert.assertEquals("El valor del fee iva", BigDecimal.ZERO.longValue(),trxs.get(0).getFeeIva().longValue());

      getPrepaidAccountingEJBBean10().processIpmFileTransactions(null, ipmFile);

      trxs = getDbTransactions();
      trxcle = getDbClearingTransactions();

      Assert.assertEquals("No debe tener 1 trx Accounting: ",50,trxs.size());
      Assert.assertEquals("No debe tener 1 trx Clearing: ",50,trxcle.size());
      boolean found = false;
      for(AccountingData10 acc : trxs){
        if(acc.getId() == 575){
          found = true;
          Assert.assertNotEquals("El valor del ammount MCAR 0", BigDecimal.ZERO,acc.getAmountMastercard().getValue().setScale(0));
          Assert.assertNotEquals("El valor del ammount USD 0", BigDecimal.ZERO,acc.getAmountUsd().getValue().longValue());
          Assert.assertNotEquals("El valor del fee", BigDecimal.ZERO.longValue(),acc.getFee());
          Assert.assertNotEquals("El valor del fee iva", BigDecimal.ZERO.longValue(),acc.getFeeIva());
          Assert.assertEquals("Status OK",AccountingStatusType.OK,acc.getStatus());
          System.out.println("FOUND");
        }
      }
      Assert.assertTrue("Debe encontrar el movimiento",found);

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("No debe estar aca");
    }


  }

  private List<ClearingData10> getDbClearingTransactions() {
    List<ClearingData10> trxs = new ArrayList<>();

    List<Map<String, Object>> rows = DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.clearing", getSchemaAccounting()));

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


    List<Map<String, Object>> rows = DBUtils.getInstance().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.accounting", getSchemaAccounting()));

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
      acc.setAccountingStatus(AccountingStatusType.fromValue((String)row.get("accounting_status")));
      acc.setStatus(AccountingStatusType.fromValue((String)row.get("status")));
      trxs.add(acc);
    }

    return trxs;
  }

}
