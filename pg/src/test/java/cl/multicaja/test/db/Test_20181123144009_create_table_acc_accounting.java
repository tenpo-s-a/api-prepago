package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181123144009_create_table_acc_accounting extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_accounting() {


    boolean exists = dbUtils.tableExists(SCHEMA_ACCOUNTING, "accounting", true,

      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_tx",  SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("type", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("accounting_mov",SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("origin",SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("amount", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("currency", SqlType.NUMERIC.getGetJavaType(), 3),
      new ColumnInfo("amount_usd", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("amount_mcar" ,SqlType.NUMERIC.getGetJavaType(),15),
      new ColumnInfo("exchange_rate_dif", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("fee", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("fee_iva", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("collector_fee", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("collector_fee_iva", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("amount_balance", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("transaction_date", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("conciliation_date", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("status", SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("file_id",SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("accounting_status",SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("create_date",SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("update_date", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla accounting", true, exists);
  }

}
