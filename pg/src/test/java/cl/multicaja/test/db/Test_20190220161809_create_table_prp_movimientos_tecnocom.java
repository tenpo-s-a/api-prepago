package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190220161809_create_table_prp_movimientos_tecnocom extends TestDbBasePg {


  @Test
  public void checkIfTableExists_apps_file() {
    Boolean exists = dbUtils.tableExists(SCHEMA, "prp_app_file", Boolean.TRUE,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType(), 19),
      new ColumnInfo("name", SqlType.VARCHAR.getGetJavaType(), 25),
      new ColumnInfo("version", SqlType.VARCHAR.getGetJavaType(), 10),
      new ColumnInfo("description", SqlType.VARCHAR.getGetJavaType(), 100),
      new ColumnInfo("mime_type", SqlType.VARCHAR.getGetJavaType(), 50),
      new ColumnInfo("location", SqlType.TEXT.getGetJavaType(), 2147483647),
      new ColumnInfo("status", SqlType.VARCHAR.getGetJavaType(), 20),
      new ColumnInfo("created_at", SqlType.TIMESTAMP.getGetJavaType(), 29),
      new ColumnInfo("updated_at", SqlType.TIMESTAMP.getGetJavaType(), 29)
    );

    Assert.assertTrue("Existe la tabla prp_app_file", exists);
  }
}
