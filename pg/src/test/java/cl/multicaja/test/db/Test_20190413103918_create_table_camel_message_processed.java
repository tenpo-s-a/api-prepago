package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190413103918_create_table_camel_message_processed extends TestDbBasePg {

  @Test
  public void checkIfExistsTable() {
    boolean exists = dbUtils.tableExists("public", "camel_messageprocessed", true,
      new ColumnInfo("processorName", SqlType.VARCHAR.getGetJavaType(), 255),
      new ColumnInfo("messageId", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("createdAt", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla camel_messageprocessed", true, exists);
  }
}
