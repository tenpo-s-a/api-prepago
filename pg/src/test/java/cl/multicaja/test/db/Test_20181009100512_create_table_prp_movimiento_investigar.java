package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import org.junit.Assert;
import org.junit.Test;

public class Test_20181009100512_create_table_prp_movimiento_investigar extends TestDbBasePg {


  @Test
  public void checkIfExistsTable_prp_movimiento_investigar() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_investigar", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("mov_ref", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("origen",SqlType.VARCHAR.getGetJavaType(),50),
      new ColumnInfo("nombre_archivo", SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("fecha_registro", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla prp_movimiento_investigar", true, exists);
  }
}
