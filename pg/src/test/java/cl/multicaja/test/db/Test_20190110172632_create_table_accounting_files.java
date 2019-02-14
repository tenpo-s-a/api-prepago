package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190110172632_create_table_accounting_files extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_accounting_files() {


    boolean exists = dbUtils.tableExists(SCHEMA_ACCOUNTING, "accounting_files", true,

      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("name",  SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("file_id", SqlType.VARCHAR.getGetJavaType(), 30),
      new ColumnInfo("type",SqlType.VARCHAR.getGetJavaType(),30),
      new ColumnInfo("format", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("url", SqlType.VARCHAR.getGetJavaType(), 200),
      new ColumnInfo("status", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("created" ,SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("updated", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla accounting_files", true, exists);
  }

}
