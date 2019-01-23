package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190122152826_create_table_conciliacion extends TestDbBasePg {


  @Test
  public void checkIfExistsTable_clearing() {

    boolean exists = dbUtils.tableExists(SCHEMA, "prp_conciliaciones", true,

      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_movimiento", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("tipo",SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("status",SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("created",SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("updated", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertTrue("Existe tabla prp_conciliaciones", exists);
  }

}
