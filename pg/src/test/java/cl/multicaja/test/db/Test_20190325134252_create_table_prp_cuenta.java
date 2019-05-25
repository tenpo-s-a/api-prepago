package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190325134252_create_table_prp_cuenta extends TestDbBasePg {

  @Test
  public void checkIfExistsTable() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_cuenta", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("uuid",SqlType.VARCHAR.getGetJavaType(),50),
      new ColumnInfo("id_usuario", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("cuenta", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("procesador", SqlType.VARCHAR.getGetJavaType(), 30),
      new ColumnInfo("saldo_info", SqlType.TEXT.getGetJavaType()),
      new ColumnInfo("saldo_expiracion", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(), 30),
      new ColumnInfo("creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("actualizacion", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla prp_cuenta", true, exists);
  }

}
