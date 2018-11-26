package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181123144009_create_table_acc_accounting extends TestDbBasePg{

  @Test
  public void checkIfExistsTable_accounting() {


    boolean exists = dbUtils.tableExists(SCHEMA_ACCOUNTING, "accounting", true,

      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_tx",  "int8", 19),
      new ColumnInfo("type", "varchar", 2147483647),
      new ColumnInfo("origin","varchar", 2147483647),
      new ColumnInfo("amount", "numeric", 131089),
      new ColumnInfo("currency", "numeric", 131089),
      new ColumnInfo("ammount_usd", "numeric", 131089),
      new ColumnInfo("exchange_rate_dif", "numeric", 131089),
      new ColumnInfo("fee", "numeric", 131089),
      new ColumnInfo("fee_iva", "numeric", 131089),
      new ColumnInfo("transaction_date", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("create_date", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("update_date", SqlType.TIMESTAMP.getGetJavaType())
    );

    Assert.assertEquals("Existe tabla accounting", true, exists);
  }

}
