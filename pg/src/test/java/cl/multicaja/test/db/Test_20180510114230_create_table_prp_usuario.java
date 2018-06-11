package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_20180510114230_create_table_prp_usuario extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_usuario() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_usuario", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_usuario_mc", "int8", 19),
      new ColumnInfo("rut", SqlType.INTEGER.getGetJavaType()),
      new ColumnInfo("estado", "varchar", 20),
      new ColumnInfo("saldo", SqlType.NUMERIC.getGetJavaType()),
      new ColumnInfo("saldo_expiracion", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("fecha_creacion", SqlType.TIMESTAMP.getGetJavaType()),
      new ColumnInfo("fecha_actualizacion", SqlType.TIMESTAMP.getGetJavaType())
    );
    Assert.assertEquals("Existe tabla prp_usuario", true, exists);
  }
}
