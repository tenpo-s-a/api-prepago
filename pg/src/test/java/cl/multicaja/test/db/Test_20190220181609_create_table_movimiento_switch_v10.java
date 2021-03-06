package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20190220181609_create_table_movimiento_switch_v10 extends TestDbBasePg {

  @Test
  public void checkIfExistsTable_prp_movimiento_switch() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_switch", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_archivo", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("id_multicaja", SqlType.VARCHAR.getGetJavaType(), 50),
      new ColumnInfo("id_cliente", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("id_multicaja_ref", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("monto", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("fecha_trx", SqlType.TIMESTAMP.getGetJavaType(), 29)
    );
    Assert.assertEquals("Existe tabla prp_movimiento_switch_hist", true, exists);
  }

  @Test
  public void checkIfExistsTable_prp_movimiento_switch_hist() {
    boolean exists = dbUtils.tableExists(SCHEMA, "prp_movimiento_switch_hist", true,
      new ColumnInfo("id", SqlType.BIGSERIAL.getGetJavaType()),
      new ColumnInfo("id_archivo", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("id_multicaja", SqlType.VARCHAR.getGetJavaType(), 50),
      new ColumnInfo("id_cliente", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("id_multicaja_ref", SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("monto", SqlType.NUMERIC.getGetJavaType(), 17),
      new ColumnInfo("fecha_trx", SqlType.TIMESTAMP.getGetJavaType(), 29)
    );
    Assert.assertEquals("Existe tabla prp_movimiento_switch_hist", true, exists);
  }
}
