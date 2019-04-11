package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190110172704_create_table_clearing extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_clearing() {

    boolean exists = dbUtils.tableExists(SCHEMA_ACCOUNTING, "clearing", true,

      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("accounting_id", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("user_account_id",SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("file_id",SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("status", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("created",SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("updated", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("bank_id", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("account_number", SqlType.VARCHAR.getGetJavaType(),30),
      new ColumnInfo("account_type", SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("account_rut", SqlType.BIGINT.getGetJavaType())
    );
    Assert.assertTrue("Existe tabla clearing", exists);
  }



}
