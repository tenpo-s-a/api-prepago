package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181218133602_create_table_acc_ipm_file extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_ipm_file() {


    boolean exists = dbUtils.tableExists(SCHEMA_ACCOUNTING, "ipm_file", true,

      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("file_name", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("file_id",SqlType.VARCHAR.getGetJavaType(), 25),
      new ColumnInfo("message_count", SqlType.NUMERIC.getGetJavaType(), 15),
      new ColumnInfo("status", SqlType.VARCHAR.getGetJavaType(), 25),
      new ColumnInfo("create_date",SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("update_date", SqlType.TIMESTAMP.getGetJavaType())
    );

    Assert.assertTrue("Existe tabla accounting", exists);
  }
}
