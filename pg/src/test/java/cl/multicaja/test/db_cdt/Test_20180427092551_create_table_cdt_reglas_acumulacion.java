package cl.multicaja.test.db_cdt;

import cl.multicaja.core.utils.db.ColumnInfo;
import cl.multicaja.core.utils.db.SqlType;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

public class Test_20180427092551_create_table_cdt_reglas_acumulacion extends TestDbBasePg {
  /**************************************************
   *
   *       id                         BIGSERIAL NOT NULL,
   *       id_categoria_movimiento    BIGSERIAL NOT NULL,
   *       periocidad                 VARCHAR(10) NOT NULL,
   *       codigo_operacion           VARCHAR(10) NOT NULL,
   *       estado                     VARCHAR(10) NOT NULL,
   *       fecha_estado               TIMESTAMP NOT NULL,
   *       fecha_creacion             TIMESTAMP NOT NULL,
   *************************************************/
  @Test
  public void CheckTableReglaAcumulacion() {
    boolean exists = dbUtils.tableExists(SCHEMA_CDT, Constants.Tables.REGLA_ACUMULACION.getName(), true,
      new ColumnInfo("id", "BIGSERIAL",19),
      new ColumnInfo("id_bolsa",SqlType.BIGINT.getGetJavaType()),
      new ColumnInfo("descripcion",SqlType.VARCHAR.getGetJavaType(),100),
      new ColumnInfo("periocidad", "VARCHAR", 10),
      new ColumnInfo("codigo_operacion", "VARCHAR", 10),
      new ColumnInfo("estado", "VARCHAR", 10),
      new ColumnInfo("fecha_estado", "TIMESTAMP", 29),
      new ColumnInfo("fecha_creacion", "TIMESTAMP", 29));
    Assert.assertEquals("Existe tabla "+SCHEMA_CDT+"."+Constants.Tables.REGLA_ACUMULACION.getName(), true, exists);
  }
}
