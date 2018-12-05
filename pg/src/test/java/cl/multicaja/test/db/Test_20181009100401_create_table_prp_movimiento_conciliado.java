package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181009100401_create_table_prp_movimiento_conciliado extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_movimiento_conciliado() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_conciliado", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_mov_ref", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("fecha_registro", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("accion", SqlType.VARCHAR.getGetJavaType(), 50),
      new ColumnInfo("estado", SqlType.VARCHAR.getGetJavaType(),50)
    );
    Assert.assertEquals("Existe tabla prp_movimiento_conciliado", true, exists);
  }
}
