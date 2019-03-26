package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190325142244_create_table_prp__movimiento_comision extends TestDbBasePg {

  @Test
  public void checkIfExistsTable() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_comision", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_movimiento", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("tipo_comision", SqlType.VARCHAR.getGetJavaType(),20),
      new ColumnInfo("monto", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("monto", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("actualizacion", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla prp_movimiento_comision", true, exists);
  }

}
